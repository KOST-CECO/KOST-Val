package Test::Alien::Diag;

use strict;
use warnings;
use 5.008004;
use Test2::API qw( context );
use base qw( Exporter );

our @EXPORT = qw( alien_diag );
our @EXPORT_OK = @EXPORT;

# ABSTRACT: Print out standard diagnostic for Aliens in the test step.
our $VERSION = '2.38'; # VERSION


sub alien_diag ($@)
{
  my $ctx = context();

  my $max = 0;
  foreach my $alien (@_)
  {
    foreach my $name (qw( cflags cflags_static libs libs_static version install_type dynamic_libs bin_dir ))
    {
      my $str = "$alien->$name";
      if(length($str) > $max)
      {
        $max = length($str);
      }
    }
  }


  $ctx->diag('');
  foreach my $alien (@_) {
    $ctx->diag('') for 1..2;

    my $found = 0;

    foreach my $name (qw( cflags cflags_static libs libs_static version install_type ))
    {
      if(eval { $alien->can($name) })
      {
        $found++;
        $ctx->diag(sprintf "%-${max}s = %s", "$alien->$name", $alien->$name);
      }
    }

    foreach my $name (qw( dynamic_libs bin_dir ))
    {
      if(eval { $alien->can($name) })
      {
        $found++;
        my @list = eval { $alien->$name };
        next if $@;
        $ctx->diag(sprintf "%-${max}s = %s", "$alien->$name", $_) for @list;
      }
    }

    $ctx->diag("no diagnostics found for $alien") unless $found;

    $ctx->diag('') for 1..2;
  }

  $ctx->release;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Test::Alien::Diag - Print out standard diagnostic for Aliens in the test step.

=head1 VERSION

version 2.38

=head1 SYNOPSIS

 use Test2::V0;
 use Test::Alien::Diag qw( alien_diag );

=head1 DESCRIPTION

This module provides an C<alien_diag> method that prints out diagnostics useful for
cpantesters for other bug reports that gives a quick summary of the important settings
like C<clfags> and C<libs>.

=head1 FUNCTIONS

=head2 alien_diag

 alien_diag $alien;

prints out diagnostics.

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
