package FFI::Build::PluginData;

use strict;
use warnings;
use parent qw( Exporter );

our @EXPORT_OK = qw( plugin_data );

# ABSTRACT: Platform and local customizations of FFI::Build
our $VERSION = '2.08'; # VERSION


sub plugin_data
{
  my($self) = @_;
  my $caller = caller;
  if($caller =~ /^FFI::Build::Plugin::(.*)$/)
  {
    return $self->{plugin_data}->{$1} ||= {};
  }
  else
  {
    require Carp;
    Carp::croak("plugin_data must be called by a plugin");
  }
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Build::PluginData - Platform and local customizations of FFI::Build

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
