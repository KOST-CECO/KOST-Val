package Net::DNS::RR::MB;

use strict;
use warnings;
our $VERSION = (qw$Id: MB.pm 1910 2023-03-30 19:16:30Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::MB - DNS MB resource record

=cut

use integer;

use Net::DNS::DomainName;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my ( $self, @argument ) = @_;

	$self->{madname} = Net::DNS::DomainName1035->decode(@argument);
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my ( $self, @argument ) = @_;

	return $self->{madname}->encode(@argument);
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	return $self->{madname}->string;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my ( $self, @argument ) = @_;

	$self->madname(@argument);
	return;
}


sub madname {
	my ( $self, @value ) = @_;
	for (@value) { $self->{madname} = Net::DNS::DomainName1035->new($_) }
	return $self->{madname} ? $self->{madname}->name : undef;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name MB madname');

=head1 DESCRIPTION

Class for DNS Mailbox (MB) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 madname

    $madname = $rr->madname;
    $rr->madname( $madname );

A domain name which specifies a host which has the
specified mailbox.


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
L<RFC1035(3.3.3)|https://tools.ietf.org/html/rfc1035>

=cut
