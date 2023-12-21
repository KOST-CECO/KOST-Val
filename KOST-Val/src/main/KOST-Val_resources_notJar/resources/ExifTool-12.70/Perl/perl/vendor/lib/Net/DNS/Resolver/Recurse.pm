package Net::DNS::Resolver::Recurse;

use strict;
use warnings;
our $VERSION = (qw$Id: Recurse.pm 1896 2023-01-30 12:59:25Z willem $)[2];


=head1 NAME

Net::DNS::Resolver::Recurse - DNS recursive resolver


=head1 SYNOPSIS

    use Net::DNS::Resolver::Recurse;

    my $resolver = new Net::DNS::Resolver::Recurse();
    $resolver->debug(1);

    $resolver->hints('198.41.0.4');	# A.ROOT-SERVER.NET.

    my $packet = $resolver->send( 'www.rob.com.au.', 'A' );


=head1 DESCRIPTION

This module is a subclass of Net::DNS::Resolver.

=cut


use base qw(Net::DNS::Resolver);


=head1 METHODS

This module inherits almost all the methods from Net::DNS::Resolver.
Additional module-specific methods are described below.


=head2 hints

This method specifies a list of the IP addresses of nameservers to
be used to discover the addresses of the root nameservers.

    $resolver->hints(@ip);

If no hints are passed, the priming query is directed to nameservers
drawn from a built-in list of IP addresses.

=cut

my @hints;
my $root = [];

sub hints {
	my ( undef, @argument ) = @_;
	return @hints unless scalar @argument;
	$root  = [];
	@hints = @argument;
	return;
}


=head2 query, search, send

The query(), search() and send() methods produce the same result
as their counterparts in Net::DNS::Resolver.

    $packet = $resolver->send( 'www.example.com.', 'A' );

Server-side recursion is suppressed by clearing the recurse flag in
query packets and recursive name resolution is performed explicitly.

The query() and search() methods are inherited from Net::DNS::Resolver
and invoke send() indirectly.

=cut

sub send {
	my ( $self, @q ) = @_;
	my @conf = ( recurse => 0, udppacketsize => 1024 );	# RFC8109
	return bless( {persistent => {'.' => $root}, %$self, @conf}, ref($self) )->_send(@q);
}


sub query_dorecursion {			## historical
	my ($self) = @_;					# uncoverable pod
	$self->_deprecate('prefer  $resolver->send(...)');
	return &send;
}


sub _send {
	my ( $self, @q ) = @_;
	my $query = $self->_make_query_packet(@q);

	unless ( scalar(@$root) ) {
		$self->_diag("resolver priming query");
		$self->nameservers( scalar(@hints) ? @hints : $self->_hints );
		my $packet = $self->SUPER::send(qw(. NS));
		$self->_callback($packet);
		$self->_referral($packet);
		$root = $self->{persistent}->{'.'};
	}

	return $self->_recurse( $query, '.' );
}


sub _recurse {
	my ( $self, $query, $apex ) = @_;
	$self->_diag("using cached nameservers for $apex");
	my $nslist = $self->{persistent}->{$apex};
	$self->nameservers(@$nslist);
	$query->header->id(undef);
	my $reply = $self->SUPER::send($query);
	$self->_callback($reply);
	return unless $reply;
	my $qname = lc( ( $query->question )[0]->qname );
	my $zone  = $self->_referral($reply) || return $reply;
	return $reply if grep { lc( $_->owner ) eq $qname } $reply->answer;
	return $self->_recurse( $query, $zone );
}


sub _referral {
	my ( $self, $packet ) = @_;
	return unless $packet;
	my @auth = grep { $_->type eq 'NS' } $packet->answer, $packet->authority;
	return unless scalar(@auth);
	my $owner = lc( $auth[0]->owner );
	my $cache = $self->{persistent}->{$owner};
	return $owner if $cache && scalar(@$cache);
	my @addr = grep { $_->can('address') } $packet->additional;
	my @ip;
	my @ns = map { lc( $_->nsdname ) } @auth;

	foreach my $ns (@ns) {
		push @ip, map { $_->address } grep { $ns eq lc( $_->owner ) } @addr;
	}
	$self->_diag("resolving glue for $owner")   unless scalar(@ip);
	@ip = $self->nameservers( $ns[0], $ns[-1] ) unless scalar(@ip);
	$self->_diag("caching nameservers for $owner");
	$self->{persistent}->{$owner} = \@ip;
	return $owner;
}


=head2 callback

This method specifies a code reference to a subroutine,
which is then invoked at each stage of the recursive lookup.

For example to emulate dig's C<+trace> function:

    my $coderef = sub {
	my $packet = shift;

	printf ";; Received %d bytes from %s\n\n",
		$packet->answersize, $packet->answerfrom;
    };

    $resolver->callback($coderef);

The callback subroutine is not called
for queries for missing glue records.

=cut

sub callback {
	my ( $self, @argument ) = @_;
	for ( grep { ref($_) eq 'CODE' } @argument ) {
		$self->{callback} = $_;
	}
	return;
}

sub _callback {
	my ( $self, @argument ) = @_;
	my $callback = $self->{callback};
	$callback->(@argument) if $callback;
	return;
}

sub recursion_callback {		## historical
	my ($self) = @_;					# uncoverable pod
	$self->_deprecate('prefer  $resolver->callback(...)');
	&callback;
	return;
}


1;

__END__


=head1 ACKNOWLEDGEMENT

This package is an improved and compatible reimplementation of the
Net::DNS::Resolver::Recurse.pm created by Rob Brown in 2002,
whose contribution is gratefully acknowledged.


=head1 COPYRIGHT

Copyright (c)2014,2019 Dick Franks.

Portions Copyright (c)2002 Rob Brown.

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

L<Net::DNS::Resolver>

=cut

