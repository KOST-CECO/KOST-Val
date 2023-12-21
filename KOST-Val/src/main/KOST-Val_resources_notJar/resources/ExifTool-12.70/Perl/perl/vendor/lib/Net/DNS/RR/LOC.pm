package Net::DNS::RR::LOC;

use strict;
use warnings;
our $VERSION = (qw$Id: LOC.pm 1896 2023-01-30 12:59:25Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::LOC - DNS LOC resource record

=cut

use integer;

use Carp;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $version = $self->{version} = unpack "\@$offset C", $$data;
	@{$self}{qw(size hp vp latitude longitude altitude)} = unpack "\@$offset xC3N3", $$data;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack 'C4N3', @{$self}{qw(version size hp vp latitude longitude altitude)};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my ( $altitude, @precision ) = map { $self->$_() . 'm' } qw(altitude size hp vp);
	my $precision = join ' ', @precision;
	for ($precision) {
		s/^1m 10000m 10m$//;
		s/ 10000m 10m$//;
		s/ 10m$//;
	}
	return ( $self->latitude, '', $self->longitude, '', $altitude, $precision );
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	my @lat;
	while ( scalar @argument ) {
		my $this = shift @argument;
		push( @lat, $this );
		last if $this =~ /[NSns]/;
	}
	$self->latitude(@lat);

	my @long;
	while ( scalar @argument ) {
		my $this = shift @argument;
		push( @long, $this );
		last if $this =~ /[EWew]/;
	}
	$self->longitude(@long);

	foreach my $attr (qw(altitude size hp vp)) {
		$self->$attr(@argument);
		shift @argument;
	}
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->{version} = 0;
	$self->size(1);
	$self->hp(10000);
	$self->vp(10);
	return;
}


sub latitude {
	my ( $self, @value ) = @_;
	$self->{latitude} = _encode_angle(@value) if scalar @value;
	return _decode_angle( $self->{latitude} || return, 'N', 'S' );
}


sub longitude {
	my ( $self, @value ) = @_;
	$self->{longitude} = _encode_angle(@value) if scalar @value;
	return _decode_angle( $self->{longitude} || return, 'E', 'W' );
}


sub altitude {
	my ( $self, @value ) = @_;
	$self->{altitude} = _encode_alt(@value) if scalar @value;
	return _decode_alt( $self->{altitude} );
}


sub size {
	my ( $self, @value ) = @_;
	$self->{size} = _encode_prec(@value) if scalar @value;
	return _decode_prec( $self->{size} );
}


sub hp {
	my ( $self, @value ) = @_;
	$self->{hp} = _encode_prec(@value) if scalar @value;
	return _decode_prec( $self->{hp} );
}

sub horiz_pre { return &hp; }					# uncoverable pod


sub vp {
	my ( $self, @value ) = @_;
	$self->{vp} = _encode_prec(@value) if scalar @value;
	return _decode_prec( $self->{vp} );
}

sub vert_pre { return &vp; }					# uncoverable pod


sub latlon {
	my ( $self, @argument ) = @_;
	my @lat = @argument;
	my ( undef, @long ) = @argument;
	return ( scalar $self->latitude(@lat), scalar $self->longitude(@long) );
}


sub version {
	return shift->{version};
}


########################################

no integer;

use constant ALTITUDE0 => 10000000;
use constant ORDINATE0 => 0x80000000;

sub _decode_angle {
	my ( $msec, $N, $S ) = @_;
	return int( 0.5 + ( $msec - ORDINATE0 ) / 0.36 ) / 10000000 unless wantarray;
	use integer;
	my $abs = abs( $msec - ORDINATE0 );
	my $deg = int( $abs / 3600000 );
	my $min = int( $abs / 60000 ) % 60;
	no integer;
	my $sec = ( $abs % 60000 ) / 1000;
	return ( $deg, $min, $sec, ( $msec < ORDINATE0 ? $S : $N ) );
}


sub _encode_angle {
	my @ang = @_;
	@ang = split /[\s\260'"]+/, shift @ang unless scalar @ang > 1;
	my $ang = ( 0 + shift @ang ) * 3600000;
	my $neg = ( @ang ? pop @ang : '' ) =~ /[SWsw]/;
	$ang += ( @ang ? shift @ang : 0 ) * 60000;
	$ang += ( @ang ? shift @ang : 0 ) * 1000;
	return int( 0.5 + ( $neg ? ORDINATE0 - $ang : ORDINATE0 + $ang ) );
}


sub _decode_alt {
	my $cm = ( shift || ALTITUDE0 ) - ALTITUDE0;
	return 0.01 * $cm;
}


sub _encode_alt {
	( my $argument = shift ) =~ s/[Mm]$//;
	$argument += 0;
	return int( 0.5 + ALTITUDE0 + 100 * $argument );
}


my @power10 = ( 0.01, 0.1, 1, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 0, 0, 0, 0, 0 );

sub _decode_prec {
	my $argument = shift || 0;
	my $mantissa = $argument >> 4;
	return $mantissa * $power10[$argument & 0x0F];
}

sub _encode_prec {
	( my $argument = shift ) =~ s/[Mm]$//;
	my $exponent = 0;
	until ( $argument < $power10[1 + $exponent] ) { $exponent++ }
	my $mantissa = int( 0.5 + $argument / $power10[$exponent] );
	return ( $mantissa & 0xF ) << 4 | $exponent;
}

########################################


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name LOC latitude longitude altitude size hp vp');

=head1 DESCRIPTION

DNS geographical location (LOC) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 latitude

    $latitude = $rr->latitude;
    ($deg, $min, $sec, $ns ) = $rr->latitude;

    $rr->latitude( 42.357990 );
    $rr->latitude( 42, 21, 28.764, 'N' );
    $rr->latitude( '42 21 28.764 N' );

When invoked in scalar context, latitude is returned in degrees,
a negative ordinate being south of the equator.

When invoked in list context, latitude is returned as a list of
separate degree, minute, and second values followed by N or S
as appropriate.

Optional replacement values may be represented as single value, list
or formatted string. Trailing zero values are optional.

=head2 longitude

    $longitude = $rr->longitude;
    ($deg, $min, $sec, $ew ) = $rr->longitude;

    $rr->longitude( -71.014338 );
    $rr->longitude( 71, 0, 51.617, 'W' );
    $rr->longitude( '71 0 51.617 W' );

When invoked in scalar context, longitude is returned in degrees,
a negative ordinate being west of the prime meridian.

When invoked in list context, longitude is returned as a list of
separate degree, minute, and second values followed by E or W
as appropriate.

=head2 altitude

    $altitude = $rr->altitude;

Represents altitude, in metres, relative to the WGS 84 reference
spheroid used by GPS.

=head2 size

    $size = $rr->size;

Represents the diameter, in metres, of a sphere enclosing the
described entity.

=head2 hp

    $hp = $rr->hp;

Represents the horizontal precision of the data expressed as the
diameter, in metres, of the circle of error.

=head2 vp

    $vp = $rr->vp;

Represents the vertical precision of the data expressed as the
total spread, in metres, of the distribution of possible values.

=head2 latlon

    ($lat, $lon) = $rr->latlon;
    $rr->latlon($lat, $lon);

Representation of the latitude and longitude coordinate pair as
signed floating-point degrees.

=head2 version

    $version = $rr->version;

Version of LOC protocol.


=head1 COPYRIGHT

Copyright (c)1997 Michael Fuhr. 

Portions Copyright (c)2011 Dick Franks. 

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
L<RFC1876|https://tools.ietf.org/html/rfc1876>

=cut
