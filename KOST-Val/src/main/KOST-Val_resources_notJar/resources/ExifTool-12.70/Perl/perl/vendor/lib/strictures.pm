package strictures;

use strict;
use warnings FATAL => 'all';

BEGIN {
  *_PERL_LT_5_8_4 = ("$]" < 5.008004) ? sub(){1} : sub(){0};
  # goto &UNIVERSAL::VERSION usually works on 5.8, but fails on some ARM
  # machines.  Seems to always work on 5.10 though.
  *_CAN_GOTO_VERSION = ("$]" >= 5.010000) ? sub(){1} : sub(){0};
}

our $VERSION = '2.000006';
$VERSION =~ tr/_//d;

our @WARNING_CATEGORIES = grep { exists $warnings::Offsets{$_} } qw(
  closure
  chmod
  deprecated
  exiting
  experimental
    experimental::alpha_assertions
    experimental::autoderef
    experimental::bitwise
    experimental::const_attr
    experimental::declared_refs
    experimental::lexical_subs
    experimental::lexical_topic
    experimental::postderef
    experimental::private_use
    experimental::re_strict
    experimental::refaliasing
    experimental::regex_sets
    experimental::script_run
    experimental::signatures
    experimental::smartmatch
    experimental::win32_perlio
  glob
  imprecision
  io
    closed
    exec
    layer
    newline
    pipe
    syscalls
    unopened
  locale
  misc
  missing
  numeric
  once
  overflow
  pack
  portable
  recursion
  redefine
  redundant
  regexp
  severe
    debugging
    inplace
    internal
    malloc
  shadow
  signal
  substr
  syntax
    ambiguous
    bareword
    digit
    illegalproto
    parenthesis
    precedence
    printf
    prototype
    qw
    reserved
    semicolon
  taint
  threads
  uninitialized
  umask
  unpack
  untie
  utf8
    non_unicode
    nonchar
    surrogate
  void
  void_unusual
  y2k
);

sub VERSION {
  {
    no warnings;
    local $@;
    if (defined $_[1] && eval { &UNIVERSAL::VERSION; 1}) {
      $^H |= 0x20000
        unless _PERL_LT_5_8_4;
      $^H{strictures_enable} = int $_[1];
    }
  }
  _CAN_GOTO_VERSION ? goto &UNIVERSAL::VERSION : &UNIVERSAL::VERSION;
}

our %extra_load_states;

our $Smells_Like_VCS;

sub import {
  my $class = shift;
  my %opts = @_ == 1 ? %{$_[0]} : @_;
  if (!exists $opts{version}) {
    $opts{version}
      = exists $^H{strictures_enable} ? delete $^H{strictures_enable}
      : int $VERSION;
  }
  $opts{file} = (caller)[1];
  $class->_enable(\%opts);
}

sub _enable {
  my ($class, $opts) = @_;
  my $version = $opts->{version};
  $version = 'undef'
    if !defined $version;
  my $method = "_enable_$version";
  if (!$class->can($method)) {
    require Carp;
    Carp::croak("Major version specified as $version - not supported!");
  }
  $class->$method($opts);
}

sub _enable_1 {
  my ($class, $opts) = @_;
  strict->import;
  warnings->import(FATAL => 'all');

  if (_want_extra($opts->{file})) {
    _load_extras(qw(indirect multidimensional bareword::filehandles));
    indirect->unimport(':fatal')
      if $extra_load_states{indirect};
    multidimensional->unimport
      if $extra_load_states{multidimensional};
    bareword::filehandles->unimport
      if $extra_load_states{'bareword::filehandles'};
  }
}

our @V2_NONFATAL = grep { exists $warnings::Offsets{$_} } (
  'exec',         # not safe to catch
  'recursion',    # will be caught by other mechanisms
  'internal',     # not safe to catch
  'malloc',       # not safe to catch
  'newline',      # stat on nonexistent file with a newline in it
  'experimental', # no reason for these to be fatal
  'deprecated',   # unfortunately can't make these fatal
  'portable',     # everything worked fine here, just may not elsewhere
);
our @V2_DISABLE = grep { exists $warnings::Offsets{$_} } (
  'once'          # triggers inconsistently, can't be fatalized
);

sub _enable_2 {
  my ($class, $opts) = @_;
  strict->import;
  warnings->import;
  warnings->import(FATAL => @WARNING_CATEGORIES);
  warnings->unimport(FATAL => @V2_NONFATAL);
  warnings->import(@V2_NONFATAL);
  warnings->unimport(@V2_DISABLE);

  if (_want_extra($opts->{file})) {
    _load_extras(qw(indirect multidimensional bareword::filehandles));
    indirect->unimport(':fatal')
      if $extra_load_states{indirect};
    multidimensional->unimport
      if $extra_load_states{multidimensional};
    bareword::filehandles->unimport
      if $extra_load_states{'bareword::filehandles'};
  }
}

sub _want_extra_env {
  if (exists $ENV{PERL_STRICTURES_EXTRA}) {
    if (_PERL_LT_5_8_4 and $ENV{PERL_STRICTURES_EXTRA}) {
      die 'PERL_STRICTURES_EXTRA checks are not available on perls older'
        . "than 5.8.4: please unset \$ENV{PERL_STRICTURES_EXTRA}\n";
    }
    return $ENV{PERL_STRICTURES_EXTRA} ? 1 : 0;
  }
  return undef;
}

sub _want_extra {
  my $file = shift;
  my $want_env = _want_extra_env();
  return $want_env
    if defined $want_env;
  return (
    !_PERL_LT_5_8_4
    and $file =~ /^(?:t|xt|lib|blib)[\\\/]/
    and defined $Smells_Like_VCS ? $Smells_Like_VCS
      : ( $Smells_Like_VCS = !!(
        -e '.git' || -e '.svn' || -e '.hg' || -e '.bzr'
        || (-e '../../dist.ini'
          && (-e '../../.git' || -e '../../.svn' || -e '../../.hg' || -e '../../.bzr' ))
      ))
  );
}

sub _load_extras {
  my @extras = @_;
  my @failed;
  foreach my $mod (@extras) {
    next
      if exists $extra_load_states{$mod};

    $extra_load_states{$mod} = eval "require $mod; 1;" or do {
      push @failed, $mod;

      #work around 5.8 require bug
      (my $file = $mod) =~ s|::|/|g;
      delete $INC{"${file}.pm"};
    };
  }

  if (@failed) {
    my $failed = join ' ', @failed;
    my $extras = join ' ', @extras;
    print STDERR <<EOE;
strictures.pm extra testing active but couldn't load all modules. Missing were:

  $failed

Extra testing is auto-enabled in checkouts only, so if you're the author
of a strictures-using module you need to run:

  cpan $extras

but these modules are not required by your users.
EOE
  }
}

1;

__END__
=head1 NAME

strictures - Turn on strict and make most warnings fatal

=head1 SYNOPSIS

  use strictures 2;

is equivalent to

  use strict;
  use warnings FATAL => 'all';
  use warnings NONFATAL => qw(
    exec
    recursion
    internal
    malloc
    newline
    experimental
    deprecated
    portable
  );
  no warnings 'once';

except when called from a file which matches:

  (caller)[1] =~ /^(?:t|xt|lib|blib)[\\\/]/

and when either C<.git>, C<.svn>, C<.hg>, or C<.bzr> is present in the current
directory (with the intention of only forcing extra tests on the author side)
-- or when C<.git>, C<.svn>, C<.hg>, or C<.bzr> is present two directories up
along with C<dist.ini> (which would indicate we are in a C<dzil test> operation,
via L<Dist::Zilla>) -- or when the C<PERL_STRICTURES_EXTRA> environment variable
is set, in which case it also does the equivalent of

  no indirect 'fatal';
  no multidimensional;
  no bareword::filehandles;

Note that C<PERL_STRICTURES_EXTRA> may at some point add even more tests, with
only a minor version increase, but any changes to the effect of C<use
strictures> in normal mode will involve a major version bump.

If any of the extra testing modules are not present, L<strictures> will
complain loudly, once, via C<warn()>, and then shut up. But you really
should consider installing them, they're all great anti-footgun tools.

=head1 DESCRIPTION

I've been writing the equivalent of this module at the top of my code for
about a year now. I figured it was time to make it shorter.

Things like the importer in C<use Moose> don't help me because they turn
warnings on but don't make them fatal -- which from my point of view is
useless because I want an exception to tell me my code isn't warnings-clean.

Any time I see a warning from my code, that indicates a mistake.

Any time my code encounters a mistake, I want a crash -- not spew to STDERR
and then unknown (and probably undesired) subsequent behaviour.

I also want to ensure that obvious coding mistakes, like indirect object
syntax (and not so obvious mistakes that cause things to accidentally compile
as such) get caught, but not at the cost of an XS dependency and not at the
cost of blowing things up on another machine.

Therefore, L<strictures> turns on additional checking, but only when it thinks
it's running in a test file in a VCS checkout -- although if this causes
undesired behaviour this can be overridden by setting the
C<PERL_STRICTURES_EXTRA> environment variable.

If additional useful author side checks come to mind, I'll add them to the
C<PERL_STRICTURES_EXTRA> code path only -- this will result in a minor version
increase (e.g. 1.000000 to 1.001000 (1.1.0) or similar). Any fixes only to the
mechanism of this code will result in a sub-version increase (e.g. 1.000000 to
1.000001 (1.0.1)).

=head1 CATEGORY SELECTIONS

strictures does not enable fatal warnings for all categories.

=over 4

=item exec

Includes a warning that can cause your program to continue running
unintentionally after an internal fork.  Not safe to fatalize.

=item recursion

Infinite recursion will end up overflowing the stack eventually anyway.

=item internal

Triggers deep within perl, in places that are not safe to trap.

=item malloc

Triggers deep within perl, in places that are not safe to trap.

=item newline

Includes a warning for using stat on a valid but suspect filename, ending in a
newline.

=item experimental

Experimental features are used intentionally.

=item deprecated

Deprecations will inherently be added to in the future in unexpected ways,
so making them fatal won't be reliable.

=item portable

Doesn't indicate an actual problem with the program, only that it may not
behave properly if run on a different machine.

=item once

Can't be fatalized.  Also triggers very inconsistently, so we just disable it.

=back

=head1 VERSIONS

Depending on the version of strictures requested, different warnings will be
enabled.  If no specific version is requested, the current version's behavior
will be used.  Versions can be requested using perl's standard mechanism:

  use strictures 2;

Or, by passing in a C<version> option:

  use strictures version => 2;

=head2 VERSION 2

Equivalent to:

  use strict;
  use warnings FATAL => 'all';
  use warnings NONFATAL => qw(
    exec
    recursion
    internal
    malloc
    newline
    experimental
    deprecated
    portable
  );
  no warnings 'once';

  # and if in dev mode:
  no indirect 'fatal';
  no multidimensional;
  no bareword::filehandles;

Additionally, any warnings created by modules using L<warnings::register> or
C<warnings::register_categories()> will not be fatalized.

=head2 VERSION 1

Equivalent to:

  use strict;
  use warnings FATAL => 'all';
  # and if in dev mode:
  no indirect 'fatal';
  no multidimensional;
  no bareword::filehandles;

=head1 METHODS

=head2 import

This method does the setup work described above in L</DESCRIPTION>.  Optionally
accepts a C<version> option to request a specific version's behavior.

=head2 VERSION

This method traps the C<< strictures->VERSION(1) >> call produced by a use line
with a version number on it and does the version check.

=head1 EXTRA TESTING RATIONALE

Every so often, somebody complains that they're deploying via C<git pull>
and that they don't want L<strictures> to enable itself in this case -- and that
setting C<PERL_STRICTURES_EXTRA> to 0 isn't acceptable (additional ways to
disable extra testing would be welcome but the discussion never seems to get
that far).

In order to allow us to skip a couple of stages and get straight to a
productive conversation, here's my current rationale for turning the
extra testing on via a heuristic:

The extra testing is all stuff that only ever blows up at compile time;
this is intentional. So the oft-raised concern that it's different code being
tested is only sort of the case -- none of the modules involved affect the
final optree to my knowledge, so the author gets some additional compile
time crashes which he/she then fixes, and the rest of the testing is
completely valid for all environments.

The point of the extra testing -- especially C<no indirect> -- is to catch
mistakes that newbie users won't even realise are mistakes without
help. For example,

  foo { ... };

where foo is an & prototyped sub that you forgot to import -- this is
pernicious to track down since all I<seems> fine until it gets called
and you get a crash. Worse still, you can fail to have imported it due
to a circular require, at which point you have a load order dependent
bug which I've seen before now I<only> show up in production due to tiny
differences between the production and the development environment. I wrote
L<http://shadow.cat/blog/matt-s-trout/indirect-but-still-fatal/> to explain
this particular problem before L<strictures> itself existed.

As such, in my experience so far L<strictures>' extra testing has
I<avoided> production versus development differences, not caused them.

Additionally, L<strictures>' policy is very much "try and provide as much
protection as possible for newbies -- who won't think about whether there's
an option to turn on or not" -- so having only the environment variable
is not sufficient to achieve that (I get to explain that you need to add
C<use strict> at least once a week on freenode #perl -- newbies sometimes
completely skip steps because they don't understand that that step
is important).

I make no claims that the heuristic is perfect -- it's already been evolved
significantly over time, especially for 1.004 where we changed things to
ensure it only fires on files in your checkout (rather than L<strictures>-using
modules you happened to have installed, which was just silly). However, I
hope the above clarifies why a heuristic approach is not only necessary but
desirable from a point of view of providing new users with as much safety as
possible, and will allow any future discussion on the subject to focus on "how
do we minimise annoyance to people deploying from checkouts intentionally".

=head1 SEE ALSO

=over 4

=item *

L<indirect>

=item *

L<multidimensional>

=item *

L<bareword::filehandles>

=back

=head1 COMMUNITY AND SUPPORT

=head2 IRC channel

irc.perl.org #toolchain

(or bug 'mst' in query on there or freenode)

=head2 Git repository

Gitweb is on http://git.shadowcat.co.uk/ and the clone URL is:

  git clone git://git.shadowcat.co.uk/p5sagit/strictures.git

The web interface to the repository is at:

  http://git.shadowcat.co.uk/gitweb/gitweb.cgi?p=p5sagit/strictures.git

=head1 AUTHOR

mst - Matt S. Trout (cpan:MSTROUT) <mst@shadowcat.co.uk>

=head1 CONTRIBUTORS

Karen Etheridge (cpan:ETHER) <ether@cpan.org>

Mithaldu - Christian Walde (cpan:MITHALDU) <walde.christian@gmail.com>

haarg - Graham Knop (cpan:HAARG) <haarg@haarg.org>

=head1 COPYRIGHT

Copyright (c) 2010 the strictures L</AUTHOR> and L</CONTRIBUTORS>
as listed above.

=head1 LICENSE

This library is free software and may be distributed under the same terms
as perl itself.

=cut
