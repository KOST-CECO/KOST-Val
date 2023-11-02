package Alien::Build::Plugin::Core::CleanInstall;

use strict;
use warnings;
use 5.008004;
use Alien::Build::Plugin;
use Path::Tiny ();

# ABSTRACT: Implementation for clean_install hook.
our $VERSION = '2.80'; # VERSION


sub init
{
  my($self, $meta) = @_;

  $meta->default_hook(
    clean_install => sub {
      my($build) = @_;
      my $root = Path::Tiny->new(
        $build->runtime_prop->{prefix}
      );
      if(-d $root)
      {
        foreach my $child ($root->children)
        {
          if($child->basename eq '_alien')
          {
            $build->log("keeping  $child");
          }
          else
          {
            $build->log("removing $child");
            $child->remove_tree({ safe => 0});
          }
        }
      }
    }
  );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Alien::Build::Plugin::Core::CleanInstall - Implementation for clean_install hook.

=head1 VERSION

version 2.80

=head1 SYNOPSIS

 use alienfile;
 # already loaded

=head1 DESCRIPTION

This plugin implements the default C<clean_install> hook.
You shouldn't use it directly.

=head1 SEE ALSO

L<Alien::Build>, L<Alien::Base::ModuleBuild>

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

Petr Písař (ppisar)

Lance Wicks (LANCEW)

Ahmad Fatoum (a3f, ATHREEF)

José Joaquín Atria (JJATRIA)

Duke Leto (LETO)

Shoichi Kaji (SKAJI)

Shawn Laffan (SLAFFAN)

Paul Evans (leonerd, PEVANS)

Håkon Hægland (hakonhagland, HAKONH)

nick nauwelaerts (INPHOBIA)

Florian Weimer

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2011-2022 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
