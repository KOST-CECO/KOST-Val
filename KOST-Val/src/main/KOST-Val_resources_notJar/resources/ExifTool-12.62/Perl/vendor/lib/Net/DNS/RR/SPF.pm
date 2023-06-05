package Net::DNS::RR::SPF;

use strict;
use warnings;
our $VERSION = (qw$Id: SPF.pm 1814 2020-10-14 21:49:16Z willem $)[2];

use base qw(Net::DNS::RR::TXT);


=head1 NAME

Net::DNS::RR::SPF - DNS SPF resource record

=cut

use integer;


sub spfdata {
	my @spf = shift->char_str_list(@_);
	return wantarray ? @spf : join '', @spf;
}

sub txtdata { return &spfdata; }


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name SPF spfdata ...');

    $rr = Net::DNS::RR->new( name    => 'name',
			    type    => 'SPF',
			    spfdata => 'single text string'
			    );

    $rr = Net::DNS::RR->new( name    => 'name',
			    type    => 'SPF',
			    spfdata => [ 'multiple', 'strings', ... ]
			    );

=head1 DESCRIPTION

Class for DNS Sender Policy Framework (SPF) resource records.

SPF records inherit most of the properties of the Net::DNS::RR::TXT
class.

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 spfdata

=head2 txtdata

    $string = $rr->spfdata;
    @list   = $rr->spfdata;

    $rr->spfdata( @list );

When invoked in scalar context, spfdata() returns the policy text as
a single string, with text elements concatenated without intervening
spaces.

In a list context, spfdata() returns a list of the text elements.


=head1 COPYRIGHT

Copyright (c)2005 Olaf Kolkman, NLnet Labs.

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

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, L<Net::DNS::RR::TXT>, RFC7208

=cut
