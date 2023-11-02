package Moose::Exception::InvalidNameForType;
our $VERSION = '2.2203';

use Moose;
extends 'Moose::Exception';

has 'name' => (
    is       => 'ro',
    isa      => 'Str',
    required => 1,
);

sub _build_message {
    my $self = shift;
    $self->name." contains invalid characters for a type name. Names can contain alphanumeric characters, ':', and '.'";
}
1;
