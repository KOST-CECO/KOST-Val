package Net::DNS::Parameters;

################################################
##
##	Domain Name System (DNS) Parameters
##	(last updated 2023-04-28)
##
################################################

use strict;
use warnings;
our $VERSION = (qw$Id: Parameters.pm 1921 2023-05-08 18:39:59Z willem $)[2];

use integer;
use Carp;

use base qw(Exporter);

our @EXPORT_OK = qw(
		classbyname classbyval %classbyname
		typebyname typebyval %typebyname
		opcodebyname opcodebyval
		rcodebyname rcodebyval
		ednsoptionbyname ednsoptionbyval
		dsotypebyname dsotypebyval
		);

our %EXPORT_TAGS = (
	class	   => [qw(classbyname classbyval)],
	type	   => [qw(typebyname typebyval)],
	opcode	   => [qw(opcodebyname opcodebyval)],
	rcode	   => [qw(rcodebyname rcodebyval)],
	ednsoption => [qw(ednsoptionbyname ednsoptionbyval)],
	dsotype	   => [qw(dsotypebyname dsotypebyval)],
	);


# Registry: DNS CLASSes
my @classbyname = (
	IN   => 1,						# RFC1035
	CH   => 3,						# Chaosnet
	HS   => 4,						# Hesiod
	NONE => 254,						# RFC2136
	ANY  => 255,						# RFC1035
	);
our %classbyval = reverse( CLASS0 => 0, @classbyname );
push @classbyname, map { /^\d/ ? $_ : lc($_) } @classbyname;
our %classbyname = ( '*' => 255, @classbyname );


# Registry: Resource Record (RR) TYPEs
my @typebyname = (
	A	   => 1,					# RFC1035
	NS	   => 2,					# RFC1035
	MD	   => 3,					# RFC1035
	MF	   => 4,					# RFC1035
	CNAME	   => 5,					# RFC1035
	SOA	   => 6,					# RFC1035
	MB	   => 7,					# RFC1035
	MG	   => 8,					# RFC1035
	MR	   => 9,					# RFC1035
	NULL	   => 10,					# RFC1035
	WKS	   => 11,					# RFC1035
	PTR	   => 12,					# RFC1035
	HINFO	   => 13,					# RFC1035
	MINFO	   => 14,					# RFC1035
	MX	   => 15,					# RFC1035
	TXT	   => 16,					# RFC1035
	RP	   => 17,					# RFC1183
	AFSDB	   => 18,					# RFC1183 RFC5864
	X25	   => 19,					# RFC1183
	ISDN	   => 20,					# RFC1183
	RT	   => 21,					# RFC1183
	NSAP	   => 22,					# RFC1706 https://datatracker.ietf.org/doc/status-change-int-tlds-to-historic
	'NSAP-PTR' => 23,					# RFC1706 https://datatracker.ietf.org/doc/status-change-int-tlds-to-historic
	SIG	   => 24,					# RFC2536 RFC2931 RFC3110 RFC4034
	KEY	   => 25,					# RFC2536 RFC2539 RFC3110 RFC4034
	PX	   => 26,					# RFC2163
	GPOS	   => 27,					# RFC1712
	AAAA	   => 28,					# RFC3596
	LOC	   => 29,					# RFC1876
	NXT	   => 30,					# RFC2535 RFC3755
	EID	   => 31,					# http://ana-3.lcs.mit.edu/~jnc/nimrod/dns.txt
	NIMLOC	   => 32,					# http://ana-3.lcs.mit.edu/~jnc/nimrod/dns.txt
	SRV	   => 33,					# RFC2782
	ATMA	   => 34,					# http://www.broadband-forum.org/ftp/pub/approved-specs/af-dans-0152.000.pdf
	NAPTR	   => 35,					# RFC3403
	KX	   => 36,					# RFC2230
	CERT	   => 37,					# RFC4398
	A6	   => 38,					# RFC2874 RFC3226 RFC6563
	DNAME	   => 39,					# RFC6672
	SINK	   => 40,					# draft-eastlake-kitchen-sink
	OPT	   => 41,					# RFC3225 RFC6891
	APL	   => 42,					# RFC3123
	DS	   => 43,					# RFC4034
	SSHFP	   => 44,					# RFC4255
	IPSECKEY   => 45,					# RFC4025
	RRSIG	   => 46,					# RFC4034
	NSEC	   => 47,					# RFC4034 RFC9077
	DNSKEY	   => 48,					# RFC4034
	DHCID	   => 49,					# RFC4701
	NSEC3	   => 50,					# RFC5155 RFC9077
	NSEC3PARAM => 51,					# RFC5155
	TLSA	   => 52,					# RFC6698
	SMIMEA	   => 53,					# RFC8162
	HIP	   => 55,					# RFC8005
	NINFO	   => 56,					#
	RKEY	   => 57,					#
	TALINK	   => 58,					#
	CDS	   => 59,					# RFC7344
	CDNSKEY	   => 60,					# RFC7344
	OPENPGPKEY => 61,					# RFC7929
	CSYNC	   => 62,					# RFC7477
	ZONEMD	   => 63,					# RFC8976
	SVCB	   => 64,					# RFC-ietf-dnsop-svcb-https-12
	HTTPS	   => 65,					# RFC-ietf-dnsop-svcb-https-12
	SPF	   => 99,					# RFC7208
	UINFO	   => 100,					# IANA-Reserved
	UID	   => 101,					# IANA-Reserved
	GID	   => 102,					# IANA-Reserved
	UNSPEC	   => 103,					# IANA-Reserved
	NID	   => 104,					# RFC6742
	L32	   => 105,					# RFC6742
	L64	   => 106,					# RFC6742
	LP	   => 107,					# RFC6742
	EUI48	   => 108,					# RFC7043
	EUI64	   => 109,					# RFC7043
	TKEY	   => 249,					# RFC2930
	TSIG	   => 250,					# RFC8945
	IXFR	   => 251,					# RFC1995
	AXFR	   => 252,					# RFC1035 RFC5936
	MAILB	   => 253,					# RFC1035
	MAILA	   => 254,					# RFC1035
	ANY	   => 255,					# RFC1035 RFC6895 RFC8482
	URI	   => 256,					# RFC7553
	CAA	   => 257,					# RFC8659
	AVC	   => 258,					#
	DOA	   => 259,					# draft-durand-doa-over-dns
	AMTRELAY   => 260,					# RFC8777
	TA	   => 32768,					# http://cameo.library.cmu.edu/ http://www.watson.org/~weiler/INI1999-19.pdf
	DLV	   => 32769,					# RFC8749 RFC4431
	);
our %typebyval = reverse( TYPE0 => 0, @typebyname );
push @typebyname, map { /^\d/ ? $_ : lc($_) } @typebyname;
our %typebyname = ( '*' => 255, @typebyname );


# Registry: DNS OpCodes
my @opcodebyname = (
	QUERY  => 0,						# RFC1035
	IQUERY => 1,						# RFC3425
	STATUS => 2,						# RFC1035
	NOTIFY => 4,						# RFC1996
	UPDATE => 5,						# RFC2136
	DSO    => 6,						# RFC8490
	);
our %opcodebyval = reverse @opcodebyname;
push @opcodebyname, map { /^\d/ ? $_ : lc($_) } @opcodebyname;
our %opcodebyname = ( NS_NOTIFY_OP => 4, @opcodebyname );


# Registry: DNS RCODEs
my @rcodebyname = (
	NOERROR	  => 0,						# RFC1035
	FORMERR	  => 1,						# RFC1035
	SERVFAIL  => 2,						# RFC1035
	NXDOMAIN  => 3,						# RFC1035
	NOTIMP	  => 4,						# RFC1035
	REFUSED	  => 5,						# RFC1035
	YXDOMAIN  => 6,						# RFC2136 RFC6672
	YXRRSET	  => 7,						# RFC2136
	NXRRSET	  => 8,						# RFC2136
	NOTAUTH	  => 9,						# RFC2136
	NOTAUTH	  => 9,						# RFC8945
	NOTZONE	  => 10,					# RFC2136
	DSOTYPENI => 11,					# RFC8490
	BADVERS	  => 16,					# RFC6891
	BADSIG	  => 16,					# RFC8945
	BADKEY	  => 17,					# RFC8945
	BADTIME	  => 18,					# RFC8945
	BADMODE	  => 19,					# RFC2930
	BADNAME	  => 20,					# RFC2930
	BADALG	  => 21,					# RFC2930
	BADTRUNC  => 22,					# RFC8945
	BADCOOKIE => 23,					# RFC7873
	);
our %rcodebyval = reverse( BADSIG => 16, @rcodebyname );
push @rcodebyname, map { /^\d/ ? $_ : lc($_) } @rcodebyname;
our %rcodebyname = @rcodebyname;


# Registry: DNS EDNS0 Option Codes (OPT)
my @ednsoptionbyname = (
	LLQ		 => 1,					# RFC8764
	UL		 => 2,					# http://files.dns-sd.org/draft-sekar-dns-ul.txt
	NSID		 => 3,					# RFC5001
	DAU		 => 5,					# RFC6975
	DHU		 => 6,					# RFC6975
	N3U		 => 7,					# RFC6975
	'CLIENT-SUBNET'	 => 8,					# RFC7871
	EXPIRE		 => 9,					# RFC7314
	COOKIE		 => 10,					# RFC7873
	'TCP-KEEPALIVE'	 => 11,					# RFC7828
	PADDING		 => 12,					# RFC7830
	CHAIN		 => 13,					# RFC7901
	'KEY-TAG'	 => 14,					# RFC8145
	'EXTENDED-ERROR' => 15,					# RFC8914
	'CLIENT-TAG'	 => 16,					# draft-bellis-dnsop-edns-tags
	'SERVER-TAG'	 => 17,					# draft-bellis-dnsop-edns-tags
	'UMBRELLA-IDENT' => 20292,				# https://developer.cisco.com/docs/cloud-security/#!integrating-network-devic
	DEVICEID	 => 26946,				# https://developer.cisco.com/docs/cloud-security/#!network-devices-getting-s
	);
our %ednsoptionbyval = reverse @ednsoptionbyname;
push @ednsoptionbyname, map { /^\d/ ? $_ : lc($_) } @ednsoptionbyname;
our %ednsoptionbyname = @ednsoptionbyname;


# Registry: DNS Header Flags
my @dnsflagbyname = (
	AA => 0x0400,						# RFC1035
	TC => 0x0200,						# RFC1035
	RD => 0x0100,						# RFC1035
	RA => 0x0080,						# RFC1035
	AD => 0x0020,						# RFC4035 RFC6840
	CD => 0x0010,						# RFC4035 RFC6840
	);
push @dnsflagbyname, map { /^\d/ ? $_ : lc($_) } @dnsflagbyname;
our %dnsflagbyname = @dnsflagbyname;


# Registry: EDNS Header Flags (16 bits)
my @ednsflagbyname = (
	DO => 0x8000,						# RFC4035 RFC3225 RFC6840
	);
push @ednsflagbyname, map { /^\d/ ? $_ : lc($_) } @ednsflagbyname;
our %ednsflagbyname = @ednsflagbyname;


# Registry: DSO Type Codes
my @dsotypebyname = (
	KEEPALIVE	  => 0x0001,				# RFC8490
	RETRYDELAY	  => 0x0002,				# RFC8490
	ENCRYPTIONPADDING => 0x0003,				# RFC8490
	SUBSCRIBE	  => 0x0040,				# RFC8765
	PUSH		  => 0x0041,				# RFC8765
	UNSUBSCRIBE	  => 0x0042,				# RFC8765
	RECONFIRM	  => 0x0043,				# RFC8765
	);
our %dsotypebyval = reverse @dsotypebyname;
push @dsotypebyname, map { /^\d/ ? $_ : lc($_) } @dsotypebyname;
our %dsotypebyname = @dsotypebyname;


# Registry: Extended DNS Error Codes
my @dnserrorbyval = (
	0  => 'Other Error',					# RFC8914
	1  => 'Unsupported DNSKEY Algorithm',			# RFC8914
	2  => 'Unsupported DS Digest Type',			# RFC8914
	3  => 'Stale Answer',					# RFC8914 RFC8767
	4  => 'Forged Answer',					# RFC8914
	5  => 'DNSSEC Indeterminate',				# RFC8914
	6  => 'DNSSEC Bogus',					# RFC8914
	7  => 'Signature Expired',				# RFC8914
	8  => 'Signature Not Yet Valid',			# RFC8914
	9  => 'DNSKEY Missing',					# RFC8914
	10 => 'RRSIGs Missing',					# RFC8914
	11 => 'No Zone Key Bit Set',				# RFC8914
	12 => 'NSEC Missing',					# RFC8914
	13 => 'Cached Error',					# RFC8914
	14 => 'Not Ready',					# RFC8914
	15 => 'Blocked',					# RFC8914
	16 => 'Censored',					# RFC8914
	17 => 'Filtered',					# RFC8914
	18 => 'Prohibited',					# RFC8914
	19 => 'Stale NXDomain Answer',				# RFC8914
	20 => 'Not Authoritative',				# RFC8914
	21 => 'Not Supported',					# RFC8914
	22 => 'No Reachable Authority',				# RFC8914
	23 => 'Network Error',					# RFC8914
	24 => 'Invalid Data',					# RFC8914
	25 => 'Signature Expired before Valid',			# https://github.com/NLnetLabs/unbound/pull/604#discussion_r802678343
	26 => 'Too Early',					# RFC9250
	27 => 'Unsupported NSEC3 Iterations Value',		# RFC9276
	28 => 'Unable to conform to policy',			# draft-homburg-dnsop-codcp-00
	29 => 'Synthesized',					# https://github.com/PowerDNS/pdns/pull/12334
	);
our %dnserrorbyval = @dnserrorbyval;


########

# The following functions are wrappers around similarly named hashes.

sub classbyname {
	my $name = shift;

	return $classbyname{$name} || $classbyname{uc $name} || return do {
		croak qq[unknown class "$name"] unless $name =~ m/^(CLASS)?(\d+)/i;
		my $val = 0 + $2;
		croak qq[classbyname("$name") out of range] if $val > 0x7fff;
		return $val;
	}
}

sub classbyval {
	my $arg = shift;

	return $classbyval{$arg} || return do {
		my $val = ( $arg += 0 ) & 0x7fff;		# MSB used by mDNS
		croak qq[classbyval($arg) out of range] if $arg > 0xffff;
		return $classbyval{$arg} = $classbyval{$val} || "CLASS$val";
	}
}


sub typebyname {
	my $name = shift;

	return $typebyname{$name} || return do {
		if ( $name =~ m/^(TYPE)?(\d+)/i ) {
			my $val = 0 + $2;
			croak qq[typebyname("$name") out of range] if $val > 0xffff;
			return $val;
		}
		_typespec("$name.RRNAME") unless $typebyname{uc $name};
		return $typebyname{uc $name} || croak qq[unknown type "$name"];
	}
}

sub typebyval {
	my $val = shift;

	return $typebyval{$val} || return do {
		$val += 0;
		croak qq[typebyval($val) out of range] if $val > 0xffff;
		$typebyval{$val} = "TYPE$val";
		_typespec("$val.RRTYPE");
		return $typebyval{$val};
	}
}


sub opcodebyname {
	my $arg = shift;
	my $val = $opcodebyname{$arg};
	return $val if defined $val;
	return $arg if $arg =~ /^\d/;
	croak qq[unknown opcode "$arg"];
}

sub opcodebyval {
	my $val = shift;
	return $opcodebyval{$val} || return "$val";
}


sub rcodebyname {
	my $arg = shift;
	my $val = $rcodebyname{$arg};
	return $val if defined $val;
	return $arg if $arg =~ /^\d/;
	croak qq[unknown rcode "$arg"];
}

sub rcodebyval {
	my $val = shift;
	return $rcodebyval{$val} || return "$val";
}


sub ednsoptionbyname {
	my $arg = shift;
	my $val = $ednsoptionbyname{$arg};
	return $val if defined $val;
	return $arg if $arg =~ /^\d/;
	croak qq[unknown option "$arg"];
}

sub ednsoptionbyval {
	my $val = shift;
	return $ednsoptionbyval{$val} || return "$val";
}


sub dsotypebyname {
	my $arg = shift;
	my $val = $dsotypebyname{$arg};
	return $val if defined $val;
	return $arg if $arg =~ /^\d/;
	croak qq[unknown DSO type "$arg"];
}

sub dsotypebyval {
	my $val = shift;
	return $dsotypebyval{$val} || return "$val";
}


use constant EXTLANG => defined eval { require Net::DNS::Extlang };

sub _typespec {
	my $generate = defined wantarray;
	return EXTLANG ? eval <<'END' : '';	## no critic
	my ($node) = @_;		## draft-levine-dnsextlang
	my $instance = Net::DNS::Extlang->new();
	my $basename = $instance->domain || return '';

	require Net::DNS::Resolver;
	my $resolver = Net::DNS::Resolver->new();
	my $response = $resolver->send( "$node.$basename", 'TXT' ) || return '';

	foreach my $txt ( grep { $_->type eq 'TXT' } $response->answer ) {
		my @stanza = $txt->txtdata;
		my ( $tag, $identifier, @attribute ) = @stanza;
		next unless defined($tag) && $tag =~ /^RRTYPE=\d+$/;
		if ( $identifier =~ /^(\w+):(\d+)\W*/ ) {
			my ( $mnemonic, $rrtype ) = ( uc($1), $2 );
			croak qq["$mnemonic" is a CLASS identifier] if $classbyname{$mnemonic};
			for ( typebyval($rrtype) ) {
				next if /^$mnemonic$/i;		# duplicate registration
				croak qq["$mnemonic" conflicts with TYPE$rrtype ($_)] unless /^TYPE\d+$/;
				my $known = $typebyname{$mnemonic};
				croak qq["$mnemonic" conflicts with TYPE$known] if $known;
				$typebyval{$rrtype} = $mnemonic;
				$typebyname{$mnemonic} = $rrtype;
			}
		}
		return unless $generate;

		my $recipe = $instance->xlstorerecord( $identifier, @attribute );
		return $instance->compilerr($recipe);
	}
END
}


1;
__END__


=head1 NAME

Net::DNS::Parameters - DNS parameter assignments


=head1 SYNOPSIS

    use Net::DNS::Parameters;


=head1 DESCRIPTION

Net::DNS::Parameters is a Perl package representing the DNS parameter
allocation (key,value) tables as recorded in the definitive registry
maintained and published by IANA.


=head1 FUNCTIONS

=head2 classbyname, typebyname, opcodebyname, rcodebyname, ednsoptionbyname, dsotypebyname

Access functions which return the numerical code corresponding to
the given mnemonic.

=head2 classbyval, typebyval, opcodebyval, rcodebyval, ednsoptionbyval, dsotypebyval

Access functions which return the canonical mnemonic corresponding to
the given numerical code.


=head1 COPYRIGHT

Copyright (c)2012,2016 Dick Franks.

Portions Copyright (c)1997 Michael Fuhr.

Portions Copyright (c)2003 Olaf Kolkman.

All rights reserved.


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

L<perl>, L<Net::DNS>,
L<IANA Registry|http://www.iana.org/assignments/dns-parameters>

=cut

