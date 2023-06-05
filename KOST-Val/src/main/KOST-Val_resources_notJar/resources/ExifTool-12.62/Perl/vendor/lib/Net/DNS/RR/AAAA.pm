package Net::DNS::RR::AAAA;

use strict;
use warnings;
our $VERSION = (qw$Id: AAAA.pm 1814 2020-10-14 21:49:16Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::AAAA - DNS AAAA resource record

=cut

use integer;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my $self = shift;
	my ( $data, $offset ) = @_;

	$self->{address} = unpack "\@$offset a16", $$data;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack 'a16', $self->{address};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	return $self->address_short;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my $self = shift;

	$self->address(shift);
	return;
}


sub address_long {
	my $addr = pack 'a*@16', grep {defined} shift->{address};
	return sprintf '%x:%x:%x:%x:%x:%x:%x:%x', unpack 'n8', $addr;
}


sub address_short {
	my $addr = pack 'a*@16', grep {defined} shift->{address};
	local $_ = sprintf ':%x:%x:%x:%x:%x:%x:%x:%x:', unpack 'n8', $addr;
	s/(:0[:0]+:)(?!.+:0\1)/::/;				# squash longest zero sequence
	s/^:// unless /^::/;					# prune LH :
	s/:$// unless /::$/;					# prune RH :
	return $_;
}


sub address {
	my $self = shift;

	return address_long($self) unless scalar @_;

	my $addr  = shift;
	my @parse = split /:/, "0$addr";

	if ( (@parse)[$#parse] =~ /\./ ) {			# embedded IPv4
		my @ip4 = split /\./, pop(@parse);
		my $rhs = pop(@ip4);
		my @ip6 = map { /./ ? hex($_) : (0) x ( 7 - @parse ) } @parse;
		return $self->{address} = pack 'n6 C4', @ip6, @ip4, (0) x ( 3 - @ip4 ), $rhs;
	}

	# Note: pack() masks overlarge values, mostly without warning.
	my @expand = map { /./ ? hex($_) : (0) x ( 9 - @parse ) } @parse;
	return $self->{address} = pack 'n8', @expand;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name IN AAAA address');

    $rr = Net::DNS::RR->new(
	name	=> 'example.com',
	type	=> 'AAAA',
	address => '2001:DB8::8:800:200C:417A'
	);

=head1 DESCRIPTION

Class for DNS IPv6 Address (AAAA) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 address

    $IPv6_address = $rr->address;

Returns the text representation of the IPv6 address.


=head2 address_long

    $IPv6_address = $rr->address_long;

Returns the text representation specified in RFC3513, 2.2(1).


=head2 address_short

    $IPv6_address = $rr->address_short;

Returns the textual form of address recommended by RFC5952.


=head1 COPYRIGHT

Copyright (c)1997 Michael Fuhr. 

Portions Copyright (c)2003 Chris Reinhardt.

Portions Copyright (c)2012 Dick Franks.

All rights reserved.

Package template (c)2009,2012 O.M.Kolkman and R.W.Franks.


=head1 LICENSE

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose and without fee is hereby granted, provided
that the above copyright notice appear in all copies and that both that
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

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, RFC3596, RFC3513, RFC5952

=cut
