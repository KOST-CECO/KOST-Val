use strict;
use warnings;

package URI::cpan::distfile;
# ABSTRACT: cpan:///distfile/AUTHOR/Dist-1.234.tar.gz
$URI::cpan::distfile::VERSION = '1.007';
use parent qw(URI::cpan);

use Carp ();
use CPAN::DistnameInfo;

#pod =head1 SYNOPSIS
#pod
#pod This URL refers to a file in an author directory on the CPAN, and expects the
#pod format AUTHOR/DISTFILE
#pod
#pod =head1 METHODS
#pod
#pod =cut

sub validate {
  my ($self) = @_;

  my (undef, undef, $author, $filename) = split m{/}, $self->path, 4;

  Carp::croak "invalid cpan URI: invalid author part in $self"
    unless $author =~ m{\A[A-Z][-0-9A-Z]*\z};
}

#pod =head1 dist_name
#pod
#pod This returns the name of the dist, like F<CGI.pm> or F<Acme-Drunk>.
#pod
#pod =cut

sub dist_name {
  my ($self) = @_;
  my $dist = CPAN::DistnameInfo->new($self->_p_rel);
  my $name = $dist->dist;

  $name =~ s/-undef$// if ! defined $dist->version;

  return $name;
}

#pod =head1 dist_version
#pod
#pod This returns the version of the dist, or undef if the version can't be found or
#pod is the string "undef"
#pod
#pod =cut

sub dist_version {
  my ($self) = @_;
  CPAN::DistnameInfo->new($self->_p_rel)->version;
}

#pod =head1 dist_filepath
#pod
#pod This returns the path to the dist file.  This is the whole URL after the
#pod C<distfile> part.
#pod
#pod =cut

sub dist_filepath {
  my ($self) = @_;
  $self->_p_rel;
}

#pod =head1 author
#pod
#pod This returns the name of the author whose file is referred to.
#pod
#pod =cut

sub author {
  my ($self) = @_;
  my ($author) = $self->_p_rel =~ m{^([A-Z]+)/};
  return $author;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

URI::cpan::distfile - cpan:///distfile/AUTHOR/Dist-1.234.tar.gz

=head1 VERSION

version 1.007

=head1 SYNOPSIS

This URL refers to a file in an author directory on the CPAN, and expects the
format AUTHOR/DISTFILE

=head1 METHODS

=head1 dist_name

This returns the name of the dist, like F<CGI.pm> or F<Acme-Drunk>.

=head1 dist_version

This returns the version of the dist, or undef if the version can't be found or
is the string "undef"

=head1 dist_filepath

This returns the path to the dist file.  This is the whole URL after the
C<distfile> part.

=head1 author

This returns the name of the author whose file is referred to.

=head1 AUTHOR

Ricardo SIGNES <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2009 by Ricardo SIGNES.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
