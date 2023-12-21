package Net::DNS::RR::AMTRELAY;

use strict;
use warnings;
our $VERSION = (qw$Id: AMTRELAY.pm 1896 2023-01-30 12:59:25Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::AMTRELAY - DNS AMTRELAY resource record

=cut

use integer;

use Carp;

use Net::DNS::DomainName;
use Net::DNS::RR::A;
use Net::DNS::RR::AAAA;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $size = $self->{rdlength} - 2;
	@{$self}{qw(precedence relaytype relay)} = unpack "\@$offset C2 a$size", $$data;

	for ( $self->relaytype ) {
		/^3$/ && return $self->{relay} = Net::DNS::DomainName->decode( $data, $offset + 2 );
		/^2$/ && return $self->{relay} = pack( 'a16', $self->{relay} );
		/^1$/ && return $self->{relay} = pack( 'a4',  $self->{relay} );
	}
	$self->{relay} = '';
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	for ( $self->relaytype ) {
		/^3$/ && return pack( 'C2 a*',	@{$self}{qw(precedence relaytype)}, $self->{relay}->encode );
		/^2$/ && return pack( 'C2 a16', @{$self}{qw(precedence relaytype relay)} );
		/^1$/ && return pack( 'C2 a4',	@{$self}{qw(precedence relaytype relay)} );
	}
	return pack( 'C2', @{$self}{qw(precedence relaytype)} );
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @rdata = map { $self->$_ } qw(precedence dbit relaytype relay);
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	foreach (qw(precedence dbit relaytype relay)) {
		$self->$_( shift @argument );
	}
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	@{$self}{qw(precedence relaytype relay)} = ( 0, 0, '' );
	return;
}


sub precedence {
	my ( $self, @value ) = @_;
	for (@value) { $self->{precedence} = 0 + $_ }
	return $self->{precedence} || 0;
}


sub dbit {
	my ( $self, @value ) = @_;				# uncoverable pod
	for (@value) { $self->{relaytype} = $self->relaytype | ( $_ ? 0x80 : 0 ) }
	return ( $self->{relaytype} || 0 ) >> 7;
}

sub d { return &dbit }						# uncoverable pod


sub relaytype {
	my ( $self, @value ) = @_;
	for (@value) { $self->{relaytype} = $self->dbit ? ( 0x80 | $_ ) : $_ }
	return 0x7f & ( $self->{relaytype} || 0 );
}


sub relay {
	my ( $self, @value ) = @_;

	for (@value) {
		/^\.*$/ && do {
			$self->relaytype(0);
			$self->{relay} = '';			# no relay
			last;
		};
		/:.*:/ && do {
			$self->relaytype(2);
			$self->{relay} = Net::DNS::RR::AAAA::address( {}, $_ );
			last;
		};
		/\.\d+$/ && do {
			$self->relaytype(1);
			$self->{relay} = Net::DNS::RR::A::address( {}, $_ );
			last;
		};
		/\..+/ && do {
			$self->relaytype(3);
			$self->{relay} = Net::DNS::DomainName->new($_);
			last;
		};
		croak 'unrecognised relay type';
	}

	if ( defined wantarray ) {
		for ( $self->relaytype ) {
			/^1$/ && return Net::DNS::RR::A::address( {address => $self->{relay}} );
			/^2$/ && return Net::DNS::RR::AAAA::address( {address => $self->{relay}} );
			/^3$/ && return wantarray ? $self->{relay}->string : $self->{relay}->name;
		}
	}
	return wantarray ? '.' : undef;
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
    $rr = Net::DNS::RR->new('owner AMTRELAY precedence Dbit relaytype relay');

=head1 DESCRIPTION

AMTRELAY resource record designed to permit DNS Reverse IP AMT Discovery
(DRIAD), a mechanism for AMT gateways to discover AMT relays that are
capable of forwarding multicast traffic from a known source IP address.

AMT (Automatic Multicast Tunneling) is defined in RFC7450 and provides a
method to transport multicast traffic over a unicast tunnel in order to
traverse network segments that are not multicast capable.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 precedence

    $precedence = $rr->precedence;
    $rr->precedence( $precedence );

8-bit integer which indicates relative precedence within the RRset.
Relays listed in AMTRELAY records with lower precedence are to be
attempted first.

=head2 Dbit, Discovery Optional

    $Dbit = $rr->Dbit;
    $rr->Dbit(1);

Boolean field which indicates that the gateway MAY send an AMT Request
message directly to the discovered relay address without first sending
an AMT Discovery message.

=head2 relaytype

    $relaytype = $rr->relaytype;

The relaytype type field indicates the format of the information that is
stored in the relay field.

The following values are defined:

=over 4

0: The relay field is empty (0 bytes).

1: The relay field contains a 4-octet IPv4 address.

2: The relay field contains a 16-octet IPv6 address.

3: The relay field contains a wire-encoded domain name.

=back

=head2 relay

    $relay = $rr->relay;
    $rr->relay( $relay );

The relay field is the address or domain name of the AMT relay.
It is formatted according to the relaytype field.


=head1 COPYRIGHT

Copyright (c)2020 Dick Franks.

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
L<RFC8777|https://tools.ietf.org/html/rfc8777>

=cut
