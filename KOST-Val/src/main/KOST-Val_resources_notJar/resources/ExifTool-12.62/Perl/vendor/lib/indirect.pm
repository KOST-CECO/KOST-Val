package indirect;

use 5.008_001;

use strict;
use warnings;

=head1 NAME

indirect - Lexically warn about using the indirect method call syntax.

=head1 VERSION

Version 0.39

=cut

our $VERSION;
BEGIN {
 $VERSION = '0.39';
}

=head1 SYNOPSIS

In a script :

    no indirect;               # lexically enables the pragma
    my $x = new Apple 1, 2, 3; # warns
    {
     use indirect;     # lexically disables the pragma
     my $y = new Pear; # legit, does not warn
     {
      # lexically specify an hook called for each indirect construct
      no indirect hook => sub {
       die "You really wanted $_[0]\->$_[1] at $_[2]:$_[3]"
      };
      my $z = new Pineapple 'fresh'; # croaks 'You really wanted...'
     }
    }
    try { ... }; # warns if try() hasn't been declared in this package

    no indirect 'fatal';     # or ':fatal', 'FATAL', ':Fatal' ...
    if (defied $foo) { ... } # croaks, note the typo

Global uses :

    # Globally enable the pragma from the command-line
    perl -M-indirect=global -e 'my $x = new Banana;' # warns

    # Globally enforce the pragma each time perl is executed
    export PERL5OPT="-M-indirect=global,fatal"
    perl -e 'my $y = new Coconut;' # croaks

=head1 DESCRIPTION

When enabled, this pragma warns about indirect method calls that are present in your code.

The indirect syntax is now considered harmful, since its parsing has many quirks and its use is error prone : when the subroutine C<foo> has not been declared in the current package, C<foo $x> actually compiles to C<< $x->foo >>, and C<< foo { key => 1 } >> to C<< 'key'->foo(1) >>.
Please refer to the L</REFERENCES> section for a more complete list of reasons for avoiding this construct.

This pragma currently does not warn for core functions (C<print>, C<say>, C<exec> or C<system>).
This may change in the future, or may be added as optional features that would be enabled by passing options to C<unimport>.

This module is B<not> a source filter.

=cut

BEGIN {
 if ($ENV{PERL_INDIRECT_PM_DISABLE}) {
  *_tag = sub ($) { 1 };
  *I_THREADSAFE = sub () { 1 };
  *I_FORKSAFE   = sub () { 1 };
 } else {
  require XSLoader;
  XSLoader::load(__PACKAGE__, $VERSION);
 }
}

=head1 METHODS

=head2 C<unimport>

    no indirect;
    no indirect 'fatal';
    no indirect hook => sub { my ($obj, $name, $file, $line) = @_; ... };
    no indirect 'global';
    no indirect 'global, 'fatal';
    no indirect 'global', hook => sub { ... };

Magically called when C<no indirect @opts> is encountered.
Turns the module on.
The policy to apply depends on what is first found in C<@opts> :

=over 4

=item *

If it is a string that matches C</^:?fatal$/i>, the compilation will croak when the first indirect method call is found.

This option is mutually exclusive with the C<'hook'> option.

=item *

If the key/value pair C<< hook => $hook >> comes first, C<$hook> will be called for each error with a string representation of the object as C<$_[0]>, the method name as C<$_[1]>, the current file as C<$_[2]> and the line number as C<$_[3]>.
If and only if the object is actually a block, C<$_[0]> is assured to start by C<'{'>.

This option is mutually exclusive with the C<'fatal'> option.

=item *

If none of C<fatal> and C<hook> are specified, a warning will be emitted for each indirect method call.

=item *

If C<@opts> contains a string that matches C</^:?global$/i>, the pragma will be globally enabled for B<all> code compiled after the current C<no indirect> statement, except for code that is in the lexical scope of C<use indirect>.
This option may come indifferently before or after the C<fatal> or C<hook> options, in the case they are also passed to L</unimport>.

The global policy applied is the one resulting of the C<fatal> or C<hook> options, thus defaults to a warning when none of those are specified :

    no indirect 'global';                # warn for any indirect call
    no indirect qw<global fatal>;        # die on any indirect call
    no indirect 'global', hook => \&hook # custom global action

Note that if another policy is installed by a C<no indirect> statement further in the code, it will overrule the global policy :

    no indirect 'global';  # warn globally
    {
     no indirect 'fatal';  # throw exceptions for this lexical scope
     ...
     require Some::Module; # the global policy will apply for the
                           # compilation phase of this module
    }

=back

=cut

sub _no_hook_and_fatal {
 require Carp;
 Carp::croak("The 'fatal' and 'hook' options are mutually exclusive");
}

sub unimport {
 shift;

 my ($global, $fatal, $hook);

 while (@_) {
  my $arg = shift;
  if ($arg eq 'hook') {
   _no_hook_and_fatal() if $fatal;
   $hook = shift;
  } elsif ($arg =~ /^:?fatal$/i) {
   _no_hook_and_fatal() if defined $hook;
   $fatal = 1;
  } elsif ($arg =~ /^:?global$/i) {
   $global = 1;
  }
 }

 unless (defined $hook) {
  $hook = $fatal ? sub { die msg(@_) } : sub { warn msg(@_) };
 }

 $^H |= 0x00020000;
 if ($global) {
  delete $^H{+(__PACKAGE__)};
  _global($hook);
 } else {
  $^H{+(__PACKAGE__)} = _tag($hook);
 }

 return;
}

=head2 C<import>

    use indirect;

Magically called at each C<use indirect>. Turns the module off.

As explained in L</unimport>'s description, an C<use indirect> statement will lexically override a global policy previously installed by C<no indirect 'global', ...> (if there's one).

=cut

sub import {
 $^H |= 0x00020000;
 $^H{+(__PACKAGE__)} = _tag(undef);

 return;
}

=head1 FUNCTIONS

=head2 C<msg>

    my $msg = msg($object, $method, $file, $line);

Returns the default error message that C<indirect> generates when an indirect method call is reported.

=cut

sub msg {
 my $obj = $_[0];

 join ' ', "Indirect call of method \"$_[1]\" on",
           ($obj =~ /^\s*\{/ ? "a block" : "object \"$obj\""),
           "at $_[2] line $_[3].\n";
};

=head1 CONSTANTS

=head2 C<I_THREADSAFE>

True iff the module could have been built with thread-safety features enabled.

=head2 C<I_FORKSAFE>

True iff this module could have been built with fork-safety features enabled.
This will always be true except on Windows where it's false for perl 5.10.0 and below .

=head1 DIAGNOSTICS

=head2 C<Indirect call of method "%s" on object "%s" at %s line %d.>

The default warning/exception message thrown when an indirect method call on an object is found.

=head2 C<Indirect call of method "%s" on a block at %s line %d.>

The default warning/exception message thrown when an indirect method call on a block is found.

=head1 ENVIRONMENT

=head2 C<PERL_INDIRECT_PM_DISABLE>

If this environment variable is set to true when the pragma is used for the first time, the XS code won't be loaded and, although the C<'indirect'> lexical hint will be set to true in the scope of use, the pragma itself won't do anything.
In this case, the pragma will always be considered to be thread-safe, and as such L</I_THREADSAFE> will be true.
This is useful for disabling C<indirect> in production environments.

Note that clearing this variable after C<indirect> was loaded has no effect.
If you want to re-enable the pragma later, you also need to reload it by deleting the C<'indirect.pm'> entry from C<%INC>.

=head1 CAVEATS

The implementation was tweaked to work around several limitations of vanilla C<perl> pragmas : it's thread safe, and does not suffer from a C<perl 5.8.x-5.10.0> bug that causes all pragmas to propagate into C<require>d scopes.

Before C<perl> 5.12, C<meth $obj> (no semicolon) at the end of a file is not seen as an indirect method call, although it is as soon as there is another token before the end (as in C<meth $obj;> or C<meth $obj 1>).
If you use C<perl> 5.12 or greater, those constructs are correctly reported.

With 5.8 perls, the pragma does not propagate into C<eval STRING>.
This is due to a shortcoming in the way perl handles the hints hash, which is addressed in perl 5.10.

The search for indirect method calls happens before constant folding.
Hence C<my $x = new Class if 0> will be caught.

=head1 REFERENCES

Numerous articles have been written about the quirks of the indirect object construct :

=over 4

=item *

L<http://markmail.org/message/o7d5sxnydya7bwvv> : B<Far More Than Everything You've Ever Wanted to Know about the Indirect Object syntax>, Tom Christiansen, 1998-01-28.

This historical post to the C<perl5-porters> mailing list raised awareness about the perils of this syntax.

=item *

L<http://www.shadowcat.co.uk/blog/matt-s-trout/indirect-but-still-fatal> : B<Indirect but still fatal>, Matt S. Trout, 2009-07-29.

In this blog post, the author gives an example of an undesirable indirect method call on a block that causes a particularly bewildering error.

=back

=head1 DEPENDENCIES

L<perl> 5.8.1.

A C compiler.
This module may happen to build with a C++ compiler as well, but don't rely on it, as no guarantee is made in this regard.

L<Carp> (standard since perl 5), L<XSLoader> (since perl 5.6.0).

=head1 AUTHOR

Vincent Pit C<< <vpit@cpan.org> >>.

You can contact me by mail or on C<irc.perl.org> (vincent).

=head1 BUGS

Please report any bugs or feature requests to C<bug-indirect at rt.cpan.org>, or through the web interface at L<http://rt.cpan.org/NoAuth/ReportBug.html?Queue=indirect>.
I will be notified, and then you'll automatically be notified of progress on your bug as I make changes.

=head1 SUPPORT

You can find documentation for this module with the perldoc command.

    perldoc indirect

=head1 ACKNOWLEDGEMENTS

Bram, for motivation and advices.

Andrew Main and Florian Ragwitz, for testing on real-life code and reporting issues.

=head1 COPYRIGHT & LICENSE

Copyright 2008,2009,2010,2011,2012,2013,2014,2015,2016,2017,2019 Vincent Pit, all rights reserved.

This program is free software; you can redistribute it and/or modify it under the same terms as Perl itself.

=cut

1; # End of indirect
