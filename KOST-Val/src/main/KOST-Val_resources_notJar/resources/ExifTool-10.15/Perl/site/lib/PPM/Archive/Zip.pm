package PPM::Archive::Zip;

use strict;
use Archive::Zip;
use base qw(PPM::Archive);

sub load {
    my $o = shift;
    my $f = $o->{file};
    $o->{archive} = Archive::Zip::->new($f);
}

sub list_files {
    my $o = shift;
    $o->{archive}->memberNames;
}

sub extract {
    my $o = shift;
    my $f = shift;
    $o->{archive}->extractMember($f);
}

1;
