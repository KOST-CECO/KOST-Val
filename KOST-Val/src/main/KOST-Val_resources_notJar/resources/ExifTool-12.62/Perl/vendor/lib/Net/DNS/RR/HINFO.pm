package Net::DNS::RR::HINFO;

use strict;
use warnings;
our $VERSION = (qw$Id: HINFO.pm 1896 2023-01-30 12:59:25Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::HINFO - DNS HINFO resource record

=cut

use integer;

use Net::DNS::Text;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, $data, $offset ) = @_;

	( $self->{cpu}, $offset ) = Net::DNS::Text->decode( $data, $offset );
	( $self->{os},	$offset ) = Net::DNS::Text->decode( $data, $offset );
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	return join '', $self->{cpu}->encode, $self->{os}->encode;
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	return join ' ', $self->{cpu}->string, $self->{os}->string;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	$self->cpu( shift @argument );
	$self->os(@argument);
	return;
}


sub cpu {
	my ( $self, @value ) = @_;
	for (@value) { $self->{cpu} = Net::DNS::Text->new($_) }
	return $self->{cpu} ? $self->{cpu}->value : undef;
}


sub os {
	my ( $self, @value ) = @_;
	for (@value) { $self->{os} = Net::DNS::Text->new($_) }
	return $self->{os} ? $self->{os}->value : undef;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name HINFO cpu os');

=head1 DESCRIPTION

Class for DNS Hardware Information (HINFO) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 cpu

    $cpu = $rr->cpu;
    $rr->cpu( $cpu );

Returns the CPU type for this RR.

=head2 os

    $os = $rr->os;
    $rr->os( $os );

Returns the operating system type for this RR.


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
L<RFC1035(3.3.2)|https://tools.ietf.org/html/rfc1035>

=cut
