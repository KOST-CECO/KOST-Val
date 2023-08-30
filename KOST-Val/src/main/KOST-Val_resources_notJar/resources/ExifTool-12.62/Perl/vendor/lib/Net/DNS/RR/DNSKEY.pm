package Net::DNS::RR::DNSKEY;

use strict;
use warnings;
our $VERSION = (qw$Id: DNSKEY.pm 1814 2020-10-14 21:49:16Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::DNSKEY - DNS DNSKEY resource record

=cut

use integer;

use Carp;

use constant BASE64 => defined eval { require MIME::Base64 };

#
# source: http://www.iana.org/assignments/dns-sec-alg-numbers
#
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
		return $key =~ /^\d/ ? $arg : croak qq[unknown algorithm "$arg"];
	}

	sub _algbyval {
		my $value = shift;
		return $algbyval{$value} || return $value;
	}
}


sub _decode_rdata {			## decode rdata from wire-format octet string
	my $self = shift;
	my ( $data, $offset ) = @_;

	my $rdata = substr $$data, $offset, $self->{rdlength};
	$self->{keybin} = unpack '@4 a*', $rdata;
	@{$self}{qw(flags protocol algorithm)} = unpack 'n C*', $rdata;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack 'n C2 a*', @{$self}{qw(flags protocol algorithm keybin)};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my $algorithm = $self->{algorithm};
	$self->_annotation( 'Key ID =', $self->keytag ) if $algorithm;
	return $self->SUPER::_format_rdata() unless BASE64;
	my @param = ( @{$self}{qw(flags protocol)}, $algorithm );
	my @rdata = ( @param, split /\s+/, MIME::Base64::encode( $self->{keybin} ) || '-' );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my $self = shift;

	my $flags = shift;		## avoid destruction by CDNSKEY algorithm(0)
	$self->protocol(shift);
	$self->algorithm(shift);
	$self->flags($flags);
	$self->key(@_);
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->algorithm(1);
	$self->flags(256);
	$self->protocol(3);
	$self->keybin('');
	return;
}


sub flags {
	my $self = shift;

	$self->{flags} = 0 + shift if scalar @_;
	return $self->{flags} || 0;
}


sub zone {
	my $self = shift;
	if ( scalar @_ ) {
		for ( $self->{flags} ) {
			$_ = 0x0100 | ( $_ || 0 );
			$_ ^= 0x0100 unless shift;
		}
	}
	return 0x0100 & ( $self->{flags} || 0 );
}


sub revoke {
	my $self = shift;
	if ( scalar @_ ) {
		for ( $self->{flags} ) {
			$_ = 0x0080 | ( $_ || 0 );
			$_ ^= 0x0080 unless shift;
		}
	}
	return 0x0080 & ( $self->{flags} || 0 );
}


sub sep {
	my $self = shift;
	if ( scalar @_ ) {
		for ( $self->{flags} ) {
			$_ = 0x0001 | ( $_ || 0 );
			$_ ^= 0x0001 unless shift;
		}
	}
	return 0x0001 & ( $self->{flags} || 0 );
}


sub protocol {
	my $self = shift;

	$self->{protocol} = 0 + shift if scalar @_;
	return $self->{protocol} || 0;
}


sub algorithm {
	my ( $self, $arg ) = @_;

	unless ( ref($self) ) {		## class method or simple function
		my $argn = pop;
		return $argn =~ /\D/ ? _algbyname($argn) : _algbyval($argn);
	}

	return $self->{algorithm} unless defined $arg;
	return _algbyval( $self->{algorithm} ) if uc($arg) eq 'MNEMONIC';
	return $self->{algorithm} = _algbyname($arg) || die _algbyname('')    # disallow algorithm(0)
}


sub key {
	my $self = shift;
	return MIME::Base64::encode( $self->keybin(), "" ) unless scalar @_;
	return $self->keybin( MIME::Base64::decode( join "", @_ ) );
}


sub keybin {
	my $self = shift;

	$self->{keybin} = shift if scalar @_;
	return $self->{keybin} || "";
}


sub publickey { return shift->key(@_); }


sub privatekeyname {
	my $self = shift;
	my $name = $self->signame;
	return sprintf 'K%s+%03d+%05d.private', $name, $self->algorithm, $self->keytag;
}


sub signame {
	my $self = shift;
	return lc $self->{owner}->fqdn;
}


sub keylength {
	my $self = shift;

	my $keybin = $self->keybin || return;

	local $_ = _algbyval( $self->{algorithm} );

	if (/^RSA/) {

		# Modulus length, see RFC 3110
		if ( my $exp_length = unpack 'C', $keybin ) {

			return ( length($keybin) - $exp_length - 1 ) << 3;

		} else {
			$exp_length = unpack 'x n', $keybin;
			return ( length($keybin) - $exp_length - 3 ) << 3;
		}

	} elsif (/^DSA/) {

		# Modulus length, see RFC 2536
		my $T = unpack 'C', $keybin;
		return ( $T << 6 ) + 512;
	}

	return length($keybin) << 2;	## ECDSA / EdDSA
}


sub keytag {
	my $self = shift;

	my $keybin = $self->keybin || return 0;

	# RFC4034 Appendix B.1: most significant 16 bits of least significant 24 bits
	return unpack 'n', substr $keybin, -3 if $self->{algorithm} == 1;

	# RFC4034 Appendix B
	my $od = length($keybin) & 1;
	my $rd = pack "n C2 a* x$od", @{$self}{qw(flags protocol algorithm)}, $keybin;
	my $ac = 0;
	$ac += $_ for unpack 'n*', $rd;
	$ac += ( $ac >> 16 );
	return $ac & 0xFFFF;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name DNSKEY flags protocol algorithm publickey');

=head1 DESCRIPTION

Class for DNSSEC Key (DNSKEY) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 flags

    $flags = $rr->flags;
    $rr->flags( $flags );

Unsigned 16-bit number representing Boolean flags.

=over 4

=item zone

 $rr->zone(1);

 if ( $rr->zone ) {
	...
 }

Boolean ZONE flag.

=back

=over 4

=item revoke

 $rr->revoke(1);

 if ( $rr->revoke ) {
	...
 }

Boolean REVOKE flag.

=back

=over 4

=item sep

 $rr->sep(1);

 if ( $rr->sep ) {
	...
 }

Boolean Secure Entry Point (SEP) flag.

=back

=head2 protocol

    $protocol = $rr->protocol;
    $rr->protocol( $protocol );

The 8-bit protocol number.  This field MUST have value 3.

=head2 algorithm

    $algorithm = $rr->algorithm;
    $rr->algorithm( $algorithm );

The 8-bit algorithm number describes the public key algorithm.

algorithm() may also be invoked as a class method or simple function
to perform mnemonic and numeric code translation.

=head2 publickey

=head2 key

    $key = $rr->key;
    $rr->key( $key );

Base64 representation of the public key material.

=head2 keybin

    $keybin = $rr->keybin;
    $rr->keybin( $keybin );

Opaque octet string representing the public key material.

=head2 privatekeyname

    $privatekeyname = $rr->privatekeyname;

Returns the name of the privatekey as it would be generated by
the BIND dnssec-keygen program. The format of that name being:

	K<fqdn>+<algorithm>+<keyid>.private

=head2 signame

Returns the canonical signer name of the privatekey.

=head2 keylength

Returns the length (in bits) of the modulus calculated from the key text.

=head2 keytag

    print "keytag = ", $rr->keytag, "\n";

Returns the 16-bit numerical key tag of the key. (RFC2535 4.1.6)


=head1 COPYRIGHT

Copyright (c)2003-2005 RIPE NCC.  Author Olaf M. Kolkman

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

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, RFC4034, RFC3755

L<Algorithm Numbers|http://www.iana.org/assignments/dns-sec-alg-numbers>,
L<DNSKEY Flags|http://www.iana.org/assignments/dnskey-flags>

=cut
