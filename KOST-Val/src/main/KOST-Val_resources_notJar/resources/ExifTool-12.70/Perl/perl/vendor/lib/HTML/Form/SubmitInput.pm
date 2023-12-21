package HTML::Form::SubmitInput;

use strict;
use parent 'HTML::Form::Input';

our $VERSION = '6.11';

# ABSTRACT: An HTML form submit input element for use with HTML::Form

#input/image
#input/submit

sub click {
    my ( $self, $form, $x, $y ) = @_;
    for ( $x, $y ) { $_ = 1 unless defined; }
    local ( $self->{clicked} ) = [ $x, $y ];
    local ( $self->{value} )   = "" unless defined $self->value;
    return $form->make_request;
}

sub form_name_value {
    my $self = shift;
    return unless $self->{clicked};
    return $self->SUPER::form_name_value(@_);
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::SubmitInput - An HTML form submit input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
