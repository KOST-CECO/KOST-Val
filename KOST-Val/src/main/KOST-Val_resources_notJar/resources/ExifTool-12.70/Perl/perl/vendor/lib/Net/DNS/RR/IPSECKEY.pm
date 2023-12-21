package Net::DNS::RR::IPSECKEY;

use strict;
use warnings;
our $VERSION = (qw$Id: IPSECKEY.pm 1909 2023-03-23 11:36:16Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::IPSECKEY - DNS IPSECKEY resource record

=cut

use integer;

use Carp;

use Net::DNS::DomainName;
use Net::DNS::RR::A;
use Net::DNS::RR::AAAA;

use constant BASE64 => defined eval { require MIME::Base64 };

my %wireformat = (
	0 => 'C3 a0 a*',
	1 => 'C3 a4 a*',
	2 => 'C3 a16 a*',
	3 => 'C3 a* a*',
	);


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};

	@{$self}{qw(precedence gatetype algorithm)} = unpack "\@$offset C3", $$data;
	$offset += 3;

	my $gatetype = $self->{gatetype};
	if ( not $gatetype ) {
		$self->{gateway} = undef;			# no gateway

	} elsif ( $gatetype == 1 ) {
		$self->{gateway} = unpack "\@$offset a4", $$data;
		$offset += 4;

	} elsif ( $gatetype == 2 ) {
		$self->{gateway} = unpack "\@$offset a16", $$data;
		$offset += 16;

	} elsif ( $gatetype == 3 ) {
		my $name;
		( $name, $offset ) = Net::DNS::DomainName->decode( $data, $offset );
		$self->{gateway} = $name->encode;

	} else {
		die "unknown gateway type ($gatetype)";
	}

	$self->keybin( substr $$data, $offset, $limit - $offset );
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my $gatetype   = $self->gatetype;
	my $gateway    = $self->{gateway};
	my $precedence = $self->precedence;
	my $algorithm  = $self->algorithm;
	my $keybin     = $self->keybin;

	return pack $wireformat{$gatetype}, $precedence, $gatetype, $algorithm, $gateway, $keybin;
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	return $self->SUPER::_format_rdata() unless BASE64;
	my @rdata  = map { $self->$_ } qw(precedence gatetype algorithm);
	my @base64 = split /\s+/, MIME::Base64::encode( $self->keybin );
	push @rdata, ( $self->gateway, @base64 );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	foreach (qw(precedence gatetype algorithm gateway)) { $self->$_( shift @argument ) }
	$self->key(@argument);
	return;
}


sub precedence {
	my ( $self, @value ) = @_;
	for (@value) { $self->{precedence} = 0 + $_ }
	return $self->{precedence} || 0;
}


sub gatetype {
	return shift->{gatetype} || 0;
}


sub algorithm {
	my ( $self, @value ) = @_;
	for (@value) { $self->{algorithm} = 0 + $_ }
	return $self->{algorithm} || 0;
}


sub gateway {
	my ( $self, @value ) = @_;

	for (@value) {
		/^\.*$/ && do {
			$self->{gatetype} = 0;
			$self->{gateway}  = '';			# no gateway
			last;
		};
		/:.*:/ && do {
			$self->{gatetype} = 2;
			$self->{gateway}  = Net::DNS::RR::AAAA::address( {}, $_ );
			last;
		};
		/\.\d+$/ && do {
			$self->{gatetype} = 1;
			$self->{gateway}  = Net::DNS::RR::A::address( {}, $_ );
			last;
		};
		/\..+/ && do {
			$self->{gatetype} = 3;
			$self->{gateway}  = Net::DNS::DomainName->new($_)->encode;
			last;
		};
		croak 'unrecognised gateway type';
	}

	if ( defined wantarray ) {
		my $gateway = $self->{gateway};
		for ( $self->gatetype ) {
			/^1$/ && return Net::DNS::RR::A::address( {address => $gateway} );
			/^2$/ && return Net::DNS::RR::AAAA::address( {address => $gateway} );
			/^3$/ && return Net::DNS::DomainName->decode( \$gateway )->name;
		}
		return wantarray ? '.' : undef;
	}
	return;
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


sub pubkey { return &key; }


my $function = sub {			## sort RRs in numerically ascending order.
	return $Net::DNS::a->{'preference'} <=> $Net::DNS::b->{'preference'};
};

__PACKAGE__->set_rrsort_func( 'preference', $function );

__PACKAGE__->set_rrsort_func( 'default_sort', $function );


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name IPSECKEY precedence gatetype algorithm gateway key');

=head1 DESCRIPTION

DNS IPSEC Key Storage (IPSECKEY) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 precedence

    $precedence = $rr->precedence;
    $rr->precedence( $precedence );

This is an 8-bit precedence for this record.  Gateways listed in
IPSECKEY records with lower precedence are to be attempted first.

=head2 gatetype

    $gatetype = $rr->gatetype;

The gateway type field indicates the format of the information that is
stored in the gateway field.

=head2 algorithm

    $algorithm = $rr->algorithm;
    $rr->algorithm( $algorithm );

The algorithm type field identifies the public keys cryptographic
algorithm and determines the format of the public key field.

=head2 gateway

    $gateway = $rr->gateway;
    $rr->gateway( $gateway );

The gateway field indicates a gateway to which an IPsec tunnel may be
created in order to reach the entity named by this resource record.

=head2 pubkey

=head2 key

    $key = $rr->key;
    $rr->key( $key );

Base64 representation of the optional public key block for the resource record.

=head2 keybin

    $keybin = $rr->keybin;
    $rr->keybin( $keybin );

Binary representation of the public key block for the resource record.


=head1 COPYRIGHT

Copyright (c)2007 Olaf Kolkman, NLnet Labs.

Portions Copyright (c)2012,2015 Dick Franks.

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
L<RFC4025|https://tools.ietf.org/html/rfc4025>

=cut
