package Net::DNS::Mailbox;

use strict;
use warnings;

our $VERSION = (qw$Id: Mailbox.pm 1910 2023-03-30 19:16:30Z willem $)[2];


=head1 NAME

Net::DNS::Mailbox - DNS mailbox representation

=head1 SYNOPSIS

    use Net::DNS::Mailbox;

    $mailbox = Net::DNS::Mailbox->new('user@example.com');
    $address = $mailbox->address;

=head1 DESCRIPTION

The Net::DNS::Mailbox module implements a subclass of DNS domain name
objects representing the DNS coded form of RFC822 mailbox address.

The Net::DNS::Mailbox1035 and Net::DNS::Mailbox2535 packages
implement mailbox representation subtypes which provide the name
compression and canonicalisation specified by RFC1035 and RFC2535.
These are necessary to meet the backward compatibility requirements
introduced by RFC3597.

=cut


use integer;
use Carp;

use base qw(Net::DNS::DomainName);


=head1 METHODS

=head2 new

    $mailbox = Net::DNS::Mailbox->new('John Doe <john.doe@example.com>');
    $mailbox = Net::DNS::Mailbox->new('john.doe@example.com');
    $mailbox = Net::DNS::Mailbox->new('john\.doe.example.com');

Creates a mailbox object representing the RFC822 mail address specified by
the character string argument. An encoded domain name is also accepted for
backward compatibility with Net::DNS 0.68 and earlier.

The argument string consists of printable characters from the 7-bit
ASCII repertoire.

=cut

sub new {
	my $class = shift;
	local $_ = shift;
	croak 'undefined mail address' unless defined $_;

	s/^.*<//g;						# strip excess on left
	s/>.*$//g;						# strip excess on right
	s/^\@.+://;						# strip deprecated source route
	s/\\\./\\046/g;						# disguise escaped dots

	my ( $localpart, @domain ) = split /[@.]([^@;:"]*$)/;	# split on rightmost @
	s/\./\\046/g for $localpart ||= '';			# escape dots in local part

	return bless __PACKAGE__->SUPER::new( join '.', $localpart, @domain ), $class;
}


=head2 address

    $address = $mailbox->address;

Returns a character string containing the RFC822 mailbox address
corresponding to the encoded domain name representation described
in RFC1035 section 8.

=cut

sub address {
	return unless defined wantarray;
	my @label = shift->label;
	local $_ = shift(@label) || return '<>';
	s/\\\\//g;						# delete escaped \
	s/^\\034(.*)\\034$/"$1"/;				# unescape enclosing quotes
	s/\\\d\d\d//g;						# delete non-printable
	s/\\\./\./g;						# unescape dots
	s/\\//g;						# delete escapes
	return $_ unless scalar(@label);
	return join '@', $_, join '.', @label;
}


########################################

package Net::DNS::Mailbox1035;		## no critic ProhibitMultiplePackages
our @ISA = qw(Net::DNS::Mailbox);

sub encode { return &Net::DNS::DomainName1035::encode; }


package Net::DNS::Mailbox2535;		## no critic ProhibitMultiplePackages
our @ISA = qw(Net::DNS::Mailbox);

sub encode { return &Net::DNS::DomainName2535::encode; }


1;
__END__


########################################

=head1 COPYRIGHT

Copyright (c)2009,2012 Dick Franks.

All rights reserved.


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

L<perl> L<Net::DNS> L<Net::DNS::DomainName>
L<RFC1035|https://tools.ietf.org/html/rfc1035>
L<RFC5322|https://tools.ietf.org/html/rfc5322>

=cut

