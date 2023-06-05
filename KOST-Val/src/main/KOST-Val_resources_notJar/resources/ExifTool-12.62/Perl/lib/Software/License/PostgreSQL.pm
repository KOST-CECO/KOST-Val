use strict;
use warnings;
package Software::License::PostgreSQL;
$Software::License::PostgreSQL::VERSION = '0.103014';
use parent 'Software::License';
# ABSTRACT: The PostgreSQL License

sub name { 'The PostgreSQL License' }
sub url  { 'http://www.opensource.org/licenses/postgresql' }

sub meta_name  { 'open_source' }
sub meta2_name { 'open_source' }
sub spdx_expression  { 'PostgreSQL' }

1;

=pod

=encoding UTF-8

=head1 NAME

Software::License::PostgreSQL - The PostgreSQL License

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
__LICENSE__
The PostgreSQL License

Copyright (c) {{$self->year}}, {{$self->holder}}

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose, without fee, and without a written agreement is
hereby granted, provided that the above copyright notice and this paragraph
and the following two paragraphs appear in all copies.

IN NO EVENT SHALL {{$self->holder}} BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
{{$self->holder}} HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

{{$self->holder}} SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS,
AND {{$self->holder}} HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
