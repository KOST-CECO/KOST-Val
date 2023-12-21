package Net::DNS::RR::ZONEMD;

use strict;
use warnings;
our $VERSION = (qw$Id: ZONEMD.pm 1896 2023-01-30 12:59:25Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::ZONEMD - DNS ZONEMD resource record

=cut

use integer;

use Carp;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $rdata = substr $$data, $offset, $self->{rdlength};
	@{$self}{qw(serial scheme algorithm digestbin)} = unpack 'NC2a*', $rdata;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack 'NC2a*', @{$self}{qw(serial scheme algorithm digestbin)};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @digest = split /(\S{64})/, $self->digest || qq("");
	my @rdata  = ( @{$self}{qw(serial scheme algorithm)}, @digest );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	for (qw(serial scheme algorithm)) { $self->$_( shift @argument ) }
	$self->digest(@argument);
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->_parse_rdata( 0, 1, 1, '' );
	return;
}


sub serial {
	my ( $self, @value ) = @_;
	for (@value) { $self->{serial} = 0 + $_ }
	return $self->{serial} || 0;
}


sub scheme {
	my ( $self, @value ) = @_;
	for (@value) { $self->{scheme} = 0 + $_ }
	return $self->{scheme} || 0;
}


sub algorithm {
	my ( $self, @value ) = @_;
	for (@value) { $self->{algorithm} = 0 + $_ }
	return $self->{algorithm} || 0;
}


sub digest {
	my ( $self, @value ) = @_;
	return unpack "H*", $self->digestbin() unless scalar @value;
	my @hex = map { /^"*([\dA-Fa-f]*)"*$/ || croak("corrupt hex"); $1 } @value;
	return $self->digestbin( pack "H*", join "", @hex );
}


sub digestbin {
	my ( $self, @value ) = @_;
	for (@value) { $self->{digestbin} = $_ }
	return $self->{digestbin} || "";
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new("example.com. ZONEMD 2018031500 1 1
	FEBE3D4CE2EC2FFA4BA99D46CD69D6D29711E55217057BEE
	7EB1A7B641A47BA7FED2DD5B97AE499FAFA4F22C6BD647DE");

=head1 DESCRIPTION

Class for DNS Zone Message Digest (ZONEMD) resource record.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 serial

    $serial = $rr->serial;
    $rr->serial( $serial );

Unsigned 32-bit integer zone serial number.

=head2 scheme

    $scheme = $rr->scheme;
    $rr->scheme( $scheme );

The scheme field is an 8-bit unsigned integer that identifies the
methods by which data is collated and presented as input to the
hashing function.

=head2 algorithm

    $algorithm = $rr->algorithm;
    $rr->algorithm( $algorithm );

The algorithm field is an 8-bit unsigned integer that identifies
the cryptographic hash algorithm used to construct the digest.

=head2 digest

    $digest = $rr->digest;
    $rr->digest( $digest );

Hexadecimal representation of the digest over the zone content.

=head2 digestbin

    $digestbin = $rr->digestbin;
    $rr->digestbin( $digestbin );

Binary representation of the digest over the zone content.


=head1 COPYRIGHT

Copyright (c)2019 Dick Franks.

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
L<RFC8976|https://tools.ietf.org/html/rfc8976>

=cut
