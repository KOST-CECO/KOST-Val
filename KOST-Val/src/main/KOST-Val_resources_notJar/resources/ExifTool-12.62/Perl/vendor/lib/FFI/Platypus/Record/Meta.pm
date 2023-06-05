package FFI::Platypus::Record::Meta;

use strict;
use warnings;
use 5.008004;

# ABSTRACT: FFI support for structured records data
our $VERSION = '1.34'; # VERSION


{
  require FFI::Platypus;
  my $ffi = FFI::Platypus->new(
    api          => 1,
  );
  $ffi->bundle;
  $ffi->mangler(sub {
    my($name) = @_;
    $name =~ s/^/ffi_platypus_record_meta__/;
    $name;
  });

  $ffi->type('opaque' => 'ffi_type');

  $ffi->custom_type('meta_t' => {
    native_type    => 'opaque',
    perl_to_native => sub {
      ${ $_[0] };
    },
  });

  $ffi->attach( _find_symbol => ['string'] => 'ffi_type');

  $ffi->attach( new => ['ffi_type[]'] => 'meta_t', sub {
    my($xsub, $class, $elements) = @_;

    if(ref($elements) ne 'ARRAY')
    {
      require Carp;
      Carp::croak("passed something other than a array ref to @{[ __PACKAGE__ ]}");
    }

    my @element_type_pointers;
    foreach my $element_type (@$elements)
    {
      my $ptr = _find_symbol($element_type);
      if($ptr)
      {
        push @element_type_pointers, $ptr;
      }
      else
      {
        require Carp;
        Carp::croak("unknown type: $element_type");
      }
    }

    push @element_type_pointers, undef;

    my $ptr = $xsub->(\@element_type_pointers);
    bless \$ptr, $class;
  });

  $ffi->attach( ffi_type         => ['meta_t'] => 'ffi_type'   );
  $ffi->attach( size             => ['meta_t'] => 'size_t'     );
  $ffi->attach( alignment        => ['meta_t'] => 'ushort'     );
  $ffi->attach( element_pointers => ['meta_t'] => 'ffi_type[]' );

  $ffi->attach( DESTROY          => ['meta_t'] => 'void'       );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Record::Meta - FFI support for structured records data

=head1 VERSION

version 1.34

=head1 DESCRIPTION

This class is private to FFI::Platypus.  See L<FFI::Platypus::Record> for
the public interface to Platypus records.

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
