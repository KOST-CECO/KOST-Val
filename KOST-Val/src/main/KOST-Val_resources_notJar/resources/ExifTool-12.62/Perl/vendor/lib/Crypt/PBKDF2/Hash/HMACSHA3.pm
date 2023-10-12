package Crypt::PBKDF2::Hash::HMACSHA3;
# ABSTRACT: HMAC-SHA3 support for Crypt::PBKDF2 using Digest::SHA
our $VERSION = '0.161520'; # VERSION
our $AUTHORITY = 'cpan:ARODLAND'; # AUTHORITY
use Moo 2;
use strictures 2;
use namespace::autoclean;
use Digest::HMAC 1.01 ();
use Digest::SHA3 0.22 ();
use Type::Tiny;
use Types::Standard qw(Enum);

with 'Crypt::PBKDF2::Hash';

has 'sha_size' => (
  is => 'ro',
  isa => Type::Tiny->new(
    name => 'SHASize',
    parent => Enum[qw( 224 256 384 512 )],
    display_name => 'valid number of bits for SHA-2',
  ),
  default => 256,
);

has '_hasher' => (
  is => 'lazy',
  init_arg => undef,
);

sub _build__hasher {
  my $self = shift;
  my $shasize = $self->sha_size;

  return Digest::SHA3->can("sha3_$shasize");
}

sub hash_len {
  my $self = shift;
  return $self->sha_size() / 8;
}

sub generate {
  my ($self, $data, $key) = @_;
  return Digest::HMAC::hmac($data, $key, $self->_hasher);
}

sub to_algo_string {
  my $self = shift;

  return $self->sha_size;
}

sub from_algo_string {
  my ($class, $str) = @_;

  return $class->new( sha_size => $str );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Crypt::PBKDF2::Hash::HMACSHA3 - HMAC-SHA3 support for Crypt::PBKDF2 using Digest::SHA

=head1 VERSION

version 0.161520

=head1 DESCRIPTION

Uses L<Digest::HMAC> and L<Digest::SHA3> C<sha3_256>/C<sha3_384>/C<sha3_512>
to provide the HMAC-ShA3 family of hashes for L<Crypt::PBKDF2>.

This could be done with L<Crypt::PBKDF2::Hash::DigestHMAC> instead, but it
seemed nice to have a uniform interface to HMACSHA*.

=head1 AUTHOR

Andrew Rodland <arodland@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2016 by Andrew Rodland.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
