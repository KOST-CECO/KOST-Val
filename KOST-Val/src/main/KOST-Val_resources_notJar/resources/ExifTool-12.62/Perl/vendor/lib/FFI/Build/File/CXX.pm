package FFI::Build::File::CXX;

use strict;
use warnings;
use 5.008004;
use parent qw( FFI::Build::File::C );
use constant default_suffix => '.cxx';
use constant default_encoding => ':utf8';

# ABSTRACT: Class to track C source file in FFI::Build
our $VERSION = '2.08'; # VERSION


sub accept_suffix
{
  (qr/\.c(xx|pp)$/)
}

sub cc
{
  my($self) = @_;
  $self->platform->cxx;
}

sub ld
{
  my($self) = @_;
  $self->platform->cxxld;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Build::File::CXX - Class to track C source file in FFI::Build

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 use FFI::Build::File::CXX;
 
 my $c = FFI::Build::File::CXX->new('src/foo.cxx');

=head1 DESCRIPTION

File class for C++ source files.

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
