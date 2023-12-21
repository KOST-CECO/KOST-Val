package Crypt::PBKDF2::Hash;
# ABSTRACT: Abstract role for PBKDF2 hashing algorithms.
our $VERSION = '0.161520'; # VERSION
our $AUTHORITY = 'cpan:ARODLAND'; # AUTHORITY
use Moo::Role 2;
use strictures 2;
use namespace::autoclean;

requires 'hash_len';

requires 'generate';

requires 'to_algo_string';

requires 'from_algo_string';

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Crypt::PBKDF2::Hash - Abstract role for PBKDF2 hashing algorithms.

=head1 VERSION

version 0.161520

=head1 METHODS

=head2 hash_len()

Returns the length (in bytes) of the hashes this algorithm generates.

=head2 generate($data, $key)

Generate strong pseudorandom bits based on the C<$data> and C<$key>

=head2 to_algo_string()

Return a string representing any optional arguments this object was created
with, for use by L<Crypt::PBKDF2>'s C<generate> and C<encode_string>
methods. May return undef if no arguments are required, in which case none
will be serialized and C<from_algo_string> won't be called on reading the
hash.

=head2 from_algo_string($str)

Given a string as produced by C<from_algo_string>, return an instance of
this class with options corresponding to those in C<$str>. If no options are
expected, it's permissible for this method to throw an exception.

=head1 AUTHOR

Andrew Rodland <arodland@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2016 by Andrew Rodland.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
