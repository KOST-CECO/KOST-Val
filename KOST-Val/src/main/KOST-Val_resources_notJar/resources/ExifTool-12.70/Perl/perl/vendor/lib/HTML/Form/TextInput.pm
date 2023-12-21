package HTML::Form::TextInput;

use strict;
use parent 'HTML::Form::Input';

our $VERSION = '6.11';

# ABSTRACT: An HTML form text input element for use with HTML::Form

#input/text
#input/password
#input/hidden
#textarea

sub value {
    my $self = shift;
    my $old  = $self->{value};
    $old = "" unless defined $old;
    if (@_) {
        Carp::croak("Input '$self->{name}' is readonly")
            if $self->{strict} && $self->{readonly};
        my $new = shift;
        my $n   = exists $self->{maxlength} ? $self->{maxlength} : undef;
        Carp::croak("Input '$self->{name}' has maxlength '$n'")
            if $self->{strict}
            && defined($n)
            && defined($new)
            && length($new) > $n;
        $self->{value} = $new;
    }
    $old;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::TextInput - An HTML form text input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
