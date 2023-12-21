package HTML::Form::ImageInput;

use strict;
use parent 'HTML::Form::SubmitInput';

our $VERSION = '6.11';

# ABSTRACT: An HTML form image input element for use with HTML::Form

sub form_name_value {
    my $self    = shift;
    my $clicked = $self->{clicked};
    return unless $clicked;
    return if $self->{disabled};
    my $name = $self->{name};
    $name = ( defined($name) && length($name) ) ? "$name." : "";
    return (
        "${name}x" => $clicked->[0],
        "${name}y" => $clicked->[1]
    );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::ImageInput - An HTML form image input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
