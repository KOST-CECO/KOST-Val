package Net::DNS::RR::KX;

use strict;
use warnings;
our $VERSION = (qw$Id: KX.pm 1898 2023-02-15 14:27:22Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::KX - DNS KX resource record

=cut

use integer;

use Net::DNS::DomainName;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	$self->{preference} = unpack( "\@$offset n", $$data );
	$self->{exchange}   = Net::DNS::DomainName2535->decode( $data, $offset + 2 );
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my ( $self, $offset, @opaque ) = @_;

	my $exchange = $self->{exchange};
	return pack 'n a*', $self->preference, $exchange->encode( $offset + 2, @opaque );
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my $exchange = $self->{exchange};
	return join ' ', $self->preference, $exchange->string;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	for (qw(preference exchange)) { $self->$_( shift @argument ) }
	return;
}


sub preference {
	my ( $self, @value ) = @_;
	for (@value) { $self->{preference} = 0 + $_ }
	return $self->{preference} || 0;
}


sub exchange {
	my ( $self, @value ) = @_;
	for (@value) { $self->{exchange} = Net::DNS::DomainName2535->new($_) }
	return $self->{exchange} ? $self->{exchange}->name : undef;
}


my $function = sub {			## sort RRs in numerically ascending order.
	$Net::DNS::a->{'preference'} <=> $Net::DNS::b->{'preference'};
};

__PACKAGE__->set_rrsort_func( 'preference', $function );

__PACKAGE__->set_rrsort_func( 'default_sort', $function );


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name KX preference exchange');

=head1 DESCRIPTION

DNS Key Exchange Delegation (KX) record

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 preference

    $preference = $rr->preference;
    $rr->preference( $preference );

A 16 bit integer which specifies the preference
given to this RR among others at the same owner.
Lower values are preferred.

=head2 exchange

    $exchange = $rr->exchange;
    $rr->exchange( $exchange );

A domain name which specifies a host willing
to act as a key exchange for the owner name.


=head1 COPYRIGHT

Copyright (c)2009 Olaf Kolkman, NLnet Labs.

All rights reserved.

Package template (c)2009,2012 O.M.Kolkman and R.W.Franks.


=head1 LICENSE

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose and without fee is hereby granted, provided
that the original copyright notices appear in all copies and that both
copyright notice and this permission notice appear in supporting
documentation, and that the name of the author not be used in advertising
or publicity pertaining to distribution of the software without specific
prior written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.


=head1 SEE ALSO

L<perl> L<Net::DNS> L<Net::DNS::RR>
L<RFC2230|https://tools.ietf.org/html/rfc2230>

=cut
