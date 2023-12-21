package Crypt::PBKDF2::Hash::HMACSHA1;
# ABSTRACT: HMAC-SHA1 support for Crypt::PBKDF2 using Digest::SHA
our $VERSION = '0.161520'; # VERSION
our $AUTHORITY = 'cpan:ARODLAND'; # AUTHORITY
use Moo 2;
use strictures 2;
use namespace::autoclean;
use Digest::SHA ();
use Carp qw(croak);

with 'Crypt::PBKDF2::Hash';

sub hash_len {
  return 20;
}

sub generate {
  my $self = shift; # ($data, $key)
  return Digest::SHA::hmac_sha1(@_);
}

sub to_algo_string {
  return;
}

sub from_algo_string {
  croak "No argument expected";
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Crypt::PBKDF2::Hash::HMACSHA1 - HMAC-SHA1 support for Crypt::PBKDF2 using Digest::SHA

=head1 VERSION

version 0.161520

=head1 DESCRIPTION

Uses L<Digest::SHA> C<hmac_sha1> to provide the HMAC-SHA1 backend for
L<Crypt::PBKDF2>.

=head1 AUTHOR

Andrew Rodland <arodland@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2016 by Andrew Rodland.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
