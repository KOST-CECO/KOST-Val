package Net::DNS::RR::GPOS;

use strict;
use warnings;
our $VERSION = (qw$Id: GPOS.pm 1910 2023-03-30 19:16:30Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::GPOS - DNS GPOS resource record

=cut

use integer;

use Carp;
use Net::DNS::Text;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	for (qw(latitude longitude altitude)) {
		my $text;
		( $text, $offset ) = Net::DNS::Text->decode( $data, $offset );
		$self->$_( $text->value );
	}
	croak('corrupt GPOS data') unless $offset == $limit;	# more or less FUBAR
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return join '', map { Net::DNS::Text->new($_)->encode } @{$self}{qw(latitude longitude altitude)};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	return map { Net::DNS::Text->new($_)->string } @{$self}{qw(latitude longitude altitude)};
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	$self->latitude( shift @argument );
	$self->longitude( shift @argument );
	$self->altitude(@argument);
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->_parse_rdata(qw(0.0 0.0 0.0));
	return;
}


sub latitude {
	my ( $self, @value ) = @_;
	for (@value) { return $self->{latitude} = _fp($_) }
	return $self->{latitude};
}


sub longitude {
	my ( $self, @value ) = @_;
	for (@value) { return $self->{longitude} = _fp($_) }
	return $self->{longitude};
}


sub altitude {
	my ( $self, @value ) = @_;
	for (@value) { return $self->{altitude} = _fp($_) }
	return $self->{altitude};
}


########################################

sub _fp {
	no integer;
	return sprintf( '%1.10g', 0.0 + shift );
}

########################################


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name GPOS latitude longitude altitude');

=head1 DESCRIPTION

Class for DNS Geographical Position (GPOS) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 latitude

    $latitude = $rr->latitude;
    $rr->latitude( $latitude );

Floating-point representation of latitude, in degrees.

=head2 longitude

    $longitude = $rr->longitude;
    $rr->longitude( $longitude );

Floating-point representation of longitude, in degrees.

=head2 altitude

    $altitude = $rr->altitude;
    $rr->altitude( $altitude );

Floating-point representation of altitude, in metres.


=head1 COPYRIGHT

Copyright (c)1997,1998 Michael Fuhr. 

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
L<RFC1712|https://tools.ietf.org/html/rfc1712>

=cut
