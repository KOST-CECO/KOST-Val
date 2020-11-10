package PPM::Config;

use strict;
use Data::Dumper;
use File::Path;
require PPM::YAML;

$PPM::Config::VERSION = '3.00';

sub new {
    my $class = shift;
    my $self = bless { }, ref($class) || $class;
    my $file = shift;
    $self->{DATA} = {};
    if (defined $file) {
	$self->loadfile($file, 'load');
	$self->setfile($file);
	$self->setsave;
    }
    return $self;
}

sub config {
    my $o = shift;
    return wantarray ? %{$o->{DATA}} : $o->{DATA};
}

sub loadfile {
    my $o = shift;
    my $file = shift;
    my $action = shift;
    print "DEBUG: Loading file: $file.\n" if $ENV{PPM3_CONFIG_DEBUG};
    open(FILE, "< $file")		|| die "can't read $file: $!";
    my $str = do { local $/; <FILE> };
    my $dat = eval { PPM::YAML::deserialize($str) } || {};
    close(FILE)				|| die "can't close $file: $!";
    $o->load($dat, $action);
    $o;
}

sub load {
    my $o = shift;
    my $dat = shift;
    my $action = shift || 'load';
    if ($action eq 'load' or not exists $o->{DATA}) {
	$o->{DATA} = $dat;
    }
    else {
	$o->merge($dat);
    }
    $o;
}

sub file { $_[0]->{file} }

sub setfile {
    my $o = shift;
    my $file = shift;
    $o->{file} = $file;
    $o;
}

sub setsave {
    my $o = shift;
    $o->{autosave} = 1;
    $o;
}

sub save {
    my $o = shift;
    my $file = shift || $o->{file};
    my $mode = (stat($file))[2] & 07777;
    $mode |= 0222;      # turn on write permissions (if not already)
    chmod $mode, $file; # ignore failures
    open(FILE, "> $file") or do {
	print STDERR <<END;
Warning: save $file: $!.
    => Configuration not saved.
END
	return;
    };
    my $str = PPM::YAML::serialize($o->{DATA});
    print FILE $str;
    close(FILE)				|| die "can't close $file: $!";
    $o;
}

sub merge {
    my $o = shift;
    my $dat = shift;
    _merge(\$o->{DATA}, \$dat)
      if (defined $dat);
    $o;
}

sub DESTROY {
    my $o = shift;
    $o->save if $o->{autosave};
}

sub _merge {
    my ($old_ref, $new_ref) = @_;

    return unless defined $old_ref and defined $new_ref;

    my $r_old = ref($old_ref);
    my $r_new = ref($new_ref);

    return unless $r_old eq $r_new;
    
    if ($r_old eq 'SCALAR') {
	$$old_ref = $$new_ref;
    }
    elsif ($r_old eq 'REF') {
	my $old = $$old_ref;
	my $new = $$new_ref;
	$r_old = ref($old);
	$r_new = ref($new);

	return unless $r_old eq $r_new;

	if (ref($old) eq 'HASH') {
	    for my $key (keys %$new) {
		if (exists $old->{$key} and
		    defined $old->{$key} and
		    defined $new->{$key}) {
		    _merge(\$old->{$key}, \$new->{$key});
		}
		else {
		    $old->{$key} = $new->{$key};
		}
	    }
	}
	elsif (ref($old) eq 'ARRAY') {
	    for my $item (@$new) {
		if (ref($item) eq '' and not grep { $item eq $_ } @$old) {
		    push @$old, $item;
		}
		elsif(ref($item)) {
		    push @$old, $item;
		}
	    }
	}
    }
}

#=============================================================================
# get_conf_dirs(): return a list of directories to search for config files.
#=============================================================================
use constant DELIM	=> $^O eq 'MSWin32' ? ';' : ':';
use constant PATHSEP	=> $^O eq 'MSWin32' ? '\\' : '/';
use constant KEYDIR	=> 'ActiveState';
use constant KEYFILE	=> 'ActiveState.lic';
use constant CONFDIR	=> 'PPM';
use constant CONFIG_SUFFIX => '.cfg';
use constant UNIX_SHARED_ROOT => '/usr/local/etc';

sub mymkpath {
    my $path = shift;
    unless (-d $path) {
	mkpath($path);
	die "Couldn't create directory $path: $!"
	  unless -d $path;
    }
    $path;
}

sub get_license_file {
    my $license_dir = licGetHomeDir();
    my $lic_file = join PATHSEP, $license_dir, KEYFILE;
    return $lic_file;
}

BEGIN {
    if ($ENV{PPM3_CONFIG_DEBUG}) {
	my $shared = $ENV{PPM3_SHARED} ? '' : 'not ';
	my $user   = $ENV{PPM3_USER}   ? '' : 'not ';
	print <<END;
DEBUG: Will ${shared}stat shared configuration files...
DEBUG: Will ${user}stat user's configuration files...
END
    }
}

sub load_config_file {
    my $orig = shift;
    my $mode = shift || 'rw'; # 'ro' for read-only.

    my $name = $orig . CONFIG_SUFFIX;
    my $conf = PPM::Config->new;

    # Load all config files in the "configuration path"
    my $treedir = eval { get_tree_conf_dir()   };
    my $userdir = eval { get_user_conf_dir()   };
    my $shrddir = eval { get_shared_conf_dir() };
    unless (grep { defined $_ } ($userdir, $shrddir, $treedir)) {
	print <<END;

    *** FATAL ERROR *** 
    
    Couldn't find the PPM configuration directories in either your home
    directory or the shared directory. That probably means neither of the
    environment variables PPM3_SHARED and PPM3_USER were set by the wrapper
    program "ppm3".

    Set the environment variable PPM3_CONFIG_DEBUG to 1, then rerun PPM
    to get more diagnostics about where it loaded its initial
    configuration.

END
	exit(1);
    }
    my ($treefile, $userfile, $shrdfile);
    $treefile = defined $treedir ? join PATHSEP, $treedir, $name : '';
    $userfile = defined $userdir ? join PATHSEP, $userdir, $name : '';
    $shrdfile = defined $shrddir ? join PATHSEP, $shrddir, $name : '';
    print "DEBUG: treefile='$treefile'\n" if $ENV{PPM3_CONFIG_DEBUG};
    print "DEBUG: userfile='$userfile'\n" if $ENV{PPM3_CONFIG_DEBUG};
    print "DEBUG: shrdfile='$shrdfile'\n" if $ENV{PPM3_CONFIG_DEBUG};

    # Pick the least public place to save changes.
    my $saveto = $treefile ? $treefile :
		 $userfile ? $userfile : $shrdfile;
    $conf->setfile($saveto);
    $conf->setsave unless $mode eq 'ro';

    # Load the "most private" file.
    return $conf->loadfile($treefile) if -f $treefile && -s _;
    return $conf->loadfile($userfile) if -f $userfile && -s _;
    return $conf->loadfile($shrdfile) if -f $shrdfile && -s _;

    # Neither the shared nor the user's file exists. Let's attempt to
    # create a stub copy of the file, initialised to reasonable defaults.
    print "DEBUG: Writing a stub config file for '$name'.\n"
	if $ENV{PPM3_CONFIG_DEBUG};
    eval {
	# Create config dir _even_ if we're going to load the file ro.
	my $stubfile = $conf->file;
	local *FILE;
	open (FILE, "> $stubfile") or die $!;	# caught by the eval
	print FILE config_file_stub($orig);	# write stub config
	close FILE or die $!;
	$conf->loadfile($stubfile);
    };
    if ($@) {
	die "Fatal error: couldn't find or create config file $name: $@";
    }

    return $conf;
}

# Returns the "tree" configuration directory. This is the directory used by
# 'ppminst'.
sub tree_conf_dir {
    my $d = $ENV{PPM3_PERL_SITELIB}
            || do { require Config; $Config::Config{sitelibexp} };
    return "$d/ppm-conf";
}

sub get_tree_conf_dir {
    return mymkpath(tree_conf_dir());
}

# Returns the user's configuration directory. Note: throws an exception if the
# directory doesn't exist and cannot be created.
sub get_user_conf_dir {
    return undef unless $ENV{PPM3_USER};
    return mymkpath(join PATHSEP, licGetHomeDir(), CONFDIR);
}

# Returns the shared configuration directory. Note: throws no exception, but
# the directory is not guaranteed to exist. Install scripts and such should be
# sure to create this directory themselves.
sub get_shared_conf_dir {
    return undef unless $ENV{PPM3_SHARED};
    return join PATHSEP, UNIX_SHARED_ROOT, KEYDIR, CONFDIR
      if $^O ne 'MSWin32';

    my ($R,%R);
    require Win32::TieRegistry;
    Win32::TieRegistry->import(TiedHash => \%R);
    bless do { $R = \%R }, "Win32::TieRegistry";
    $R->Delimiter('/');
    my $wkey = $R->{"HKEY_LOCAL_MACHINE/SOFTWARE/Microsoft/Windows/"};
    my $xkey = $wkey->{"CurrentVersion/Explorer/Shell Folders/"};
    my $shared_root = $xkey->{"/Common AppData"};
    return join PATHSEP, $shared_root, KEYDIR, CONFDIR;
}

sub get_conf_dirs {
    my @path;
    push @path, get_shared_conf_dir(), get_user_conf_dir();
    @path
}

#=============================================================================
# licGetHomeDir(): copied and converted from the Licence_V8 code:
#=============================================================================
sub licGetHomeDir {
    my $dir;
    my ($env1, $env2);

    if ($^O eq 'MSWin32') {
	require Win32;
	if (defined &Win32::GetFolderPath) {
	    $env1 = Win32::GetFolderPath(Win32::CSIDL_APPDATA());
	}
	$env1 = $ENV{APPDATA} unless defined $env1;
    }

    unless ($env1) {
	$env1 = $ENV{HOME};
    }

    # On Linux & Solaris:
    if ($^O ne 'MSWin32') {
	unless ($env1) {
	    $env1 = (getpwuid $<)[7]; # Try to get $ENV{HOME} the hard way
	}
	$dir = sprintf("%s/.%s", $env1, KEYDIR);
    }

    # On Windows:
    else {
	unless ($env1) {
	    $env1 = $ENV{USERPROFILE};
	}
	unless ($env1) {
	    $env1 = $ENV{HOMEDRIVE};
	    $env2 = $ENV{HOMEPATH};
	}
	unless ($env1) {
	    $env1 = $ENV{windir};
	}
	unless ($env1) {
	    die ("Couldn't find HOME / USERPROFILE / HOMEDRIVE&HOMEPATH / windir");
	}
	$env2 ||= "";
	$dir = $env1 . $env2;
	$dir =~ s|/|\\|g;

	# Win32 _stat() doesn't like trailing backslashes, except for x:\
	while (length($dir) > 3 && substr($dir, -1) eq '\\') {
	    chop($dir);
	}

	die ("Not a directory: $dir") unless -d $dir;

	$dir .= PATHSEP;
	$dir .= KEYDIR;
    }

    # Create it if it doesn't exist yet
    return mymkpath($dir);
}

sub repository {
    my $rep  = shift;
    my $ver = $^V ? sprintf("%vd", substr($^V,0,2)) : $];

    my $ppm3 = "http://ppm.ActiveState.com/PPM/ppmserver%s.plex?urn:/PPM/Server/SQL";
    my $ppm2 = "http://ppm.ActiveState.com/cgibin/PPM/ppmserver%s.pl?urn:/PPMServer";
    my $www  = "http://ppm.ActiveState.com/PPMPackages/%s";

    my $verplat1 = "";
    my $verplat2 = $ver;

    if ($^V and $^V ge v5.8.0) {
	my %osmap = (MSWin32 => "windows");
	my $plat = $osmap{$^O} || $^O;
	$verplat1 = "-$ver-$plat";
	$verplat2 = "$ver-$plat";
    }
    my %reps = (
	ppm3 => sprintf($ppm3, $verplat1),
	ppm2 => sprintf($ppm2, $verplat1),
	www  => sprintf($www, $verplat2),
    );
    return $reps{$rep};
}

sub config_file_stub {
    my $name = shift;
    if ($name eq 'clientlib') {
	my $tmp = $ENV{TEMP} || $^O eq 'MSWin32' ? 'C:\TEMP' : '/tmp';
	my $server = repository('ppm3');
	return <<END;
downloadbytes: 16384
profile_enable: 0
profile_server: $server
rebuildhtml: 0
tempdir: $tmp
tracefile: ppm3.log
tracelvl: 0
END
    }
    elsif ($name eq 'cmdline') {
	# This is actually a little bit wrong, since there are (potentially)
	# multiple frontends. Each frontend should really be responsible for
	# its own configuration data. Still, I don't care all that much.
	return <<'END';
case-sensitivity: 0
fields: name version abstract
follow-install: 1
force-install: 0
install-verbose: 1
max_history: 100
page-lines: 24
pager: ""
prompt-context: 0
prompt-slotsize: 11
prompt-verbose: 0
remove-verbose: 1
sort-field: name
upgrade-verbose: 1
verbose-startup: 1
END
    }
    elsif ($name eq 'instkey') {
	my $txt = do {
	    require PPM::Sysinfo;
	    my $DATA = PPM::Sysinfo::generate_inst_key();
	    return PPM::YAML::serialize($DATA);
	};
    }
    elsif ($name eq 'repositories') {
	my $url_ppm2 = repository('ppm2');
	my $url_ppm3 = repository('ppm3');
	return <<END;
ActiveState Package Repository: %
    url: $url_ppm3
ActiveState PPM2 Repository: %
    url: $url_ppm2
END
    }
    elsif ($name eq 'targets') {
	# Targets.cfg is the only oddball, because there are multiple targets
	# out there. What we do is this: at build time (at ActiveState) we
	# write a targets.cfg file which is to be relocated at install time.
	# Instead of installing it using a post-install script, ppm3-bin will
	# now look for it next to the binary, and use its contents as the stub
	# targets.cfg file. If the user deletes their .ActiveState/PPM
	# directory, this will magically reappear.
	require FindBin;
	my $f = "$FindBin::Bin/ppm3-bin.cfg";
	my $txt;
	if (-f $f) {
	    local *STUB;
	    open STUB, $f	or die "can't open $f: $!";
	    $txt = do { local $/; <STUB> };
	    close STUB		or die "can't close $f: $!";
	}
	return $txt;
    }
    return '';	# unrecognized file
}

1;
