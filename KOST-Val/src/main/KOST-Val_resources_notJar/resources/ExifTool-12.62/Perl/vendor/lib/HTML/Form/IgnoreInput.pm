package HTML::Form::IgnoreInput;

use strict;
use parent 'HTML::Form::Input';

our $VERSION = '6.11';

# ABSTRACT: An HTML form ignored input element for use with HTML::Form

# This represents buttons and resets whose values shouldn't matter
# but should buttons not be like submits?!

#input/button
#input/reset

sub value { return }

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::IgnoreInput - An HTML form ignored input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
