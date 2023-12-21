package FFI::Build::File::Object;

use strict;
use warnings;
use 5.008004;
use parent qw( FFI::Build::File::Base );
use constant default_encoding => ':raw';
use Carp ();

# ABSTRACT: Class to track object file in FFI::Build
our $VERSION = '2.08'; # VERSION


sub default_suffix
{
  shift->platform->object_suffix;
}

sub build_item
{
  my($self) = @_;
  unless(-f $self->path)
  {
    Carp::croak "File not built"
  }
  return;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Build::File::Object - Class to track object file in FFI::Build

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 use FFI::Build::File::Object;
 my $o = FFI::Build::File::Object->new('src/_build/foo.o');

=head1 DESCRIPTION

This class represents an object file.  You normally do not need
to use it directly.

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
