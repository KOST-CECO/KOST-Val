package HTML::Form::KeygenInput;

use strict;
use parent 'HTML::Form::Input';

our $VERSION = '6.11';

# ABSTRACT: An HTML form keygen input element for use with HTML::Form

sub challenge {
    my $self = shift;
    return $self->{challenge};
}

sub keytype {
    my $self = shift;
    return lc( $self->{keytype} || 'rsa' );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::KeygenInput - An HTML form keygen input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
