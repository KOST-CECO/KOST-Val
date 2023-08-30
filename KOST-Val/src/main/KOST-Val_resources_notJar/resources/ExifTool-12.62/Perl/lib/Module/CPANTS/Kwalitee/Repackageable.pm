package Module::CPANTS::Kwalitee::Repackageable;
use warnings;
use strict;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 900 }

##################################################################
# Analyse
##################################################################

sub analyse {
    my $class = shift;
    my $me = shift;

    # NOTE: The analysis/metric in this module has moved to
    # Module::CPANTS::SiteKwalitee.

    # Note also that this stub should not be removed so that
    # this can replace the old ::Repackageable module, and the old
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

Module::CPANTS::Kwalitee::Repackageable - Checks for various signs that make a module packageable

=head1 SYNOPSIS

The metrics in this module have moved to L<Module::CPANTS::SiteKwalitee|https://github.com/cpants/Module-CPANTS-SiteKwalitee>.

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

=head3 analyse

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Thomas Klausner|https://metacpan.org/author/domm>
and L<Gábor Szabó|https://metacpan.org/author/szabgab>

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2009 L<Thomas Klausner|https://metacpan.org/author/domm>

Copyright © 2006–2008 L<Gábor Szabó|https://metacpan.org/author/szabgab>

You may use and distribute this module according to the same terms
that Perl is distributed under.
