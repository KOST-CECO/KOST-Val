package Module::CPANTS::Kwalitee::Signature;
use strict;
use warnings;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 100 }

sub analyse {
    my ($class, $self) = @_;

    # NOTE: The analysis/metric in this module has moved to
    # Module::CPANTS::SiteKwalitee because this requires an external
    # tool (though optional) and decent network connection to
    # validate a signature.

    # Note also that this stub should not be removed so that
    # this can replace the old ::Signature module, and the old
    # metrics will not be loaded while loading plugins.
}

sub kwalitee_indicators {
    return [];
}

1;

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::Signature - dist has a valid signature

=head1 SYNOPSIS

The metrics in this module have moved to L<Module::CPANTS::SiteKwalitee::Signature|https://github.com/cpants/Module-CPANTS-SiteKwalitee>.

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

Lars Dɪᴇᴄᴋᴏᴡ C<< <daxim@cpan.org> >>

=head1 LICENCE AND COPYRIGHT

Copyright © 2012, Lars Dɪᴇᴄᴋᴏᴡ C<< <daxim@cpan.org> >>.

This module is free software; you can redistribute it and/or
modify it under the same terms as Perl 5.14.
