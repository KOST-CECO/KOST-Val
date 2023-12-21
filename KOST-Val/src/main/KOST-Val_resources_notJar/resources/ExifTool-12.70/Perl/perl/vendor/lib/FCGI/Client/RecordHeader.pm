package FCGI::Client::RecordHeader;
use strict;
use warnings;
use FCGI::Client::Constant;
use Types::Standard qw/Str/;

use Moo;
has raw        => ( is => 'ro', isa => Str );
no Moo;

sub content_length { unpack( 'x4n', $_[0]->raw ) }
sub padding_length { unpack( 'x6C', $_[0]->raw ) }
sub type           { unpack( 'xC',  $_[0]->raw ) }
sub request_id     { unpack( 'xxn', $_[0]->raw ) }

__PACKAGE__->meta->make_immutable;
__END__

=head1 NAME

FCGI::Client::RecordHeader - record header object for FCGI

=head1 SYNOPSIS

    my $header = FCGI::Client::RecordHeader->new(raw => $raw);
    say $header->type;

=head1 DESCRIPTION

This module is record header class for L<FCGI::Client>.

=head1 ATTRIBUTES

=over 4

=item raw

the raw string of record header.
size of record header is defined at L<FCGI::Client::Constant::FCGI_HEADER_LEN>.

=back

=head1 INSTANCE METHOD

=over 4

=item $self->request_id()

=item $self->type()

=item $self->content_length()

=item $self->padding_length()

These methods returns each field of record header.

=back

=head1 SEE ALSO

L<FCGI::Client>

