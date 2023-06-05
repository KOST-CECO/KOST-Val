package Net::DNS::RR::RRSIG;

use strict;
use warnings;
our $VERSION = (qw$Id: RRSIG.pm 1819 2020-10-19 08:07:24Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::RRSIG - DNS RRSIG resource record

=cut

use integer;

use Carp;
use Time::Local;

use Net::DNS::Parameters qw(:type);

use constant DEBUG => 0;

use constant UTIL => defined eval { require Scalar::Util; };

eval { require MIME::Base64 };

# IMPORTANT: Downstream distros MUST NOT create dependencies on Net::DNS::SEC	(strong crypto prohibited in many territories)
use constant USESEC => defined $INC{'Net/DNS/SEC.pm'};		# Discover how we got here, without exposing any crypto
								# Discourage static code analysers and casual greppers
use constant DNSSEC => USESEC && defined eval join '', qw(r e q u i r e), ' Net::DNS', qw(:: SEC :: Private);	## no critic

my @index;
if (DNSSEC) {
	my $key = Net::DNS::RR->new( type => 'DNSKEY', key => 'AwEAAQ==' );
	foreach my $class ( map {"Net::DNS::SEC::$_"} qw(RSA DSA ECCGOST ECDSA EdDSA) ) {
		my @algorithms = eval join '', qw(r e q u i r e), " $class; $class->_index";	## no critic
		@algorithms = grep { eval { $key->algorithm($_); $class->verify( '', $key, '' ); 1 } } ( 1 .. 16 )
				unless scalar(@algorithms);	# Grotesquely inefficient; but need to support pre-1.14 API
		push @index, map { ( $_ => $class ) } @algorithms;
	}
}

my %DNSSEC_verify = @index;
my %DNSSEC_siggen = @index;

my @deprecated = ( 1, 3, 6, 12 );				# RFC8624
delete @DNSSEC_siggen{@deprecated};

my @field = qw(typecovered algorithm labels orgttl sigexpiration siginception keytag);


sub _decode_rdata {			## decode rdata from wire-format octet string
	my $self = shift;
	my ( $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	@{$self}{@field} = unpack "\@$offset n C2 N3 n", $$data;
	( $self->{signame}, $offset ) = Net::DNS::DomainName->decode( $data, $offset + 18 );
	$self->{sigbin} = substr $$data, $offset, $limit - $offset;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my $signame = $self->{signame};
	return pack 'n C2 N3 n a* a*', @{$self}{@field}, $signame->canonical, $self->sigbin;
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my $signame = $self->{signame};
	my @sig64   = split /\s+/, MIME::Base64::encode( $self->sigbin );
	my @rdata   = ( map( { $self->$_ } @field ), $signame->string, @sig64 );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my $self = shift;

	foreach ( @field, qw(signame) ) { $self->$_(shift) }
	$self->signature(@_);
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->sigval(30);
	return;
}


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


sub typecovered {
	my $self = shift;
	$self->{typecovered} = typebyname(shift) if scalar @_;
	my $typecode = $self->{typecovered};
	return defined $typecode ? typebyval($typecode) : undef;
}


sub algorithm {
	my ( $self, $arg ) = @_;

	unless ( ref($self) ) {		## class method or simple function
		my $argn = pop;
		return $argn =~ /[^0-9]/ ? _algbyname($argn) : _algbyval($argn);
	}

	return $self->{algorithm} unless defined $arg;
	return _algbyval( $self->{algorithm} ) if $arg =~ /MNEMONIC/i;
	return $self->{algorithm} = _algbyname($arg);
}


sub labels {
	my $self = shift;

	$self->{labels} = 0 + shift if scalar @_;
	return $self->{labels} || 0;
}


sub orgttl {
	my $self = shift;

	$self->{orgttl} = 0 + shift if scalar @_;
	return $self->{orgttl} || 0;
}


sub sigexpiration {
	my $self = shift;
	$self->{sigexpiration} = _string2time(shift) if scalar @_;
	my $time = $self->{sigexpiration};
	return unless defined wantarray && defined $time;
	return UTIL ? Scalar::Util::dualvar( $time, _time2string($time) ) : _time2string($time);
}

sub siginception {
	my $self = shift;
	$self->{siginception} = _string2time(shift) if scalar @_;
	my $time = $self->{siginception};
	return unless defined wantarray && defined $time;
	return UTIL ? Scalar::Util::dualvar( $time, _time2string($time) ) : _time2string($time);
}

sub sigex { return &sigexpiration; }	## historical

sub sigin { return &siginception; }	## historical

sub sigval {
	my $self = shift;
	no integer;
	return ( $self->{sigval} ) = map { int( 86400 * $_ ) } @_;
}


sub keytag {
	my $self = shift;

	$self->{keytag} = 0 + shift if scalar @_;
	return $self->{keytag} || 0;
}


sub signame {
	my $self = shift;

	$self->{signame} = Net::DNS::DomainName->new(shift) if scalar @_;
	return $self->{signame} ? $self->{signame}->name : undef;
}


sub sig {
	my $self = shift;
	return MIME::Base64::encode( $self->sigbin(), "" ) unless scalar @_;
	return $self->sigbin( MIME::Base64::decode( join "", @_ ) );
}


sub sigbin {
	my $self = shift;

	$self->{sigbin} = shift if scalar @_;
	return $self->{sigbin} || "";
}


sub signature { return &sig; }


sub create {
	unless (DNSSEC) {
		croak qq[No "use Net::DNS::SEC" declaration in application code];
	} else {
		my ( $class, $rrsetref, $priv_key, %etc ) = @_;

		$rrsetref = [$rrsetref] unless ref($rrsetref) eq 'ARRAY';
		my $RR = $rrsetref->[0];
		croak '$rrsetref is not reference to RR array' unless ref($RR) =~ /^Net::DNS::RR/;

		# All the TTLs need to be the same in the data RRset.
		my $ttl = $RR->ttl;
		croak 'RRs in RRset do not have same TTL' if grep { $_->ttl != $ttl } @$rrsetref;

		my $private = ref($priv_key) ? $priv_key : Net::DNS::SEC::Private->new($priv_key);
		croak 'unable to parse private key' unless ref($private) eq 'Net::DNS::SEC::Private';

		my @label = grep { $_ ne chr(42) } $RR->{owner}->_wire;	   # count labels

		my $self = Net::DNS::RR->new(
			name	     => $RR->name,
			type	     => 'RRSIG',
			class	     => 'IN',
			ttl	     => $ttl,
			typecovered  => $RR->type,
			labels	     => scalar @label,
			orgttl	     => $ttl,
			siginception => time(),
			algorithm    => $private->algorithm,
			keytag	     => $private->keytag,
			signame	     => $private->signame,
			);

		while ( my ( $attribute, $value ) = each %etc ) {
			$self->$attribute($value);
		}

		$self->{sigexpiration} = $self->{siginception} + $self->{sigval}
				unless $self->{sigexpiration};

		my $sigdata = $self->_CreateSigData($rrsetref);
		$self->_CreateSig( $sigdata, $private );
		return $self;
	}
}


sub verify {

	# Reminder...

	# $rrsetref must be a reference to an array of RR objects.

	# $keyref is either a key object or a reference to an array of key objects.

	unless (DNSSEC) {
		croak qq[No "use Net::DNS::SEC" declaration in application code];
	} else {
		my ( $self, $rrsetref, $keyref ) = @_;

		croak '$keyref argument is scalar or undefined' unless ref($keyref);

		print '$keyref argument is ', ref($keyref), "\n" if DEBUG;
		if ( ref($keyref) eq "ARRAY" ) {

			#  We will iterate over the supplied key list and
			#  return when there is a successful verification.
			#  If not, continue so that we survive key-id collision.

			print "Iterating over ", scalar(@$keyref), " keys\n" if DEBUG;
			my @error;
			foreach my $keyrr (@$keyref) {
				my $result = $self->verify( $rrsetref, $keyrr );
				return $result if $result;
				my $error = $self->{vrfyerrstr};
				my $keyid = $keyrr->keytag;
				push @error, "key $keyid: $error";
				print "key $keyid: $error\n" if DEBUG;
				next;
			}

			$self->{vrfyerrstr} = join "\n", @error;
			return 0;

		} elsif ( $keyref->isa('Net::DNS::RR::DNSKEY') ) {

			print "Validating using key with keytag: ", $keyref->keytag, "\n" if DEBUG;

		} else {
			croak join ' ', ref($keyref), 'can not be used as DNSSEC key';
		}


		$rrsetref = [$rrsetref] unless ref($rrsetref) eq 'ARRAY';
		my $RR = $rrsetref->[0];
		croak '$rrsetref not a reference to array of RRs' unless ref($RR) =~ /^Net::DNS::RR/;

		if (DEBUG) {
			print "\n ---------------------- RRSIG DEBUG --------------------";
			print "\n  SIG:\t", $self->string;
			print "\n  KEY:\t", $keyref->string;
			print "\n -------------------------------------------------------\n";
		}

		$self->{vrfyerrstr} = '';
		unless ( $self->algorithm == $keyref->algorithm ) {
			$self->{vrfyerrstr} = 'algorithm does not match';
			return 0;
		}

		unless ( $self->keytag == $keyref->keytag ) {
			$self->{vrfyerrstr} = 'keytag does not match';
			return 0;
		}

		my $sigdata = $self->_CreateSigData($rrsetref);
		$self->_VerifySig( $sigdata, $keyref ) || return 0;

		# time to do some time checking.
		my $t = time;

		if ( _ordered( $self->{sigexpiration}, $t ) ) {
			$self->{vrfyerrstr} = join ' ', 'Signature expired at', $self->sigexpiration;
			return 0;
		} elsif ( _ordered( $t, $self->{siginception} ) ) {
			$self->{vrfyerrstr} = join ' ', 'Signature valid from', $self->siginception;
			return 0;
		}

		return 1;
	}
}								#END verify


sub vrfyerrstr {
	my $self = shift;
	return $self->{vrfyerrstr};
}


########################################

sub _CreateSigData {

	# This method creates the data string that will be signed.
	# See RFC4034(6) and RFC6840(5.1) on how this string is constructed

	# This method is called by the method that creates a signature
	# and by the method that verifies the signature. It is assumed
	# that the creation method has checked that all the TTLs are
	# the same for the rrsetref and that sig->orgttl has been set
	# to the TTL of the data. This method will set the datarr->ttl
	# to the sig->orgttl for all the RR in the rrsetref.

	if (DNSSEC) {
		my ( $self, $rrsetref ) = @_;

		print "_CreateSigData\n" if DEBUG;

		my $sigdata = pack 'n C2 N3 n a*', @{$self}{@field}, $self->{signame}->canonical;
		print "\npreamble\t", unpack( 'H*', $sigdata ), "\n" if DEBUG;

		my $owner = $self->{owner};			# create wildcard domain name
		my $limit = $self->{labels};
		my @label = $owner->_wire;
		shift @label while scalar @label > $limit;
		my $wild   = bless {label => \@label}, ref($owner);    # DIY to avoid wrecking name cache
		my $suffix = $wild->canonical;
		unshift @label, chr(42);			# asterisk

		my @RR	  = map { bless( {%$_}, ref($_) ) } @$rrsetref;	   # shallow RR clone
		my $rr	  = $RR[0];
		my $class = $rr->class;
		my $type  = $rr->type;
		my $ttl	  = $self->orgttl;

		my %table;
		foreach my $RR (@RR) {
			my $ident = $RR->{owner}->canonical;
			my $match = substr $ident, -length($suffix);
			croak 'RRs in RRset have different NAMEs' if $match ne $suffix;
			croak 'RRs in RRset have different TYPEs' if $type ne $RR->type;
			croak 'RRs in RRset have different CLASS' if $class ne $RR->class;
			$RR->ttl($ttl);				# reset TTL

			my $offset = 10 + length($suffix);	# RDATA offset
			if ( $ident ne $match ) {
				$RR->{owner} = $wild;
				$offset += 2;
				print "\nsubstituting wildcard name: ", $RR->name if DEBUG;
			}

			# For sorting we create a hash table of canonical data keyed on RDATA
			my $canonical = $RR->canonical;
			$table{substr $canonical, $offset} = $canonical;
		}

		$sigdata = join '', $sigdata, map { $table{$_} } sort keys %table;

		if (DEBUG) {
			my $i = 0;
			foreach my $rdata ( sort keys %table ) {
				print "\n>>> ", $i++, "\tRDATA:\t", unpack 'H*', $rdata;
				print "\nRR: ", unpack( 'H*', $table{$rdata} ), "\n";
			}
			print "\n sigdata:\t", unpack( 'H*', $sigdata ), "\n";
		}

		return $sigdata;
	}
}


########################################

sub _CreateSig {
	if (DNSSEC) {
		my $self = shift;

		my $algorithm = $self->algorithm;
		my $class     = $DNSSEC_siggen{$algorithm};

		return eval {
			die "algorithm $algorithm not supported\n" unless $class;
			$self->sigbin( $class->sign(@_) );
		} || return croak "${@}signature generation failed";
	}
}


sub _VerifySig {
	if (DNSSEC) {
		my $self = shift;

		my $algorithm = $self->algorithm;
		my $class     = $DNSSEC_verify{$algorithm};

		my $retval = eval {
			die "algorithm $algorithm not supported\n" unless $class;
			$class->verify( @_, $self->sigbin );
		};

		unless ($retval) {
			$self->{vrfyerrstr} = "${@}signature verification failed";
			print "\n", $self->{vrfyerrstr}, "\n" if DEBUG;
			return 0;
		}

		# uncoverable branch true	# bug in Net::DNS::SEC or dependencies
		croak "unknown error in $class->verify" unless $retval == 1;
		print "\nalgorithm $algorithm verification successful\n" if DEBUG;
		return 1;
	}
}


sub _ordered() {			## irreflexive 32-bit partial ordering
	use integer;
	my ( $n1, $n2 ) = @_;

	return 0 unless defined $n2;				# ( any, undef )
	return 1 unless defined $n1;				# ( undef, any )

	# unwise to assume 64-bit arithmetic, or that 32-bit integer overflow goes unpunished
	if ( $n2 < 0 ) {					# fold, leaving $n2 non-negative
		$n1 = ( $n1 & 0xFFFFFFFF ) ^ 0x80000000;	# -2**31 <= $n1 < 2**32
		$n2 = ( $n2 & 0x7FFFFFFF );			#  0	 <= $n2 < 2**31
	}

	return $n1 < $n2 ? ( $n1 > ( $n2 - 0x80000000 ) ) : ( $n2 < ( $n1 - 0x80000000 ) );
}


my $y1998 = timegm( 0, 0, 0, 1, 0, 1998 );
my $y2026 = timegm( 0, 0, 0, 1, 0, 2026 );
my $y2082 = $y2026 << 1;
my $y2054 = $y2082 - $y1998;
my $m2026 = int( 0x80000000 - $y2026 );
my $m2054 = int( 0x80000000 - $y2054 );
my $t2082 = int( $y2082 & 0x7FFFFFFF );
my $t2100 = 1960058752;

sub _string2time {			## parse time specification string
	my $arg = shift;
	return int($arg) if length($arg) < 12;
	my ( $y, $m, @dhms ) = unpack 'a4 a2 a2 a2 a2 a2', $arg . '00';
	if ( $arg lt '20380119031408' ) {			# calendar folding
		return timegm( reverse(@dhms), $m - 1, $y ) if $y < 2026;
		return timegm( reverse(@dhms), $m - 1, $y - 56 ) + $y2026;
	} elsif ( $y > 2082 ) {
		my $z = timegm( reverse(@dhms), $m - 1, $y - 84 );    # expunge 29 Feb 2100
		return $z < 1456790400 ? $z + $y2054 : $z + $y2054 - 86400;
	}
	return ( timegm( reverse(@dhms), $m - 1, $y - 56 ) + $y2054 ) - $y1998;
}


sub _time2string {			## format time specification string
	my $arg	 = shift;
	my $ls31 = int( $arg & 0x7FFFFFFF );
	if ( $arg & 0x80000000 ) {

		if ( $ls31 > $t2082 ) {
			$ls31 += 86400 unless $ls31 < $t2100;	# expunge 29 Feb 2100
			my ( $yy, $mm, @dhms ) = reverse( ( gmtime( $ls31 + $m2054 ) )[0 .. 5] );
			return sprintf '%d%02d%02d%02d%02d%02d', $yy + 1984, $mm + 1, @dhms;
		}

		my ( $yy, $mm, @dhms ) = reverse( ( gmtime( $ls31 + $m2026 ) )[0 .. 5] );
		return sprintf '%d%02d%02d%02d%02d%02d', $yy + 1956, $mm + 1, @dhms;


	} elsif ( $ls31 > $y2026 ) {
		my ( $yy, $mm, @dhms ) = reverse( ( gmtime( $ls31 - $y2026 ) )[0 .. 5] );
		return sprintf '%d%02d%02d%02d%02d%02d', $yy + 1956, $mm + 1, @dhms;
	}

	my ( $yy, $mm, @dhms ) = reverse( ( gmtime $ls31 )[0 .. 5] );
	return sprintf '%d%02d%02d%02d%02d%02d', $yy + 1900, $mm + 1, @dhms;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name RRSIG typecovered algorithm labels
				orgttl sigexpiration siginception
				keytag signame signature');

    use Net::DNS::SEC;
    $sigrr = Net::DNS::RR::RRSIG->create( \@rrset, $keypath,
					sigex => 20191231010101
					sigin => 20191201010101
					);

    $sigrr->verify( \@rrset, $keyrr ) || die $sigrr->vrfyerrstr;

=head1 DESCRIPTION

Class for DNS digital signature (RRSIG) resource records.

In addition to the regular methods inherited from Net::DNS::RR the
class contains a method to sign RRsets using private keys (create)
and a method for verifying signatures over RRsets (verify).

The RRSIG RR is an implementation of RFC4034. 
See L<Net::DNS::RR::SIG> for an implementation of SIG0 (RFC2931).

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 typecovered

    $typecovered = $rr->typecovered;

The typecovered field identifies the type of the RRset that is
covered by this RRSIG record.

=head2 algorithm

    $algorithm = $rr->algorithm;

The algorithm number field identifies the cryptographic algorithm
used to create the signature.

algorithm() may also be invoked as a class method or simple function
to perform mnemonic and numeric code translation.

=head2 labels

    $labels = $rr->labels;
    $rr->labels( $labels );

The labels field specifies the number of labels in the original RRSIG
RR owner name.

=head2 orgttl

    $orgttl = $rr->orgttl;
    $rr->orgttl( $orgttl );

The original TTL field specifies the TTL of the covered RRset as it
appears in the authoritative zone.

=head2 sigexpiration and siginception times

=head2 sigex sigin sigval

    $expiration = $rr->sigexpiration;
    $expiration = $rr->sigexpiration( $value );

    $inception = $rr->siginception;
    $inception = $rr->siginception( $value );

The signature expiration and inception fields specify a validity
time interval for the signature.

The value may be specified by a string with format 'yyyymmddhhmmss'
or a Perl time() value.

Return values are dual-valued, providing either a string value or 
numerical Perl time() value.

=head2 keytag

    $keytag = $rr->keytag;
    $rr->keytag( $keytag );

The keytag field contains the key tag value of the DNSKEY RR that
validates this signature.

=head2 signame

    $signame = $rr->signame;
    $rr->signame( $signame );

The signer name field value identifies the owner name of the DNSKEY
RR that a validator is supposed to use to validate this signature.

=head2 signature

=head2 sig

    $sig = $rr->sig;
    $rr->sig( $sig );

The Signature field contains the cryptographic signature that covers
the RRSIG RDATA (excluding the Signature field) and the RRset
specified by the RRSIG owner name, RRSIG class, and RRSIG type
covered fields.

=head2 sigbin

    $sigbin = $rr->sigbin;
    $rr->sigbin( $sigbin );

Binary representation of the cryptographic signature.

=head2 create

Create a signature over a RR set.

    use Net::DNS::SEC;

    $keypath = '/home/olaf/keys/Kbla.foo.+001+60114.private';

    $sigrr = Net::DNS::RR::RRSIG->create( \@rrsetref, $keypath );

    $sigrr = Net::DNS::RR::RRSIG->create( \@rrsetref, $keypath,
					sigex => 20191231010101
					sigin => 20191201010101
					);
    $sigrr->print;


    # Alternatively use Net::DNS::SEC::Private 

    $private = Net::DNS::SEC::Private->new($keypath);

    $sigrr= Net::DNS::RR::RRSIG->create( \@rrsetref, $private );


create() is an alternative constructor for a RRSIG RR object.  

This method returns an RRSIG with the signature over the subject rrset
(an array of RRs) made with the private key stored in the key file.

The first argument is a reference to an array that contains the RRset
that needs to be signed.

The second argument is a string which specifies the path to a file
containing the private key as generated by dnssec-keygen.

The optional remaining arguments consist of ( name => value ) pairs
as follows:

	sigex  => 20191231010101,	# signature expiration
	sigin  => 20191201010101,	# signature inception
	sigval => 30,			# validity window (days)
	ttl    => 3600			# TTL

The sigin and sigex values may be specified as Perl time values or as
a string with the format 'yyyymmddhhmmss'. The default for sigin is
the time of signing. 

The sigval argument specifies the signature validity window in days
( sigex = sigin + sigval ).

By default the signature is valid for 30 days.

By default the TTL matches the RRset that is presented for signing.

=head2 verify

    $verify = $sigrr->verify( $rrsetref, $keyrr );
    $verify = $sigrr->verify( $rrsetref, [$keyrr, $keyrr2, $keyrr3] );

$rrsetref contains a reference to an array of RR objects and the
method verifies the RRset against the signature contained in the
$sigrr object itself using the public key in $keyrr.

The second argument can either be a Net::DNS::RR::KEYRR object or a
reference to an array of such objects. Verification will return
successful as soon as one of the keys in the array leads to positive
validation.

Returns 0 on error and sets $sig->vrfyerrstr

=head2 vrfyerrstr

    $verify = $sigrr->verify( $rrsetref, $keyrr );
    print $sigrr->vrfyerrstr unless $verify;

    $sigrr->verify( $rrsetref, $keyrr ) || die $sigrr->vrfyerrstr;

=head1 KEY GENERATION

Private key files and corresponding public DNSKEY records
are most conveniently generated using dnssec-keygen,
a program that comes with the ISC BIND distribution.

    dnssec-keygen -a 10 -b 2048 -f ksk	rsa.example.
    dnssec-keygen -a 10 -b 1024		rsa.example.

    dnssec-keygen -a 14	-f ksk	ecdsa.example.
    dnssec-keygen -a 14		ecdsa.example.

Do not change the name of the private key file.
The create method uses the filename as generated by dnssec-keygen
to determine the keyowner, algorithm, and the keyid (keytag).


=head1 REMARKS

The code is not optimised for speed.
It is probably not suitable to be used for signing large zones.

If this code is still around in 2100 (not a leap year) you will
need to check for proper handling of times after 28th February.

=head1 ACKNOWLEDGMENTS

Although their original code may have disappeared following redesign of
Net::DNS, Net::DNS::SEC and the OpenSSL API, the following individual
contributors deserve to be recognised for their significant influence
on the development of the RRSIG package.

Andy Vaskys (Network Associates Laboratories) supplied code for RSA.

T.J. Mather provided support for the DSA algorithm.

Dick Franks added support for elliptic curve and Edwards curve algorithms.

Mike McCauley created the Crypt::OpenSSL::ECDSA perl extension module
specifically for this development.


=head1 COPYRIGHT

Copyright (c)2001-2005 RIPE NCC,   Olaf M. Kolkman

Copyright (c)2007-2008 NLnet Labs, Olaf M. Kolkman

Portions Copyright (c)2014 Dick Franks 

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

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, L<Net::DNS::SEC>,
RFC4034, RFC6840, RFC3755

L<Algorithm Numbers|http://www.iana.org/assignments/dns-sec-alg-numbers>

L<BIND 9 Administrator Reference Manual|http://www.bind9.net/manuals>

=cut
