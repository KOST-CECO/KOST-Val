package PPM::Repository::Local;

use strict;
use PPM::PPD;
use PPM::Search;
use PPM::Result qw(Ok Warning Error List);
use URI; # not actually needed -- that base class requires it.
use Cwd qw(cwd);

use Data::Dumper;

use base qw(PPM::Repository::WWW);
use vars qw($VERSION);
$VERSION = '3.05';

sub init {
    my $o = shift;

    # $o->{location} is set to what the base class thinks is a local
    # directory. Verify that it is an existing directory, then set up a URL
    # for searching through it.
    $o->{errmsg}=<<END and return unless -d $o->{location};
Can't initialize repository at '$o->{location}': $!
END

    # $o->{location} is a directory!
    my $d = $o->{dir} = $o->{location};

    # Now, try to create a URL for it, so we can use the base class' search
    # methods. We also need it for "absolutizing" the PPM package locations:
    my $url = URI->new;
    $url->scheme('file');

    if ($^O eq 'MSWin32') {
	$d =~ s#\\#/#g; # make the regular expressions tolerable

	# UNC path:
	if ($d =~ m#^//([^/]+)/(.*)#) {
	    my ($host, $path) = ($1, $2);
	    $url->host($host);
	    $url->path($path);
	}

	# Pathname with drive letter:
	elsif ($d =~ m#^[A-Z]:/#i) {
	    $d =~ s/:/|/; # replace the drive's colon with a pipe. Weird.
	    $url->host('localhost');
	    $url->path($d);
	}

	# other cases will be caught be the non-windows cases.
    }

    unless ($url->path) {
	# Make sure it's absolute by prepending $cwd to it:
	if ($d !~ m#^/#) {
	    my $cwd = cwd;

	    # Fixup the drive letter and backslashes on Windows:
	    if ($^O eq 'MSWin32') {
		$o->{location} = $o->{dir} = "$cwd/$o->{dir}";
		$cwd =~ s#^([A-Z]):#$1|#;
		$cwd =~ s#\\#/#g;
	    }
	    else {
		$o->{location} = $o->{dir} = "$cwd\\$o->{dir}";
	    }

	    chop $cwd if substr($cwd, -1) eq '/';
	    $d = "$cwd/$d";
        }

	$url->host('localhost');
	$url->path($d);
    }
    $o->{url} = $o->{url_base} = $url;
    $o->{url_base} .= '/' unless $o->{url_base} =~ m#/$#;
    1;
}

sub describe {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    return Ok($o->{ppds}{$pkg})
	if exists $o->{ppds}{$pkg} and $o->{ppds}{$pkg}->is_complete;
    my $file = $o->{dir} =~ m,[\\/]$, ? "$o->{dir}$pkg" : "$o->{dir}/$pkg";
    $file .= ".ppd" unless $file =~ /\.ppd$/i;
    return Error("Package '$pkg' not found. Please 'search' for it first.")
        unless -f $file;
    $o->{ppds}{$pkg} = PPM::PPD->new($file, $o, $pkg);
    $o->{ppds}{$pkg}{from} = 'repository';
    Ok($o->{ppds}{$pkg});
}

sub type_printable { "Local directory" }
