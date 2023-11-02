package Net::DNS::RR::DNSKEY;

use strict;
use warnings;
our $VERSION = (qw$Id: DNSKEY.pm 1910 2023-03-30 19:16:30Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::DNSKEY - DNS DNSKEY resource record

=cut

use integer;

use Carp;

use constant BASE64 => defined eval { require MIME::Base64 };


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $rdata = substr $$data, $offset, $self->{rdlength};
	@{$self}{qw(flags protocol algorithm keybin)} = unpack 'n C2 a*', $rdata;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return pack 'n C2 a*', @{$self}{qw(flags protocol algorithm keybin)};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @rdata = @{$self}{qw(flags protocol algorithm)};
	if ( my $keybin = $self->keybin ) {
		$self->_annotation( 'Key ID =', $self->keytag );
		return $self->SUPER::_format_rdata() unless BASE64;
		push @rdata, split /\s+/, MIME::Base64::encode($keybin);
	} else {
		push @rdata, '""';
	}
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	$self->flags( shift @argument );
	$self->protocol( shift @argument );
	my $algorithm = shift @argument;
	$self->key(@argument) if $algorithm;
	$self->algorithm($algorithm);
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->flags(256);
	$self->protocol(3);
	$self->algorithm(1);
	$self->keybin('');
	return;
}


sub flags {
	my ( $self, @value ) = @_;
	for (@value) { $self->{flags} = 0 + $_ }
	return $self->{flags} || 0;
}


sub zone {
	my ( $self, @value ) = @_;
	for ( $self->{flags} |= 0 ) {
		if ( scalar @value ) {
			$_ |= 0x0100;
			$_ ^= 0x0100 unless shift @value;
		}
	}
	return $self->{flags} & 0x0100;
}


sub revoke {
	my ( $self, @value ) = @_;
	for ( $self->{flags} |= 0 ) {
		if ( scalar @value ) {
			$_ |= 0x0080;
			$_ ^= 0x0080 unless shift @value;
		}
	}
	return $self->{flags} & 0x0080;
}


sub sep {
	my ( $self, @value ) = @_;
	for ( $self->{flags} |= 0 ) {
		if ( scalar @value ) {
			$_ |= 0x0001;
			$_ ^= 0x0001 unless shift @value;
		}
	}
	return $self->{flags} & 0x0001;
}


sub protocol {
	my ( $self, @value ) = @_;
	for (@value) { $self->{protocol} = 0 + $_ }
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
	my ( $self, @value ) = @_;
	return MIME::Base64::encode( $self->keybin(), "" ) unless scalar @value;
	return $self->keybin( MIME::Base64::decode( join "", @value ) );
}


sub keybin {
	my ( $self, @value ) = @_;
	for (@value) { $self->{keybin} = $_ }
	return $self->{keybin} || "";
}


sub publickey {
	my ( $self, @value ) = @_;
	return $self->key(@value);
}


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

	my $keybin = $self->{keybin} || return;

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
L<RFC4034|https://tools.ietf.org/html/rfc4034>

L<DNSKEY Flags|http://www.iana.org/assignments/dnskey-flags>

L<Algorithm Numbers|http://www.iana.org/assignments/dns-sec-alg-numbers>

=cut
