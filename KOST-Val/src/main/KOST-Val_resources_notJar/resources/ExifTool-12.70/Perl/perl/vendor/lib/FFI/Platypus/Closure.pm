package FFI::Platypus::Closure;

use strict;
use warnings;
use 5.008004;
use FFI::Platypus;
use Scalar::Util qw( refaddr);
use Carp qw( croak );
use overload '&{}' => sub {
  my $self = shift;
  sub { $self->{code}->(@_) };
}, bool => sub { 1 }, fallback => 1;

# ABSTRACT: Platypus closure object
our $VERSION = '2.08'; # VERSION


sub new
{
  my($class, $coderef) = @_;
  croak "not a coderef" unless ref($coderef) eq 'CODE';
  my $self = bless { code => $coderef, cbdata => {}, sticky => 0 }, $class;
  $self;
}

sub add_data
{
  my($self, $payload, $type) = @_;
  $self->{cbdata}{$type} = bless \$payload, 'FFI::Platypus::ClosureData';
}

sub get_data
{
  my($self, $type) = @_;

  if (exists $self->{cbdata}->{$type}) {
      return ${$self->{cbdata}->{$type}};
  }

  return 0;
}


sub call
{
  my $self = shift;
  $self->{code}->(@_)
}


sub sticky
{
  my($self) = @_;
  return if $self->{sticky};
  $self->{sticky} = 1;
  $self->_sticky;
}


sub unstick
{
  my($self) = @_;
  return unless $self->{sticky};
  $self->{sticky} = 0;
  $self->_unstick;
}

package FFI::Platypus::ClosureData;

our $VERSION = '2.08'; # VERSION

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Closure - Platypus closure object

=head1 VERSION

version 2.08

=head1 SYNOPSIS

create closure with OO interface

 use FFI::Platypus::Closure;
 my $closure = FFI::Platypus::Closure->new(sub { print "hello world\n" });

create closure from Platypus object

 use FFI::Platypus 2.00;
 my $ffi = FFI::Platypus->new( api => 2 );
 my $closure = $ffi->closure(sub { print "hello world\n" });

use closure

 $ffi->function(foo => ['()->void'] => 'void')->call($closure);

=head1 DESCRIPTION

This class represents a Perl code reference that can be called from compiled code.
When you create a closure object, you can pass it into any function that expects
a function pointer.  Care needs to be taken with closures because compiled languages
typically have a different way of handling lifetimes of objects.  You have to make
sure that if the compiled code is going to call a closure that the closure object
is still in scope somewhere, or has been made sticky, otherwise you may get a
segment violation or other mysterious crash.

=head1 CONSTRUCTOR

=head2 new

 my $closure = FFI::Platypus::Closure->new($coderef);

Create a new closure object; C<$coderef> must be a subroutine code reference.

=head1 METHODS

=head2 call

 $closure->call(@arguments);
 $closure->(@arguments);

Call the closure from Perl space.  May also be invoked by treating
the closure object as a code reference.

=head2 sticky

 $closure->sticky;

Mark the closure sticky, meaning that it won't be free'd even if
all the reference of the object fall out of scope.

=head2 unstick

 $closure->unstick;

Unmark the closure as sticky.

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
