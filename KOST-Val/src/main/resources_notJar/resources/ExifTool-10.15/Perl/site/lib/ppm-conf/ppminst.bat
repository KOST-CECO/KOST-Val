@rem = '--*-Perl-*--
@echo off
if "%OS%" == "Windows_NT" goto WinNT
"C:\Perl\bin\perl.exe"  -x -S "%0" %1 %2 %3 %4 %5 %6 %7 %8 %9
goto endofperl_ppminst
:WinNT
"C:\Perl\bin\perl.exe"  -x -S %0 %*
if NOT "%COMSPEC%" == "%SystemRoot%\system32\cmd.exe" goto endofperl_ppminst
if %errorlevel% == 9009 echo You do not have Perl in your PATH.
if errorlevel 1 goto script_failed_so_exit_with_non_zero_val 2>nul
goto endofperl_ppminst
@rem ';
#!perl
#line 15
use strict;
use FindBin;
use Data::Dumper;
use PPM::Config;

my $VERSION;
BEGIN { $VERSION = '3.05' }

my %INST;
my %CONF;
my %keys = (
	    root		=> 1,
	    tempdir		=> 1,
	    rebuildhtml		=> 1,

	    ARCHITECTURE	=> 0,
	    CPU			=> 0,
	    OSVALUE		=> 0,
	    OSVERSION		=> 0,
	    PERLCORE		=> 0,
	    TARGET_TYPE		=> 0,
	    LANGUAGE		=> 0,
	    VERSION		=> 0,
	   );
my $ERR;

#============================================================================
# Register a dummy object which implements the required interface.
#============================================================================
my $i = Implementation->new($ENV{PPM_PORT});

#============================================================================
# Command Implementors
#============================================================================
package Implementation;
use base qw(PPM::InstallerClient);

use Config;
use Fcntl qw(LOCK_SH LOCK_UN LOCK_EX);
use PPM::PPD;
use PPM::Search;
use Data::Dumper;
require File::Spec;

# There's a bug in ExtUtils::Install in perl 5.6.1.
# Also exists in ActivePerl 522 (line 168)
BEGIN {
    local $^W;
    require ExtUtils::Install;
}

# Query installed packages: returns a list of records about the results.
sub query {
    my $inst = shift;
    my $query = shift;
    my $case = shift;

    load_pkgs();
    my @ppds = map { $_->{ppd} } values %INST;
    my $compiled = PPM::PPD::Search->new($query, $case);
    unless ($compiled->valid) {
	$ERR = $compiled->error;
	return 0;
    }
    $ERR = '';
    my @matches = $compiled->search(@ppds);
    return 1, map { $_->ppd } @matches;
}

sub properties {
    my $inst = shift;
    my $pkg = shift;
    if (pkg_installed($pkg) && load_pkg($pkg)) {
	return ($INST{$pkg}{ppd}->ppd,
		$INST{$pkg}{pkg}{INSTDATE},
		$INST{$pkg}{pkg}{LOCATION});
    }
    $ERR = "package '$pkg' is not installed.";
    return ();
}

sub dependents {
    my $inst = shift;
    my $pkg = shift;
    if (pkg_installed($pkg) && load_pkg($pkg)) {
	return @{ $INST{$pkg}{pkg}{dependents} || [] };
    }
    undef;
}

sub remove {
    my ($inst, $pkg, $verbose) = @_;

    if (pkg_installed($pkg) && load_pkg($pkg)) {

	# Is there an uninstall script?
	my $u_script = eval {
	    $INST{$pkg}{ppd}->find_impl_raw($inst)->uninstall_script
	};
	if ($u_script) {
	    my %opts = (
		PPM_INSTARCHLIB => $Config{installsitearch},
		PPM_INSTPACKLIST => $INST{$pkg}{pkg}{INSTPACKLIST},
		PPM_INSTROOT => $INST{$pkg}{pkg}{INSTROOT},
		PPM_ACTION => 'uninstall',
	    );
	    $inst->run_script(pkg_uninstaller($pkg), $u_script, \%opts)
	      or return 0;
	}

	my $packlist = $INST{$pkg}{pkg}{INSTPACKLIST};
	(my $altpacklist = $packlist) =~ s<\Q$CONF{ARCHITECTURE}\E[\\/]><>i;
	my $forceunlink = \&ExtUtils::Install::forceunlink;
	eval {
	    *ExtUtils::Install::forceunlink = sub {
		goto &$forceunlink if -f $_[0];
		warn "Warning: $_[0] was already deleted\n";
	    };
	    if (-f $packlist) {
		ExtUtils::Install::uninstall($packlist, $verbose, 0);
	    }
	    elsif (-f $altpacklist) {
		ExtUtils::Install::uninstall($altpacklist, $verbose, 0);
	    }
	};
	*ExtUtils::Install::forceunlink = $forceunlink;
	$ERR = "$@" and return 0 if $@;

	# Update html and table of contents, if ActivePerl::DocTools is installed:
	if (eval { require ActivePerl::DocTools; 1 }) {
	    ActivePerl::DocTools::WriteTOC();
	}

	# Remove the package and references to it:
	my $ppd_ref = $INST{$pkg}{ppd};
	my @prereqs;
	eval { @prereqs = $ppd_ref->find_impl_raw($inst)->prereqs };
	del_dependent($_->name, $ppd_ref->name) for @prereqs;
	purge_pkg($pkg);
    }
    else {
	$ERR = "package '$pkg' not installed.";
	return 0;
    }
    return 1;
}

sub precious {
    return @{$CONF{precious}};
}

sub bundled {
    return @{$CONF{bundled}};
}

sub upgrade {
    my ($inst, $pkg, $ppmpath, $ppd, $repos, $verbose) = @_;
    local %ENV = %ENV;
    $ENV{PPM_ACTION} = 'upgrade';
    $inst->install($pkg, $ppmpath, $ppd, $repos, $verbose);
}

# This sub is called when the frontend has found an implementation suitable
# for this target, and is double-checking whether we can actually install this
# "content-type".
sub can_install {
    my ($inst, $lang, $version, $compat_type) = @_;
    return 0 unless $lang eq $inst->config_get('LANGUAGE');
    
    # There are two distinct version number schemes for Perl: 5.00x_yy, and
    # 5.x.y. At least, those are the ones I care about. To handle both, I'm
    # going to detect the new format and demote it to the old format. Then I
    # can compare using a regular numeric comparison. The reason I use the old
    # one is so that the PPM3 backend will still work with pre-5.6.0 perls.
    my @parts = split /\./, $version;
    if (@parts > 2) {
	$version = sprintf("%i.%.03i%.03i", @parts);
    }
    return $] >= $version;
}

sub install {
    my ($inst, $pkg, $ppmpath, $ppd, $repos, $verbose) = @_;
    use Cwd qw(cwd);
    my $cwd = cwd();
    my $ppd_obj = PPM::PPD->new($ppd);

    # Install:
    # 1. chdir to temp directory
    chdir $ppmpath or do {
	$ERR = "can't chdir to $ppmpath: $!";
	return 0;
    };
    chdir $pkg; # this is expected to fail!

    use ActiveState::RelocateTree qw(relocate spongedir);
    relocate (
	to      => '.',
	inplace => 1,
	search  => spongedir('ppm'),
	replace => $Config{prefix},
    ) if $Config{osname} ne 'MSWin32';

    # 2. set up the install parameters:
    my ($packlist, %opts, %inst_opts);
    {
	my $inst_archlib = $Config{installsitearch};
	my $inst_root = $Config{prefix};
	$packlist = File::Spec->catfile("$inst_archlib/auto",
				split(/-/, $pkg), ".packlist");
	
	# Copied from ExtUtils::Install
	my $INST_LIB = File::Spec->catdir(File::Spec->curdir, "blib", "lib");
	my $INST_ARCHLIB = File::Spec->catdir(File::Spec->curdir, "blib", "arch");
	my $INST_BIN = File::Spec->catdir(File::Spec->curdir, "blib", "bin");
	my $INST_SCRIPT = File::Spec->catdir(File::Spec->curdir, "blib", "script");
	my $INST_MAN1DIR = File::Spec->catdir(File::Spec->curdir, "blib", "man1");
	my $INST_MAN3DIR = File::Spec->catdir(File::Spec->curdir, "blib", "man3");
	my $INST_HTMLDIR = File::Spec->catdir(File::Spec->curdir, "blib", "html");
	my $INST_HTMLHELPDIR = File::Spec->catdir(File::Spec->curdir, "blib", "htmlhelp");

	my $inst_script = $Config{installscript};
	my $inst_man1dir = $Config{installman1dir};
	my $inst_man3dir = $Config{installman3dir};
	my $inst_bin = $Config{installbin};
	my $inst_htmldir = $Config{installhtmldir};
	my $inst_htmlhelpdir = $Config{installhtmlhelpdir};
	my $inst_lib = $Config{installsitelib};
	$inst_htmldir ||= "$inst_bin/../html";
	$inst_htmlhelpdir ||= "$inst_bin/../html";

	# %inst_opts is used for ExtUtils::Install installs.
	%opts = (
	    PPM_INSTARCHLIB => $inst_archlib,
	    PPM_INSTROOT => $inst_root,
	    PPM_INSTPACKLIST => $packlist,
	    PPM_ACTION => (
		defined $ENV{PPM_ACTION} ? $ENV{PPM_ACTION} : 'install'
	    ),
	    PPM_NEW_VERSION => $ppd_obj->version,
	    (
		pkg_installed($pkg) && load_pkg($pkg)
		? (PPM_PREV_VERSION => $INST{$pkg}{ppd}->version)
		: ()
	    ),
	);
	%inst_opts = (
	    read => $packlist,
	    write => $packlist,
	    $INST_LIB => $inst_lib,
	    $INST_ARCHLIB => $inst_archlib,
	    $INST_BIN => $inst_bin,
	    $INST_SCRIPT => $inst_script,
	    $INST_MAN1DIR => $inst_man1dir,
	    $INST_MAN3DIR => $inst_man3dir,
	    $INST_HTMLDIR => $inst_htmldir,
	    $INST_HTMLHELPDIR => $inst_htmlhelpdir
	);
	if ($CONF{root} && $CONF{root} !~ /^\Q$inst_root\E$/i) {
	    my $root = $CONF{root};
	    $_ =~ s/\Q$inst_root/$root\E/i for values %inst_opts;
	    $_ =~ s/\Q$inst_root/$root\E/i for values %opts;
	    $inst_root = $root;
	}
    }

    # 3. Install the package.
    #    This operates slightly differently than PPM2. First,
    #    ExtUtils::Install is only called if q(blib) exists and is a
    #    directory. Next, the install script is run. If it fails, then the
    #    results of ExtUtils::Install are backed out.
    my $inst_blib = -d "blib";
    my $inst_script = eval { $ppd_obj->find_impl_raw($inst)->install_script };
    if ($inst_blib) {
	while (1) {
	    eval {
		my $verbose = $verbose - 1; # $verbose < 0 implies silence.
		ExtUtils::Install::install(
		    {%inst_opts},
		    $verbose,0,0
		);
	    };
	    # install might have croaked in another directory
	    chdir $ppmpath;
	    # Can't remove some DLLs, but we can rename them and try again.
	    if ($@ && $@ =~ /Cannot forceunlink (\S+)/) {
		my $oldname = $1;
		$oldname =~ s/:$//;
		my $newname = $oldname . "." . time();
		unless (rename($oldname, $newname)) {
		    $ERR = "renaming $oldname to $newname: $!";
		    return 0;
		}
	    }
	    # Some other error
	    elsif($@) {
		$ERR = "$@";
		return 0;
	    }
	    else { last; }
	}
    }
    if ($inst_script) {
	$inst->run_script("install_script", $inst_script, \%opts, $verbose)
	    or do {
	    # Back out ExtUtils::Install
	    if ($inst_blib) {
		ExtUtils::Install::uninstall($packlist, $verbose, 0);
	    }
	    return 0;
	};
    }
    chdir $cwd;

    # 4. update html table of contents, if ActivePerl::DocTools is installed:
    if (eval { require ActivePerl::DocTools; 1 }) {
	ActivePerl::DocTools::UpdateHTML()
	    if $inst->config_get('rebuildhtml');  # XXX this doesn't work
	ActivePerl::DocTools::WriteTOC();
    }

    # Add the package to the list of installed packages
    $INST{$pkg} = {
	pkg => {
		INSTDATE => scalar localtime,
		LOCATION => $repos,
		INSTROOT => $opts{PPM_INSTROOT},
		INSTPACKLIST => $packlist,
	       },
	ppd => $ppd_obj,
    };
    save_pkg($pkg, "$ppmpath/uninstall_script");

    # "Register" the package as dependent on each prerequisite:
    my @prereqs;
    eval { @prereqs = $ppd_obj->find_impl_raw($inst)->prereqs };
    add_dependent($_->name, $pkg) for @prereqs;

    return 1;
}

sub config_keys {
    map { [$_, $keys{$_}] } keys %keys;
}

sub _str {
    my $a = shift;
    return '' unless defined $a;
    $a;
}

sub config_info {
    map { [$_, _str($CONF{$_})] } keys %keys;
}

sub config_set {
    my $inst = shift;
    my ($key, $val) = @_;
    unless (defined $keys{$key}) {
	$ERR = "unknown config key '$key'";
	return 0;
    }

    $CONF{$key} = $val;
    save_conf();
    return 1;
}

sub config_get {
    my $inst = shift;
    my $key = shift;
    unless (defined $key and exists $keys{$key}) {
	$key = '' unless defined $key;
	$ERR = "unknown config key '$key'";
	return undef;
    }
    _str($CONF{$key});
}

sub error_str {
    defined $ERR ? $ERR : 'No error';
}

#----------------------------------------------------------------------------
# Utilities
#----------------------------------------------------------------------------

# This can deal with files as well as directories
sub abspath {
    use Cwd qw(abs_path);
    my ($path, $file) = shift;
    if (-f $path) {
        my @p = split '/', $path;
        $path = join '/', @p[0..$#p-1]; # can't use -2 in a range
        $file = $p[-1];
    }
    $path = abs_path($path || '.');
    return ($path, $file, defined $file ? join '/', $path, $file : ())
      if wantarray;
    return defined $file ? join '/', $path, $file : $path;
}

sub run_script {
    my $o	= shift;
    my $file	= shift;
    my $inst	= shift;
    my $setenv	= shift;
    my $verbose	= shift;
    my ($exec, $href, $content) = map { $inst->$_ } qw(exec href script);

    # Export %setenv to %ENV:
    local %ENV = %ENV;
    my %setenv = (
	PPM_VERSION => $VERSION,
	PPM_PERL => $Config{perlpath},
	%$setenv,
    );
    $ENV{$_} = $setenv->{$_} for keys %setenv;

    # Evaluate special case of EXEC:
    $exec =~ s/\bPPM_PERL\b/$Config{perlpath}/i;

    # Four cases:
    # 1. !EXEC && !HREF: system($_) for split ';;';
    # 2. !EXEC &&  HREF: system($_) for split '\n';
    # 3.  EXEC && !HREF: split ';;' => $tmpfile; system($exec, $tmpfile);
    # 4.  EXEC &&  HREF: system($exec, $file);

    if (not $exec and not $href) {
	for (split ';;', $content) {
	    system($_) == 0 and next;
	    $ERR = "system() return non-zero value ($?): '$_'";
	    return 0;
	}
    }
    elsif (not $exec) {	# and $href (of course)
	local *INPUT;
	open (INPUT, $file) or do {
	    $ERR = "can't open $file: $!";
	    return 0;
	};
	while (<INPUT>) {
	    system($_) == 0 and next;
	    $ERR = "system() returned non-zero value ($?): '$_'";
	    return 0;
	}
    }
    elsif (not $href) { # and $exec (of course)
	local *INPUT;
	open (INPUT, "> $file") or die "can't write $file: $!";
	print INPUT "$_\n" for (split ';;', $content);
	close (INPUT) or die "can't close $file: $!";
	system("$exec $file") == 0 or do {
	    $ERR = "system() returned non-zero value ($?): '$exec $file'";
	    return 0;
	};
	# only a convenience: this whole directory will be removed.
	unlink $file;
    }
    else {
	$exec =~ s/\bSELF\b/abspath($file)/ei and chmod(0777, $file);
	-f $file or do {
	    $ERR = "can't run '$exec $file': $!";
	    return 0;
	};
	system("$exec $file") == 0 or do {
	    $ERR = "(un)install script failed: '$exec $file'";
	    return 0;
	};
    }
    $ERR = "";
    return 1;
}

sub abs_packlist {
    my $pl = shift;
    $pl =~ s[\%SITELIB\%][$Config{sitelib}]g;
    unless (-f $pl) {
	my $i = $^O eq 'MSWin32' ? '(?i)' : '';
	$pl =~ s[$i^\Q$Config{sitelib}\E][$Config{sitearch}];
	return undef unless -f $pl;
    }
    return $pl;
}

#============================================================================
# Settings and packages
#============================================================================
my ($conf_dir, $conf, $conf_obj);
BEGIN {
    # By putting an invalid package character in the directory, we're making
    # sure no real package could overwrite our settings, and vice versa.
    $conf_dir = PPM::Config::tree_conf_dir();
    $conf = join '/', $conf_dir, 'ppm.cfg';
}

# Loads the configuration file and populates %CONF
sub load_conf {
    $conf_obj = PPM::Config->new->loadfile($conf);
    %CONF = $conf_obj->config;

    # Special values; set them here
    $CONF{ARCHITECTURE} = $Config{archname};
    # Append "-5.8" to architecture name for Perl 5.8 and later
    if (length($^V) && ord(substr($^V,1)) >= 8) {
        $CONF{ARCHITECTURE} .= sprintf("-%d.%d", ord($^V), ord(substr($^V,1)));
    }
    $CONF{PERLCORE} = $Config{version};
    $CONF{TARGET_TYPE} = "perl";
    $CONF{LANGUAGE} = "Perl";
    $CONF{VERSION} = $VERSION;
    $CONF{OSVALUE} = $^O;
    $CONF{OSVERSION} = join(
	',',
	(((split '\.', $Config{osvers}), (0) x 4)[0..3])
    );
}

# Saves %CONF to the configuration file
sub save_conf {
    $conf_obj->merge(\%CONF);
    # Make the file writeable if it isn't already:
    chmod 0666, $conf;
    $conf_obj->save($conf);
}

# Loads the given package into $INST{$pkg}. Returns true if the package could
# be loaded, false otherwise.
sub load_pkg {
    my $pkg = shift;

    return 1 if exists $INST{$pkg};

    return 0 unless -f "$conf_dir/$pkg.ppd";
    return 0 unless -f "$conf_dir/$pkg.pkg";

    my $ppdref = PPM::PPD->new("$conf_dir/$pkg.ppd");
    my $pkgfile = "$conf_dir/$pkg.pkg";
    my $pkgref = PPM::Config->new->loadfile($pkgfile);

    $INST{$pkg}{ppd} = $ppdref;
    $INST{$pkg}{pkg} = $pkgref->config;

    # Substitute the %SITELIB% variable properly.
    $INST{$pkg}{pkg}{INSTPACKLIST} =
	abs_packlist($INST{$pkg}{pkg}{INSTPACKLIST});
    defined $INST{$pkg}{pkg}{INSTPACKLIST}
	or do { purge_pkg($pkg); return 0 };

    return 1;
}

# Saves the given package from $INST{$pkg}.
sub save_pkg {
    my $pkg = shift;
    my $uninst = shift;
    return 0 unless exists $INST{$pkg};

    # the PPD file:
    my $ppdfile = "$conf_dir/$pkg.ppd";
    if (-f $ppdfile) {
	unlink $ppdfile		or die "$0: can't delete $ppdfile: $!";
    }
    open PPD, "> $ppdfile"	or die "$0: can't write $ppdfile: $!";
    print PPD $INST{$pkg}{ppd}->ppd;
    close PPD			or die "$0: can't close $ppdfile: $!";

    # the PKG file:
    my $c = PPM::Config->new;
    $c->load($INST{$pkg}{pkg});
    $c->save("$conf_dir/$pkg.pkg");

    # save the uninstall script:
    if ($uninst && -f $uninst) {
	my $saveto = "$conf_dir/$pkg.u";
	use File::Copy qw(copy);
	copy($uninst, $saveto);
    }
    return 1;
}

sub add_dependent {
    my $package = shift;
    my $dependent = shift;
    return 0 unless load_pkg($package);
    push @{$INST{$package}{pkg}{dependents}}, $dependent;
    save_pkg($package);
}

sub del_dependent {
    my $package = shift;
    my $dependent = shift;
    return 0 unless load_pkg($package);
    @{$INST{$package}{pkg}{dependents}}
      = grep { $_ ne $dependent }
	@{$INST{$package}{pkg}{dependents}};
    save_pkg($package);
}

sub purge_pkg {
    my $pkg = shift;

    # The PPD file:
    my $ppdfile = "$conf_dir/$pkg.ppd";
    if (-f $ppdfile) {
	unlink $ppdfile		or die "$0: can't delete $ppdfile: $!";
    }

    # The %INST entry:
    delete $INST{$pkg};

    # The PKG file:
    my $pkgfile = "$conf_dir/$pkg.pkg";
    if (-f $pkgfile) {
	unlink $pkgfile		or die "$0: can't delete $pkgfile: $!";
    }

    # The uninstall file:
    my $ufile = "$conf_dir/$pkg.u";
    if (-f $ufile) {
	unlink $ufile		or die "$0: can't delete $ufile: $!";
    }

    return 1;
}

sub pkg_uninstaller {
    my $pkg = shift;
    return "$conf_dir/$pkg.u";
}

# Load all packages: only needed when doing an advanced query
sub load_pkgs {
    my @pkgs = map { s/\.ppd$//; s!.*/([^/]+)$!$1!g; $_ } #!
      glob "$conf_dir/*.ppd"; 
    load_pkg($_) for @pkgs;
}

sub pkg_installed {
    my $pkg = $_[0];
    return unless -f "$conf_dir/$pkg.ppd" && -f "$conf_dir/$pkg.pkg";
    if ($^O eq "MSWin32") {
	# Make sure $pkg uses the same "spelling" as the file system
	my $name = Win32::GetLongPathName("$conf_dir/$pkg.pkg");
	$_[0] = substr((Win32::GetFullPathName($name))[1], 0, -4);
    }
    return 1;
}

BEGIN {
    load_conf();
}

__END__
:endofperl_ppminst
