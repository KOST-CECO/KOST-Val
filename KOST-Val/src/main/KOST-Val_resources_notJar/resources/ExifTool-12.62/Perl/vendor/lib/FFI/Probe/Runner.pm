package FFI::Probe::Runner;

use strict;
use warnings;
use 5.008004;
use Capture::Tiny qw( capture );
use FFI::Probe::Runner::Result;

# ABSTRACT: Probe runner for FFI
our $VERSION = '2.08'; # VERSION


sub new
{
  my($class, %args) = @_;

  $args{exe} ||= do {
    require FFI::Platypus::ShareConfig;
    require File::Spec;
    require Config;
    File::Spec->catfile(FFI::Platypus::ShareConfig::dist_dir('FFI::Platypus'), 'probe', 'bin', "dlrun$Config::Config{exe_ext}");
  };

  defined $args{flags} or $args{flags} = '-';

  die "probe runner executable not found at: $args{exe}" unless -f $args{exe};

  my $self = bless {
    exe   => $args{exe},
    flags => $args{flags},
  }, $class;
  $self;
}


sub exe   { shift->{exe}   }
sub flags { shift->{flags} }


sub verify
{
  my($self) = @_;
  my $exe = $self->exe;
  my($out, $err, $ret) = capture {
    $! = 0;
    system $exe, 'verify', 'self';
  };
  return 1 if $ret == 0 && $out =~ /dlrun verify self ok/;
  print $out;
  print STDERR $err;
  die "verify failed";
}


sub run
{
  my($self, $dll, @args) = @_;
  my $exe   = $self->exe;
  my $flags = $self->flags;
  my($out, $err, $ret) = capture {
    my @cmd = ($exe, $dll, $flags, @args);
    $! = 0;
    system @cmd;
    $?;
  };
  FFI::Probe::Runner::Result->new(
    stdout => $out,
    stderr => $err,
    rv     => $ret >> 8,
    signal => $ret & 127,
  );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Probe::Runner - Probe runner for FFI

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 use FFI::Probe::Runner;
 
 my $runner = FFI::Probe::Runner->new;
 $runner->run('foo.so');

=head1 DESCRIPTION

This class executes code in a dynamic library for probing and detecting platform
properties.

=head1 CONSTRUCTOR

=head2 new

 my $runner = FFI::Probe::Runner->new(%args);

Creates a new instance.

=over 4

=item exe

The path to the dlrun wrapper.  The default is usually correct.

=item flags

The flags to pass into C<dlopen>.  The default is C<RTLD_LAZY> on Unix
and C<0> on windows..

=back

=head1 METHODS

=head2 exe

 my $exe = $runner->exe;

The path to the dlrun wrapper.

=head2 flags

 my $flags = $runner->flags;

The flags to pass into C<dlopen>.

=head2 verify

 $runner->verify;

Verifies the dlrun wrapper is working.  Throws an exception in the event of failure.

=head2 run

 $runner->run($dll, @args);

Runs the C<dlmain> function in the given dynamic library, passing in the
given arguments.  Returns a L<FFI::Probe::Runner::Result> object which
contains the results.

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
