package Net::DNS::RR::GPOS;

use strict;
use warnings;
our $VERSION = (qw$Id: GPOS.pm 1814 2020-10-14 21:49:16Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::GPOS - DNS GPOS resource record

=cut

use integer;

use Carp;
use Net::DNS::Text;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my $self = shift;
	my ( $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	( $self->{latitude},  $offset ) = Net::DNS::Text->decode( $data, $offset ) if $offset < $limit;
	( $self->{longitude}, $offset ) = Net::DNS::Text->decode( $data, $offset ) if $offset < $limit;
	( $self->{altitude},  $offset ) = Net::DNS::Text->decode( $data, $offset ) if $offset < $limit;
	croak('corrupt GPOS data') unless $offset == $limit;	# more or less FUBAR
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return '' unless defined $self->{altitude};
	return join '', map { $self->{$_}->encode } qw(latitude longitude altitude);
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	return '' unless defined $self->{altitude};
	return join ' ', map { $self->{$_}->string } qw(latitude longitude altitude);
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my $self = shift;

	$self->latitude(shift);
	$self->longitude(shift);
	$self->altitude(shift);
	die 'too many arguments for GPOS' if scalar @_;
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->_parse_rdata(qw(0.0 0.0 0.0));
	return;
}


sub latitude {
	my $self = shift;
	$self->{latitude} = _fp2text(shift) if scalar @_;
	return defined(wantarray) ? _text2fp( $self->{latitude} ) : undef;
}


sub longitude {
	my $self = shift;
	$self->{longitude} = _fp2text(shift) if scalar @_;
	return defined(wantarray) ? _text2fp( $self->{longitude} ) : undef;
}


sub altitude {
	my $self = shift;
	$self->{altitude} = _fp2text(shift) if scalar @_;
	return defined(wantarray) ? _text2fp( $self->{altitude} ) : undef;
}


########################################

sub _fp2text {
	return Net::DNS::Text->new( sprintf( '%1.10g', shift ) );
}

sub _text2fp {
	no integer;
	return ( 0.0 + shift->value );
}


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

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, RFC1712

=cut
