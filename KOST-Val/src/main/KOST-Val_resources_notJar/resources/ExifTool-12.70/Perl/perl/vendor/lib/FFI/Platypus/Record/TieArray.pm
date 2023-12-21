package FFI::Platypus::Record::TieArray;

use strict;
use warnings;
use 5.008004;
use Carp qw( croak );

# ABSTRACT: Tied array interface for record array members
our $VERSION = '2.08'; # VERSION


sub TIEARRAY
{
  my $class = shift;
  bless [ @_ ], $class;
}

sub FETCH
{
  my($self, $key) = @_;
  my($obj, $member) = @$self;
  $obj->$member($key);
}

sub STORE
{
  my($self, $key, $value) = @_;
  my($obj, $member) = @$self;
  $obj->$member($key, $value);
}

sub FETCHSIZE
{
  my($self) = @_;
  $self->[2];
}

sub CLEAR
{
  my($self) = @_;
  my($obj, $member) = @$self;

  $obj->$member([]);
}

sub EXTEND
{
  my($self, $count) = @_;
  croak "tried to extend a fixed length array" if $count > $self->[2];
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Record::TieArray - Tied array interface for record array members

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 package Foo;
 
 use FFI::Platypus::Record;
 use FFI::Platypus::Record::TieArray;
 
 record_layout(qw(
   int[20]  _bar
 ));
 
 sub bar
 {
   my($self, $arg) = @_;
   $self->_bar($arg) if ref($arg) eq ' ARRAY';
   tie my @list, 'FFI::Platypus::Record::TieArray',
     $self, '_bar', 20;
 }
 
 package main;
 
 my $foo = Foo->new;
 
 my $bar5 = $foo->bar->[5];  # get the 5th element of the bar array
 $foo->bar->[5] = 10;        # set the 5th element of the bar array
 @{ $foo->bar } = ();        # set all elements in bar to 0
 @{ $foo->bar } = (1..5);    # set the first five elements of the bar array

=head1 DESCRIPTION

B<WARNING>: This module is considered EXPERIMENTAL.  It may go away or
be changed in incompatible ways, possibly without notice, but not
without a good reason.

This class provides a tie interface for record array members.

In the future a short cut for using this with L<FFI::Platypus::Record>
directly may be provided.

=head1 SEE ALSO

=over 4

=item L<FFI::Platypus>

The main Platypus documentation.

=item L<FFI::Platypus::Record>

Documentation on Platypus records.

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
