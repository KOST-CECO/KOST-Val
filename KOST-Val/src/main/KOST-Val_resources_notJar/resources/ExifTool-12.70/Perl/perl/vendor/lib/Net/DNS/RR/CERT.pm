package Net::DNS::RR::CERT;

use strict;
use warnings;
our $VERSION = (qw$Id: CERT.pm 1896 2023-01-30 12:59:25Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::CERT - DNS CERT resource record

=cut

use integer;

use Carp;
use MIME::Base64;

my %certtype = (
	PKIX	=> 1,						# X.509 as per PKIX
	SPKI	=> 2,						# SPKI certificate
	PGP	=> 3,						# OpenPGP packet
	IPKIX	=> 4,						# The URL of an X.509 data object
	ISPKI	=> 5,						# The URL of an SPKI certificate
	IPGP	=> 6,						# The fingerprint and URL of an OpenPGP packet
	ACPKIX	=> 7,						# Attribute Certificate
	IACPKIX => 8,						# The URL of an Attribute Certificate
	URI	=> 253,						# URI private
	OID	=> 254,						# OID private
	);


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	@{$self}{qw(certtype keytag algorithm)} = unpack "\@$offset n2 C", $$data;
	$self->{certbin} = substr $$data, $offset + 5, $self->{rdlength} - 5;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack "n2 C a*", $self->certtype, $self->keytag, $self->algorithm, $self->{certbin};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @param = ( $self->certtype, $self->keytag, $self->algorithm );
	my @rdata = ( @param, split /\s+/, encode_base64( $self->{certbin} ) );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	foreach (qw(certtype keytag algorithm)) {
		$self->$_( shift @argument );
	}
	$self->cert(@argument);
	return;
}


sub certtype {
	my ( $self, @value ) = @_;

	return $self->{certtype} unless scalar @value;

	my $certtype = shift @value;
	return $self->{certtype} = $certtype unless $certtype =~ /\D/;

	my $typenum = $certtype{$certtype};
	$typenum || croak qq[unknown certtype $certtype];
	return $self->{certtype} = $typenum;
}


sub keytag {
	my ( $self, @value ) = @_;
	for (@value) { $self->{keytag} = 0 + $_ }
	return $self->{keytag} || 0;
}


sub algorithm {
	my ( $self, $arg ) = @_;

	return $self->{algorithm} unless defined $arg;
	return _algbyval( $self->{algorithm} ) if uc($arg) eq 'MNEMONIC';
	return $self->{algorithm} = _algbyname($arg);
}


sub certificate { return &certbin; }


sub certbin {
	my ( $self, @value ) = @_;
	for (@value) { $self->{certbin} = $_ }
	return $self->{certbin} || "";
}


sub cert {
	my ( $self, @value ) = @_;
	return MIME::Base64::encode( $self->certbin(), "" ) unless scalar @value;
	return $self->certbin( MIME::Base64::decode( join "", @value ) );
}


sub format { return &certtype; }				# uncoverable pod

sub tag { return &keytag; }					# uncoverable pod


########################################

{
	my @algbyname = (
		'DELETE'	     => 0,			# [RFC4034][RFC4398][RFC8078]
		'RSAMD5'	     => 1,			# [RFC3110][RFC4034]
		'DH'		     => 2,			# [RFC2539]
		'DSA'		     => 3,			# [RFC3755][RFC2536]
					## Reserved	=> 4,	# [RFC6725]
		'RSASHA1'	     => 5,			# [RFC3110][RFC4034]
		'DSA-NSEC3-SHA1'     => 6,			# [RFC5155]
		'RSASHA1-NSEC3-SHA1' => 7,			# [RFC5155]
		'RSASHA256'	     => 8,			# [RFC5702]
					## Reserved	=> 9,	# [RFC6725]
		'RSASHA512'	     => 10,			# [RFC5702]
					## Reserved	=> 11,	# [RFC6725]
		'ECC-GOST'	     => 12,			# [RFC5933]
		'ECDSAP256SHA256'    => 13,			# [RFC6605]
		'ECDSAP384SHA384'    => 14,			# [RFC6605]
		'ED25519'	     => 15,			# [RFC8080]
		'ED448'		     => 16,			# [RFC8080]

		'INDIRECT'   => 252,				# [RFC4034]
		'PRIVATEDNS' => 253,				# [RFC4034]
		'PRIVATEOID' => 254,				# [RFC4034]
					## Reserved	=> 255,	# [RFC4034]
		);

	my %algbyval = reverse @algbyname;

	foreach (@algbyname) { s/[\W_]//g; }			# strip non-alphanumerics
	my @algrehash = map { /^\d/ ? ($_) x 3 : uc($_) } @algbyname;
	my %algbyname = @algrehash;				# work around broken cperl

	sub _algbyname {
		my $arg = shift;
		my $key = uc $arg;				# synthetic key
		$key =~ s/[\W_]//g;				# strip non-alphanumerics
		my $val = $algbyname{$key};
		return $val if defined $val;
		return $key =~ /^\d/ ? $arg : croak qq[unknown algorithm $arg];
	}

	sub _algbyval {
		my $value = shift;
		return $algbyval{$value} || return $value;
	}
}

########################################


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name IN CERT certtype keytag algorithm cert');

=head1 DESCRIPTION

Class for DNS Certificate (CERT) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 certtype

    $certtype = $rr->certtype;

Returns the certtype code for the certificate (in numeric form).

=head2 keytag

    $keytag = $rr->keytag;
    $rr->keytag( $keytag );

Returns the key tag for the public key in the certificate

=head2 algorithm

    $algorithm = $rr->algorithm;

Returns the algorithm used by the certificate (in numeric form).

=head2 certificate

=head2 certbin

    $certbin = $rr->certbin;
    $rr->certbin( $certbin );

Binary representation of the certificate.

=head2 cert

    $cert = $rr->cert;
    $rr->cert( $cert );

Base64 representation of the certificate.


=head1 COPYRIGHT

Copyright (c)2002 VeriSign, Mike Schiraldi

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
L<RFC4398|https://tools.ietf.org/html/rfc4398>

L<Algorithm Numbers|http://www.iana.org/assignments/dns-sec-alg-numbers>

=cut
