package Test::UseAllModules;

use strict;
use warnings;
use ExtUtils::Manifest qw( maniread );

our $VERSION = '0.17';

use Exporter;

our @ISA = qw/Exporter/;
our @EXPORT = qw/all_uses_ok/;

use Test::More;

my $RULE = qr{^lib/(.+)\.pm$};

sub import {
  shift->export_to_level(1);

  shift if @_ && $_[0] eq 'under';
  my @dirs = ('lib', @_);
  my %seen;
  @dirs  = grep { !$seen{$_}++ } map  { s|/+$||; $_ } @dirs;
  $RULE = '^(?:'.(join '|', @dirs).')/(.+)\.pm\s*$';
  unshift @INC, @dirs;
}

sub _get_module_list {
  shift if @_ && $_[0] eq 'except';
  my @exceptions = @_;
  my @modules;

  my $manifest = maniread();

READ:
  foreach my $file (keys %{ $manifest }) {
    if (my ($module) = $file =~ m|$RULE|) {
      $module =~ s|/|::|g;

      foreach my $rule (@exceptions) {
        next READ if $module eq $rule || $module =~ /$rule/;
      }

      push @modules, $module;
    }
  }
  return @modules;
}

sub _planned {
  Test::More->builder->has_plan;
}

sub all_uses_ok {
  unless (-f 'MANIFEST') {
    plan skip_all => 'no MANIFEST' unless _planned();
    return;
  }

  my @modules = _get_module_list(@_);

  unless (@modules) {
    plan skip_all => 'no .pm files are found under the lib directory' unless _planned();
    return;
  }
  plan tests => scalar @modules unless _planned();

  my @failed;
  foreach my $module (@modules) {
    use_ok($module) or push @failed, $module;
  }

  BAIL_OUT( 'failed: ' . (join ',', @failed) ) if @failed;
}

1;
__END__

=head1 NAME

Test::UseAllModules - do use_ok() for all the MANIFESTed modules

=head1 SYNOPSIS

  # basic usage
  use strict;
  use Test::UseAllModules;
  
  BEGIN { all_uses_ok(); }

  # if you also want to test modules under t/lib
  use strict;
  use Test::UseAllModules under => qw(lib t/lib);
  
  BEGIN { all_uses_ok(); }

  # if you have modules that'll fail use_ok() for themselves
  use strict;
  use Test::UseAllModules;
  
  BEGIN {
    all_uses_ok except => qw(
      Some::Dependent::Module
      Another::Dependent::Module
      ^Yet::Another::Dependent::.*   # you can use regex
    )
  }

=head1 DESCRIPTION

I'm sick of writing 00_load.t (or something like that) that'll do use_ok() for every module I write. I'm sicker of updating 00_load.t when I add another file to the distro. This module reads MANIFEST to find modules to be tested and does use_ok() for each of them. Now all you have to do is update MANIFEST. You don't have to modify the test any more (hopefully).

=head1 EXPORTED FUNCTION

=head2 all_uses_ok

Does Test::More's use_ok() for every module found in MANIFEST. If you have modules you don't want to test, give those modules or some regex rules as the argument. The word 'except' is ignored as shown above. 

As of 0.11, you can also test modules under arbitrary directories by providing a directory list at the loading time (the word 'under' is ignored as shown above). Modules under the lib directory are always tested.

=head1 PROTECTED FUNCTION

=head2 _get_module_list

Returns module paths to test. This function will not be exported. If you want to use this (see below), you always need to call it by the full qualified name.

=head1 NOTES

As of 0.03, this module calls BAIL_OUT of Test::More if any of the use_ok tests should fail. (Thus the following tests will be ignored. Missing or unloadable modules cause a lot of errors of the same kind.)

As of 0.12, you can add extra tests before/after all_uses_ok() if you explicitly declare test plan like this.

  use strict;
  use warnings;
  use Test::More;
  use Test::UseAllModules;
  use Test::NoWarnings;

  plan tests => Test::UseAllModules::_get_module_list() + 1;

  all_uses_ok();

  # and extra nowarnings test

=head1 SEE ALSO

There're several modules like this on the CPAN now. L<Test::Compile> and a bit confusing L<Test::LoadAllModules> try to find modules to test by traversing directories. I'm not a big fan of them as they tend to find temporary or unrelated modules as well, but they may be handier especially if you're too lazy to update MANIFEST every time.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2006 by Kenichi Ishigaki

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut
