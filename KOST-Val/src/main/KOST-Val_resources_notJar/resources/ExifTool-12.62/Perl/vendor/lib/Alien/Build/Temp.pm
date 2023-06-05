package Alien::Build::Temp;

use strict;
use warnings;
use 5.008004;
use Carp ();
use Path::Tiny ();
use File::Temp ();
use File::Spec ();

# ABSTRACT: Temp Dir support for Alien::Build
our $VERSION = '2.38'; # VERSION


# problem with vanilla File::Temp is that is often uses
# as /tmp that has noexec turned on.  Workaround is to
# create a temp directory in the build directory, but
# we have to be careful about cleanup.  This puts all that
# (attempted) carefulness in one place so that when we
# later discover it isn't so careful we can fix it in
# one place rather thabn alllll the places that we need
# temp directories.

my %root;

sub _root
{
  return File::Spec->tmpdir if $^O eq 'MSWin32';

  my $root = Path::Tiny->new(-d "_alien" ? "_alien/tmp" : ".tmp")->absolute;
  unless(-d $root)
  {
    mkdir $root or die "unable to create temp root $!";
  }

  # TODO: doesn't account for fork...
  my $lock = $root->child("l$$");
  unless(-f $lock)
  {
    open my $fh, '>', $lock;
    close $fh;
  }
  $root{"$root"} = 1;
  $root;
}

END {
  foreach my $root (keys %root)
  {
    my $lock = Path::Tiny->new($root)->child("l$$");
    unlink $lock;
    # try to delete if possible.
    # if not possible then punt
    rmdir $root if -d $root;
  }
}

sub newdir
{
  my $class = shift;
  Carp::croak "uneven" if @_ % 2;
  File::Temp->newdir(DIR => _root, @_);
}

sub new
{
  my $class = shift;
  Carp::croak "uneven" if @_ % 2;
  File::Temp->new(DIR => _root, @_);
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Alien::Build::Temp - Temp Dir support for Alien::Build

=head1 VERSION

version 2.38

=head1 DESCRIPTION

This class is private to L<Alien::Build>.

=head1 AUTHOR

Author: Graham Ollis E<lt>plicease@cpan.orgE<gt>

Contributors:

Diab Jerius (DJERIUS)

Roy Storey (KIWIROY)

Ilya Pavlov

David Mertens (run4flat)

Mark Nunberg (mordy, mnunberg)

Christian Walde (Mithaldu)

Brian Wightman (MidLifeXis)

Zaki Mughal (zmughal)

mohawk (mohawk2, ETJ)

Vikas N Kumar (vikasnkumar)

Flavio Poletti (polettix)

Salvador Fandiño (salva)

Gianni Ceccarelli (dakkar)

Pavel Shaydo (zwon, trinitum)

Kang-min Liu (劉康民, gugod)

Nicholas Shipp (nshp)

Juan Julián Merelo Guervós (JJ)

Joel Berger (JBERGER)

Petr Pisar (ppisar)

Lance Wicks (LANCEW)

Ahmad Fatoum (a3f, ATHREEF)

José Joaquín Atria (JJATRIA)

Duke Leto (LETO)

Shoichi Kaji (SKAJI)

Shawn Laffan (SLAFFAN)

Paul Evans (leonerd, PEVANS)

Håkon Hægland (hakonhagland, HAKONH)

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2011-2020 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
