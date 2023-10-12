package FFI::Platypus::TypeParser::Version2;

use strict;
use warnings;
use 5.008004;
use parent qw( FFI::Platypus::TypeParser::Version1 );
use constant _version => 2;

# ABSTRACT: FFI Type Parser Version Two
our $VERSION = '2.08'; # VERSION


1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::TypeParser::Version2 - FFI Type Parser Version Two

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 use FFI::Platypus 2.00;
 my $ffi = FFI::Platypus->new( api => 2 );
 $ffi->type('string(10)');

=head1 DESCRIPTION

This documents the third (version 2) type parser for L<FFI::Platypus>.
This type parser was included with L<FFI::Platypus> starting with version
C<1.58> in an experimental capability, and C<2.00> as a stable interface.
Starting with version C<1.00> the main L<FFI::Platypus> documentation
describes the version 2 API and you can refer to
L<FFI::Platypus::TypeParser::Version1> for details on the version1 API.

=head1 SEE ALSO

=over 4

=item L<FFI::Platypus>

The core L<FFI::Platypus> documentation.

=item L<FFI::Platypus::TypeParser::Version0>

The API C<0.02> type parser.

=item L<FFI::Platypus::TypeParser::Version1>

The API C<1.00> type parser.

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
