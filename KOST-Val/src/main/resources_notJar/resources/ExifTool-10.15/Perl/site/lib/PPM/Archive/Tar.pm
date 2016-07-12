package PPM::Archive::Tar;

use strict;
use Archive::Tar;
use base qw(PPM::Archive);

sub is_gzipped {
    my $o = shift;
    return $o->{file} =~ /\.gz$/i
}

#sub is_bzipped {
#    my $o = shift;
#    return $o->{file} =~ /\.bz(?:ip)?2$/;
#}

sub load {
    my $o = shift;
    my $f = $o->{file};
#    if ($o->is_bzipped) {
#	require Compress::Bzip2;
#	$o->{archive} = Archive::Tar::->new($f);
#    }
#   else {
	$o->{archive} = Archive::Tar::->new($f, $o->is_gzipped);
#   }
}

sub list_files {
    my $o = shift;
    $o->{archive}->list_files;
}

sub extract {
    my $o = shift;
    my $f = shift;
    $o->{archive}->extract($f);
}

1;
