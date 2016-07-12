package ActiveState::RelocateTree;
require Exporter;

use strict;
use Config;
use Cwd qw(abs_path getcwd);
use File::Basename qw(dirname basename);
use File::Copy ();
use File::Find;
use File::Path;
use File::Spec;

use vars qw(@ISA @EXPORT_OK $VERSION);
@ISA = qw(Exporter);
@EXPORT_OK = qw(relocate edit check move_tree spongedir rel2abs abs2rel);
$VERSION = '0.03';

my $modifier = $^O eq 'MSWin32' ? '(?i)' : '';

# This variable has to be built up, or this package itself will be relocated.
# That should never happen, since the unmodified path is needed by PPM.
# Scripts like reloc_perl provide their own default, which will of course get
# relocated as wanted.
sub spongedir {
    my %sponges = (
	ppm => '/tmp'.'/.ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZpErLZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZperl',
	thisperl => 'C:\p4view\Apps\Gecko\build\build-2004'.'0601T114421-qjjgghwhfp\ActivePerl\Perl',
    );
    return $sponges{lc$_[0]};
}

sub relocate {
    my %opt = (
#	to       => ??? -> you have to provide this one
	from     => $Config{prefix},

	quiet    => 0,
	verbose  => 0,
	filelist => undef,

	ranlib   => 1,
	textonly => 0,

	savebaks => 0,
	bak      => '.~1~',

	inplace  => 0,
	killorig => 0,
	usenlink => 1,
	@_,
    );
    $opt{search}  = $opt{from} unless exists $opt{search};
    $opt{replace} = $opt{to}   unless exists $opt{replace};
    $opt{inplace} = 1          if $opt{to} eq $opt{from};

    local *STDOUT if $opt{quiet};
    unless ($opt{to}) {
	warn "No `to' path given to relocate(): cannot continue";
	return;
    }

    # Substitute '/' or '\\' characters with [/\\] if this is Windows. This
    # allows matching either slashes or backslashes.
    my $regexp;
    if ($^O eq 'MSWin32') {
	my @parts = map { quotemeta } split m#[/\\]#, $opt{search};
	$regexp = join '[/\\\\]', @parts;
    }
    else {
	$regexp = quotemeta($opt{search});
    }

    move_tree(@opt{qw(from to killorig verbose usenlink)})
	unless $opt{inplace};

    my (@bin, @text);
    {
	# On HP-UX with pfs_mount, nlink is always 2.
	local $File::Find::dont_use_nlink = !$opt{usenlink};
	find(sub {
	    return if -l;
	    return unless -f && -s;
	    if (-B) {
		return if $opt{textonly};
		push @bin, $File::Find::name if check($_, $regexp, 1);
	    }
	    else {
		push @text, $File::Find::name if check($_, $regexp, 0)
	    }
	}, resolve($opt{to}));
    }

    # show affected files
    print "Configuring Perl installation at $opt{to}\n"
	if @bin or @text;

    if ($opt{filelist}) {
	open LOG, "> $opt{filelist}" or die "can't open $opt{filelist}: $!";
	print LOG "$_\n" for (@bin, @text);
	close LOG or die "can't close $opt{filelist}: $!";
    }
    if ($opt{verbose}) {
	print "Translating $opt{search} to $opt{replace}\n";
	print "editing $_\n" for (@bin, @text);
    }

    # edit files
    edit($regexp, @opt{qw(search replace bak)}, 0, @text);
    edit($regexp, @opt{qw(search replace bak)}, 1, @bin);

    # clobber backups
    unless ($opt{savebaks}) {
	print "cleaning out backups\n" if $opt{verbose};
	unlink "$_$opt{bak}" for (@text, @bin);
    }

    # run ranlib, where appropriate
    my $rl = $Config{ranlib};
    $rl = '' if $rl =~ /^:?\s*$/;
    if ($rl and $opt{ranlib}) {
	for (@bin) {
	    if (/\Q$Config{_a}\E$/o) {
		print "$rl $_\n" if $opt{verbose};
		system("$rl $_") == 0 or die "`$rl $_' failed: $?";
	    }
	}
    }
}

sub check {
    my $file = shift;
    my $re   = shift;
    my $bin  = shift;
    local (*F, $_);
    open F, "< $file" or die "Can't open `$file': $!";
    binmode F if $bin;
    my $mod = $modifier;
    while (<F>) {
	return 1 if /$mod$re/;
    }
    return 0;
}

sub edit {
    my $re   = shift;
    my $from = shift;
    my $dest = shift;
    my $bak  = shift;
    my $bin  = shift;
    return unless @_; # prevent reading from STDIN

    my $mod  = $modifier;
    my $term = $bin ? '([^\0]*\0)' : '()';
    my $pad  = $bin ? "\0" x (length($from) - length($dest)) : '';

    local ($_, *INPUT, *OUTPUT);
    my $old = select(STDOUT);
    for my $file (@_) {
	rename($file, "$file$bak")
	    or do { warn "can't rename $file: $!" if $^W; next };
	open(INPUT,   "< $file$bak")
	    or do { warn "can't open $file$bak: $!" if $^W; next };
	open(OUTPUT,  "> $file")
	    or do { warn "can't write $file: $!" if $^W; next };
	chmod((stat "$file$bak")[2] & 07777, $file);
	binmode(INPUT), binmode(OUTPUT) if $bin;
	select(OUTPUT);
	if ($^O eq 'MSWin32') {
	    while (<INPUT>) {
	        if (m[($mod$re$term)]) {
		    # if the string to be modified has backslashes anywhere
		    # in it and has no forward slashes, make the replacement
		    # string backslashed too
		    my $match = $1;
		    my $d = $dest;
		    if ($match =~ m[\\] and $match !~ m[/]) {
		        $d =~ s[/][\\]g;
		    }
		    s[$mod$re$term][$d$1$pad]g;
		}
		print;
	    }
	}
	else {
	    while (<INPUT>) {
		s[$mod$re$term][$dest$1$pad]g;
		print;
	    }
	}
	close(OUTPUT);
	close(INPUT);
    }
    select($old);

# Unfortunately, this doesn't work in 5.005_03. Oh, how I wish it would just
# die once and for all!
#    local ($_, *ARGV, *ARGVOUT);
#    local $^I = $bak;
#    @ARGV = @_;
#    binmode(ARGV), binmode(ARGVOUT) if $bin;
#    while (<>) {
#	s[$mod\Q$from\E$term][$dest$1$pad]g;
#	print;
#	close ARGV if eof;
#    }
}

sub move_tree {
    my ($from, $to, $kill, $verbose, $usenlink) = @_;
    $from = rel2abs(resolve($from));
    $to   = rel2abs($to);
    # On HP-UX with pfs_mount, nlink is always 2.
    local $File::Find::dont_use_nlink = !$usenlink;
    find(sub {
	my $src = abs2rel($File::Find::name, $from);
	if (-l) {
	    # Resolve the source link. If it points inside the source tree,
	    # build a similar one which points to the same relative location
	    # in the destination tree. Else, copy it if it points to a file,
	    # else *ignore it*.
	    my $resolved = resolve($_);
	    if ($resolved =~ /^$modifier\Q$from\E/) {
		my $rel = abs2rel($resolved, $from);
		my $dest = File::Spec->catfile($to, $rel);
		my $link = File::Spec->catfile($to, $src);
		symlink($dest, $link)
		    || die "Can't create symlink at '$link': $!";
		return;
	    }
	}
	if (-f) {
	    my $file = File::Spec->catfile($to, $src);
	    File::Copy::syscopy($File::Find::name, $file)
		|| die "Can't copy to '$file': $!";
	    my $srcmode = (stat $File::Find::name)[2];
	    chmod($srcmode & 07777, $file);
	}
	elsif (-d) {
	    my $dir = File::Spec->catdir($to, $src);
	    mkpath($dir, 0, 0755); # don't bother preserving directory mode
	}
	else {
	    # silently ignore
	}
    }, $from);
    if ($kill) {
	print "deleting $from\n" if $verbose;
	rmtree($from, 0, 0);
    }
}

{
    my $rel2abs_test = eval { File::Spec->rel2abs('.'); 1 };
    my $abs2rel_test = eval { File::Spec->abs2rel('.', '.'); $^O ne 'MSWin32' };
    my $symlink_test = eval { symlink('', ''); 1 };

    sub resolve {
	my $l = shift;
	return $l unless $symlink_test;
	return $l unless -l $l;
	my $d = dirname($l);
	my $v = readlink($l);
	return rel2abs($v, $d);
    }

    sub rel2abs {
	my ($rel, $relto) = @_;
	my ($base, $rest);
	if ($rel2abs_test) {
	    $base = File::Spec->rel2abs(@_);
	    $rest = '';
	}
	else {
	    # Support for 5.005:
	    return $rel if File::Spec->file_name_is_absolute($rel);
	    if    (!defined $relto) { $relto = getcwd(); }
	    elsif (!File::Spec->file_name_is_absolute($relto)) {
		$relto = rel2abs($relto);
	    }
	    ($base, $rest) = (File::Spec->catdir($relto, $rel), '');
	}
	until (-d $base) {
	    $rest = File::Spec->catdir(basename($base), $rest);
	    $base = dirname($base);
	}
	return File::Spec->catdir(abs_path($base), $rest) if $base and $rest;
	return abs_path($base) if $base and not $rest;
	die "can't absolutize $rel against $relto\n";
    }

    sub abs2rel {
	return File::Spec->abs2rel(@_) if $abs2rel_test;

	# Support for 5.005:
	my $abs  = shift;
	my $from = shift;
	(my $rel  = $abs) =~ s#$modifier^\Q$from\E[\\/]?##;
	return $rel;
    }
}

1;
