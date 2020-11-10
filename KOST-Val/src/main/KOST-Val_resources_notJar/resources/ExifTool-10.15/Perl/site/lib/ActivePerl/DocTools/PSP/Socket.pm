
#
# An Simple Web server Socket.
#
# Contributed by John Holdsworth (c) 2001.
# http://www.openpsp.org
#
# This code is distriubuted under the 
# "Artistic" license a copy of which 
# is distributed with perl.
#


my $ID = q$Id: Socket.pm,v 3.49 2002/01/19 21:37:02 johnh Exp $;

require 5.004;
package ActivePerl::DocTools::PSP::Socket;
use base qw(IO::Socket::INET);

use vars qw($ID $config $log %env %roots %hosts %users %times);
use Sys::Hostname;
use IO::Socket;
use Symbol;
use strict;

my $nuser;

$config = {
    # base directory of server for @INC and DOCUMENT_ROOT
    root => (__FILE__ =~ m@(.*)/PSP/Socket.pm@)[0] || ".",

};

sub newListener {
    # String, String, int
    my $pkg = shift;
    my $socket = $pkg->SUPER::new( Listen=>5, Reuse=>1,
				   Type=>SOCK_STREAM, Proto=>6,
#				   @_ ? @_ : @_ = (LocalPort=>9090)
				   ) or die "@_ not available as '$!'";
    return $socket->initenv();
}

sub initenv {
    # PSP::Socket [, PSP::Socket]
    my $listener = $_[1] || $_[0];

    # set up static environment variables
    if ( !$ENV{SERVER_PORT} ) {
	@ENV{map "SERVER_$_", qw(SOFTWARE PROTOCOL NAME PORT)} = 
	    ( $ID, "HTTP/1.0", hostname(), $listener->sockport );
	$ENV{GATEWAY_INTERFACE} = "CGI/1.0";
    }

    return $listener;
}

sub run {
    # PSP::Socket, PSP::State, PSP::Env
    my $listener = shift;
    $listener = $listener->new( LocalPort=>9090 ) if !ref $listener;
    $_[0] ||= {listener=>$listener, sessions=>{}};

    $SIG{PIPE} = (sub { warn "** Write to closed socket" }) if !$SIG{PIPE};

    while ( fileno $listener ) {
	$listener->poll( @_ );
    }

    warn "*** Server exiting *** Last known error: '$!'";
}

sub poll {
    # PSP::Socket, PSP::State, PSP::Env
    my $listener = shift;
    my ($client, $from) = $listener->accept( ref $listener );
    $client->accepted( $from, @_ ) if $client;
}

sub accepted {
    # PSP::Socket, Bytes[], PSP::State, PSP::Env
    my ($client, $from) = (shift, shift);
    my $stdin = $client->divert( $client->fileno() );
    map $_="", @env{keys %env};

    # parse header from browser into %env
    $client->parse_request( $stdin => \%env, $from ) || return;

    # these are required for CGI.pm to be re-entrant
    CGI->_reset_globals();  # reset parameters
    CGI->nph( 1 );  # Non parsed headers

    my $delta = time() - $times{$env{REMOTE_HANDLE}};
    my @log = @env{qw(REMOTE_HANDLE REQUEST_METHOD HTTP_HOST REQUEST_URI)};
    warn "@log +$delta\n" if $log && $env{REQUEST_URI} !~ /^\/default.ida/;

    @ENV{keys %env} = map $_ || "", values %env;
    eval { $client->main( @_, bless \%ENV, 'PSP::Env' ) };
    $client->error() if $@;
}

sub error {
    # PSP::Socket [, String]
    my $error = $_[1] || $@;
    warn "Error processing url: '$ENV{REQUEST_URI}':\n$error";

    # perl errors are also sent to client
    print <<ERROR;
HTTP/1.0 200 OK
Content-type: text/html

<plaintext>
*** Error processing url: '$ENV{REQUEST_URI}':
$error
ERROR
}

sub main {
    # PSP::Socket, PSP::State, PSP::Env
    return main::main( @_ ) if "main"->can( "main" );

    $_[0]->error( "A subclass of package PSP::Socket must implement main()" );
}

sub parse_request {
    # PSP::Socket, PSP::Socket, PSP::Env, Bytes[]
    my ($client, $stdin, $env, $from) = @_;	
    my $request = $stdin->getline() || return;

    # standard workaround to avoid any blank line left over after a POST
    $request = $stdin->getline() || return if $request =~ /^\r?\n/;

    # remote client can be relayed from parent server
    if ( $request =~ /^FROM 0x(\S*)/ ) {
	$from = pack "h*", $1;
	$request = $stdin->getline();
    }

    # warn "Request is: $request";
    if ( @$env{qw(REQUEST_METHOD PROXY_REQUEST PROXY_HOST PROXY_PORT
		  REQUEST_URI SCRIPT_NAME PATH_INFO
		  DOCUMENT_ZIPPED DOCUMENT_DIR DOCUMENT_REST DOCUMENT_EXT
		  QUERY_STRING REQUEST_PROTOCOL)} = 
	 $request =~ m@^(\S+)\s  # REQUEST_METHOD (GET or POST etc.)
	 (\w+://(?:([^/:]+)(?::(\d+))?))? # PROXY_REQUEST (HOST, PORT)
	 (() # REQUEST_URI + empty initial SCRIPT_NAME + GZIP flag
	  ((/gzip)?/([^./?\s]+(?=/|\s|$))? # DOCUMENT_DIR (first part of path)
	  ([^.?\s]*(?:\.+([^?\.\s]+))*)) # PATH_INFO and DOCUMENT_EXT
	  (?:\?(\S*))? # QUERY_STRING (URI after any "?")
	  )\s+(\S+)? # REQUEST_PROTOCOL (if present, header lines follow)
	 @xs ) {

	# HTTP>=1.0 has header of "name: value\r\n" pairs then a blank line
	if ( $$env{REQUEST_PROTOCOL} ) { 

	    while ( my ($name, $value) =
		    $stdin->getline() =~ /^([^:]*):? ([^\r\n]*)/ ) {

		warn "$name: '$value'"
		    if $log && $log > 1 && $name =~ /Cookie/i;

		# convert to upper case and replace "-" with "_"
		$name =~ tr/-a-z/_A-Z/;

		# prefix names with "HTTP_" unless they start with "CONTENT_"
		$name = "HTTP_$name" if $name !~ /^CONTENT_/;
		$$env{$name} = $value;
	    }
	}

	# resolve address into hostname of remote client
	# log characterisation and assign user handle
	$client->characterise( $from, $env ) if $from;

	# unescape any naughty characters in document path
	$$env{PATH_INFO} =~ s/%(\w{2})/pack "c", hex $1/ge;

	# determine root for this server
	$client->document_root( $env );

	# use host reported by browser as host name for urls()
	($$env{SERVER_NAME} = $$env{HTTP_HOST}) =~ s/:.*//g;
	return $$env{PATH_INFO};
    }

    warn "Unable to parse: $request";
    return;
}

sub characterise {
    my ($client, $from, $env) = @_;
    ($$env{REMOTE_PORT}, my $addr) = unpack_sockaddr_in( $from );
    my $dots = join '.', unpack 'C*', $addr;

    $$env{REMOTE_HOST} = $hosts{$addr} ||= 
	gethostbyaddr( $addr, AF_INET ) || $dots;
    $$env{HTTP_HOST} ||= "unknown";

    # users are refered to by their "handles" in the server logs
    $$env{REMOTE_HANDLE} = $users{
	my $hndl = "@$env{qw(REMOTE_HOST HTTP_HOST HTTP_USER_AGENT)}"};
    if ( !$$env{REMOTE_HANDLE} ) {
	$$env{REMOTE_HANDLE} = $users{$hndl} = "user".++$nuser;
	$times{$$env{REMOTE_HANDLE}} = time();
	$$env{USER_AGENT} ||= "unknown";

	warn $$env{REQUEST_URI} !~ /^\/defaut.ida/ ?
	    <<NEW_USER : "red alert $$env{REMOTE_HOST} ($$env{REMOTE_HANDLE}";
** New user: $$env{REMOTE_HANDLE} - @{[scalar localtime]} - protocol: $$env{REQUEST_PROTOCOL}
  Browser: $$env{HTTP_USER_AGENT}
  Referer: @{[$$env{HTTP_REFERER}||""]}
  Machine: $$env{REMOTE_HOST}
  Address: $dots

NEW_USER
    }
}

sub document_root {
    my ($client, $env) = @_;
    my $host ||= $$env{HTTP_HOST} ||= hostname();
    return $roots{$host} if exists $roots{$host};

    my $root = $config->{root};
    my $port = $host =~ s/:(\d+)$// ? $1 : 80;
    my $name = $client->sockname();
    my $addr = $name? join '.',unpack 'C*',(unpack_sockaddr_in $name)[1]:$host;

    # Simple name based virtual hosting is implemented here
    # Different sets of documents can be selected on the basis
    # of which name the server is refered to at the client.
    # This is passed through as the "Host: " header parameter.
    $$env{DOCUMENT_ROOT} = (grep -d $_, map "$root/$_", 
	    "$addr:$port", $addr, # virtual hosting by IP address
	    "$host:$port", $host, # virtual hosting by Host: from header
	    "docs:$port", "docs", "default")[0];
    $$env{PATH_TRANSLATED} = $$env{DOCUMENT_ROOT}.$$env{PATH_INFO};
    return $$env{DOCUMENT_ROOT};
}

sub params {
    # read in any POST input POST and convert into method "GET"
    # in case a package CGI instance is created later
    if ( $ENV{REQUEST_METHOD} eq "POST" ) {
	read STDIN, $ENV{QUERY_STRING}, $ENV{CONTENT_LENGTH};
	$ENV{REQUST_METHOD} = "GET";
    }

    # split the QUERY_STRING in the format name1=value2&name2=name2
    # into a hash escaping "+" to " " and "%nn" back to ascii char
    map $_ =~ s/%(\w{2})|\+/$1 ? pack "c", hex $1 : " "/ge,
    my @pairs = split /=|&/, $ENV{QUERY_STRING};
    return wantarray() ? @pairs : {@pairs};
}

sub cookies {
    # split header string Cookie: name1=value1; name2=value2 into hash
    return {map {
	map $_ =~ s/%(\w{2})/pack "c", hex $1/ge,
	my ( $key, @values ) = split /=|&/, $_;
	$key, \@values;
    } split "; ", $ENV{HTTP_COOKIE}};
}

sub authenticate {
    # PSP::Socket, String
    my ($client, $realm, $stdout, $env) = @_;
    $env ||= \%ENV;

    if ( (delete $$env{HTTP_AUTHORIZATION} or "") =~ /Basic ([^;]*)/ ) {
	(my $str = $1) =~ tr@A-Za-z0-9+=/@@cd;  # remove non-base64 chars
	warn "Length of base64 data not a multiple of 4" if (length($str) % 4);

	$str =~ s/=+$//;                        # remove padding
	$str =~ tr@A-Za-z0-9+/@ -_@;            # convert to uuencoded format

	return join "", map {
	    unpack("u", chr(32 + length($_)*3/4) . $_ );    # uudecode
	} $str =~ /(.{1,60})/gs;
    }
    else {
	$stdout ||= \*STDOUT;
	print $stdout <<AUTH_REQUIRED;
HTTP/1.0 401 Authentification required
WWW-Authenticate: Basic realm="$realm"

AUTH_REQUIRED
	return ();
    }
}

sub divert {
    # PSP::Socket, int
    my ($client, $fileno) = @_;

    # redirect STDIN and STDOUT as a CGI script would expect
    open STDIN,  "<& $fileno" or die "dup STDIN  => $fileno '$!'";
    open STDOUT, ">& $fileno" or die "dup STDOUT => $fileno '$!'";

    select STDOUT; $| = 1;
    return bless \*STDIN, 'IO::Handle';
}

sub DESTROY {
    # reopen these to close them and keep them out of dups way
    my $log = fileno main::STDLOG ? "main::STDLOG" : "STDERR";
    open STDIN,  ">& $log" or die "dup STDIN ->CONSOLE '$!'";
    open STDERR, ">& $log" or die "dup STDERR->CONSOLE '$!'"
	if fileno main::STDLOG;

    close STDOUT; # reaps any gzips
    open STDOUT, ">& $log" or die "dup STDOUT->CONSOLE '$!'";
    select STDOUT; $| = 1;

    # warn "   DESTROYED @_" if $debug;
    return shift->SUPER::DESTROY();
}

1;

__END__

=head1 NAME

PSP::Socket - Socket to process Web requests.

=head1 SYNOPSIS

Subclass of IO::Socket::INET to simulate CGI interface without a fork().
A web application can subclass this package and request will be served
by the main() method of that class. This method can be coded as you would
a noraml CGI script. A script "../exmaple.pl" is provided with this
distribution demonstrates this with a simple implementation of "mastermind".

=head1 DESCRIPTION

This class accepts connections from browser clients on the specified
LocalPort and reqdirects STDIN and STDOUT to the client to allow
the request to be precessed without a fork in a manner compatabile
with the Apache "CGI" interface. Requests are expected to be processed
by overriding the "main()" method of a subclass of this package.

=head2 METHODS

=head3 $listener = PSP::Socket->new( LocalPort=>9090 );

Create a new instance of a Web server for accepting and serving connections
from browser clients. This will generally be called for a subclass of 
PSP::Socket

=head3 $listener->initenv();

Setup unchanging environment varaiable which are part of the CGI interface
which identify the server software, port number etc.

=head3 $listener->run();

Run the Web serverm polling for new connections, accepting them, parsing
their requests and serveing them by calling the main() method of listeners
package.

=head3 $listener->poll();

Poll for a connection from a browser by calling "accept()". This would
generally block unless the socket has been fcntled to be O_NONBLOCK
or the socket was opened with a zero (or near zero to work around a
restriction in IO::Socket) timeout. Calls "accepted()" on the client
socket returned for it to be processed.

=head3 $client->accepted( $from, ... );

A new connection has arrived from a client and should be processed.
The request header is parsed by "parse_request()" and STDIN/STDOUT
redirected to be connected directly to the client browser (so called
"NPH" or non-parsed-header operation). Method main is then called on
the socket to process the request.

=head3 $client->error( ... );

Create a Web page reporting an server error to the client browser.

=head3 $client->main( ... );

This is the method which should actually process the request and
should be overidden in a subclass of thispackage to do something useful.

=head3 $client->parse_request();

Parse the incomming header from the browser client and setup the
environment varaiables specified in the CGI "common gateway interface"
to allow modules such as "CGI.pm" to operate as the would from
a CGI script.

=head3 $client->characterise( $from $env );

As a new user connects, record any refering page and the browser type.
This is used to determine which browsers to support.

=head3 $client->document_root( $env );

Determine the root directory for documents for the server that the client
connected to. A Web servers host can be connected to using a number of 
different names and this information is recorded in the "Host:" request
attribute sent from the client browser. This can be used to switch between
a number document driectories to create a "Virtual hosts". Create a driectory
with the name of the vritual host and it will be used for that web site.

=head3 $client->params();

An implementation of parameter parsing to demystify it a little
but in general it is best to use CGI.pm.

=head3 $client->cookies();

Returns a hash of cookies again to show how it is done.

=head3 $client->authenticate( $realm );

The last utility routine which requires the user to login to a page for
which this function is called. The resulting login is unencoded and
returned to the callee. If this function returns an empty array a
"401 authentification required" header has been sent to the browser
to popup a login panel for the "realm" and thepage must retry.

=head3 $client->divert();

connects STDIN and STDOUT to the socket connected to the client browser
so that output sent using "print()" will find its way to the browser.

=head3 $client->DESTROY();

called when the client socket closes to unrediect STDIN and STDOUT
so the the connect to the client is closed correctly.

