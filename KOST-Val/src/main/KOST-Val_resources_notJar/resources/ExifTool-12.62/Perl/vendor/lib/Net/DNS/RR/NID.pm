package Net::DNS::RR::NID;

use strict;
use warnings;
our $VERSION = (qw$Id: NID.pm 1896 2023-01-30 12:59:25Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::NID - DNS NID resource record

=cut

use integer;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	@{$self}{qw(preference nodeid)} = unpack "\@$offset n a8", $$data;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack 'n a8', $self->{preference}, $self->{nodeid};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	return join ' ', $self->preference, $self->nodeid;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	for (qw(preference nodeid)) { $self->$_( shift @argument ) }
	return;
}


sub preference {
	my ( $self, @value ) = @_;
	for (@value) { $self->{preference} = 0 + $_ }
	return $self->{preference} || 0;
}


sub nodeid {
	my ( $self, $idnt ) = @_;
	$self->{nodeid} = pack 'n4', map { hex($_) } split /:/, $idnt if defined $idnt;
	return $self->{nodeid} ? sprintf( '%0.4x:%0.4x:%0.4x:%0.4x', unpack 'n4', $self->{nodeid} ) : undef;
}


my $function = sub {			## sort RRs in numerically ascending order.
	return $Net::DNS::a->{'preference'} <=> $Net::DNS::b->{'preference'};
};

__PACKAGE__->set_rrsort_func( 'preference', $function );

__PACKAGE__->set_rrsort_func( 'default_sort', $function );


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name IN NID preference nodeid');

    $rr = Net::DNS::RR->new(
	name	   => 'example.com',
	type	   => 'NID',
	preference => 10,
	nodeid	   => '8:800:200C:417A'
	);

=head1 DESCRIPTION

Class for DNS Node Identifier (NID) resource records.

The Node Identifier (NID) DNS resource record is used to hold values
for Node Identifiers that will be used for ILNP-capable nodes.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 preference

    $preference = $rr->preference;
    $rr->preference( $preference );

A 16 bit unsigned integer in network byte order that indicates the
relative preference for this NID record among other NID records
associated with this owner name.  Lower values are preferred over
higher values.

=head2 nodeid

    $nodeid = $rr->nodeid;

The NodeID field is an unsigned 64-bit value in network byte order.
The text representation uses the same syntax (i.e., groups of 4
hexadecimal digits separated by a colons) that is already used for
IPv6 interface identifiers.


=head1 COPYRIGHT

Copyright (c)2012 Dick Franks.

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
L<RFC6742|https://tools.ietf.org/html/rfc6742>

=cut
