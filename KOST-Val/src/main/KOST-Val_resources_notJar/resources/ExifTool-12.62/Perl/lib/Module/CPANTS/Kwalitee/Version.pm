package Module::CPANTS::Kwalitee::Version;
use warnings;
use strict;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 100 }

##################################################################
# Analyse
##################################################################

sub analyse {
    # NOTE: The analysis/metrics in this module have moved to
    # Module::CPANTS::SiteKwalitee because these require
    # a finalized META file to detect (or ignore) versions
    # correctly.

    # Note also that this stub should not be removed so that
    # this can replace the old ::Version module, and the old
    # metrics will not be loaded while loading plugins.
}



##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators {
  return [];
}


q{Favourite record of the moment:
  Fat Freddys Drop: Based on a true story};


__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::Version - check versions

=head1 SYNOPSIS

The metrics in this module have moved to L<Module::CPANTS::SiteKwalitee::Version|https://github.com/cpants/Module-CPANTS-SiteKwalitee>.

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<100>.

=head3 analyse

Does nothing now.

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Thomas Klausner|https://metacpan.org/author/domm>

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2006, 2009 L<Thomas Klausner|https://metacpan.org/author/domm>

You may use and distribute this module according to the same terms
that Perl is distributed under.
