use strict;
use warnings;
package Sub::Exporter::Util;
# ABSTRACT: utilities to make Sub::Exporter easier
$Sub::Exporter::Util::VERSION = '0.989';
use Data::OptList ();
use Params::Util ();

#pod =head1 DESCRIPTION
#pod
#pod This module provides a number of utility functions for performing common or
#pod useful operations when setting up a Sub::Exporter configuration.  All of the
#pod utilities may be exported, but none are by default.
#pod
#pod =head1 THE UTILITIES
#pod
#pod =head2 curry_method
#pod
#pod   exports => {
#pod     some_method => curry_method,
#pod   }
#pod
#pod This utility returns a generator which will produce an invocant-curried version
#pod of a method.  In other words, it will export a method call with the exporting
#pod class built in as the invocant.
#pod
#pod A module importing the code some the above example might do this:
#pod
#pod   use Some::Module qw(some_method);
#pod
#pod   my $x = some_method;
#pod
#pod This would be equivalent to:
#pod
#pod   use Some::Module;
#pod
#pod   my $x = Some::Module->some_method;
#pod
#pod If Some::Module is subclassed and the subclass's import method is called to
#pod import C<some_method>, the subclass will be curried in as the invocant.
#pod
#pod If an argument is provided for C<curry_method> it is used as the name of the
#pod curried method to export.  This means you could export a Widget constructor
#pod like this:
#pod
#pod   exports => { widget => curry_method('new') }
#pod
#pod This utility may also be called as C<curry_class>, for backwards compatibility.
#pod
#pod =cut

sub curry_method {
  my $override_name = shift;
  sub {
    my ($class, $name) = @_;
    $name = $override_name if defined $override_name;
    sub { $class->$name(@_); };
  }
}

BEGIN { *curry_class = \&curry_method; }

#pod =head2 curry_chain
#pod
#pod C<curry_chain> behaves like C<L</curry_method>>, but is meant for generating
#pod exports that will call several methods in succession.
#pod
#pod   exports => {
#pod     reticulate => curry_chain(
#pod       new => gather_data => analyze => [ detail => 100 ] => 'results'
#pod     ),
#pod   }
#pod
#pod If imported from Spliner, calling the C<reticulate> routine will be equivalent
#pod to:
#pod
#pod   Spliner->new->gather_data->analyze(detail => 100)->results;
#pod
#pod If any method returns something on which methods may not be called, the routine
#pod croaks.
#pod
#pod The arguments to C<curry_chain> form an optlist.  The names are methods to be
#pod called and the arguments, if given, are arrayrefs to be dereferenced and passed
#pod as arguments to those methods.  C<curry_chain> returns a generator like those
#pod expected by Sub::Exporter.
#pod
#pod B<Achtung!> at present, there is no way to pass arguments from the generated
#pod routine to the method calls.  This will probably be solved in future revisions
#pod by allowing the opt list's values to be subroutines that will be called with
#pod the generated routine's stack.
#pod
#pod =cut

sub curry_chain {
  # In the future, we can make \%arg an optional prepend, like the "special"
  # args to the default Sub::Exporter-generated import routine.
  my (@opt_list) = @_;

  my $pairs = Data::OptList::mkopt(\@opt_list, 'args', 'ARRAY');

  sub {
    my ($class) = @_;

    sub {
      my $next = $class;

      for my $i (0 .. $#$pairs) {
        my $pair = $pairs->[ $i ];
        
        unless (Params::Util::_INVOCANT($next)) { ## no critic Private
          my $str = defined $next ? "'$next'" : 'undef';
          Carp::croak("can't call $pair->[0] on non-invocant $str")
        }

        my ($method, $args) = @$pair;

        if ($i == $#$pairs) {
          return $next->$method($args ? @$args : ());
        } else {
          $next = $next->$method($args ? @$args : ());
        }
      }
    };
  }
}

# =head2 name_map
# 
# This utility returns an list to be used in specify export generators.  For
# example, the following:
# 
#   exports => {
#     name_map(
#       '_?_gen'  => [ qw(fee fie) ],
#       '_make_?' => [ qw(foo bar) ],
#     ),
#   }
# 
# is equivalent to:
# 
#   exports => {
#     name_map(
#       fee => \'_fee_gen',
#       fie => \'_fie_gen',
#       foo => \'_make_foo',
#       bar => \'_make_bar',
#     ),
#   }
# 
# This can save a lot of typing, when providing many exports with similarly-named
# generators.
# 
# =cut
# 
# sub name_map {
#   my (%groups) = @_;
# 
#   my %map;
# 
#   while (my ($template, $names) = each %groups) {
#     for my $name (@$names) {
#       (my $export = $template) =~ s/\?/$name/
#         or Carp::croak 'no ? found in name_map template';
# 
#       $map{ $name } = \$export;
#     }
#   }
# 
#   return %map;
# }

#pod =head2 merge_col
#pod
#pod   exports => {
#pod     merge_col(defaults => {
#pod       twiddle => \'_twiddle_gen',
#pod       tweak   => \&_tweak_gen,
#pod     }),
#pod   }
#pod
#pod This utility wraps the given generator in one that will merge the named
#pod collection into its args before calling it.  This means that you can support a
#pod "default" collector in multiple exports without writing the code each time.
#pod
#pod You can specify as many pairs of collection names and generators as you like.
#pod
#pod =cut

sub merge_col {
  my (%groups) = @_;

  my %merged;

  while (my ($default_name, $group) = each %groups) {
    while (my ($export_name, $gen) = each %$group) {
      $merged{$export_name} = sub {
        my ($class, $name, $arg, $col) = @_;

        my $merged_arg = exists $col->{$default_name}
                       ? { %{ $col->{$default_name} }, %$arg }
                       : $arg;

        if (Params::Util::_CODELIKE($gen)) { ## no critic Private
          $gen->($class, $name, $merged_arg, $col);
        } else {
          $class->$$gen($name, $merged_arg, $col);
        }
      }
    }
  }

  return %merged;
}

#pod =head2 mixin_installer
#pod
#pod   use Sub::Exporter -setup => {
#pod     installer => Sub::Exporter::Util::mixin_installer,
#pod     exports   => [ qw(foo bar baz) ],
#pod   };
#pod
#pod This utility returns an installer that will install into a superclass and
#pod adjust the ISA importing class to include the newly generated superclass.
#pod
#pod If the target of importing is an object, the hierarchy is reversed: the new
#pod class will be ISA the object's class, and the object will be reblessed.
#pod
#pod B<Prerequisites>: This utility requires that Package::Generator be installed.
#pod
#pod =cut

sub __mixin_class_for {
  my ($class, $mix_into) = @_;
  require Package::Generator;
  my $mixin_class = Package::Generator->new_package({
    base => "$class\:\:__mixin__",
  });

  ## no critic (ProhibitNoStrict)
  no strict 'refs';
  if (ref $mix_into) {
    unshift @{"$mixin_class" . "::ISA"}, ref $mix_into;
  } else {
    unshift @{"$mix_into" . "::ISA"}, $mixin_class;
  }
  return $mixin_class;
}

sub mixin_installer {
  sub {
    my ($arg, $to_export) = @_;

    my $mixin_class = __mixin_class_for($arg->{class}, $arg->{into});
    bless $arg->{into} => $mixin_class if ref $arg->{into};

    Sub::Exporter::default_installer(
      { %$arg, into => $mixin_class },
      $to_export,
    );
  };
}

sub mixin_exporter {
  Carp::cluck "mixin_exporter is deprecated; use mixin_installer instead; it behaves identically";
  return mixin_installer;
}

#pod =head2 like
#pod
#pod It's a collector that adds imports for anything like given regex.
#pod
#pod If you provide this configuration:
#pod
#pod   exports    => [ qw(igrep imap islurp exhausted) ],
#pod   collectors => { -like => Sub::Exporter::Util::like },
#pod
#pod A user may import from your module like this:
#pod
#pod   use Your::Iterator -like => qr/^i/; # imports igre, imap, islurp
#pod
#pod or
#pod
#pod   use Your::Iterator -like => [ qr/^i/ => { -prefix => 'your_' } ];
#pod
#pod The group-like prefix and suffix arguments are respected; other arguments are
#pod passed on to the generators for matching exports.
#pod
#pod =cut

sub like {
  sub {
    my ($value, $arg) = @_;
    Carp::croak "no regex supplied to regex group generator" unless $value;

    # Oh, qr//, how you bother me!  See the p5p thread from around now about
    # fixing this problem... too bad it won't help me. -- rjbs, 2006-04-25
    my @values = eval { $value->isa('Regexp') } ? ($value, undef)
               :                                  @$value;

    while (my ($re, $opt) = splice @values, 0, 2) {
      Carp::croak "given pattern for regex group generater is not a Regexp"
        unless eval { $re->isa('Regexp') };
      my @exports  = keys %{ $arg->{config}->{exports} };
      my @matching = grep { $_ =~ $re } @exports;

      my %merge = $opt ? %$opt : ();
      my $prefix = (delete $merge{-prefix}) || '';
      my $suffix = (delete $merge{-suffix}) || '';

      for my $name (@matching) {
        my $as = $prefix . $name . $suffix;
        push @{ $arg->{import_args} }, [ $name => { %merge, -as => $as } ];
      }
    }

    1;
  }
}

use Sub::Exporter -setup => {
  exports => [ qw(
    like
    name_map
    merge_col
    curry_method curry_class
    curry_chain
    mixin_installer mixin_exporter
  ) ]
};

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Sub::Exporter::Util - utilities to make Sub::Exporter easier

=head1 VERSION

version 0.989

=head1 DESCRIPTION

This module provides a number of utility functions for performing common or
useful operations when setting up a Sub::Exporter configuration.  All of the
utilities may be exported, but none are by default.

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 THE UTILITIES

=head2 curry_method

  exports => {
    some_method => curry_method,
  }

This utility returns a generator which will produce an invocant-curried version
of a method.  In other words, it will export a method call with the exporting
class built in as the invocant.

A module importing the code some the above example might do this:

  use Some::Module qw(some_method);

  my $x = some_method;

This would be equivalent to:

  use Some::Module;

  my $x = Some::Module->some_method;

If Some::Module is subclassed and the subclass's import method is called to
import C<some_method>, the subclass will be curried in as the invocant.

If an argument is provided for C<curry_method> it is used as the name of the
curried method to export.  This means you could export a Widget constructor
like this:

  exports => { widget => curry_method('new') }

This utility may also be called as C<curry_class>, for backwards compatibility.

=head2 curry_chain

C<curry_chain> behaves like C<L</curry_method>>, but is meant for generating
exports that will call several methods in succession.

  exports => {
    reticulate => curry_chain(
      new => gather_data => analyze => [ detail => 100 ] => 'results'
    ),
  }

If imported from Spliner, calling the C<reticulate> routine will be equivalent
to:

  Spliner->new->gather_data->analyze(detail => 100)->results;

If any method returns something on which methods may not be called, the routine
croaks.

The arguments to C<curry_chain> form an optlist.  The names are methods to be
called and the arguments, if given, are arrayrefs to be dereferenced and passed
as arguments to those methods.  C<curry_chain> returns a generator like those
expected by Sub::Exporter.

B<Achtung!> at present, there is no way to pass arguments from the generated
routine to the method calls.  This will probably be solved in future revisions
by allowing the opt list's values to be subroutines that will be called with
the generated routine's stack.

=head2 merge_col

  exports => {
    merge_col(defaults => {
      twiddle => \'_twiddle_gen',
      tweak   => \&_tweak_gen,
    }),
  }

This utility wraps the given generator in one that will merge the named
collection into its args before calling it.  This means that you can support a
"default" collector in multiple exports without writing the code each time.

You can specify as many pairs of collection names and generators as you like.

=head2 mixin_installer

  use Sub::Exporter -setup => {
    installer => Sub::Exporter::Util::mixin_installer,
    exports   => [ qw(foo bar baz) ],
  };

This utility returns an installer that will install into a superclass and
adjust the ISA importing class to include the newly generated superclass.

If the target of importing is an object, the hierarchy is reversed: the new
class will be ISA the object's class, and the object will be reblessed.

B<Prerequisites>: This utility requires that Package::Generator be installed.

=head2 like

It's a collector that adds imports for anything like given regex.

If you provide this configuration:

  exports    => [ qw(igrep imap islurp exhausted) ],
  collectors => { -like => Sub::Exporter::Util::like },

A user may import from your module like this:

  use Your::Iterator -like => qr/^i/; # imports igre, imap, islurp

or

  use Your::Iterator -like => [ qr/^i/ => { -prefix => 'your_' } ];

The group-like prefix and suffix arguments are respected; other arguments are
passed on to the generators for matching exports.

=head1 AUTHOR

Ricardo Signes <cpan@semiotic.systems>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2007 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
