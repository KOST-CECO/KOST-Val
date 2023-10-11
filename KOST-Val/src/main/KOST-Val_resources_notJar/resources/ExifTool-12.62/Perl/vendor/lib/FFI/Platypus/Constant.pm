package FFI::Platypus::Constant;

use strict;
use warnings;
use 5.008004;
use constant 1.32 ();
use FFI::Platypus;

# ABSTRACT: Define constants in C space for Perl
our $VERSION = '2.08'; # VERSION


{
  my $ffi = FFI::Platypus->new( api => 2 );
  $ffi->bundle;

  $ffi->type( 'opaque'                => 'ffi_platypus_constant_t' );
  $ffi->type( '(string,string)->void' => 'set_str_t'       );
  $ffi->type( '(string,sint64)->void' => 'set_sint_t'      );
  $ffi->type( '(string,uint64)->void' => 'set_uint_t'      );
  $ffi->type( '(string,double)->void' => 'set_double_t'    );

  $ffi->mangler(sub {
    my($name) = @_;
    $name =~ s/^/ffi_platypus_constant__/;
    $name;
  });

  $ffi->attach( new => [ 'set_str_t', 'set_sint_t', 'set_uint_t', 'set_double_t' ] => 'ffi_platypus_constant_t' => sub {
    my($xsub, $class, $default_package) = @_;
    my $f = $ffi->closure(sub {
      my($name, $value) = @_;
      if($name !~ /::/)
      {
        $name = join('::', $default_package, $name);
      }
      constant->import($name, $value);
    });

    bless {
      ptr => $xsub->($f, $f, $f, $f),
      f   => $f,
    }, $class;
  });

  $ffi->attach( DESTROY => ['ffi_platypus_constant_t'] => 'void' => sub {
    my($xsub, $self) = @_;
    $xsub->($self->ptr);
  });

  sub ptr { shift->{ptr} }

}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Constant - Define constants in C space for Perl

=head1 VERSION

version 2.08

=head1 SYNOPSIS

C<ffi/foo.c>:

 #include <ffi_platypus_bundle.h>
 
 void
 ffi_pl_bundle_constant(const char *package, ffi_platypus_constant_t *c)
 {
   c->set_str("FOO", "BAR");       /* sets $package::FOO to "BAR" */
   c->set_str("ABC::DEF", "GHI");  /* sets ABC::DEF to GHI        */
 }

C<lib/Foo.pm>:

 package Foo;
 
 use strict;
 use warnings;
 use FFI::Platypus 2.00;
 use Exporter qw( import );
 
 my $ffi = FFI::Platypus->new( api => 2 );
 # sets constants Foo::FOO and ABC::DEF from C
 $ffi->bundle;
 
 1;

=head1 DESCRIPTION

The Platypus bundle interface (see L<FFI::Platypus::Bundle>) has an entry point
C<ffi_pl_bundle_constant> that lets you define constants in Perl space from C.

 void ffi_pl_bundle_constant(const char *package, ffi_platypus_constant_t *c);

The first argument C<package> is the name of the Perl package.  The second argument
C<c> is a struct with function pointers that lets you define constants of different
types.  The first argument for each function is the name of the constant and the
second is the value.  If C<::> is included in the constant name then it will be
defined in that package space.  If it isn't then the constant will be defined in
whichever package called C<bundle>.

=over 4

=item set_str

 c->set_str(name, value);

Sets a string constant.

=item set_sint

 c->set_sint(name, value);

Sets a 64-bit signed integer constant.

=item set_uint

 c->set_uint(name, value);

Sets a 64-bit unsigned integer constant.

=item set_double

 c->set_double(name, value);

Sets a double precision floating point constant.

=back

=head2 Example

Suppose you have a header file C<myheader.h>:

 #ifndef MYHEADER_H
 #define MYHEADER_H
 
 #define MYVERSION_STRING "1.2.3"
 #define MYVERSION_MAJOR 1
 #define MYVERSION_MINOR 2
 #define MYVERSION_PATCH 3
 
 enum {
   MYBAD = -1,
   MYOK  = 1
 };
 
 #define MYPI 3.14
 
 #endif

You can define these constants from C:

 #include <ffi_platypus_bundle.h>
 #include "myheader.h"
 
 void ffi_pl_bundle_constant(const char *package, ffi_platypus_constant_t *c)
 {
   c->set_str("MYVERSION_STRING", MYVERSION_STRING);
   c->set_uint("MYVERSION_MAJOR", MYVERSION_MAJOR);
   c->set_uint("MYVERSION_MINOR", MYVERSION_MINOR);
   c->set_uint("MYVERSION_PATCH", MYVERSION_PATCH);
   c->set_sint("MYBAD", MYBAD);
   c->set_sint("MYOK", MYOK);
   c->set_double("MYPI", MYPI);
 }

Your Perl code doesn't have to do anything when calling bundle:

 package Const;
 
 use strict;
 use warnings;
 use FFI::Platypus 2.00;
 
 {
   my $ffi = FFI::Platypus->new( api => 2 );
   $ffi->bundle;
 }
 
 1;

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
