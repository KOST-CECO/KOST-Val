package Net::DNS::RR::CSYNC;

use strict;
use warnings;
our $VERSION = (qw$Id: CSYNC.pm 1910 2023-03-30 19:16:30Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::CSYNC - DNS CSYNC resource record

=cut

use integer;

use Net::DNS::RR::NSEC;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	@{$self}{qw(soaserial flags)} = unpack "\@$offset Nn", $$data;
	$offset += 6;
	$self->{typebm} = substr $$data, $offset, $limit - $offset;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack 'N n a*', $self->soaserial, $self->flags, $self->{typebm};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @rdata = ( $self->soaserial, $self->flags, $self->typelist );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	$self->soaserial( shift @argument );
	$self->flags( shift @argument );
	$self->typelist(@argument);
	return;
}


sub soaserial {
	my ( $self, @value ) = @_;
	for (@value) { $self->{soaserial} = 0 + $_ }
	return $self->{soaserial} || 0;
}


sub flags {
	my ( $self, @value ) = @_;
	for (@value) { $self->{flags} = 0 + $_ }
	return $self->{flags} || 0;
}


sub immediate {
	my ( $self, @value ) = @_;
	for ( $self->{flags} |= 0 ) {
		if ( scalar @value ) {
			$_ |= 0x0001;
			$_ ^= 0x0001 unless shift @value;
		}
	}
	return $self->{flags} & 0x0001;
}


sub soaminimum {
	my ( $self, @value ) = @_;
	for ( $self->{flags} |= 0 ) {
		if ( scalar @value ) {
			$_ |= 0x0002;
			$_ ^= 0x0002 unless shift @value;
		}
	}
	return $self->{flags} & 0x0002;
}


sub typelist {
	return &Net::DNS::RR::NSEC::typelist;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name CSYNC SOAserial flags typelist');

=head1 DESCRIPTION

Class for DNSSEC CSYNC resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 SOAserial

=head2 soaserial

    $soaserial = $rr->soaserial;
    $rr->soaserial( $soaserial );

The SOA Serial field contains a copy of the 32-bit SOA serial number from
the child zone.

=head2 flags

    $flags = $rr->flags;
    $rr->flags( $flags );

The flags field contains 16 bits of boolean flags that define operations
which affect the processing of the CSYNC record.

=over 4

=item immediate

 $rr->immediate(1);

 if ( $rr->immediate ) {
	...
 }

If not set, a parental agent must not process the CSYNC record until
the zone administrator approves the operation through an out-of-band
mechanism.

=back

=over 4

=item soaminimum

 $rr->soaminimum(1);

 if ( $rr->soaminimum ) {
	...
 }

If set, a parental agent querying child authoritative servers must not
act on data from zones advertising an SOA serial number less than the
SOAserial value.

=back

=head2 typelist

    @typelist = $rr->typelist;
    $typelist = $rr->typelist;

The type list indicates the record types to be processed by the parental
agent. When called in scalar context, the list is interpolated into a
string.


=head1 COPYRIGHT

Copyright (c)2015 Dick Franks

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
L<RFC7477|https://tools.ietf.org/html/rfc7477>

=cut
