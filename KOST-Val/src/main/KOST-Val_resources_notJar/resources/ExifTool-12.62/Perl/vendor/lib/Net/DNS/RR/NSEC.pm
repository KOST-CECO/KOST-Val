package Net::DNS::RR::NSEC;

use strict;
use warnings;
our $VERSION = (qw$Id: NSEC.pm 1896 2023-01-30 12:59:25Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::NSEC - DNS NSEC resource record

=cut

use integer;

use Net::DNS::DomainName;
use Net::DNS::Parameters qw(:type);


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	( $self->{nxtdname}, $offset ) = Net::DNS::DomainName->decode( $data, $offset );
	$self->{typebm} = substr $$data, $offset, $limit - $offset;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my $nxtdname = $self->{nxtdname};
	return join '', $nxtdname->encode(), $self->{typebm};
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my $nxtdname = $self->{nxtdname};
	return ( $nxtdname->string(), $self->typelist );
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	$self->nxtdname( shift @argument );
	$self->typelist(@argument);
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->_parse_rdata('.');
	return;
}


sub nxtdname {
	my ( $self, @value ) = @_;
	for (@value) { $self->{nxtdname} = Net::DNS::DomainName->new($_) }
	return $self->{nxtdname} ? $self->{nxtdname}->name : undef;
}


sub typelist {
	my ( $self, @argument ) = @_;

	if ( scalar(@argument) || !defined(wantarray) ) {
		$self->{typebm} = &_type2bm(@argument);
		return;
	}

	my @type = &_bm2type( $self->{typebm} );
	return wantarray ? (@type) : "@type";
}


sub typemap {
	my ( $self, $type ) = @_;

	my $number = typebyname($type);
	my $window = $number >> 8;
	my $bitnum = $number & 255;

	my $typebm = $self->{typebm} || return;
	my @bitmap;
	my $index = 0;
	while ( $index < length $typebm ) {
		my ( $block, $size ) = unpack "\@$index C2", $typebm;
		$bitmap[$block] = unpack "\@$index xxa$size", $typebm;
		$index += $size + 2;
	}

	my @bit = split //, unpack 'B*', ( $bitmap[$window] || return );
	return $bit[$bitnum];
}


sub match {
	my ( $self, $qname ) = @_;
	my $name = Net::DNS::DomainName->new($qname)->canonical;
	return $name eq $self->{owner}->canonical;
}


sub covers {
	my ( $self, $qname ) = @_;
	my $name = join chr(0), reverse Net::DNS::DomainName->new($qname)->_wire;
	my $this = join chr(0), reverse $self->{owner}->_wire;
	my $next = join chr(0), reverse $self->{nxtdname}->_wire;
	foreach ( $name, $this, $next ) {tr /\101-\132/\141-\172/}

	return ( $name cmp $this ) + ( "$next\001" cmp $name ) == 2 unless $next gt $this;
	return ( $name cmp $this ) + ( $next cmp $name ) == 2;
}


sub encloser {
	my ( $self, $qname ) = @_;
	my @label = Net::DNS::Domain->new($qname)->label;

	my @owner = $self->{owner}->label;
	my $depth = scalar(@owner);
	my $next;
	while ( scalar(@label) > $depth ) {
		$next = shift @label;
	}

	return unless defined $next;

	my $nextcloser = join( '.', $next, @label );
	return if lc($nextcloser) ne lc( join '.', $next, @owner );

	$self->{nextcloser} = $nextcloser;
	$self->{wildcard}   = join( '.', '*', @label );
	return $self->owner;
}


sub nextcloser { return shift->{nextcloser}; }

sub wildcard { return shift->{wildcard}; }


########################################

sub _type2bm {
	my @typelist = @_;
	my @typearray;
	foreach my $typename ( map { split() } @typelist ) {
		my $number = typebyname($typename);
		my $window = $number >> 8;
		my $bitnum = $number & 255;
		my $octet  = $bitnum >> 3;
		my $bit	   = $bitnum & 7;
		$typearray[$window][$octet] |= 0x80 >> $bit;
	}

	my $bitmap = '';
	my $window = 0;
	foreach (@typearray) {
		if ( my $pane = $typearray[$window] ) {
			my @content = map { $_ || 0 } @$pane;
			$bitmap .= pack 'CC C*', $window, scalar(@content), @content;
		}
		$window++;
	}

	return $bitmap;
}


sub _bm2type {
	my @empty;
	my $bitmap = shift || return @empty;

	my $index = 0;
	my $limit = length $bitmap;
	my @typelist;

	while ( $index < $limit ) {
		my ( $block, $size ) = unpack "\@$index C2", $bitmap;
		my $typenum = $block << 8;
		foreach my $octet ( unpack "\@$index xxC$size", $bitmap ) {
			my $i = $typenum += 8;
			my @name;
			while ($octet) {
				--$i;
				unshift @name, typebyval($i) if $octet & 1;
				$octet = $octet >> 1;
			}
			push @typelist, @name;
		}
		$index += $size + 2;
	}

	return @typelist;
}


sub typebm {				## historical
	my ( $self, @typebm ) = @_;				# uncoverable pod
	for (@typebm) { $self->{typebm} = $_ }
	$self->_deprecate('prefer $rr->typelist() or $rr->typemap()');
	return $self->{typebm};
}

sub covered {				## historical
	my ( $self, @argument ) = @_;				# uncoverable pod
	return $self->covers(@argument);
}

########################################


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new( 'name NSEC nxtdname typelist' );

=head1 DESCRIPTION

Class for DNSSEC NSEC resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 nxtdname

    $nxtdname = $rr->nxtdname;
    $rr->nxtdname( $nxtdname );

The Next Domain field contains the next owner name (in the
canonical ordering of the zone) that has authoritative data
or contains a delegation point NS RRset.

=head2 typelist

    @typelist = $rr->typelist;
    $typelist = $rr->typelist;

typelist() identifies the RRset types that exist at the NSEC RR
owner name.  When called in scalar context, the list is interpolated
into a string.

=head2 typemap

    $exists = $rr->typemap($rrtype);

typemap() returns a Boolean true value if the specified RRtype occurs
in the type bitmap of the NSEC record.

=head2 match

    $matched = $rr->match( 'example.foo' );

match() returns a Boolean true value if the canonical form of the name
argument matches the canonical owner name of the NSEC RR.


=head2 covers

    $covered = $rr->covers( 'example.foo' );

covers() returns a Boolean true value if the canonical form of the name,
or one of its ancestors, falls between the owner name and the nxtdname
field of the NSEC record.

=head2 encloser, nextcloser, wildcard

    $encloser = $rr->encloser( 'example.foo' );
    print "encloser: $encloser\n" if $encloser;

encloser() returns the name of a provable encloser of the query name
argument obtained from the NSEC RR.

nextcloser() returns the next closer name, which is one label longer
than the closest encloser.
This is only valid after encloser() has returned a valid domain name.

wildcard() returns the unexpanded wildcard name from which the next
closer name was possibly synthesised.
This is only valid after encloser() has returned a valid domain name.


=head1 COPYRIGHT

Copyright (c)2001-2005 RIPE NCC.  Author Olaf M. Kolkman

Portions Copyright (c)2018-2019 Dick Franks

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
L<RFC9077|https://tools.ietf.org/html/rfc9077>

=cut
