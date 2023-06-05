package Perl::PrereqScanner::NotQuiteLite::App;

use strict;
use warnings;
use File::Find;
use File::Glob 'bsd_glob';
use File::Basename;
use File::Spec;
use CPAN::Meta::Prereqs;
use CPAN::Meta::Requirements;
use Perl::PrereqScanner::NotQuiteLite;
use Perl::PrereqScanner::NotQuiteLite::Util::Prereqs;

use constant WIN32 => $^O eq 'MSWin32';

my %IsTestClassFamily = map {$_ => 1} qw(
  Test::Class
  Test::Class::Moose
  Test::Class::Most
  Test::Class::Sugar
  Test::Classy
);

sub new {
  my ($class, %opts) = @_;

  for my $key (keys %opts) {
    next unless $key =~ /\-/;
    (my $replaced_key = $key) =~ s/\-/_/g;
    $opts{$replaced_key} = $opts{$key};
  }

  $opts{prereqs} = CPAN::Meta::Prereqs->new;
  $opts{parsers} = [':bundled'] unless defined $opts{parsers};
  $opts{recommends} = 0 unless defined $opts{recommends};
  $opts{suggests} = 0 unless defined $opts{suggests};
  $opts{base_dir} ||= File::Spec->curdir;

  $opts{cpanfile} = 1 if $opts{save_cpanfile};

  if ($opts{features} and ref $opts{features} ne 'HASH') {
    my @features;
    if (!ref $opts{features}) {
      @features = split ';', $opts{features};
    } elsif (ref $opts{features} eq 'ARRAY') {
      @features = @{$opts{features}};
    }
    my %map;
    for my $spec (@features) {
      my ($identifier, $description, $paths) = split ':', $spec;
      my @paths = map { bsd_glob(File::Spec->catdir($opts{base_dir}, $_)) } split ',', $paths;
      if (WIN32) {
          s|\\|/|g for @paths;
      }
      $map{$identifier} = {
        description => $description,
        paths => \@paths,
      };
    }
    $opts{features} = \%map;
  }

  if ($opts{ignore} and ref $opts{ignore} eq 'ARRAY') {
    require Regexp::Trie;
    my $re = Regexp::Trie->new;
    for (@{$opts{ignore}}) {
        s|\\|/|g if WIN32;
        $re->add($_);
    }
    $opts{ignore_re} ||= $re->_regexp;
  }

  if ($opts{private} and ref $opts{private} eq 'ARRAY') {
    require Regexp::Trie;
    my $re = Regexp::Trie->new;
    for (@{$opts{private}}) {
        $re->add($_);
    }
    $opts{private_re} ||= $re->_regexp;
  }

  if (my $index_name = delete $opts{use_index}) {
    my $index_package = "CPAN::Common::Index::$index_name";
    if (eval "require $index_package; 1") {
      $opts{index} = $index_package->new;
    }
  }

  if ($opts{scan_also}) {
    $opts{libs} ||= delete $opts{scan_also};
  }

  bless \%opts, $class;
}

sub run {
  my ($self, @args) = @_;

  unless (@args) {
    # for configure requires
    push @args, "Makefile.PL", "Build.PL";

    # for test requires
    push @args, "t";

    # for runtime requires;
    if ($self->{blib} and -d File::Spec->catdir($self->{base_dir}, 'blib')) {
      push @args, "blib/lib", "blib/bin", "blib/script";
    } else {
      push @args, "lib";
      push @args, glob(File::Spec->catfile($self->{base_dir}, '*.pm'));
      push @args, "bin", "script", "scripts";
    }

    # extra libs
    push @args, map { bsd_glob(File::Spec->catdir($self->{base_dir}, $_)) } @{$self->{libs} || []};

    # for develop requires
    push @args, "xt", "author" if $self->{develop};
  }

  if ($self->{verbose}) {
    print STDERR "Scanning the following files/directories\n";
    print STDERR "  $_\n" for sort @args;
  }

  for my $path (@args) {
    my $item = File::Spec->file_name_is_absolute($path) ? $path : File::Spec->catfile($self->{base_dir}, $path);
    -d $item ? $self->_scan_dir($item) :
    -f $item ? $self->_scan_file($item) :
    next;
  }

  # add test requirements by .pm files used in .t files
  $self->_add_test_requires($self->{allow_test_pms});

  $self->_exclude_local_modules;

  if ($self->{exclude_core}) {
    $self->_exclude_core_prereqs;
  }

  if ($self->{index}) {
    $self->_dedupe_indexed_prereqs;
  }

  $self->_dedupe;

  if ($self->{print} or $self->{cpanfile}) {
    if ($self->{json}) {
      # TODO: feature support (how should we express it?)
      eval { require JSON::PP } or die "requires JSON::PP";
      print JSON::PP->new->pretty(1)->canonical->encode($self->{prereqs}->as_string_hash);
    } elsif ($self->{cpanfile}) {
      eval { require Perl::PrereqScanner::NotQuiteLite::Util::CPANfile } or die "requires Module::CPANfile";
      my $file = File::Spec->catfile($self->{base_dir}, "cpanfile");
      my $cpanfile = Perl::PrereqScanner::NotQuiteLite::Util::CPANfile->load_and_merge($file, $self->{prereqs}, $self->{features});

      $self->_dedupe_indexed_prereqs($cpanfile->prereqs) if $self->{index};

      if ($self->{save_cpanfile}) {
        $cpanfile->save($file);
      } elsif ($self->{print}) {
        print $cpanfile->to_string, "\n";
      }
      return $cpanfile;
    } elsif ($self->{print}) {
      $self->_print_prereqs;
    }
  }
  $self->{prereqs};
}

sub index { shift->{index} }

sub _print_prereqs {
  my $self = shift;

  my $combined = CPAN::Meta::Requirements->new;

  for my $req ($self->_requirements) {
    $combined->add_requirements($req);
  }
  my $hash = $combined->as_string_hash;
  for my $module (sort keys %$hash) {
    next if $module eq 'perl';
    my $version = $hash->{$module} || 0;
    $version = qq{"$version"} unless $version =~ /^[0-9]+(?:\.[0-9]+)?$/;
    print $version eq '0' ? "$module\n" : "$module~$version\n";
  }
}

sub _requirements {
  my ($self, $prereqs) = @_;

  $prereqs ||= $self->{prereqs};
  my @phases = qw/configure runtime build test/;
  push @phases, 'develop' if $self->{develop};
  my @types = $self->{suggests} ? qw/requires recommends suggests/ : $self->{recommends} ? qw/requires recommends/ : qw/requires/;
  my @requirements;
  for my $phase (@phases) {
    for my $type (@types) {
      my $req = $prereqs->requirements_for($phase, $type);
      next unless $req->required_modules;
      push @requirements, $req;
    }
  }

  if ($self->{features}) {
    my @feature_prereqs = grep defined, map {$self->{features}{$_}{prereqs}} keys %{$self->{features} || {}};
    for my $feature_prereqs (@feature_prereqs) {
      for my $phase (@phases) {
        for my $type (@types) {
          my $req = $feature_prereqs->requirements_for($phase, $type);
          next unless $req->required_modules;
          push @requirements, $req;
        }
      }
    }
  }

  @requirements;
}

sub _exclude_local_modules {
  my $self = shift;

  my @local_dirs = ("inc", @{$self->{libs} || []});
  for my $dir (@local_dirs) {
    my $local_dir = File::Spec->catdir($self->{base_dir}, $dir);
    next unless -d $local_dir;
    find({
      wanted => sub {
        my $file = $_;
        return unless -f $file;
        my $relpath = File::Spec->abs2rel($file, $local_dir);

        return unless $relpath =~ /\.pm$/;
        my $module = $relpath;
        $module =~ s!\.pm$!!;
        $module =~ s![\\/]!::!g;
        $self->{possible_modules}{$module} = 1;
        $self->{possible_modules}{"inc::$module"} = 1 if $dir eq 'inc';
      },
      no_chdir => 1,
    }, $local_dir);
  }

  my $private_re = $self->{private_re};
  for my $req ($self->_requirements) {
    for my $module ($req->required_modules) {
      next unless $self->{possible_modules}{$module} or ($private_re and $module =~ /$private_re/);
      $req->clear_requirement($module);
      if ($self->{verbose}) {
        print STDERR "  excluded $module (local)\n";
      }
    }
  }
}

sub _exclude_core_prereqs {
  my $self = shift;

  eval { require Module::CoreList; Module::CoreList->VERSION('2.99') } or die "requires Module::CoreList 2.99";

  my $perl_version = $self->{perl_version} || $self->_find_used_perl_version || '5.008001';
  if ($perl_version =~ /^v?5\.(0?[1-9][0-9]?)(?:\.([0-9]))?$/) {
    $perl_version = sprintf '5.%03d%03d', $1, $2 || 0;
  }
  $perl_version = '5.008001' unless exists $Module::CoreList::version{$perl_version};

  my %core_alias = (
    'Getopt::Long::Parser'   => 'Getopt::Long',
    'Tie::File::Cache'       => 'Tie::File',
    'Tie::File::Heap'        => 'Tie::File',
    'Tie::StdScalar'         => 'Tie::Scalar',
    'Tie::StdArray'          => 'Tie::Array',
    'Tie::StdHash'           => 'Tie::Hash',
    'Tie::ExtraHash'         => 'Tie::Hash',
    'Tie::RefHash::Nestable' => 'Tie::RefHash',
  );

  for my $req ($self->_requirements) {
    for my $module ($req->required_modules) {
      $module = $core_alias{$module} if exists $core_alias{$module};
      if (Module::CoreList::is_core($module, undef, $perl_version) and
          !Module::CoreList::deprecated_in($module, undef, $perl_version)
      ) {
        my $core_version = $Module::CoreList::version{$perl_version}{$module} or next;
        next unless $req->accepts_module($module => $core_version);
        $req->clear_requirement($module);
        if ($self->{verbose}) {
          print STDERR "  excluded $module ($perl_version core)\n";
        }
      }
    }
  }
}

sub _find_used_perl_version {
  my $self = shift;
  my @perl_versions;
  my $perl_requirements = CPAN::Meta::Requirements->new;
  for my $req ($self->_requirements) {
    my $perl_req = $req->requirements_for_module('perl');
    $perl_requirements->add_string_requirement('perl', $perl_req) if $perl_req;
  }
  return $perl_requirements->is_simple ? $perl_requirements->requirements_for_module('perl') : undef;
}

sub _add_test_requires {
  my ($self, $force) = @_;

  if (my $test_reqs = $self->{prereqs}->requirements_for('test', 'requires')) {
    my @required_modules = $test_reqs->required_modules;
    for my $module (@required_modules) {
      $force = 1 if exists $IsTestClassFamily{$module};
      my $relpath = $self->{possible_modules}{$module} or next;
      my $context = delete $self->{_test_pm}{$relpath} or next;
      $test_reqs->add_requirements($context->requires);
      if ($self->{recommends} or $self->{suggests}) {
        $self->{prereqs}->requirements_for('test', 'recommends')->add_requirements($context->recommends);
      }
      if ($self->{suggests}) {
        $self->{prereqs}->requirements_for('test', 'suggests')->add_requirements($context->suggests);
      }
    }
    if ($force) {
      for my $context (values %{$self->{_test_pm} || {}}) {
        $test_reqs->add_requirements($context->requires);
        if ($self->{recommends} or $self->{suggests}) {
          $self->{prereqs}->requirements_for('test', 'recommends')->add_requirements($context->recommends);
        }
        if ($self->{suggests}) {
          $self->{prereqs}->requirements_for('test', 'suggests')->add_requirements($context->suggests);
        }
      }
    }
  }
}

sub _dedupe {
  my $self = shift;

  my $prereqs = $self->{prereqs};

  my %features = map {$_ => $self->{features}{$_}{prereqs}} keys %{$self->{features} || {}};

  dedupe_prereqs_and_features($prereqs, \%features);
}

sub _get_uri {
  my ($self, $module) = @_;
  $self->{uri_cache}{$module} ||= $self->__get_uri($module);
}

sub __get_uri {
  my ($self, $module) = @_;
  my $res = $self->{index}->search_packages({ package => $module }) or return;
  ## ignore (non-dual) core modules
  return if URI->new($res->{uri})->dist_name eq 'perl';
  return $res->{uri};
}

sub _dedupe_indexed_prereqs {
  my ($self, $prereqs) = @_;

  require URI::cpan;

  for my $req ($self->_requirements($prereqs)) {
    my %uri_map;
    for my $module ($req->required_modules) {
      next if $module eq 'perl';
      my $uri = $self->_get_uri($module) or next;
      $uri_map{$uri}{$module} = $req->requirements_for_module($module);
    }
    for my $uri (keys %uri_map) {
      my @modules = keys %{$uri_map{$uri}};
      next if @modules < 2;

      my @modules_without_version = grep {!$uri_map{$uri}{$_}} @modules;
      next unless @modules_without_version;

      # clear unversioned prereqs if a versioned prereq exists
      if (@modules > @modules_without_version) {
        $req->clear_requirement($_) for @modules_without_version;
        next;
      }

      # Replace with the main module if none is versioned
      my $dist = URI->new($uri)->dist_name;
      (my $main_module = $dist) =~ s/-/::/g;
      if ($self->_get_uri($main_module)) {
        $req->add_minimum($main_module);
        for my $module (@modules_without_version) {
          next if $main_module eq $module;
          $req->clear_requirement($module);
          if ($self->{verbose}) {
            print STDERR "  deduped $module (in favor of $main_module)\n";
          }
        }
      } else {
        # special case for distributions without a main module
        my %score;
        for my $module (@modules_without_version) {
          my $depth = $module =~ s/::/::/g;
          my $length = length $module;
          $score{$module} = join ".", ($depth || 0), $length;
        }
        my $topmost = (sort {$score{$a} <=> $score{$b} or $a cmp $b} @modules_without_version)[0];
        for my $module (@modules_without_version) {
          next if $topmost eq $module;
          $req->clear_requirement($module);
          if ($self->{verbose}) {
            print STDERR "  deduped $module (in favor of $topmost)\n";
          }
        }
      }
    }
  }
}

sub _scan_dir {
  my ($self, $dir) = @_;
  find ({
    no_chdir => 1,
    wanted => sub {
      my $file = $_;
      return unless -f $file;
      my $relpath = File::Spec->abs2rel($file, $self->{base_dir});

      return unless $relpath =~ /\.(?:pl|PL|pm|cgi|psgi|t)$/ or
                    dirname($relpath) =~ m!\b(?:bin|scripts?)$! or
                    ($self->{develop} and $relpath =~ /^(?:author)\b/);
      $self->_scan_file($file);
    },
  }, $dir);
}

sub _scan_file {
  my ($self, $file) = @_;

  $file =~ s|\\|/|g if WIN32;
  if ($self->{ignore_re}) {
    return if $file =~ /\b$self->{ignore_re}\b/;
  }

  my $context = Perl::PrereqScanner::NotQuiteLite->new(
    parsers => $self->{parsers},
    recommends => $self->{recommends},
    suggests => $self->{suggests},
    verbose => $self->{verbose},
  )->scan_file($file);

  my $relpath = File::Spec->abs2rel($file, $self->{base_dir});
  $relpath =~ s|\\|/|g if WIN32;

  my $prereqs = $self->{prereqs};
  if ($self->{features}) {
    for my $identifier (keys %{$self->{features}}) {
      my $feature = $self->{features}{$identifier};
      if (grep {$file =~ m!^$_(?:/|$)!} @{$feature->{paths}}) {
        $prereqs = $feature->{prereqs} ||= CPAN::Meta::Prereqs->new;
        last;
      }
    }
  }

  if ($relpath =~ m!(?:^|[\\/])t[\\/]!) {
    if ($relpath =~ /\.t$/) {
      $self->_add($prereqs, test => $context);
    } elsif ($relpath =~ /\.pm$/) {
      $self->{_test_pm}{$relpath} = $context;
    }
  } elsif ($relpath =~ m!(?:^|[\\/])(?:xt|inc|author)[\\/]!) {
    $self->_add($prereqs, develop => $context);
  } elsif ($relpath =~ m!(?:(?:^|[\\/])Makefile|^Build)\.PL$!) {
    $self->_add($prereqs, configure => $context);
  } elsif ($relpath =~ m!(?:^|[\\/])(?:.+)\.PL$!) {
    $self->_add($prereqs, build => $context);
  } else {
    $self->_add($prereqs, runtime => $context);
  }

  if ($relpath =~ /\.pm$/) {
    my $module = $relpath;
    $module =~ s!\.pm$!!;
    $module =~ s![\\/]!::!g;
    $self->{possible_modules}{$module} = $relpath;
    $module =~ s!^(?:inc|blib|x?t)::!!;
    $self->{possible_modules}{$module} = $relpath;
    $module =~ s!^lib::!!;
    $self->{possible_modules}{$module} = $relpath;
  }
}

sub _add {
  my ($self, $prereqs, $phase, $context) = @_;

  $prereqs->requirements_for($phase, 'requires')
          ->add_requirements($context->requires);

  if ($self->{suggests} or $self->{recommends}) {
    $prereqs->requirements_for($phase, 'recommends')
            ->add_requirements($context->recommends);
  }

  if ($self->{suggests}) {
    $prereqs->requirements_for($phase, 'suggests')
            ->add_requirements($context->suggests);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::App

=head1 SYNOPSIS

  scan-perl-prereqs-nqlite [options] [DIRS|FILES]

  -or-

  use Perl::PrereqScanner::NotQuiteLite::App;

  my $app = Perl::PrereqScanner::NotQuiteLite::App->new(
    parsers => [qw/:bundled/],
    suggests => 1,
    perl_minimum_version => 1,
  );
  my $prereqs = $app->run;

=head1 DESCRIPTION

Perl::PrereqScanner::NotQuiteLite::App walks down a directory
and scans appropriate files to find prerequisites.
You usually don't need to touch this module directly, but you
might want to if you need finer control (to use a custom CPAN index
etc).

=head1 METHODS

=head2 new

creates an object. Notable options are:

=over 4

=item parsers

Perl::PrereqScanner::NotQuiteLite::App uses all the bundled parsers
by default, but you can change if you need your own parsers.
See L<Perl::PrereqScanner::NotQuiteLite> for details.

=item recommends, suggests, perl_minimum_version

Perl::PrereqScanner::NotQuiteLite::App usually returns C<use>d
modules only, but you can change this behavior by setting these
options. See L<Perl::PrereqScanner::NotQuiteLite> for details.

=item develop

If set, Perl::PrereqScanner::NotQuiteLite::App also scans files under
C<xt> and C<author> directories to find requirements for development.

=item exclude_core

If set, Perl::PrereqScanner::NotQuiteLite::App ignores prerequisites
that are bundled with Perl (of 5.008001 by default, or of a C<use>d
perl version if any). This requires L<Module::CoreList> version 2.99
or above.

=item perl_version

You can explicitly use this option to exclude core modules of a
specific perl version.

=item allow_test_pms

Perl::PrereqScanner::NotQuiteLite::App usually ignores C<.pm> files
under C<t/> directory if they are not used in C<.t> files, considering
they are some kind of sample files. However, this assumption may be
wrong sometimes. If this option is set, it scans all the C<.pm> files
under C<t/> directory, considering some of the test modules will use
them. If L<Test::Class> (or its equivalent) is used in a test
file, this option is implicitly set.

=item base_dir

Perl::PrereqScanner::NotQuiteLite::App usually starts traversing from
the current directory. If this option is set, it starts from there.

=item scan_also

Perl::PrereqScanner::NotQuiteLite::App usually scans C<.pm> files
in the base dir, C<Makefile.PL>/C<Build.PL>, files under C<lib>,
C<t>, C<bin>, C<script(s)> directories (and C<xt>, C<author> if asked).
If your distribution uses a different file layout, or uses extra
directories to keep submodules, you can add (a reference to) a list
of paths to scan.

=item ignore, ignore_re

Your distribution may have OS-specific modules whose prerequisites
can not be installed in other platforms. You can specify (a reference
to) a list of files that should not be scanned (with C<ignore> option),
or a regular expression that matches the files (with C<ignore_re>
option).

=item features

  my $app = Perl::PrereqScanner::NotQuiteLite::App->new(
    features => {
      'windows' => {
        description => 'Windows support',
        paths => ['lib/Foo/Win32.pm'],
      }
    },
  );

Instead of ignoring a set of files, you can use C<features> option to
let their prerequisites belong to a specific feature that will not be
installed unless asked. However, you are advised to create a separate
distribution for the specific feature.

=item private, private_re

Your distribution may use private modules that are not uploaded to
the CPAN and thus should not be included in C<cpanfile>. You can
specify (a reference to) a list of those private modules (with
C<private> option) or a regular expression that matches those modules
(with C<private_re> option).

=item use_index, index

Perl::PrereqScanner::NotQuiteLite::App usually lists all the C<use>d
modules as prerequisites, but some of them may belong to the same
distribution. If an instance of L<CPAN::Common::Index> backend is
passed, it is used to dedupe those prerequisites (as long as they are
not versioned).

  use CPAN::Common::Index::LocalPackage;
  my $index = CPAN::Common::Index::LocalPackage->new(
    { source => "$ENV{HOME}/minicpan/modules/02packages.details.txt" }
  );
  my $app = Perl::PrereqScanner::NotQuiteLite::App->new(
    index => $index,
  );

=back

=head2 run

traverses files and directories and returns a L<CPAN::Meta::Prereqs>
object that keeps all the requirements/suggestions, without printing
anything unless you explicitly pass a C<print> option to C<new>.

=head2 index

returns a L<CPAN::Common::Index> backend object (if any).

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
