package FFI::Platypus::Lang;

use strict;
use warnings;
use 5.008004;

# ABSTRACT: Language specific customizations
our $VERSION = '2.08'; # VERSION



1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Lang - Language specific customizations

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 perldoc FFI::Platypus::Lang;

=head1 DESCRIPTION

This namespace is reserved for language specific customizations of L<FFI::Platypus>.
This usually involves providing native type maps.  It can also involve computing
mangled names.  The default language is C, and is defined in L<FFI::Platypus::Lang::C>.

This package itself doesn't do anything, it serves only as documentation.

=head1 SEE ALSO

=over 4

=item L<FFI::Platypus>

Platypus itself.

=item L<FFI::Platypus::Lang::ASM>

This language plugin provides no type aliases, and is intended
for use with assembly language or for when no other language
plugin is appropriate.

=item L<FFI::Platypus::Lang::C>

Language plugin for the C programming language.

=item L<FFI::Platypus::Lang::Fortran>

Non-core language plugin for the Fortran programming language.

=item L<FFI::Platypus::Lang::CPP>

Non-core language plugin for the C++ programming language.

=item L<FFI::Platypus::Lang::Go>

Non-core language plugin for the Go programming language.

=item L<FFI::Platypus::Lang::Pascal>

Non-core language plugin for the Pascal programming language.

=item L<FFI::Platypus::Lang::Rust>

Non-core language plugin for the Rust programming language.

=item L<FFI::Platypus::Lang::Win32>

Language plugin for use with the Win32 API.

=item L<FFI::Platypus::Lang::Zig>

Non-core language plugin for the Zig programming language.

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
