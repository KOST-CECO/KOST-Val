package Net::DNS::Nameserver;

use strict;
use warnings;

our $VERSION = (qw$Id: Nameserver.pm 1925 2023-05-31 11:58:59Z willem $)[2];


=head1 NAME

Net::DNS::Nameserver - DNS server class

=head1 SYNOPSIS

    use Net::DNS::Nameserver;

    my $nameserver = Net::DNS::Nameserver->new(
	LocalAddr	=> ['::1', '127.0.0.1'],
	ZoneFile	=> "filename"
	);

    my $nameserver = Net::DNS::Nameserver->new(
	LocalAddr	=> '10.1.2.3',
	LocalPort	=> 5353,
	ReplyHandler	=> \&reply_handler
    );


=head1 DESCRIPTION

Net::DNS::Nameserver offers a simple mechanism for instantiation of
customised DNS server objects intended to provide test responses to
queries emanating from a client resolver.

It is not, nor will it ever be, a general-purpose DNS nameserver
implementation.

See L</EXAMPLE> for an example.

=cut

use integer;
use Carp;
use Net::DNS;
use Net::DNS::ZoneFile;

use IO::Socket::IP 0.38;
use IO::Select;

use constant USE_POSIX => defined eval 'use POSIX ":sys_wait_h"; 1';	## no critic

use constant DEFAULT_ADDR => qw(::1 127.0.0.1);
use constant DEFAULT_PORT => 5353;

use constant PACKETSZ => 512;


#------------------------------------------------------------------------------
# Constructor.
#------------------------------------------------------------------------------

sub new {
	my ( $class, %self ) = @_;
	my $self = bless \%self, $class;

	$self->ReadZoneFile( $self{ZoneFile} ) if exists $self{ZoneFile};

	croak 'No reply handler!' unless ref( $self{ReplyHandler} ) eq "CODE";

	# local server addresses need to be accepted by a resolver
	my $LocalAddr = $self{LocalAddr} || [DEFAULT_ADDR];
	my $resolver  = Net::DNS::Resolver->new( nameservers => $LocalAddr );
	$resolver->force_v4(1) if $self{Force_IPv4};
	$resolver->force_v6(1) if $self{Force_IPv6};
	$self{LocalAddr} = [$resolver->nameservers];
	$self{LocalPort} ||= DEFAULT_PORT;

	$self{Truncate}	   = 1	 unless defined( $self{Truncate} );
	$self{IdleTimeout} = 120 unless defined( $self{IdleTimeout} );

	return $self;
}


#------------------------------------------------------------------------------
# ReadZoneFile - Read zone file used by default reply handler
#------------------------------------------------------------------------------

sub ReadZoneFile {
	my ( $self, $file ) = @_;
	my $zonefile = Net::DNS::ZoneFile->new($file);

	my $RRhash = $self->{index} = {};
	my $RRlist = [];
	my @zonelist;
	while ( my $rr = $zonefile->read ) {
		push @{$RRhash->{lc $rr->owner}}, $rr;

		# Warning: Nasty trick abusing SOA to reference zone RR list
		if ( $rr->type eq 'SOA' ) {
			$RRlist = $rr->{RRlist} = [];
			push @zonelist, lc $rr->owner;
		} else {
			push @$RRlist, $rr;
		}
	}

	$self->{namelist}     = [sort { length($b) <=> length($a) } keys %$RRhash];
	$self->{zonelist}     = [sort { length($b) <=> length($a) } @zonelist];
	$self->{ReplyHandler} = sub { $self->ReplyHandler(@_) };
	return;
}


#------------------------------------------------------------------------------
# ReplyHandler - Default reply handler serving RRs from zone file
#------------------------------------------------------------------------------

sub ReplyHandler {
	my ( $self, $qname, $qclass, $qtype, $peerhost, $query, $conn ) = @_;
	my $RRhash = $self->{index};
	my $rcode;
	my %headermask;
	my @ans;
	my @auth;

	if ( $qtype eq 'AXFR' ) {
		my $RRlist = $RRhash->{lc $qname} || [];
		my ($soa) = grep { $_->type eq 'SOA' } @$RRlist;
		if ($soa) {
			$rcode = 'NOERROR';
			push @ans, $soa, @{$soa->{RRlist}}, $soa;
		} else {
			$rcode = 'NOTAUTH';
		}
		return ( $rcode, \@ans, [], [], {}, {} );
	}

	my @RRname = @{$self->{namelist}};			# pre-sorted, longest first
	{
		my $RRlist = $RRhash->{lc $qname} || [];	# hash, then linear search
		my @match  = @$RRlist;				# assume $qclass always 'IN'
		if ( scalar(@match) ) {				# exact match
			$rcode = 'NOERROR';
		} elsif ( grep {/\.$qname$/i} @RRname ) {	# empty non-terminal
			$rcode = 'NOERROR';			# [NODATA]
		} else {
			$rcode = 'NXDOMAIN';
			foreach ( grep {/^[*][.]/} @RRname ) {
				my $wildcard = $_;		# match wildcard per RFC4592
				s/^\*//;			# delete leading asterisk
				s/([.?*+])/\\$1/g;		# escape dots and regex quantifiers
				next unless $qname =~ /[.]?([^.]+$_)$/i;
				my $encloser = $1;		# check no ENT encloses qname
				$rcode = 'NOERROR';
				last if grep {/(^|\.)$encloser$/i} @RRname;    # [NODATA]

				my ($q) = $query->question;	# synthesise RR at qname
				foreach my $rr ( @{$RRhash->{$wildcard}} ) {
					my $clone = bless( {%$rr}, ref($rr) );
					$clone->{owner} = $q->{qname};
					push @match, $clone;
				}
				last;
			}
		}
		push @ans, my @cname = grep { $_->type eq 'CNAME' } @match;
		$qname = $_->cname for @cname;
		redo if @cname;
		push @ans, @match if $qtype eq 'ANY';		# traditional, now out of favour
		push @ans, grep { $_->type eq $qtype } @match;
		unless (@ans) {
			foreach ( @{$self->{zonelist}} ) {
				s/([.?*+])/\\$1/g;		# escape dots and regex quantifiers
				next unless $qname =~ /[^.]+[.]$_[.]?$/i;
				push @auth, grep { $_->type eq 'SOA' } @{$RRhash->{$_}};
				last;
			}
		}
		$headermask{aa} = 1;
	}
	return ( $rcode, \@ans, \@auth, [], \%headermask, {} );
}


#------------------------------------------------------------------------------
# make_reply - Make a reply packet.
#------------------------------------------------------------------------------

sub make_reply {
	my ( $self, $query, $sock ) = @_;
	my $verbose = $self->{Verbose};

	unless ($query) {
		my $empty = Net::DNS::Packet->new();		# create empty reply packet
		my $reply = $empty->reply();
		$reply->header->rcode("FORMERR");
		return $reply;
	}

	if ( $query->header->qr() ) {
		print "ERROR: invalid packet (qr set), dropping\n" if $verbose;
		return;
	}

	my $reply  = $query->reply();
	my $header = $reply->header;
	my $headermask;
	my $optionmask;

	my $opcode  = $query->header->opcode;
	my $qdcount = $query->header->qdcount;

	unless ($qdcount) {
		$header->rcode("NOERROR");

	} elsif ( $qdcount > 1 ) {
		$header->rcode("FORMERR");

	} else {
		my ($qr)   = $query->question;
		my $qname  = $qr->qname;
		my $qtype  = $qr->qtype;
		my $qclass = $qr->qclass;

		print $qr->string, "\n" if $verbose;

		my $conn = {
			peerhost => my $peer = $sock->peerhost,
			peerport => $sock->peerport,
			protocol => $sock->protocol,
			sockhost => $sock->sockhost,
			sockport => $sock->sockport
			};

		my ( $rcode, $ans, $auth, $add );
		my @arglist = ( $qname, $qclass, $qtype, $peer, $query, $conn );

		if ( $opcode eq "QUERY" ) {
			( $rcode, $ans, $auth, $add, $headermask, $optionmask ) =
					&{$self->{ReplyHandler}}(@arglist);

		} elsif ( $opcode eq "NOTIFY" ) {		#RFC1996
			if ( ref $self->{NotifyHandler} eq "CODE" ) {
				( $rcode, $ans, $auth, $add, $headermask, $optionmask ) =
						&{$self->{NotifyHandler}}(@arglist);
			} else {
				$rcode = "NOTIMP";
			}

		} elsif ( $opcode eq "UPDATE" ) {		#RFC2136
			if ( ref $self->{UpdateHandler} eq "CODE" ) {
				( $rcode, $ans, $auth, $add, $headermask, $optionmask ) =
						&{$self->{UpdateHandler}}(@arglist);
			} else {
				$rcode = "NOTIMP";
			}

		} else {
			print "ERROR: opcode $opcode unsupported\n" if $verbose;
			$rcode = "FORMERR";
		}

		if ( !defined($rcode) ) {
			print "remaining silent\n" if $verbose;
			return;
		}

		$header->rcode($rcode);

		push @{$reply->{answer}},     @$ans  if $ans;
		push @{$reply->{authority}},  @$auth if $auth;
		push @{$reply->{additional}}, @$add  if $add;
	}

	while ( my ( $key, $value ) = each %{$headermask || {}} ) {
		$header->$key($value);
	}

	while ( my ( $option, $value ) = each %{$optionmask || {}} ) {
		$reply->edns->option( $option, $value );
	}

	$header->print if $verbose && ( $headermask || $optionmask );

	return $reply;
}


#------------------------------------------------------------------------------
# TCP_connection - Handle a TCP connection.
#------------------------------------------------------------------------------

sub TCP_connection {
	my ( $self, $socket ) = @_;
	my $timeout = $self->{IdleTimeout};
	my $verbose = $self->{Verbose};

	while (1) {
		alarm $timeout;
		my $l = '';
		my $n = sysread( $socket, $l, 2 );
		unless ( defined $n ) {
			redo if $!{EINTR};	## retry if aborted by signal
			die "sysread: $!";
		}
		last if $n == 0;
		my $msglen = unpack 'n', $l;

		my $buffer = '';
		while ( $msglen > ( my $l = length $buffer ) ) {
			my $n = sysread( $socket, $buffer, ( $msglen - $l ), $l );
			unless ( defined $n ) {
				redo if $!{EINTR};	## retry if aborted by signal
				die "sysread: $!";
			}
		}

		if ($verbose) {
			my $peer = $socket->peerhost;
			my $port = $socket->peerport;
			my $size = length $buffer;
			print "Received $size octets from [$peer] port $port\n";
		}

		my $query = Net::DNS::Packet->new( \$buffer );
		if ($@) {
			print "Error decoding query packet: $@\n" if $verbose;
			undef $query;	## force FORMERR reply
		}

		my $reply = $self->make_reply( $query, $socket );
		die 'Failed to create reply' unless defined $reply;

		my $segment = $reply->data(65500);		# limit to one TCP envelope
		my $length  = length $segment;
		warn "Multi-packet TCP response not implemented" if $reply->header->tc;
		if ($verbose) {
			print "TCP response (2 + $length octets) - ";
			print $socket->send( pack 'na*', $length, $segment ) ? "sent" : "failed: $!", "\n";
		} else {
			$socket->send( pack 'na*', $length, $segment );
		}
	}
	alarm 0;
	close $socket;
	return;
}


#------------------------------------------------------------------------------
# UDP_connection - Handle a UDP connection.
#------------------------------------------------------------------------------

sub UDP_connection {
	my ( $self, $socket ) = @_;
	my $verbose = $self->{Verbose};
	alarm 3;

	my $buffer = "";
	return unless defined $socket->recv( $buffer, PACKETSZ );

	if ($verbose) {
		my $peer = $socket->peerhost;
		my $port = $socket->peerport;
		my $size = length $buffer;
		print "Received $size octets from [$peer] port $port\n";
	}

	my $query = Net::DNS::Packet->new( \$buffer );
	if ($@) {
		print "Error decoding query packet: $@\n" if $verbose;
		undef $query;		## force FORMERR reply
	}

	my $reply = $self->make_reply( $query, $socket );
	die 'Failed to create reply' unless defined $reply;

	my @UDPsize = ( $query && $self->{Truncate} ) ? $query->edns->UDPsize : ();
	if ($verbose) {
		my $response = $reply->data(@UDPsize);
		print 'UDP response (', length($response), ' octets) - ';
		print $socket->send($response) ? "sent" : "failed: $!", "\n";
	} else {
		$socket->send( $reply->data(@UDPsize) );
	}
	alarm 0;
	close $socket;
	return;
}


#------------------------------------------------------------------------------
# Socket mechanics.
#------------------------------------------------------------------------------

use constant DEBUG => 0;

sub logmsg { return print "$0 $$: @_ at ", scalar localtime(), "\n" }

sub spawn {
	my $coderef = shift;
	confess "usage: spawn CODEREF" unless ref($coderef) eq 'CODE';

	unless ( defined( my $pid = fork() ) ) {
		die "cannot fork: $!";
	} elsif ($pid) {
		logmsg "begat $pid" if DEBUG;
		return $pid;		## parent
	}

	# else ...			## child
	$coderef->();
	exit;
}

sub reaper {
	local $!;			## protect current error
	$SIG{CHLD} = \&reaper;		## no critic	sysV semantics
	while ( ( my $pid = waitpid( -1, USE_POSIX ? WNOHANG : 0 ) ) > 0 ) {
		logmsg "reaped $pid" if DEBUG;
	}
	return;
}


sub start_server {
	my $self = shift;
	my $list = $self->{LocalAddr};
	my $port = $self->{LocalPort};
	my @pid;
	foreach my $ip (@$list) {
		push @pid, spawn( sub { $self->TCP_server( $ip, $port ) } );
		push @pid, spawn( sub { $self->UDP_server( $ip, $port ) } );
	}
	return @pid;
}

sub start_noloop {
	my ( $self, $timeout ) = ( @_, 600 );
	my $list = $self->{LocalAddr};
	my $port = $self->{LocalPort};
	foreach my $ip (@$list) {
		spawn(	sub {
				alarm $timeout;
				$self->TCP_initialise( $ip, $port );
			} );
		spawn(	sub {
				alarm $timeout;
				$self->UDP_initialise( $ip, $port );
			} );
	}
	return;
}


sub TCP_initialise {
	my ( $self, $ip, $port ) = @_;
	my $socket = IO::Socket::IP->new(
		LocalAddr => $ip,
		LocalPort => $port,
		ReuseAddr => 1,
		ReusePort => 1,
		Proto	  => "tcp",
		Listen	  => SOMAXCONN,
		Type	  => SOCK_STREAM
		)
			|| die "can't setup TCP socket";

	logmsg "TCP server [$ip] port $port started";

	{
		my $client = $socket->accept() || do {
			redo if $!{EINTR};	## retry if aborted by signal
			die "accept: $!";
		};

		spawn( sub { $self->TCP_connection($client) } );
	}
	return $socket;
}

sub TCP_server {
	my ( $self, $ip, $port ) = @_;
	my $socket = $self->TCP_initialise( $ip, $port );
	while (1) {
		my $client = $socket->accept() || do {
			redo if $!{EINTR};	## retry if aborted by signal
			die "accept: $!";
		};

		spawn( sub { $self->TCP_connection($client) } );
	}
	exit;
}


sub UDP_initialise {
	my ( $self, $ip, $port ) = @_;
	my $socket = IO::Socket::IP->new(
		LocalAddr => $ip,
		LocalPort => $port,
		ReuseAddr => 1,
		ReusePort => 1,
		Proto	  => "udp",
		Type	  => SOCK_DGRAM
		)
			|| die "can't setup UDP socket";

	logmsg "UDP server [$ip] port $port started";

	my $select = IO::Select->new($socket);
	{
		local $! = 0;
		scalar( my @ready = $select->can_read() ) || do {
			redo if $!{EINTR};	## retry if aborted by signal
			die "select->can_read(): $!";
		};

		foreach my $client (@ready) {
			spawn( sub { $self->UDP_connection($client) } );
		}
	}
	return $socket;
}

sub UDP_server {
	my ( $self, $ip, $port ) = @_;
	my $socket = $self->UDP_initialise( $ip, $port );
	my $select = IO::Select->new($socket);
	while (1) {
		local $! = 0;
		scalar( my @ready = $select->can_read() ) || do {
			redo if $!{EINTR};	## retry if aborted by signal
			die "select->can_read(): $!";
		};

		foreach my $client (@ready) {
			spawn( sub { $self->UDP_connection($client) } );
		}
	}
	exit;
}


#------------------------------------------------------------------------------
# main_loop - Start nameserver loop.
#------------------------------------------------------------------------------

sub main_loop {
	local $SIG{CHLD} = \&reaper;
	my @pid = shift->start_server;
	local $SIG{TERM} = sub { kill( 'TERM', @pid ) };
	1 while waitpid( -1, 0 ) > 0;
	logmsg "@pid terminated";
	return;
}


#------------------------------------------------------------------------------
# loop_once - Start single-transaction nameserver
#------------------------------------------------------------------------------

sub loop_once {
	my ( $self, @timeout ) = @_;
	local $SIG{CHLD} = \&reaper;
	$self->start_noloop(@timeout);
	1 while waitpid( -1, 0 ) > 0;	## park main process until timeout or
	return;				## user CTRL_C kills remaining children
}


1;
__END__


=head1 METHODS

=head2 new

    $nameserver = Net::DNS::Nameserver->new(
	LocalAddr	=> ['::1', '127.0.0.1'],
	ZoneFile	=> "filename"
	);

    $nameserver = Net::DNS::Nameserver->new(
	LocalAddr	=> '10.1.2.3',
	LocalPort	=> 5353,
	ReplyHandler	=> \&reply_handler,
	Verbose		=> 1,
	Truncate	=> 0
    );

Instantiates a Net::DNS::Nameserver object.
An exception is raised if the object could not be created.

Each instance is configured using the following optional arguments:

    LocalAddr		IP address on which to listen	Defaults to loopback address
    LocalPort		Port on which to listen		Defaults to 5353
    ZoneFile		Name of file containing RRs
			accessed using the internal
			reply-handling subroutine
    ReplyHandler	Reference to customised
			reply-handling subroutine
    NotifyHandler	Reference to reply-handling
			subroutine for queries with
			opcode NOTIFY (RFC1996)
    UpdateHandler	Reference to reply-handling
			subroutine for queries with
			opcode UPDATE (RFC2136)
    Verbose		Report internal activity	Defaults to 0 (off)
    Truncate		Truncates UDP packets that
			are too big for the reply	Defaults to 1 (on)
    IdleTimeout		TCP clients are disconnected
			if they are idle longer than
			this duration			Defaults to 120 (secs)

The LocalAddr attribute may alternatively be specified as an array
of IP addresses to listen to.

The ReplyHandler subroutine is passed the query name, query class,
query type, peerhost, query record, and connection descriptor.
It must either return the response code and references to the answer,
authority, and additional sections of the response, or undef to leave
the query unanswered.  Common response codes are:

    NOERROR	No error
    FORMERR	Format error
    SERVFAIL	Server failure
    NXDOMAIN	Non-existent domain (name doesn't exist)
    NOTIMP	Not implemented
    REFUSED	Query refused

For advanced usage it may also contain a headermask containing an
hashref with the settings for the C<aa>, C<ra>, and C<ad>
header bits. The argument is of the form:
	{ad => 1, aa => 0, ra => 1}

EDNS options may be specified in a similar manner using the optionmask:
	{$optioncode => $value, $optionname => $value}

See RFC1035 and IANA DNS parameters file for more information:


The nameserver will listen for both UDP and TCP connections.
On Unix-like systems, unprivileged users are denied access to ports below 1024.

UDP reply truncation functionality was introduced in VERSION 830.
The size limit is determined by the EDNS0 size advertised in the query,
otherwise 512 is used.
If you want to do packet truncation yourself you should set C<Truncate>
to 0 and truncate the reply packet in the code of the ReplyHandler.

See L</EXAMPLE> for an example.


=head2 main_loop

    $ns->main_loop;

Start accepting queries. Calling main_loop never returns.


=head2 loop_once

    $ns->loop_once( [TIMEOUT_IN_SECONDS] );

Initialises the specified UDP and TCP sockets and starts the server
which will respond to a single connection on each socket.

The timeout parameter specifies the maximum time to wait for a request.
If called without parameters a default timeout of 10 minutes is applied.
If called with a zero parameter, the timeout function is disabled.


=head1 EXAMPLE

The following example will listen on port 5353 and respond to all queries
for A records with the IP address 10.1.2.3.  All other queries will be
answered with NXDOMAIN.	 Authority and additional sections are left empty.
The $peerhost variable catches the IP address of the peer host, so that
additional filtering on a per-host basis may be applied.

    #!/usr/bin/perl -T

    use strict;
    use warnings;
    use Net::DNS::Nameserver;

    sub reply_handler {
	my ( $qname, $qclass, $qtype, $peerhost, $query, $conn ) = @_;
	my ( $rcode, @ans, @auth, @add );

	print "Received query from $peerhost to " . $conn->{sockhost} . "\n";
	$query->print;

	if ( $qtype eq "A" && $qname eq "foo.example.com" ) {
		my ( $ttl, $rdata ) = ( 3600, "10.1.2.3" );
		my $rr = Net::DNS::RR->new("$qname $ttl $qclass $qtype $rdata");
		push @ans, $rr;
		$rcode = "NOERROR";
	} elsif ( $qname eq "foo.example.com" ) {
		$rcode = "NOERROR";

	} else {
		$rcode = "NXDOMAIN";
	}

	# mark the answer as authoritative (by setting the 'aa' flag)
	my $headermask = {aa => 1};

	# specify EDNS options	{ option => value }
	my $optionmask = {};

	return ( $rcode, \@ans, \@auth, \@add, $headermask, $optionmask );
    }


    my $ns = Net::DNS::Nameserver->new(
	LocalPort    => 5353,
	ReplyHandler => \&reply_handler,
	Verbose	     => 1
	) || die "couldn't create nameserver object\n";


    $ns->main_loop;

    exit;


=head1 BUGS

Limitations in perl make it impossible to guarantee that replies to
UDP queries from Net::DNS::Nameserver are sent from the IP-address
to which the query was directed.  This is a problem for machines with
multiple IP-addresses and causes violation of RFC2181 section 4.
Thus a UDP socket created listening to INADDR_ANY (all available
IP-addresses) will reply not necessarily with the source address being
the one to which the request was sent, but rather with the address that
the operating system chooses. This is also often called "the closest
address". This should really only be a problem on a server which has
more than one IP-address (besides localhost - any experience with IPv6
complications here, would be nice). If this is a problem for you, a
work-around would be to not listen to INADDR_ANY but to specify each
address that you want this module to listen on. A separate set of
sockets will then be created for each IP-address.


=head1 COPYRIGHT

Copyright (c)2000 Michael Fuhr.

Portions Copyright (c)2002-2004 Chris Reinhardt.

Portions Copyright (c)2005 Robert Martin-Legene.

Portions Copyright (c)2005-2009 O.M.Kolkman, RIPE NCC.

Portions Copyright (c)2017,2023 R.W.Franks.

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

L<perl> L<Net::DNS> L<Net::DNS::Resolver> L<Net::DNS::Packet>
L<Net::DNS::Update> L<Net::DNS::Header> L<Net::DNS::Question>
L<Net::DNS::RR>

L<RFC1035|https://tools.ietf.org/html/rfc1035>

L<IANA DNS parameters|http://www.iana.org/assignments/dns-parameters>

=cut

__END__

