package FFI::Platypus::Legacy;

use strict;
use warnings;
use 5.008004;

# ABSTRACT: Legacy Platypus interfaces
our $VERSION = '2.08'; # VERSION


package FFI::Platypus;

sub _package
{
  my($self, $module, $modlibname) = @_;

  ($module, $modlibname) = caller unless defined $modlibname;
  my @modparts = split /::/, $module;
  my $modfname = $modparts[-1];
  my $modpname = join('/',@modparts);
  my $c = @modparts;
  $modlibname =~ s,[\\/][^\\/]+$,, while $c--;    # Q&D basename

  {
    my @maybe = (
      "$modlibname/auto/$modpname/$modfname.txt",
      "$modlibname/../arch/auto/$modpname/$modfname.txt",
    );
    foreach my $file (@maybe)
    {
      if(-f $file)
      {
        open my $fh, '<', $file;
        my $line = <$fh>;
        close $fh;
        if($line =~ /^FFI::Build\@(.*)$/)
        {
          $self->lib("$modlibname/$1");
          return $self;
        }
      }
    }
  }

  require FFI::Platypus::ShareConfig;
  my @dlext = @{ FFI::Platypus::ShareConfig->get("config_dlext") };

  foreach my $dlext (@dlext)
  {
    my $file = "$modlibname/auto/$modpname/$modfname.$dlext";
    unless(-e $file)
    {
      $modlibname =~ s,[\\/][^\\/]+$,,;
      $file = "$modlibname/arch/auto/$modpname/$modfname.$dlext";
    }

    if(-e $file)
    {
      $self->lib($file);
      return $self;
    }
  }

  $self;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Legacy - Legacy Platypus interfaces

=head1 VERSION

version 2.08

=head1 DESCRIPTION

This class is private to L<FFI::Platypus>.

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
