package PPM::Archive;

use strict;

sub new {
    my $self = shift;
    my $file = shift;
    my $o    = bless {
	file	=> $file,
    }, ref($self) || $self;
    $o->select_handler;
    $o->load;
    return $o;
}

sub rebless {
    my $self = shift;
    my $obj  = shift;
    bless $obj, ref($self) || $self;
}

sub select_handler {
    my $o = shift;
    my $f = $o->{file};
    if ($f =~ /\.(?:tar(?:\.gz)?|tgz)$/i) {
	require PPM::Archive::Tar;
	PPM::Archive::Tar::->rebless($o);
    }
# Compress::Bzip2 doesn't yet compile or pass its 'make test', so we probably
# shouldn't support it yet. I'll contact the author and see if he's actually
# maintaining the module.
#    elsif ($f =~ /\.tar\.(?:bz(?:ip)?2)$/i) {
#	require PPM::Archive::Tar;
#	PPM::Archive::Tar::->rebless($o);
#    }
    elsif ($f =~ /\.zip$/i) {
	require PPM::Archive::Zip;
	PPM::Archive::Zip::->rebless($o);
    }
    else {
	die "Unsupported PPM archive format";
    }
}

1;
