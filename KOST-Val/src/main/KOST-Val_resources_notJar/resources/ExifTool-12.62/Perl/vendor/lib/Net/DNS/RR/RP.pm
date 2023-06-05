package Net::DNS::RR::RP;

use strict;
use warnings;
our $VERSION = (qw$Id: RP.pm 1814 2020-10-14 21:49:16Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::RP - DNS RP resource record

=cut

use integer;

use Net::DNS::DomainName;
use Net::DNS::Mailbox;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my $self = shift;
	my ( $data, $offset, @opaque ) = @_;

	( $self->{mbox}, $offset ) = Net::DNS::Mailbox2535->decode( $data, $offset, @opaque );
	$self->{txtdname} = Net::DNS::DomainName2535->decode( $data, $offset, @opaque );
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;
	my ( $offset, @opaque ) = @_;

	my $txtdname = $self->{txtdname};
	my $rdata    = $self->{mbox}->encode( $offset, @opaque );
	$rdata .= $txtdname->encode( $offset + length($rdata), @opaque );
	return $rdata;
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my @rdata = ( $self->{mbox}->string, $self->{txtdname}->string );
	return @rdata;
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my $self = shift;

	$self->mbox(shift);
	$self->txtdname(shift);
	return;
}


sub mbox {
	my $self = shift;

	$self->{mbox} = Net::DNS::Mailbox2535->new(shift) if scalar @_;
	return $self->{mbox} ? $self->{mbox}->address : undef;
}


sub txtdname {
	my $self = shift;

	$self->{txtdname} = Net::DNS::DomainName2535->new(shift) if scalar @_;
	return $self->{txtdname} ? $self->{txtdname}->name : undef;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name RP mbox txtdname');

=head1 DESCRIPTION

Class for DNS Responsible Person (RP) resource records.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 mbox

    $mbox = $rr->mbox;
    $rr->mbox( $mbox );

A domain name which specifies the mailbox for the person responsible for
this domain. The format in master files uses the DNS encoding convention
for mailboxes, identical to that used for the RNAME mailbox field in the
SOA RR. The root domain name (just ".") may be specified to indicate that
no mailbox is available.

=head2 txtdname

    $txtdname = $rr->txtdname;
    $rr->txtdname( $txtdname );

A domain name identifying TXT RRs. A subsequent query can be performed to
retrieve the associated TXT records. This provides a level of indirection
so that the entity can be referred to from multiple places in the DNS. The
root domain name (just ".") may be specified to indicate that there is no
associated TXT RR.


=head1 COPYRIGHT

Copyright (c)1997 Michael Fuhr. 

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

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, RFC1183 Section 2.2

=cut
