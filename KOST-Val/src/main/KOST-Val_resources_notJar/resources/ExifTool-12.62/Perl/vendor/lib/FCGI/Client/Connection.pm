package FCGI::Client::Connection;
use strict;
use warnings;
use FCGI::Client::Constant;
use Time::HiRes qw(time);
use List::Util qw(max);
use POSIX qw(EAGAIN);
use FCGI::Client::Record;
use FCGI::Client::RecordFactory;

use Types::Standard qw/Int/;

use Moo;

has sock => (
    is       => 'ro',
    required => 1,
);

has timeout => (
    is => 'rw',
    isa => Int,
    default => 10,
);

no Moo;

sub request {
    my ($self, $env, $content) = @_;
    local $SIG{PIPE} = "IGNORE";
    {
        local $SIG{ALRM} = sub { Carp::confess('REQUEST_TIME_OUT') };
        my $orig_alarm = alarm($self->timeout) || 0;
        $self->_send_request($env, $content);
        my @res = $self->_receive_response($self->sock);
        alarm($orig_alarm);
        return @res;
    }
}

sub _receive_response {
    my ($self, $sock) = @_;
    my ($stdout, $stderr);
    while (my $res = $self->_read_record($self)) {
        my $type = $res->type;
        if ($type == FCGI_STDOUT) {
            $stdout .= $res->content;
        } elsif ($type == FCGI_STDERR) {
            $stderr .= $res->content;
        } elsif ($type == FCGI_END_REQUEST) {
            my $appstatus = unpack('N', $res->content);
            $sock->close();
            return ($stdout, $stderr, $appstatus);
        } else {
            die "unknown response type: " . $res->type;
        }
    }
    die 'connection breaked from server process?';
}
sub _send_request {
    my ($self, $env, $content) = @_;
    my $reqid = 1;
    $self->sock->print(FCGI::Client::RecordFactory->create_request($reqid, $env, $content));
}

sub _read_record {
    my ($self) = @_;
    my $header_raw = '';
    while (length($header_raw) != FCGI_HEADER_LEN) {
        $self->_read_timeout(\$header_raw, FCGI_HEADER_LEN-length($header_raw), length($header_raw)) or return;
    }
    my $header = FCGI::Client::RecordHeader->new(raw => $header_raw);
    my $content_length = $header->content_length;
    my $content = '';
    if ($content_length != 0) {
        while (length($content) != $content_length) {
            $self->_read_timeout(\$content, $content_length-length($content), length($content)) or return;
        }
    }
    my $padding_length = $header->padding_length;
    my $padding = '';
    if ($padding_length != 0) {
        while (length($padding) != $padding_length) {
            $self->_read_timeout(\$padding, $padding_length, 0) or return;
        }
    }
    return FCGI::Client::Record->new(
        header     => $header,
        content    => $content,
    );
}

# returns 1 if socket is ready, undef on timeout
sub _wait_socket {
    my ( $self, $sock, $is_write, $wait_until ) = @_;
    do {
        my $vec = '';
        vec( $vec, $sock->fileno, 1 ) = 1;
        if (
            select(
                $is_write ? undef : $vec,
                $is_write ? $vec  : undef,
                undef,
                max( $wait_until - time, 0 )
            ) > 0
          )
        {
            return 1;
        }
    } while ( time < $wait_until );
    return;
}

# returns (positive) number of bytes read, or undef if the socket is to be closed
sub _read_timeout {
    my ( $self, $buf, $len, $off, ) = @_;
    my $sock = $self->sock;
    my $timeout = $self->timeout;
    my $wait_until = time + $timeout;
    while ( $self->_wait_socket( $sock, undef, $wait_until ) ) {
        if ( my $ret = $sock->sysread( $$buf, $len, $off ) ) {
            return $ret;
        }
        elsif ( !( !defined($ret) && $! == EAGAIN ) ) {
            last;
        }
    }
    return;
}

1;
__END__

=head1 NAME

FCGI::Client::Connection - connection to FastCGI server

=head1 DESCRIPTION

This module handles connection to FastCGI server.

=head1 ATTRIBUTES

=over 4

=item sock

the socket object.

=item timeout

read only integer value, default is 10seconds.

=back

=head1 METHODS

=over 4

=item my ($stdout, $stderr, $appstatus) = $self->request($env, $content)

$env is environment hash, same as CGI.$content is request body string.
This method returns $stdout and $stderr strings.When error got, return undef.
$appstatus is the status code of FastCGI server, this is one of the following code.

    #define FCGI_REQUEST_COMPLETE 0
    #define FCGI_CANT_MPX_CONN    1
    #define FCGI_OVERLOADED       2
    #define FCGI_UNKNOWN_ROLE     3

These constants defined at L<FCGI::Client::Constat>.

=back

=head1 FAQ

=over 4

=item Why don't support FCGI_KEEP_CONN?

FCGI_KEEP_CONN is not used by lighttpd's mod_fastcgi.c, and mod_fast_cgi for apache.
And, FCGI.xs doesn't support it.

I seems FCGI_KEEP_CONN is not used in real world.

=back
