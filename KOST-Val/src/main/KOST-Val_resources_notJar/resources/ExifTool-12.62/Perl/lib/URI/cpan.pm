use strict;
use warnings;

package URI::cpan;
# ABSTRACT: URLs that refer to things on the CPAN
$URI::cpan::VERSION = '1.007';
use parent qw(URI::_generic);

#pod =head1 SYNOPSIS
#pod
#pod   use URI::cpan;
#pod
#pod   my $uri = URI->new('cpan:///distfile/RJBS/URI-cpan-1.000.tar.gz');
#pod
#pod   $uri->author;       # => RJBS
#pod   $uri->dist_name;    # => URI-cpan
#pod   $uri->dist_version; # => 1.000
#pod
#pod Other forms of cpan: URI include:
#pod
#pod   cpan:///author/RJBS
#pod
#pod Reserved for likely future use are:
#pod
#pod   cpan:///dist
#pod   cpan:///module
#pod   cpan:///package
#pod
#pod =cut

use Carp ();
use URI::cpan::author;
use URI::cpan::dist;
use URI::cpan::distfile;
use URI::cpan::module;
use URI::cpan::package;
use URI::cpan::dist;

my %type_class = (
  author   => 'URI::cpan::author',
  distfile => 'URI::cpan::distfile',

  # These will be uncommented when we figure out what the heck to do with them.
  # -- rjbs, 2009-03-30
  #
  # dist    => 'URI::cpan::dist',
  # package => 'URI::cpan::package',
  # module  => 'URI::cpan::module',
);

sub _init {
  my $self = shift->SUPER::_init(@_);
  my $class = ref($self);

  Carp::croak "invalid cpan URI: non-empty query string not supported"
    if $self->query;

  Carp::croak "invalid cpan URI: non-empty fragment string not supported"
    if $self->fragment;

  my (undef, @path_parts) = split m{/}, $self->path;
  my $type = $path_parts[0];

  Carp::croak "invalid cpan URI: do not understand path " . $self->path
    unless my $new_class = $type_class{ $type };

  bless $self => $new_class;

  $self->validate;

  return $self;
}

sub _p_rel {
  my ($self) = @_;
  my $path = $self->path;
  $path =~ s{^/\w+/}{};
  return $path;
}

#pod =head1 WARNINGS
#pod
#pod URI objects are difficult to subclass, so I have not (yet?) taken the time to
#pod remove mutability from the objects.  This means that you can probably alter a
#pod URI::cpan object into a state where it is no longer valid.
#pod
#pod Please don't change the contents of these objects after construction.
#pod
#pod =head1 SEE ALSO
#pod
#pod L<URI::cpan::author> and L<URI::cpan::distfile>
#pod
#pod =head1 THANKS
#pod
#pod This code is derived from code written at Pobox.com by Hans Dieter Pearcey.
#pod Dieter helped thrash out this new implementation, too.
#pod
#pod =cut

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

URI::cpan - URLs that refer to things on the CPAN

=head1 VERSION

version 1.007

=head1 SYNOPSIS

  use URI::cpan;

  my $uri = URI->new('cpan:///distfile/RJBS/URI-cpan-1.000.tar.gz');

  $uri->author;       # => RJBS
  $uri->dist_name;    # => URI-cpan
  $uri->dist_version; # => 1.000

Other forms of cpan: URI include:

  cpan:///author/RJBS

Reserved for likely future use are:

  cpan:///dist
  cpan:///module
  cpan:///package

=head1 WARNINGS

URI objects are difficult to subclass, so I have not (yet?) taken the time to
remove mutability from the objects.  This means that you can probably alter a
URI::cpan object into a state where it is no longer valid.

Please don't change the contents of these objects after construction.

=head1 SEE ALSO

L<URI::cpan::author> and L<URI::cpan::distfile>

=head1 THANKS

This code is derived from code written at Pobox.com by Hans Dieter Pearcey.
Dieter helped thrash out this new implementation, too.

=head1 AUTHOR

Ricardo SIGNES <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2009 by Ricardo SIGNES.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
