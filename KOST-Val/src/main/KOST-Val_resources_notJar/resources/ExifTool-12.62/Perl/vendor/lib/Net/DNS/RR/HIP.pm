package Net::DNS::RR::HIP;

use strict;
use warnings;
our $VERSION = (qw$Id: HIP.pm 1814 2020-10-14 21:49:16Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::HIP - DNS HIP resource record

=cut

use integer;

use Carp;
use Net::DNS::DomainName;
use MIME::Base64;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my $self = shift;
	my ( $data, $offset ) = @_;

	my ( $hitlen, $pklen ) = unpack "\@$offset Cxn", $$data;
	@{$self}{qw(algorithm hitbin keybin)} = unpack "\@$offset xCxx a$hitlen a$pklen", $$data;

	my $limit = $offset + $self->{rdlength};
	$offset += 4 + $hitlen + $pklen;
	$self->{servers} = [];
	while ( $offset < $limit ) {
		my $item;
		( $item, $offset ) = Net::DNS::DomainName->decode( $data, $offset );
		push @{$self->{servers}}, $item;
	}
	croak('corrupt HIP data') unless $offset == $limit;	# more or less FUBAR
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my $hit = $self->hitbin;
	my $key = $self->keybin;
	my $nos = pack 'C2n a* a*', length($hit), $self->algorithm, length($key), $hit, $key;
	return join '', $nos, map { $_->encode } @{$self->{servers}};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my $base64 = MIME::Base64::encode( $self->{keybin}, '' );
	my @server = map { $_->string } @{$self->{servers}};
	my @rdata  = ( $self->algorithm, $self->hit, $base64, @server );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my $self = shift;

	foreach (qw(algorithm hit key)) { $self->$_(shift) }
	$self->servers(@_);
	return;
}


sub algorithm {
	my $self = shift;

	$self->{algorithm} = 0 + shift if scalar @_;
	return $self->{algorithm} || 0;
}


sub hit {
	my $self = shift;
	return unpack "H*", $self->hitbin() unless scalar @_;
	return $self->hitbin( pack "H*", join "", map { /^"*([\dA-Fa-f]*)"*$/ || croak("corrupt hex"); $1 } @_ );
}


sub hitbin {
	my $self = shift;

	$self->{hitbin} = shift if scalar @_;
	return $self->{hitbin} || "";
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


sub servers {
	my $self = shift;

	my $servers = $self->{servers} ||= [];
	@$servers = map { Net::DNS::DomainName->new($_) } @_ if scalar @_;
	return defined(wantarray) ? map( { $_->name } @$servers ) : ();
}

sub rendezvousservers {			## historical
	$_[0]->_deprecate('prefer $rr->servers()');		# uncoverable pod
	my @servers = &servers;
	return \@servers;
}

sub pkalgorithm {			## historical
	return &algorithm;					# uncoverable pod
}

sub pubkey {				## historical
	return &key;						# uncoverable pod
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name IN HIP algorithm hit key servers');

=head1 DESCRIPTION

Class for DNS Host Identity Protocol (HIP) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 algorithm

    $algorithm = $rr->algorithm;
    $rr->algorithm( $algorithm );

The PK algorithm field indicates the public key cryptographic
algorithm and the implied public key field format.
The values are those defined for the IPSECKEY algorithm type [RFC4025].

=head2 hit

    $hit = $rr->hit;
    $rr->hit( $hit );

The hexadecimal representation of the host identity tag.

=head2 hitbin

    $hitbin = $rr->hitbin;
    $rr->hitbin( $hitbin );

The binary representation of the host identity tag.

=head2 key

    $key = $rr->key;
    $rr->key( $key );

The hexadecimal representation of the public key.

=head2 keybin

    $keybin = $rr->keybin;
    $rr->keybin( $keybin );

The binary representation of the public key.

=head2 servers

    @servers = $rr->servers;

Optional list of domain names of rendezvous servers.


=head1 COPYRIGHT

Copyright (c)2009 Olaf Kolkman, NLnet Labs

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

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, RFC8005

=cut
