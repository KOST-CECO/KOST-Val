package Net::DNS::RR::TSIG;

use strict;
use warnings;
our $VERSION = (qw$Id: TSIG.pm 1909 2023-03-23 11:36:16Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::TSIG - DNS TSIG resource record

=cut

use integer;

use Carp;

use Net::DNS::DomainName;
use Net::DNS::Parameters qw(:class :type :rcode);

use constant SYMLINK => defined(&CORE::readlink);		# Except Win32, VMS, RISC OS

use constant ANY  => classbyname q(ANY);
use constant TSIG => typebyname q(TSIG);

eval { require Digest::HMAC };
eval { require Digest::MD5 };
eval { require Digest::SHA };
eval { require MIME::Base64 };


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	( $self->{algorithm}, $offset ) = Net::DNS::DomainName->decode( $data, $offset );

	# Design decision: Use 32 bits, which will work until the end of time()!
	@{$self}{qw(time_signed fudge)} = unpack "\@$offset xxN n", $$data;
	$offset += 8;

	my $mac_size = unpack "\@$offset n", $$data;
	$self->{macbin} = unpack "\@$offset xx a$mac_size", $$data;
	$offset += $mac_size + 2;

	@{$self}{qw(original_id error)} = unpack "\@$offset nn", $$data;
	$offset += 4;

	my $other_size = unpack "\@$offset n", $$data;
	$self->{other} = unpack "\@$offset xx a$other_size", $$data;
	$offset += $other_size + 2;

	croak('misplaced or corrupt TSIG') unless $limit == length $$data;
	my $raw = substr $$data, 0, $self->{offset}++;
	$self->{rawref} = \$raw;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my $offset = shift;
	my $undef  = shift;
	my $packet = shift;
	my $macbin = $self->macbin;
	unless ($macbin) {
		$self->original_id( $packet->header->id );
		my $sigdata = $self->sig_data($packet);		# form data to be signed
		$macbin = $self->macbin( $self->_mac_function($sigdata) );
	}

	my $rdata = $self->{algorithm}->canonical;

	# Design decision: Use 32 bits, which will work until the end of time()!
	$rdata .= pack 'xxN n', $self->time_signed, $self->fudge;

	$rdata .= pack 'na*', length($macbin), $macbin;

	$rdata .= pack 'nn', $self->original_id, $self->{error};

	my $other = $self->other;
	$rdata .= pack 'na*', length($other), $other;

	return $rdata;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->algorithm(157);
	$self->class('ANY');
	$self->error(0);
	$self->fudge(300);
	$self->other('');
	return;
}


sub _size {				## estimate encoded size
	my $self  = shift;
	my $clone = bless {%$self}, ref($self);			# shallow clone
	return length $clone->encode( 0, undef, Net::DNS::Packet->new() );
}


sub encode {				## override RR method
	my ( $self, @argument ) = @_;
	my $kname = $self->{owner}->encode();			# uncompressed key name
	my $rdata = eval { $self->_encode_rdata(@argument) } || '';
	return pack 'a* n2 N n a*', $kname, TSIG, ANY, 0, length $rdata, $rdata;
}


sub string {				## override RR method
	my $self	= shift;
	my $owner	= $self->{owner}->string;
	my $type	= $self->type;
	my $algorithm	= $self->algorithm;
	my $time_signed = $self->time_signed;
	my $fudge	= $self->fudge;
	my $signature	= $self->mac;
	my $original_id = $self->original_id;
	my $error	= $self->error;
	my $other	= $self->other;

	return <<"QQ";
; $owner	$type	
;	algorithm:	$algorithm
;	time signed:	$time_signed	fudge:	$fudge
;	signature:	$signature
;	original id:	$original_id
;			$error	$other
QQ
}


sub algorithm { return &_algorithm; }


sub key {
	my ( $self, @argument ) = @_;
	return MIME::Base64::encode( $self->keybin(), "" ) unless scalar @argument;
	return $self->keybin( MIME::Base64::decode( join "", @argument ) );
}


sub keybin { return &_keybin; }


sub time_signed {
	my ( $self, @value ) = @_;
	for (@value) { $self->{time_signed} = 0 + $_ }
	return $self->{time_signed} ? $self->{time_signed} : ( $self->{time_signed} = time() );
}


sub fudge {
	my ( $self, @value ) = @_;
	for (@value) { $self->{fudge} = 0 + $_ }
	return $self->{fudge} || 0;
}


sub mac {
	my ( $self, @value ) = @_;
	return MIME::Base64::encode( $self->macbin(), "" ) unless scalar @value;
	return $self->macbin( MIME::Base64::decode( join "", @value ) );
}


sub macbin {
	my ( $self, @value ) = @_;
	for (@value) { $self->{macbin} = $_ }
	return $self->{macbin} || "";
}


sub prior_mac {
	my ( $self, @value ) = @_;
	return MIME::Base64::encode( $self->prior_macbin(), "" ) unless scalar @value;
	return $self->prior_macbin( MIME::Base64::decode( join "", @value ) );
}


sub prior_macbin {
	my ( $self, @value ) = @_;
	for (@value) { $self->{prior_macbin} = $_ }
	return $self->{prior_macbin} || "";
}


sub request_mac {
	my ( $self, @value ) = @_;
	return MIME::Base64::encode( $self->request_macbin(), "" ) unless scalar @value;
	return $self->request_macbin( MIME::Base64::decode( join "", @value ) );
}


sub request_macbin {
	my ( $self, @value ) = @_;
	for (@value) { $self->{request_macbin} = $_ }
	return $self->{request_macbin} || "";
}


sub original_id {
	my ( $self, @value ) = @_;
	for (@value) { $self->{original_id} = 0 + $_ }
	return $self->{original_id} || 0;
}


sub error {
	my ( $self, @value ) = @_;
	for (@value) {
		my $error = $self->{error} = rcodebyname($_);
		$self->other( time() ) if $error == 18;
	}
	return rcodebyval( $self->{error} || '' );
}


sub other {
	my ( $self, @value ) = @_;
	for (@value) { $self->{other} = $_ ? pack( 'xxN', $_ ) : '' }
	return $self->{other} ? unpack( 'N', $self->{other} ) : '';
}


sub other_data { return &other; }				# uncoverable pod


sub sig_function {
	my ( $self, @value ) = @_;
	for (@value) { $self->{sig_function} = $_ }
	return $self->{sig_function};
}

sub sign_func { return &sig_function; }				# uncoverable pod


sub sig_data {
	my ( $self, $message ) = @_;

	if ( ref($message) ) {
		die 'missing packet reference' unless $message->isa('Net::DNS::Packet');
		my @unsigned = grep { ref($_) ne ref($self) } @{$message->{additional}};
		local $message->{additional} = \@unsigned;	# remake header image
		my @part = qw(question answer authority additional);
		my @size = map { scalar @{$message->{$_}} } @part;
		if ( my $rawref = $self->{rawref} ) {
			delete $self->{rawref};
			my $hbin = pack 'n6', $self->original_id, $message->{status}, @size;
			$message = join '', $hbin, substr $$rawref, length $hbin;
		} else {
			my $data = $message->data;
			my $hbin = pack 'n6', $message->{id}, $message->{status}, @size;
			$message = join '', $hbin, substr $data, length $hbin;
		}
	}

	# Design decision: Use 32 bits, which will work until the end of time()!
	my $time = pack 'xxN n', $self->time_signed, $self->fudge;

	# Insert the prior MAC if present (multi-packet message).
	$self->prior_macbin( $self->{link}->macbin ) if $self->{link};
	my $prior_macbin = $self->prior_macbin;
	return pack 'na* a* a*', length($prior_macbin), $prior_macbin, $message, $time if $prior_macbin;

	# Insert the request MAC if present (used to validate responses).
	my $req_mac = $self->request_macbin;
	my $sigdata = $req_mac ? pack( 'na*', length($req_mac), $req_mac ) : '';

	$sigdata .= $message || '';

	my $kname = $self->{owner}->canonical;			# canonical key name
	$sigdata .= pack 'a* n N', $kname, ANY, 0;

	$sigdata .= $self->{algorithm}->canonical;		# canonical algorithm name

	$sigdata .= $time;

	$sigdata .= pack 'n', $self->{error};

	my $other = $self->other;
	$sigdata .= pack 'na*', length($other), $other;

	return $sigdata;
}


sub create {
	my ( $class, $karg, @argument ) = @_;
	croak 'argument undefined' unless defined $karg;

	if ( ref($karg) ) {
		if ( $karg->isa('Net::DNS::Packet') ) {
			my $sigrr = $karg->sigrr;
			croak 'no TSIG in request packet' unless defined $sigrr;
			return Net::DNS::RR->new(		# ( request, options )
				name	       => $sigrr->name,
				type	       => 'TSIG',
				algorithm      => $sigrr->algorithm,
				request_macbin => $sigrr->macbin,
				@argument
				);

		} elsif ( ref($karg) eq __PACKAGE__ ) {
			my $tsig = $karg->_chain;
			$tsig->{macbin} = undef;
			return $tsig;

		} elsif ( ref($karg) eq 'Net::DNS::RR::KEY' ) {
			return Net::DNS::RR->new(
				name	  => $karg->name,
				type	  => 'TSIG',
				algorithm => $karg->algorithm,
				key	  => $karg->key,
				@argument
				);
		}

	} elsif ( ( scalar(@argument) % 2 ) == 0 ) {
		require File::Spec;				# ( keyfile, options )
		require Net::DNS::ZoneFile;
		my ($keypath) = SYMLINK ? grep( {$_} readlink($karg), $karg ) : $karg;
		my ( $vol, $dir, $name ) = File::Spec->splitpath($keypath);
		$name =~ m/^K([^+]+)\+\d+\+(\d+)\./;		# BIND dnssec-keygen
		my ( $keyname, $keytag ) = ( $1, $2 );

		my $keyfile = Net::DNS::ZoneFile->new($karg);
		my ( $algorithm, $secret );
		while ( $keyfile->_getline ) {
			/^key "([^"]+)"/     and $keyname   = $1;    # BIND tsig key
			/algorithm ([^;]+);/ and $algorithm = $1;
			/secret "([^"]+)";/  and $secret    = $1;

			/^Algorithm:/ and ( undef, $algorithm ) = split;    # BIND dnssec private key
			/^Key:/	      and ( undef, $secret )	= split;

			next unless /\bIN\s+KEY\b/;		# BIND dnssec public key
			my $keyrr = Net::DNS::RR->new($_);
			carp "$karg  does not appear to be a BIND dnssec public key"
					unless $keyrr->keytag == ( $keytag || 0 );
			return $class->create( $keyrr, @argument );
		}

		foreach ( $keyname, $algorithm, $secret ) {
			croak 'key file incompatible with TSIG' unless $_;
		}

		return Net::DNS::RR->new(
			name	  => $keyname,
			type	  => 'TSIG',
			algorithm => $algorithm,
			key	  => $secret,
			@argument
			);
	}

	croak "Usage:	$class->create( \$keyfile, \@options )";
}


sub verify {
	my ( $self, $data, @link ) = @_;
	my $fail = undef;

	if ( scalar @link ) {

		my $link = shift @link;
		unless ( ref($link) ) {
			$self->error('BADSIG');			# (multi-packet)
			return $fail;
		}

		my $signerkey = lc( join '+', $self->name, $self->algorithm );
		if ( $link->isa('Net::DNS::Packet') ) {
			my $request = $link->sigrr;		# request TSIG
			my $rqstkey = lc( join '+', $request->name, $request->algorithm );
			$self->error('BADKEY') unless $signerkey eq $rqstkey;
			$self->request_macbin( $request->macbin );

		} elsif ( $link->isa(__PACKAGE__) ) {
			my $priorkey = lc( join '+', $link->name, $link->algorithm );
			$self->error('BADKEY') unless $signerkey eq $priorkey;
			$self->prior_macbin( $link->macbin );

		} else {
			croak 'Usage: $tsig->verify( $reply, $query )';
		}
	}

	return $fail if $self->{error};

	my $sigdata = $self->sig_data($data);			# form data to be verified
	my $tsigmac = $self->_mac_function($sigdata);
	my $tsig    = $self->_chain;

	my $macbin = $self->macbin;
	my $maclen = length $macbin;
	$self->error('BADSIG') if $macbin ne substr $tsigmac, 0, $maclen;

	my $minlen = length($tsigmac) >> 1;			# per RFC4635, 3.1
	$self->error('BADTRUNC') if $maclen < $minlen or $maclen > length $tsigmac;
	$self->error('BADTRUNC') if $maclen < 10;

	my $time_signed = $self->time_signed;
	if ( abs( time() - $time_signed ) > $self->fudge ) {
		$self->error('BADTIME');
		$self->other($time_signed);
	}

	return $self->{error} ? $fail : $tsig;
}

sub vrfyerrstr { return shift->error; }


########################################

{
	# source: http://www.iana.org/assignments/tsig-algorithm-names
	my @algbyname = (
		'HMAC-MD5.SIG-ALG.REG.INT' => 157,		# numbers are from ISC BIND keygen
		'HMAC-SHA1'		   => 161,		# and not blessed by IANA
		'HMAC-SHA224'		   => 162,
		'HMAC-SHA256'		   => 163,
		'HMAC-SHA384'		   => 164,
		'HMAC-SHA512'		   => 165,
		);

	my @algalias = (
		'HMAC-MD5' => 157,
		'HMAC-SHA' => 161,
		);

	my %algbyval = reverse @algbyname;

	my @algrehash = map { /^\d/ ? ($_) x 3 : uc($_) } @algbyname, @algalias;
	foreach (@algrehash) { s/[\W_]//g; }			# strip non-alphanumerics
	my %algbyname = @algrehash;				# work around broken cperl

	sub _algbyname {
		my $key = uc shift;				# synthetic key
		$key =~ s/[\W_]//g;				# strip non-alphanumerics
		return $algbyname{$key};
	}

	sub _algbyval {
		my $value = shift;
		return $algbyval{$value};
	}
}


{
	my %digest = (
		'157' => ['Digest::MD5'],
		'161' => ['Digest::SHA'],
		'162' => ['Digest::SHA', 224, 64],
		'163' => ['Digest::SHA', 256, 64],
		'164' => ['Digest::SHA', 384, 128],
		'165' => ['Digest::SHA', 512, 128],
		);


	my %keytable;

	sub _algorithm {		## install sig function in key table
		my $self = shift;

		if ( my $algname = shift ) {

			unless ( my $digtype = _algbyname($algname) ) {
				$self->{algorithm} = Net::DNS::DomainName->new($algname);

			} else {
				$algname = _algbyval($digtype);
				$self->{algorithm} = Net::DNS::DomainName->new($algname);

				my ( $hash, @param ) = @{$digest{$digtype}};
				my ( undef, @block ) = @param;
				my $digest   = $hash->new(@param);
				my $function = sub {
					my $hmac = Digest::HMAC->new( shift, $digest, @block );
					$hmac->add(shift);
					return $hmac->digest;
				};

				$self->sig_function($function);

				my $keyname = ( $self->{owner} || return )->canonical;
				$keytable{$keyname}{digest} = $function;
			}
		}

		return defined wantarray ? $self->{algorithm}->name : undef;
	}


	sub _keybin {			## install key in key table
		my ( $self, @argument ) = @_;
		croak 'access to TSIG key material denied' unless scalar @argument;
		my $keyref  = $keytable{$self->{owner}->canonical} ||= {};
		my $private = shift @argument;			# closure keeps private key private
		$keyref->{key} = sub {
			my $function = $keyref->{digest};
			return &$function( $private, @_ );
		};
		return;
	}


	sub _mac_function {		## apply keyed hash function to argument
		my ( $self, @argument ) = @_;
		my $owner = $self->{owner}->canonical;
		$self->algorithm( $self->algorithm ) unless $keytable{$owner}{digest};
		my $keyref = $keytable{$owner};
		$keyref->{digest} = $self->sig_function unless $keyref->{digest};
		my $function = $keyref->{key};
		return &$function(@argument);
	}
}


# _chain() creates a new TSIG object linked to the original
# RR, for the purpose of signing multi-message transfers.

sub _chain {
	my $self = shift;
	$self->{link} = undef;
	return bless {%$self, link => $self}, ref($self);
}

########################################


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $tsig = Net::DNS::RR::TSIG->create( $keyfile );

    $tsig = Net::DNS::RR::TSIG->create( $keyfile,
					fudge => 300
					);

=head1 DESCRIPTION

Class for DNS Transaction Signature (TSIG) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 algorithm

    $algorithm = $rr->algorithm;
    $rr->algorithm( $algorithm );

A domain name which specifies the name of the algorithm.

=head2 key

    $rr->key( $key );

Base64 representation of the key material.

=head2 keybin

    $rr->keybin( $keybin );

Binary representation of the key material.

=head2 time_signed

    $time_signed = $rr->time_signed;
    $rr->time_signed( $time_signed );

Signing time as the number of seconds since 1 Jan 1970 00:00:00 UTC.
The default signing time is the current time.

=head2 fudge

    $fudge = $rr->fudge;
    $rr->fudge( $fudge );

"fudge" represents the permitted error in the signing time.
The default fudge is 300 seconds.

=head2 mac

    $rr->mac( $mac );

Message authentication code (MAC).
The programmer must call the Net::DNS::Packet data()
object method before this will return anything meaningful.

=head2 macbin

    $macbin = $rr->macbin;
    $rr->macbin( $macbin );

Binary message authentication code (MAC).

=head2 prior_mac

    $prior_mac = $rr->prior_mac;
    $rr->prior_mac( $prior_mac );

Prior message authentication code (MAC).

=head2 prior_macbin

    $prior_macbin = $rr->prior_macbin;
    $rr->prior_macbin( $prior_macbin );

Binary prior message authentication code.

=head2 request_mac

    $request_mac = $rr->request_mac;
    $rr->request_mac( $request_mac );

Request message authentication code (MAC).

=head2 request_macbin

    $request_macbin = $rr->request_macbin;
    $rr->request_macbin( $request_macbin );

Binary request message authentication code.

=head2 original_id

    $original_id = $rr->original_id;
    $rr->original_id( $original_id );

The message ID from the header of the original packet.

=head2 error

=head2 vrfyerrstr

     $rcode = $tsig->error;

Returns the RCODE covering TSIG processing.  Common values are
NOERROR, BADSIG, BADKEY, and BADTIME.  See RFC8945 for details.


=head2 other

     $other = $tsig->other;

This field should be empty unless the error is BADTIME, in which
case it will contain the server time as the number of seconds since
1 Jan 1970 00:00:00 UTC.

=head2 sig_function

    sub signing_function {
	my ( $keybin, $data ) = @_;

	my $hmac = Digest::HMAC->new( $keybin, 'Digest::MD5' );
	$hmac->add( $data );
	return $hmac->digest;
    }

    $tsig->sig_function( \&signing_function );

This sets the signing function to be used for this TSIG record.
The default signing function is HMAC-MD5.


=head2 sig_data

     $sigdata = $tsig->sig_data($packet);

Returns the packet packed according to RFC8945 in a form for signing. This
is only needed if you want to supply an external signing function, such as is
needed for TSIG-GSS.


=head2 create

    $tsig = Net::DNS::RR::TSIG->create( $keyfile );

    $tsig = Net::DNS::RR::TSIG->create( $keyfile,
					fudge => 300
					);

Returns a TSIG RR constructed using the parameters in the specified
key file, which is assumed to have been generated by tsig-keygen.

=head2 verify

    $verify = $tsig->verify( $data );
    $verify = $tsig->verify( $packet );

    $verify = $tsig->verify( $reply,  $query );

    $verify = $tsig->verify( $packet, $prior );

The boolean verify method will return true if the hash over the
packet data conforms to the data in the TSIG itself


=head1 TSIG Keys

The TSIG authentication mechanism employs shared secret keys
to establish a trust relationship between two entities.

It should be noted that it is possible for more than one key
to be in use simultaneously between any such pair of entities.

TSIG keys are generated using the tsig-keygen utility
distributed with ISC BIND:

    tsig-keygen -a HMAC-SHA256 host1-host2.example.

Other algorithms may be substituted for HMAC-SHA256 in the above example.

These keys must be protected in a manner similar to private keys,
lest a third party masquerade as one of the intended parties
by forging the message authentication code (MAC).


=head1 Configuring BIND Nameserver

The generated key must be added to the /etc/named.conf configuration
or a separate file introduced by the $INCLUDE directive:

    key "host1-host2.example. {
	algorithm hmac-sha256;
	secret "Secret+known+only+by+participating+entities=";
    };


=head1 ACKNOWLEDGMENT

Most of the code in the Net::DNS::RR::TSIG module was contributed
by Chris Turbeville. 

Support for external signing functions was added by Andrew Tridgell.

TSIG verification, BIND keyfile handling and support for HMAC-SHA1,
HMAC-SHA224, HMAC-SHA256, HMAC-SHA384 and HMAC-SHA512 functions was
added by Dick Franks.


=head1 BUGS

A 32-bit representation of time is used, contrary to RFC8945 which
demands 48 bits.  This design decision will need to be reviewed
before the code stops working on 7 February 2106.


=head1 COPYRIGHT

Copyright (c)2000,2001 Michael Fuhr. 

Portions Copyright (c)2002,2003 Chris Reinhardt.

Portions Copyright (c)2013,2020 Dick Franks.

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
L<RFC8945|https://tools.ietf.org/html/rfc8945>

L<TSIG Algorithm Names|http://www.iana.org/assignments/tsig-algorithm-names>

=cut
