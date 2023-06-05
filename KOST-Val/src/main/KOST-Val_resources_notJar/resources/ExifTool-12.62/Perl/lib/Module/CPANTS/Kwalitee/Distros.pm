package Module::CPANTS::Kwalitee::Distros;
use warnings;
use strict;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 800 }

##################################################################
# Analyse
##################################################################
my $debian;

sub analyse {
    my $class = shift;
    my $me = shift;

    # NOTE: The data source of these debian metrics has not been
    # updated for more than a year, and mirroring stuff from
    # external source every time you test is very nasty.

    # These metrics are deprecated and actually removed to
    # reduce unwanted dependencies for Test::Kwalitee users.

    # Note also that this stub should not be removed so that
    # this can replace the old ::Distro module, and the old
    # metrics will not be loaded while loading plugins.

    return;
}

##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators{
    return [];
}

q{Favourite record of the moment:
  Lili Allen - Allright, still};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::Distros - Information retrieved from the various Linux and other distributions

=head1 SYNOPSIS

The metrics here were based on data provided by the various downstream packaging systems, but are deprecated now.

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

=head3 analyse

Does nothing now.

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Thomas Klausner|https://metacpan.org/author/domm>
and L<Gábor Szabó|https://metacpan.org/author/szabgab>
with the help of Martín Ferrari and the
Debian Perl packaging team

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2009 L<Thomas Klausner|https://metacpan.org/author/domm>

Copyright © 2006–2008 L<Gábor Szabó|https://metacpan.org/author/szabgab>

You may use and distribute this module according to the same terms
that Perl is distributed under.
