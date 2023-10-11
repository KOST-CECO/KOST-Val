package Crypt::PBKDF2::Hash::HMACSHA2;
# ABSTRACT: HMAC-SHA2 support for Crypt::PBKDF2 using Digest::SHA
our $VERSION = '0.161520'; # VERSION
our $AUTHORITY = 'cpan:ARODLAND'; # AUTHORITY
use Moo 2;
use strictures 2;
use namespace::autoclean;
use Digest::SHA ();
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

  return Digest::SHA->can("hmac_sha$shasize");
}

sub hash_len {
  my $self = shift;
  return $self->sha_size() / 8;
}

sub generate {
  my $self = shift; # ($data, $key)
  return $self->_hasher->(@_);
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

Crypt::PBKDF2::Hash::HMACSHA2 - HMAC-SHA2 support for Crypt::PBKDF2 using Digest::SHA

=head1 VERSION

version 0.161520

=head1 DESCRIPTION

Uses L<Digest::SHA> C<hmac_sha256>/C<hmac_sha384>/C<hmac_sha512> to provide
the HMAC-SHA2 family of hashes for L<Crypt::PBKDF2>.

=head1 AUTHOR

Andrew Rodland <arodland@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2016 by Andrew Rodland.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
