package Crypt::PBKDF2::Hash::DigestHMAC;
# ABSTRACT: Digest::HMAC hash support for Crypt::PBKDF2.
our $VERSION = '0.161520'; # VERSION
our $AUTHORITY = 'cpan:ARODLAND'; # AUTHORITY
use Moo 2;
use strictures 2;
use namespace::autoclean;
use Digest 1.16 ();
use Digest::HMAC 1.01 ();
use Try::Tiny 0.04;
use Carp qw(croak);

with 'Crypt::PBKDF2::Hash';


has digest_class => (
  is => 'ro',
  required => 1,
);

has _digest => (
  is => 'lazy',
  init_arg => undef,
);

sub _build__digest {
  my $self = shift;

  return Digest->new($self->digest_class);
}

sub BUILD {
  my $self = shift;

  try {
    my $digest = $self->_digest;
  } catch {
    croak "Couldn't construct a Digest of type " . $self->digest_class . ": $_";
  }
}

sub hash_len {
  my $self = shift;
  return length( $self->_digest->clone->add("")->digest );
}

sub generate {
  my ($self, $data, $key) = @_;
  
  my $digest = $self->_digest->clone;

  return Digest::HMAC::hmac($data, $key,
    sub { $digest->add(@_)->digest });
}

sub to_algo_string {
  my $self = shift;

  return $self->digest_class;
}

sub from_algo_string {
  my ($class, $str) = shift;

  return $class->new(digest_class => $str);
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Crypt::PBKDF2::Hash::DigestHMAC - Digest::HMAC hash support for Crypt::PBKDF2.

=head1 VERSION

version 0.161520

=head1 DESCRIPTION

Uses L<Digest::HMAC> to make nearly any L<Digest>-compatible module
compatible with L<Crypt::PKBDF2> by driving it with the standard HMAC
algorithm to combine the key and the data.

=head1 ATTRIBUTES

=head2 digest_class

The Digest class to use. Will be passed to C<< Digest->new >>.

=head1 AUTHOR

Andrew Rodland <arodland@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2016 by Andrew Rodland.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
