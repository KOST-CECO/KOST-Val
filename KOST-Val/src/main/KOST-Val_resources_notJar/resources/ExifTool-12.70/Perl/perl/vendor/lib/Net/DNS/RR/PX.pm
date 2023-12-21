package Net::DNS::RR::PX;

use strict;
use warnings;
our $VERSION = (qw$Id: PX.pm 1898 2023-02-15 14:27:22Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::PX - DNS PX resource record

=cut

use integer;

use Net::DNS::DomainName;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	$self->{preference} = unpack( "\@$offset n", $$data );
	( $self->{map822},  $offset ) = Net::DNS::DomainName2535->decode( $data, $offset + 2 );
	( $self->{mapx400}, $offset ) = Net::DNS::DomainName2535->decode( $data, $offset );
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my ( $self, $offset, @opaque ) = @_;

	my $mapx400 = $self->{mapx400};
	my $rdata   = pack( 'n', $self->{preference} );
	$rdata .= $self->{map822}->encode( $offset + 2, @opaque );
	$rdata .= $mapx400->encode( $offset + length($rdata), @opaque );
	return $rdata;
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @rdata = ( $self->preference, $self->{map822}->string, $self->{mapx400}->string );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	for (qw(preference map822 mapx400)) { $self->$_( shift @argument ) }
	return;
}


sub preference {
	my ( $self, @value ) = @_;
	for (@value) { $self->{preference} = 0 + $_ }
	return $self->{preference} || 0;
}


sub map822 {
	my ( $self, @value ) = @_;
	for (@value) { $self->{map822} = Net::DNS::DomainName2535->new($_) }
	return $self->{map822} ? $self->{map822}->name : undef;
}


sub mapx400 {
	my ( $self, @value ) = @_;
	for (@value) { $self->{mapx400} = Net::DNS::DomainName2535->new($_) }
	return $self->{mapx400} ? $self->{mapx400}->name : undef;
}


my $function = sub {			## sort RRs in numerically ascending order.
	return $Net::DNS::a->{'preference'} <=> $Net::DNS::b->{'preference'};
};

__PACKAGE__->set_rrsort_func( 'preference', $function );

__PACKAGE__->set_rrsort_func( 'default_sort', $function );


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name PX preference map822 mapx400');

=head1 DESCRIPTION

Class for DNS X.400 Mail Mapping Information (PX) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 preference

    $preference = $rr->preference;
    $rr->preference( $preference );

A 16 bit integer which specifies the preference
given to this RR among others at the same owner.
Lower values are preferred.

=head2 map822

    $map822 = $rr->map822;
    $rr->map822( $map822 );

A domain name element containing <rfc822-domain>, the
RFC822 part of the MIXER Conformant Global Address Mapping.

=head2 mapx400

    $mapx400 = $rr->mapx400;
    $rr->mapx400( $mapx400 );

A <domain-name> element containing the value of
<x400-in-domain-syntax> derived from the X.400 part of
the MIXER Conformant Global Address Mapping.


=head1 COPYRIGHT

Copyright (c)1997 Michael Fuhr. 

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
L<RFC2163|https://tools.ietf.org/html/rfc2163>

=cut
