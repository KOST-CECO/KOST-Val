package FFI::Build::Plugin;

use strict;
use warnings;
use autodie;
use File::Spec::Functions qw( catdir catfile );

# ABSTRACT: Platform and local customizations of FFI::Build
our $VERSION = '2.08'; # VERSION


sub new
{
  my($class) = @_;

  my %plugins;

  foreach my $inc (@INC)
  {
    # CAVEAT: won't work with an @INC hook.  Plugins must be in a "real" directory.
    my $path = catdir($inc, 'FFI', 'Build', 'Plugin');
    next unless -d $path;
    my $dh;
    opendir $dh, $path;
    my @list = readdir $dh;
    closedir $dh;

    foreach my $name (map { my $x = $_; $x =~ s/\.pm$//; $x } grep /\.pm$/, @list)
    {
      next if defined $plugins{$name};
      my $pm = catfile('FFI', 'Build', 'Plugin', "$name.pm");
      require $pm;
      my $class = "FFI::Build::Plugin::$name";
      if($class->can("api_version") && $class->api_version == 0)
      {
        $plugins{$name} = $class->new;
      }
      else
      {
        warn "$class is not the correct api version.  You may need to upgrade the plugin, platypus or uninstall the plugin";
      }
    }
  }

  bless \%plugins, $class;
}

sub call
{
  my($self, $method, @args) = @_;

  foreach my $name (sort keys %$self)
  {
    my $plugin = $self->{$name};
    $plugin->$method(@args) if $plugin->can($method);
  }

  1;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Build::Plugin - Platform and local customizations of FFI::Build

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 perldoc FFI::Build

=head1 DESCRIPTION

This class is experimental, but may do something useful in the future.

=head1 SEE ALSO

=over 4

=item L<FFI::Platypus>

=item L<FFI::Build>

=back

=head1 AUTHOR

Author: Graham Ollis E<lt>plicease@cpan.orgE<gt>

Contributors:

Bakkiaraj Murugesan (bakkiaraj)

Dylan Cali (calid)

pipcet

Zaki Mughal (zmughal)

Fitz Elliott (felliott)

Vickenty Fesunov (vyf)

Gregor Herrmann (gregoa)

Shlomi Fish (shlomif)

Damyan Ivanov

Ilya Pavlov (Ilya33)

Petr Písař (ppisar)

Mohammad S Anwar (MANWAR)

Håkon Hægland (hakonhagland, HAKONH)

Meredith (merrilymeredith, MHOWARD)

Diab Jerius (DJERIUS)

Eric Brine (IKEGAMI)

szTheory

José Joaquín Atria (JJATRIA)

Pete Houston (openstrike, HOUSTON)

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015-2022 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
