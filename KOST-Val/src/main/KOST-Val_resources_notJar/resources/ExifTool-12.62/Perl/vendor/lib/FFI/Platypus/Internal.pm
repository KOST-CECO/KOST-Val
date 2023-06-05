package FFI::Platypus::Internal;

use strict;
use warnings;
use 5.008004;
use FFI::Platypus;
use base qw( Exporter );

require FFI::Platypus;
_init();

our @EXPORT = grep /^FFI_PL/, keys %FFI::Platypus::Internal::;

# ABSTRACT: For internal use only
our $VERSION = '1.34'; # VERSION


1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Internal - For internal use only

=head1 VERSION

version 1.34

=head1 SYNOPSIS

 perldoc FFI::Platypus

=head1 DESCRIPTION

This module is for internal use only.  Do not rely on it having any particular behavior, or even existing in future versions.
You have been warned.

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

Petr Pisar (ppisar)

Mohammad S Anwar (MANWAR)

Håkon Hægland (hakonhagland, HAKONH)

Meredith (merrilymeredith, MHOWARD)

Diab Jerius (DJERIUS)

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015,2016,2017,2018,2019,2020 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
