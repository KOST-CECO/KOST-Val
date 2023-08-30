package FFI::Probe::Runner::Result;

use strict;
use warnings;
use 5.008004;

# ABSTRACT: The results from a probe run.
our $VERSION = '1.34'; # VERSION


sub new
{
  my($class, %args) = @_;
  my $self = bless \%args, $class;
  $self;
}


sub stdout { shift->{stdout} }
sub stderr { shift->{stderr} }
sub rv     { shift->{rv}     }
sub signal { shift->{signal} }


sub pass
{
  my($self) = @_;
  $self->rv == 0 && $self->signal == 0;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Probe::Runner::Result - The results from a probe run.

=head1 VERSION

version 1.34

=head1 SYNOPSIS

=head1 DESCRIPTION

=head1 CONSTRUCTOR

=head2 new

 my $result = FFI::Probe::Runner::Result->new(%args);

Creates a new instance of the class.

=head1 METHODS

=head2 stdout

 my $stdout = $result->stdout;

=head2 stderr

 my $stderr = $result->stderr;

=head2 rv

 my $rv = $result->rv;

=head2 signal

 my $signal = $result->signal;

=head2 pass

 my $pass = $result->pass;

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
