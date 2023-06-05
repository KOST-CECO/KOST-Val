use strict;
use warnings;
package Software::License::Perl_5;
$Software::License::Perl_5::VERSION = '0.103014';
use parent 'Software::License';
# ABSTRACT: The Perl 5 License (Artistic 1 & GPL 1)

require Software::License::GPL_1;
require Software::License::Artistic_1_0;

sub name { 'the same terms as the perl 5 programming language system itself' }
sub url  { 'http://dev.perl.org/licenses/' }
sub meta_name  { 'perl' }
sub meta2_name { 'perl_5' }
sub spdx_expression  { 'Artistic-1.0-Perl OR GPL-1.0-or-later' }

sub _gpl {
  my ($self) = @_;
  return $self->{_gpl} ||= Software::License::GPL_1->new({
    year   => $self->year,
    holder => $self->holder,
  });
}

sub _tal {
  my ($self) = @_;
  return $self->{_tal} ||= Software::License::Artistic_1_0->new({
    year   => $self->year,
    holder => $self->holder,
  });
}

1;

=pod

=encoding UTF-8

=head1 NAME

Software::License::Perl_5 - The Perl 5 License (Artistic 1 & GPL 1)

=head1 VERSION

version 0.103014

=head1 AUTHOR

Ricardo Signes <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2018 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut

__DATA__
__NOTICE__
This software is copyright (c) {{$self->year}} by {{$self->_dotless_holder}}.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.
__LICENSE__
Terms of the Perl programming language system itself

a) the GNU General Public License as published by the Free
   Software Foundation; either version 1, or (at your option) any
   later version, or
b) the "Artistic License"

--- {{ $self->_gpl->name }} ---

{{$self->_gpl->fulltext}}

--- {{ $self->_tal->name }} ---

{{$self->_tal->fulltext}}
