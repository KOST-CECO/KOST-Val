package FCGI::Client;
use warnings;
use strict;
use 5.008001;

our $VERSION = '0.09';

use Carp;

use FCGI::Client::Connection;
use FCGI::Client::Record;
use FCGI::Client::RecordFactory;

1;
__END__

=head1 NAME

FCGI::Client - client library for fastcgi protocol

=head1 SYNOPSIS

    use FCGI::Client;

    my $sock = IO::Socket::INET->new(
        PeerAddr => '127.0.0.1',
        PeerPort => $port,
    ) or die $!;
    my $client = FCGI::Client::Connection->new( sock => $sock );
    my ( $stdout, $stderr ) = $client->request(
        +{
            REQUEST_METHOD => 'GET',
            QUERY_STRING   => 'foo=bar',
        },
        ''
    );

=head1 DESCRIPTION

FCGI::Client is client library for fastcgi protocol.

=head1 AUTHOR

Tokuhiro Matsuno E<lt>tokuhirom @*(#RJKLFHFSDLJF gmail.comE<gt>

=head1 THANKS TO

peterkeen

=head1 SEE ALSO

L<FCGI>, L<http://www.fastcgi.com/drupal/node/6?q=node/22>

=head1 LICENSE

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut
