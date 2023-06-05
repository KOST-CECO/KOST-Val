use strict;
use warnings;
package Data::Section;
# ABSTRACT: read multiple hunks of data out of your DATA section
$Data::Section::VERSION = '0.200007';
use Encode qw/decode/;
use MRO::Compat 0.09;
use Sub::Exporter 0.979 -setup => {
  groups     => { setup => \'_mk_reader_group' },
  collectors => { INIT => sub { $_[0] = { into => $_[1]->{into} } } },
};

#pod =head1 SYNOPSIS
#pod
#pod   package Letter::Resignation;
#pod   use Data::Section -setup;
#pod
#pod   sub quit {
#pod     my ($class, $angry, %arg) = @_;
#pod
#pod     my $template = $self->section_data(
#pod       ($angry ? "angry_" : "professional_") . "letter"
#pod     );
#pod
#pod     return fill_in($$template, \%arg);
#pod   }
#pod
#pod   __DATA__
#pod   __[ angry_letter ]__
#pod   Dear jerks,
#pod
#pod     I quit!
#pod
#pod   -- 
#pod   {{ $name }}
#pod   __[ professional_letter ]__
#pod   Dear {{ $boss }},
#pod
#pod     I quit, jerks!
#pod
#pod
#pod   -- 
#pod   {{ $name }}
#pod
#pod =head1 DESCRIPTION
#pod
#pod Data::Section provides an easy way to access multiple named chunks of
#pod line-oriented data in your module's DATA section.  It was written to allow
#pod modules to store their own templates, but probably has other uses.
#pod
#pod =head1 WARNING
#pod
#pod You will need to use C<__DATA__> sections and not C<__END__> sections.  Yes, it
#pod matters.  Who knew!
#pod
#pod =head1 EXPORTS
#pod
#pod To get the methods exported by Data::Section, you must import like this:
#pod
#pod   use Data::Section -setup;
#pod
#pod Optional arguments may be given to Data::Section like this:
#pod
#pod   use Data::Section -setup => { ... };
#pod
#pod Valid arguments are:
#pod
#pod   encoding     - if given, gives the encoding needed to decode bytes in
#pod                  data sections; default; UTF-8
#pod
#pod                  the special value "bytes" will leave the bytes in the string
#pod                  verbatim
#pod
#pod   inherit      - if true, allow packages to inherit the data of the packages
#pod                  from which they inherit; default: true
#pod
#pod   header_re    - if given, changes the regex used to find section headers
#pod                  in the data section; it should leave the section name in $1
#pod
#pod   default_name - if given, allows the first section to has no header and set
#pod                  its name
#pod
#pod Three methods are exported by Data::Section:
#pod
#pod =head2 section_data
#pod
#pod   my $string_ref = $pkg->section_data($name); 
#pod
#pod This method returns a reference to a string containing the data from the name
#pod section, either in the invocant's C<DATA> section or in that of one of its
#pod ancestors.  (The ancestor must also derive from the class that imported
#pod Data::Section.)
#pod
#pod By default, named sections are delimited by lines that look like this:
#pod
#pod   __[ name ]__
#pod
#pod You can use as many underscores as you want, and the space around the name is
#pod optional.  This pattern can be configured with the C<header_re> option (see
#pod above).  If present, a single leading C<\> is removed, so that sections can
#pod encode lines that look like section delimiters.
#pod
#pod When a line containing only C<__END__> is reached, all processing of sections
#pod ends.
#pod
#pod =head2 section_data_names
#pod
#pod   my @names = $pkg->section_data_names;
#pod
#pod This returns a list of all the names that will be recognized by the
#pod C<section_data> method.
#pod
#pod =head2 merged_section_data
#pod
#pod   my $data = $pkg->merged_section_data;
#pod
#pod This method returns a hashref containing all the data extracted from the
#pod package data for all the classes from which the invocant inherits -- as long as
#pod those classes also inherit from the package into which Data::Section was
#pod imported.
#pod
#pod In other words, given this inheritance tree:
#pod
#pod   A
#pod    \
#pod     B   C
#pod      \ /
#pod       D
#pod
#pod ...if Data::Section was imported by A, then when D's C<merged_section_data> is
#pod invoked, C's data section will not be considered.  (This prevents the read
#pod position of C's data handle from being altered unexpectedly.)
#pod
#pod The keys in the returned hashref are the section names, and the values are
#pod B<references to> the strings extracted from the data sections.
#pod
#pod =head2 merged_section_data_names
#pod
#pod   my @names = $pkg->merged_section_data_names;
#pod
#pod This returns a list of all the names that will be recognized by the
#pod C<merged_section_data> method.
#pod
#pod =head2 local_section_data
#pod
#pod   my $data = $pkg->local_section_data;
#pod
#pod This method returns a hashref containing all the data extracted from the
#pod package on which the method was invoked.  If called on an object, it will
#pod operate on the package into which the object was blessed.
#pod
#pod This method needs to be used carefully, because it's weird.  It returns only
#pod the data for the package on which it was invoked.  If the package on which it
#pod was invoked has no data sections, it returns an empty hashref.
#pod
#pod =head2 local_section_data_names
#pod
#pod   my @names = $pkg->local_section_data_names;
#pod
#pod This returns a list of all the names that will be recognized by the
#pod C<local_section_data> method.
#pod
#pod =cut

sub _mk_reader_group {
  my ($mixin, $name, $arg, $col) = @_;
  my $base = $col->{INIT}{into};

  my $default_header_re = qr/
    \A                # start
      _+\[            # __[
        \s*           # any whitespace
          ([^\]]+?)   # this is the actual name of the section
        \s*           # any whitespace
      \]_+            # ]__
      [\x0d\x0a]{1,2} # possible cariage return for windows files
    \z                # end
  /x;

  my $header_re = $arg->{header_re} || $default_header_re;
  $arg->{inherit} = 1 unless exists $arg->{inherit};

  my $default_encoding = defined $arg->{encoding} ? $arg->{encoding} : 'UTF-8';

  my %export;
  my %stash = ();

  $export{local_section_data} = sub {
    my ($self) = @_;

    my $pkg = ref $self ? ref $self : $self;

    return $stash{ $pkg } if $stash{ $pkg };

    my $template = $stash{ $pkg } = { };

    my $dh = do { no strict 'refs'; \*{"$pkg\::DATA"} }; ## no critic Strict
    return $stash{ $pkg } unless defined fileno *$dh;
    binmode( $dh, ":raw :bytes" );

    my ($current, $current_line);
    if ($arg->{default_name}) {
        $current = $arg->{default_name};
        $template->{ $current } = \(my $blank = q{});
    }
    LINE: while (my $line = <$dh>) {
      if ($line =~ $header_re) {
        $current = $1;
        $current_line = 0;
        $template->{ $current } = \(my $blank = q{});
        next LINE;
      }

      last LINE if $line =~ /^__END__/;
      next LINE if !defined $current and $line =~ /^\s*$/;

      Carp::confess("bogus data section: text outside of named section")
        unless defined $current;

      $current_line++;
      unless ($default_encoding eq 'bytes') {
        my $decoded_line = eval { decode($default_encoding, $line, Encode::FB_CROAK) }
          or warn "Invalid character encoding in $current, line $current_line\n";
        $line = $decoded_line if defined $decoded_line;
      }
      $line =~ s/\A\\//;

      ${$template->{$current}} .= $line;
    }

    return $stash{ $pkg };
  };

  $export{local_section_data_names} = sub {
    my ($self) = @_;
    my $method = $export{local_section_data};
    return keys %{ $self->$method };
  };

  $export{merged_section_data} =
    !$arg->{inherit} ? $export{local_section_data} : sub {

    my ($self) = @_;
    my $pkg = ref $self ? ref $self : $self;

    my $lsd = $export{local_section_data};

    my %merged;
    for my $class (@{ mro::get_linear_isa($pkg) }) {
      # in case of c3 + non-$base item showing up
      next unless $class->isa($base);
      my $sec_data = $class->$lsd;

      # checking for truth is okay, since things must be undef or a ref
      # -- rjbs, 2008-06-06
      $merged{ $_ } ||= $sec_data->{$_} for keys %$sec_data;
    }

    return \%merged;
  };

  $export{merged_section_data_names} = sub {
    my ($self) = @_;
    my $method = $export{merged_section_data};
    return keys %{ $self->$method };
  };

  $export{section_data} = sub {
    my ($self, $name) = @_;
    my $pkg = ref $self ? ref $self : $self;

    my $prefix = $arg->{inherit} ? 'merged' : 'local';
    my $method = "$prefix\_section_data";

    my $data = $self->$method;

    return $data->{ $name };
  };

  $export{section_data_names} = sub {
    my ($self) = @_;

    my $prefix = $arg->{inherit} ? 'merged' : 'local';
    my $method = "$prefix\_section_data_names";
    return $self->$method;
  };

  return \%export;
}

#pod =head1 TIPS AND TRICKS
#pod
#pod =head2 MooseX::Declare and namespace::autoclean
#pod
#pod The L<namespace::autoclean|namespace::autoclean> library automatically cleans
#pod foreign routines from a class, including those imported by Data::Section.
#pod
#pod L<MooseX::Declare|MooseX::Declare> does the same thing, and can also cause your
#pod C<__DATA__> section to appear outside your class's package.
#pod
#pod These are easy to address.  The
#pod L<Sub::Exporter::ForMethods|Sub::Exporter::ForMethods> library provides an
#pod installer that will cause installed methods to appear to come from the class
#pod and avoid autocleaning.  Using an explicit C<package> statement will keep the
#pod data section in the correct package.
#pod
#pod    package Foo;
#pod
#pod    use MooseX::Declare;
#pod    class Foo {
#pod
#pod      # Utility to tell Sub::Exporter modules to export methods.
#pod      use Sub::Exporter::ForMethods qw( method_installer );
#pod
#pod      # method_installer returns a sub.
#pod      use Data::Section { installer => method_installer }, -setup;
#pod
#pod      method my_method {
#pod         my $content_ref = $self->section_data('SectionA');
#pod
#pod         print $$content_ref;
#pod      }
#pod    }
#pod
#pod    __DATA__
#pod    __[ SectionA ]__
#pod    Hello, world.
#pod
#pod =head1 SEE ALSO
#pod
#pod =begin :list
#pod
#pod * L<article for RJBS Advent 2009|http://advent.rjbs.manxome.org/2009/2009-12-09.html>
#pod
#pod * L<Inline::Files|Inline::Files> does something that is at first look similar,
#pod but it works with source filters, and contains the warning:
#pod
#pod   It is possible that this module may overwrite the source code in files that
#pod   use it. To protect yourself against this possibility, you are strongly
#pod   advised to use the -backup option described in "Safety first".
#pod
#pod Enough said.
#pod
#pod =end :list
#pod
#pod =cut

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Data::Section - read multiple hunks of data out of your DATA section

=head1 VERSION

version 0.200007

=head1 SYNOPSIS

  package Letter::Resignation;
  use Data::Section -setup;

  sub quit {
    my ($class, $angry, %arg) = @_;

    my $template = $self->section_data(
      ($angry ? "angry_" : "professional_") . "letter"
    );

    return fill_in($$template, \%arg);
  }

  __DATA__
  __[ angry_letter ]__
  Dear jerks,

    I quit!

  -- 
  {{ $name }}
  __[ professional_letter ]__
  Dear {{ $boss }},

    I quit, jerks!


  -- 
  {{ $name }}

=head1 DESCRIPTION

Data::Section provides an easy way to access multiple named chunks of
line-oriented data in your module's DATA section.  It was written to allow
modules to store their own templates, but probably has other uses.

=head1 WARNING

You will need to use C<__DATA__> sections and not C<__END__> sections.  Yes, it
matters.  Who knew!

=head1 EXPORTS

To get the methods exported by Data::Section, you must import like this:

  use Data::Section -setup;

Optional arguments may be given to Data::Section like this:

  use Data::Section -setup => { ... };

Valid arguments are:

  encoding     - if given, gives the encoding needed to decode bytes in
                 data sections; default; UTF-8

                 the special value "bytes" will leave the bytes in the string
                 verbatim

  inherit      - if true, allow packages to inherit the data of the packages
                 from which they inherit; default: true

  header_re    - if given, changes the regex used to find section headers
                 in the data section; it should leave the section name in $1

  default_name - if given, allows the first section to has no header and set
                 its name

Three methods are exported by Data::Section:

=head2 section_data

  my $string_ref = $pkg->section_data($name); 

This method returns a reference to a string containing the data from the name
section, either in the invocant's C<DATA> section or in that of one of its
ancestors.  (The ancestor must also derive from the class that imported
Data::Section.)

By default, named sections are delimited by lines that look like this:

  __[ name ]__

You can use as many underscores as you want, and the space around the name is
optional.  This pattern can be configured with the C<header_re> option (see
above).  If present, a single leading C<\> is removed, so that sections can
encode lines that look like section delimiters.

When a line containing only C<__END__> is reached, all processing of sections
ends.

=head2 section_data_names

  my @names = $pkg->section_data_names;

This returns a list of all the names that will be recognized by the
C<section_data> method.

=head2 merged_section_data

  my $data = $pkg->merged_section_data;

This method returns a hashref containing all the data extracted from the
package data for all the classes from which the invocant inherits -- as long as
those classes also inherit from the package into which Data::Section was
imported.

In other words, given this inheritance tree:

  A
   \
    B   C
     \ /
      D

...if Data::Section was imported by A, then when D's C<merged_section_data> is
invoked, C's data section will not be considered.  (This prevents the read
position of C's data handle from being altered unexpectedly.)

The keys in the returned hashref are the section names, and the values are
B<references to> the strings extracted from the data sections.

=head2 merged_section_data_names

  my @names = $pkg->merged_section_data_names;

This returns a list of all the names that will be recognized by the
C<merged_section_data> method.

=head2 local_section_data

  my $data = $pkg->local_section_data;

This method returns a hashref containing all the data extracted from the
package on which the method was invoked.  If called on an object, it will
operate on the package into which the object was blessed.

This method needs to be used carefully, because it's weird.  It returns only
the data for the package on which it was invoked.  If the package on which it
was invoked has no data sections, it returns an empty hashref.

=head2 local_section_data_names

  my @names = $pkg->local_section_data_names;

This returns a list of all the names that will be recognized by the
C<local_section_data> method.

=head1 TIPS AND TRICKS

=head2 MooseX::Declare and namespace::autoclean

The L<namespace::autoclean|namespace::autoclean> library automatically cleans
foreign routines from a class, including those imported by Data::Section.

L<MooseX::Declare|MooseX::Declare> does the same thing, and can also cause your
C<__DATA__> section to appear outside your class's package.

These are easy to address.  The
L<Sub::Exporter::ForMethods|Sub::Exporter::ForMethods> library provides an
installer that will cause installed methods to appear to come from the class
and avoid autocleaning.  Using an explicit C<package> statement will keep the
data section in the correct package.

   package Foo;

   use MooseX::Declare;
   class Foo {

     # Utility to tell Sub::Exporter modules to export methods.
     use Sub::Exporter::ForMethods qw( method_installer );

     # method_installer returns a sub.
     use Data::Section { installer => method_installer }, -setup;

     method my_method {
        my $content_ref = $self->section_data('SectionA');

        print $$content_ref;
     }
   }

   __DATA__
   __[ SectionA ]__
   Hello, world.

=head1 SEE ALSO

=over 4

=item *

L<article for RJBS Advent 2009|http://advent.rjbs.manxome.org/2009/2009-12-09.html>

=item *

L<Inline::Files|Inline::Files> does something that is at first look similar,

but it works with source filters, and contains the warning:

  It is possible that this module may overwrite the source code in files that
  use it. To protect yourself against this possibility, you are strongly
  advised to use the -backup option described in "Safety first".

Enough said.

=back

=head1 AUTHOR

Ricardo SIGNES <rjbs@cpan.org>

=head1 CONTRIBUTORS

=for stopwords Christian Walde Dan Kogai David Golden Steinbrunner Karen Etheridge Kenichi Ishigaki kentfredric Tatsuhiko Miyagawa

=over 4

=item *

Christian Walde <walde.christian@googlemail.com>

=item *

Dan Kogai <dankogai+github@gmail.com>

=item *

David Golden <dagolden@cpan.org>

=item *

David Steinbrunner <dsteinbrunner@pobox.com>

=item *

Karen Etheridge <ether@cpan.org>

=item *

Kenichi Ishigaki <ishigaki@cpan.org>

=item *

kentfredric <kentfredric+gravitar@gmail.com>

=item *

Tatsuhiko Miyagawa <miyagawa@bulknews.net>

=back

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2008 by Ricardo SIGNES.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
