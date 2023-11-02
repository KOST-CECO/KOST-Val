package Net::DNS::RR::NSEC3;

use strict;
use warnings;
our $VERSION = (qw$Id: NSEC3.pm 1910 2023-03-30 19:16:30Z willem $)[2];

use base qw(Net::DNS::RR::NSEC);


=head1 NAME

Net::DNS::RR::NSEC3 - DNS NSEC3 resource record

=cut

use integer;

use base qw(Exporter);
our @EXPORT_OK = qw(name2hash);

use Carp;

require Net::DNS::DomainName;

eval { require Digest::SHA };		## optional for simple Net::DNS RR


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	my $ssize = unpack "\@$offset x4 C", $$data;
	my ( $algorithm, $flags, $iterations, $saltbin ) = unpack "\@$offset CCnx a$ssize", $$data;
	@{$self}{qw(algorithm flags iterations saltbin)} = ( $algorithm, $flags, $iterations, $saltbin );
	$offset += 5 + $ssize;
	my $hsize = unpack "\@$offset C", $$data;
	$self->{hnxtname} = unpack "\@$offset x a$hsize", $$data;
	$offset += 1 + $hsize;
	$self->{typebm} = substr $$data, $offset, ( $limit - $offset );
	$self->{hashfn} = _hashfn( $algorithm, $iterations, $saltbin );
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my $salt = $self->saltbin;
	my $hash = $self->{hnxtname};
	return pack 'CCn C a* C a* a*', $self->algorithm, $self->flags, $self->iterations,
			length($salt), $salt,
			length($hash), $hash,
			$self->{typebm};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @rdata = (
		$self->algorithm,   $self->flags,    $self->iterations,
		$self->salt || '-', $self->hnxtname, $self->typelist
		);
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	my $alg = $self->algorithm( shift @argument );
	$self->flags( shift @argument );
	my $iter = $self->iterations( shift @argument );
	my $salt = shift @argument;
	$self->salt($salt) unless $salt eq '-';
	$self->hnxtname( shift @argument );
	$self->typelist(@argument);
	$self->{hashfn} = _hashfn( $alg, $iter, $self->{saltbin} );
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->_parse_rdata( 1, 0, 0, '' );
	return;
}


sub algorithm {
	my ( $self, $arg ) = @_;

	unless ( ref($self) ) {		## class method or simple function
		my $argn = pop;
		return $argn =~ /[^0-9]/ ? _digestbyname($argn) : _digestbyval($argn);
	}

	return $self->{algorithm} unless defined $arg;
	return _digestbyval( $self->{algorithm} ) if $arg =~ /MNEMONIC/i;
	return $self->{algorithm} = _digestbyname($arg);
}


sub flags {
	my ( $self, @value ) = @_;
	for (@value) { $self->{flags} = 0 + $_ }
	return $self->{flags} || 0;
}


sub optout {
	my ( $self, @value ) = @_;
	for ( $self->{flags} |= 0 ) {
		if ( scalar @value ) {
			$_ |= 0x01;
			$_ ^= 0x01 unless shift @value;
		}
	}
	return $self->{flags} & 0x01;
}


sub iterations {
	my ( $self, @value ) = @_;
	for (@value) { $self->{iterations} = 0 + $_ }
	return $self->{iterations} || 0;
}


sub salt {
	my ( $self, @value ) = @_;
	return unpack "H*", $self->saltbin() unless scalar @value;
	my @hex = map { /^"*([\dA-Fa-f]*)"*$/ || croak("corrupt hex"); $1 } @value;
	return $self->saltbin( pack "H*", join "", @hex );
}


sub saltbin {
	my ( $self, @value ) = @_;
	for (@value) { $self->{saltbin} = $_ }
	return $self->{saltbin} || "";
}


sub hnxtname {
	my ( $self, @name ) = @_;
	for (@name) { $self->{hnxtname} = _decode_base32hex($_) }
	return defined(wantarray) ? _encode_base32hex( $self->{hnxtname} ) : undef;
}


sub match {
	my ( $self, $name ) = @_;

	my ($owner) = $self->{owner}->label;
	my $ownerhash = _decode_base32hex($owner);

	my $hashfn = $self->{hashfn};
	return $ownerhash eq &$hashfn($name);
}

sub covers {
	my ( $self, $name ) = @_;

	my ( $owner, @zone ) = $self->{owner}->label;
	my $ownerhash = _decode_base32hex($owner);
	my $nexthash  = $self->{hnxtname};

	my @label = Net::DNS::DomainName->new($name)->label;
	my @close = @label;
	foreach (@zone) { pop(@close) }				# strip zone labels
	return if lc($name) ne lc( join '.', @close, @zone );	# out of zone

	my $hashfn = $self->{hashfn};

	foreach (@close) {
		my $hash = &$hashfn( join '.', @label );
		my $cmp1 = $hash cmp $ownerhash;
		last unless $cmp1;				# stop at provable encloser
		return 1 if ( $cmp1 + ( $nexthash cmp $hash ) ) == 2;
		shift @label;
	}
	return;
}


sub encloser {
	my ( $self, $qname ) = @_;

	my ( $owner, @zone ) = $self->{owner}->label;
	my $ownerhash = _decode_base32hex($owner);
	my $nexthash  = $self->{hnxtname};

	my @label = Net::DNS::DomainName->new($qname)->label;
	my @close = @label;
	foreach (@zone) { pop(@close) }				# strip zone labels
	return if lc($qname) ne lc( join '.', @close, @zone );	# out of zone

	my $hashfn = $self->{hashfn};

	my $encloser = $qname;
	foreach (@close) {
		my $nextcloser = $encloser;
		shift @label;
		my $hash = &$hashfn( $encloser = join '.', @label );
		next if $hash ne $ownerhash;
		$self->{nextcloser} = $nextcloser;		# next closer name
		$self->{wildcard}   = "*.$encloser";		# wildcard at provable encloser
		return $encloser;				# provable encloser
	}
	return;
}


sub nextcloser { return shift->{nextcloser}; }

sub wildcard { return shift->{wildcard}; }


########################################

my @digestbyname = (
	'SHA-1' => 1,						# [RFC3658]
	);

my @digestalias = ( 'SHA' => 1 );

my %digestbyval = reverse @digestbyname;

foreach (@digestbyname) { s/[\W_]//g; }				# strip non-alphanumerics
my @digestrehash = map { /^\d/ ? ($_) x 3 : uc($_) } @digestbyname;
my %digestbyname = ( @digestalias, @digestrehash );		# work around broken cperl

sub _digestbyname {
	my $arg = shift;
	my $key = uc $arg;					# synthetic key
	$key =~ s/[\W_]//g;					# strip non-alphanumerics
	my $val = $digestbyname{$key};
	croak qq[unknown algorithm $arg] unless defined $val;
	return $val;
}

sub _digestbyval {
	my $value = shift;
	return $digestbyval{$value} || return $value;
}


my %digest = (
	'1' => scalar( eval { Digest::SHA->new(1) } ),		# RFC3658
	);


sub _decode_base32hex {
	local $_ = shift || '';
	tr [0-9A-Va-v\060-\071\101-\126\141-\166] [\000-\037\012-\037\000-\037\012-\037];
	my $l = ( 5 * length ) & ~7;
	return pack "B$l", join '', map { unpack( 'x3a5', unpack 'B8', $_ ) } split //;
}


sub _encode_base32hex {
	my @split = grep {length} split /(\S{5})/, unpack 'B*', shift;
	local $_ = join '', map { pack( 'B*', "000$_" ) } @split;
	tr [\000-\037] [0-9a-v];
	return $_;
}


my ( $cache1, $cache2, $limit ) = ( {}, {}, 10 );

sub _hashfn {
	my $hashalg    = shift;
	my $iterations = shift || 0;
	my $salt       = shift || '';

	my $hash = $digest{$hashalg};
	return sub { croak "algorithm $hashalg not supported" }
			unless $hash;
	my $clone = $hash->clone;

	my $key_adjunct = pack 'Cna*', $hashalg, $iterations, $salt;

	return sub {
		my $name  = Net::DNS::DomainName->new(shift)->canonical;
		my $key	  = join '', $name, $key_adjunct;
		my $cache = $$cache1{$key} ||= $$cache2{$key};	# two layer cache
		return $cache if defined $cache;
		( $cache1, $cache2, $limit ) = ( {}, $cache1, 50 ) unless $limit--;    # recycle cache

		$clone->add($name);
		$clone->add($salt);
		my $digest = $clone->digest;
		my $count  = $iterations;
		while ( $count-- ) {
			$clone->add($digest);
			$clone->add($salt);
			$digest = $clone->digest;
		}
		return $$cache1{$key} = $digest;
	};
}


sub hashalgo { return &algorithm; }				# uncoverable pod

sub name2hash {
	my $hashalg    = shift;					# uncoverable pod
	my $name       = shift;
	my $iterations = shift || 0;
	my $salt       = pack 'H*', shift || '';
	my $hash       = _hashfn( $hashalg, $iterations, $salt );
	return _encode_base32hex( &$hash($name) );
}

########################################


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name NSEC3 algorithm flags iterations salt hnxtname');

=head1 DESCRIPTION

Class for DNSSEC NSEC3 resource records.

The NSEC3 Resource Record (RR) provides authenticated denial of
existence for DNS Resource Record Sets.

The NSEC3 RR lists RR types present at the original owner name of the
NSEC3 RR.  It includes the next hashed owner name in the hash order
of the zone.  The complete set of NSEC3 RRs in a zone indicates which
RRSets exist for the original owner name of the RR and form a chain
of hashed owner names in the zone.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 algorithm

    $algorithm = $rr->algorithm;
    $rr->algorithm( $algorithm );

The Hash Algorithm field is represented as an unsigned decimal
integer.  The value has a maximum of 255.

algorithm() may also be invoked as a class method or simple function
to perform mnemonic and numeric code translation.

=head2 flags

    $flags = $rr->flags;
    $rr->flags( $flags );

The Flags field is an unsigned decimal integer
interpreted as eight concatenated Boolean values. 

=over 4

=item optout

 $rr->optout(1);

 if ( $rr->optout ) {
	...
 }

Boolean Opt Out flag.

=back

=head2 iterations

    $iterations = $rr->iterations;
    $rr->iterations( $iterations );

The Iterations field is represented as an unsigned decimal
integer.  The value is between 0 and 65535, inclusive. 

=head2 salt

    $salt = $rr->salt;
    $rr->salt( $salt );

The Salt field is represented as a contiguous sequence of hexadecimal
digits. A "-" (unquoted) is used in string format to indicate that the
salt field is absent. 

=head2 saltbin

    $saltbin = $rr->saltbin;
    $rr->saltbin( $saltbin );

The Salt field as a sequence of octets. 

=head2 hnxtname

    $hnxtname = $rr->hnxtname;
    $rr->hnxtname( $hnxtname );

The Next Hashed Owner Name field points to the next node that has
authoritative data or contains a delegation point NS RRset.

=head2 typelist

    @typelist = $rr->typelist;
    $typelist = $rr->typelist;
    $rr->typelist( @typelist );

typelist() identifies the RRset types that exist at the domain name
matched by the NSEC3 RR.  When called in scalar context, the list is
interpolated into a string.

=head2 typemap

    $exists = $rr->typemap($rrtype);

typemap() returns a Boolean true value if the specified RRtype occurs
in the type bitmap of the NSEC3 record.

=head2 match

    $matched = $rr->match( 'example.foo' );

match() returns a Boolean true value if the hash of the domain name
argument matches the hashed owner name of the NSEC3 RR.

=head2 covers

    $covered = $rr->covers( 'example.foo' );

covers() returns a Boolean true value if the hash of the domain name
argument, or ancestor of that name, falls between the owner name and
the next hashed owner name of the NSEC3 RR.

=head2 encloser, nextcloser, wildcard

    $encloser = $rr->encloser( 'example.foo' );
    print "encloser: $encloser\n" if $encloser;

encloser() returns the name of a provable encloser of the query name
argument obtained from the NSEC3 RR.

nextcloser() returns the next closer name, which is one label longer
than the closest encloser.
This is only valid after encloser() has returned a valid domain name.

wildcard() returns the unexpanded wildcard name from which the next
closer name was possibly synthesised.
This is only valid after encloser() has returned a valid domain name.


=head1 COPYRIGHT

Copyright (c)2017,2018 Dick Franks

Portions Copyright (c)2007,2008 NLnet Labs.  Author Olaf M. Kolkman

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
L<RFC5155|https://tools.ietf.org/html/rfc5155>
L<RFC9077|https://tools.ietf.org/html/rfc9077>

L<Hash Algorithms|http://www.iana.org/assignments/dnssec-nsec3-parameters>

=cut
