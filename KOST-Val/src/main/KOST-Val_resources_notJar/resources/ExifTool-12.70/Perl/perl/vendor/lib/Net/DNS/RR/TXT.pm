package Net::DNS::RR::TXT;

use strict;
use warnings;
our $VERSION = (qw$Id: TXT.pm 1911 2023-04-17 12:30:59Z willem $)[2];

use base qw(Net::DNS::RR);

=encoding utf8


=head1 NAME

Net::DNS::RR::TXT - DNS TXT resource record

=cut

use integer;

use Carp;
use Net::DNS::Text;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	my $limit = $offset + $self->{rdlength};
	my $text;
	my $txtdata = $self->{txtdata} = [];
	while ( $offset < $limit ) {
		( $text, $offset ) = Net::DNS::Text->decode( $data, $offset );
		push @$txtdata, $text;
	}

	croak('corrupt TXT data') unless $offset == $limit;	# more or less FUBAR
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my $txtdata = $self->{txtdata};
	return join '', map { $_->encode } @$txtdata;
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my $txtdata = $self->{txtdata};
	return ( map { $_->unicode } @$txtdata );
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	$self->{txtdata} = [map { Net::DNS::Text->new($_) } @argument];
	return;
}


sub txtdata {
	my ( $self, @value ) = @_;

	$self->{txtdata} = [map { Net::DNS::Text->new($_) } @value] if scalar @value;

	my $txtdata = $self->{txtdata} || [];

	return ( map { $_->value } @$txtdata ) if wantarray;

	return defined(wantarray) ? join( ' ', map { $_->value } @$txtdata ) : '';
}


sub char_str_list { return my @txt = &txtdata }			# uncoverable pod


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new( 'name TXT	txtdata ...' );

    $rr = Net::DNS::RR->new( name    => 'name',
			     type    => 'TXT',
			     txtdata => 'single text string'
			     );

    $rr = Net::DNS::RR->new( name    => 'name',
			     type    => 'TXT',
			     txtdata => [ 'multiple', 'strings', ... ]
			     );

    use utf8;
    $rr = Net::DNS::RR->new( 'jp TXT	古池や　蛙飛込む　水の音' );

=head1 DESCRIPTION

Class for DNS Text (TXT) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 txtdata

    $string = $rr->txtdata;
    @list   = $rr->txtdata;

    $rr->txtdata( @list );

When invoked in scalar context, txtdata() returns a concatenation
of the descriptive text elements each separated by a single space
character.

In a list context, txtdata() returns a list of the text elements.


=head1 COPYRIGHT

Copyright (c)2011 Dick Franks.

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
L<RFC1035(3.3.14)|https://tools.ietf.org/html/rfc1035>
L<RFC3629|https://tools.ietf.org/html/rfc3629>

=cut
