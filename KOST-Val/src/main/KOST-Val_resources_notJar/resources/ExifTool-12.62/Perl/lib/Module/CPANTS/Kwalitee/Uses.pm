package Module::CPANTS::Kwalitee::Uses;
use warnings;
use strict;
use File::Spec::Functions qw(catfile);
use Perl::PrereqScanner::NotQuiteLite 0.9901;
use List::Util 1.33 qw/none/;
use version;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

# These equivalents should be reasonably well-known and, preferably,
# well-documented. Don't add obscure modules used by only one person
# or a few people, to keep the list relatively small and to encourage
# people to use a better equivalent.
# "use_(strict|warnings)" should fail if someone feels the need
# to add "use $1;" in the modules.
our @STRICT_EQUIV = qw( strict );
our @WARNINGS_EQUIV = qw( warnings warnings::compat );
our @STRICT_WARNINGS_EQUIV = qw(
  common::sense
  Any::Moose
  Catmandu::Sane Coat
  Dancer
  Mo Mu
  Modern::Perl
  Moo Moo::Role
  Moose Moose::Role Moose::Exporter
  Moose::Util::TypeConstraints Moose::Util::MetaRole
  MooseX::Declare MooseX::Role::Parameterized MooseX::Types
  Mouse Mouse::Role
  perl5 perl5i::1 perl5i::2 perl5i::latest
  Pegex::Base
  Role::Tiny
  strictures
);
# These modules require a flag to enforce strictness.
push @STRICT_WARNINGS_EQUIV, qw(
  Mojo::Base
  Spiffy
);

sub order { 100 }

##################################################################
# Analyse
##################################################################

sub analyse {
    my $class = shift;
    my $me = shift;
    
    my $distdir = $me->distdir;
    my $modules = $me->d->{modules};
    my $files = $me->d->{files_hash};

    # NOTE: all files in xt/ should be ignored because they are
    # for authors only and their dependencies may not be (and
    # often are not) listed in meta files.
    my @test_files = grep {m!^t\b.*\.t$!} sort keys %$files;
    $me->d->{test_files} = \@test_files;

    my %test_modules = map {
        my $m = my $f = $_;
        $m =~ s|\.pm$||;
        $m =~ s|/|::|g;
        (my $m0 = $m) =~ s|^t::(?:lib::)?||;
        ($m => $f, $m0 => $f)
    } grep {m|^t\b.*\.pm$|} keys %$files;

    my %skip=map {$_->{module}=>1 } @$modules;

    # d->{versions} (from SiteKwalitee) knows inner packages as well
    if (my $versions = $me->d->{versions}) {
        for my $file (keys %$versions) {
            for my $module (keys %{$versions->{$file}}) {
                $skip{$module} = 1;
            }
        }
    }

    my %uses;

    my $scanner = Perl::PrereqScanner::NotQuiteLite->new(
        parsers => [':bundled'],
        suggests => 1,
        recommends => 1,
        quick => 1,
    );
    
    # modules
    my @module_files = map {$_->{file}} grep {!$_->{not_exists}} @$modules;

    # Makefile.PL runs other Makefile.PL files at configure time (except ones under t)
    # Build.PL runs other *.PL files at build time
    my @configure_files = grep {/(?:^Build|\bMakefile)\.PL$/ && !/^t[\\\/]/} @{$me->d->{files_array} || []};
    my %configure_files_map = map {$_ => 1} @configure_files;

    # Other *.PL files (including lib/Build.PL) would (probably) be run at bulid time
    my @build_files = grep {/\.PL$/ && !/^t[\\\/]/ && !$configure_files_map{$_}} @{$me->d->{files_array} || []};

    $uses{runtime} = $class->_scan($scanner, $files, $distdir, \@module_files);
    $uses{configure} = $class->_scan($scanner, $files, $distdir, \@configure_files);
    $uses{build} = $class->_scan($scanner, $files, $distdir, \@build_files);
    $uses{test} = $class->_scan($scanner, $files, $distdir, \@test_files);

    # See also .pm files under t (only) if they are used in .t files
    my $test_requirements = $uses{test}{requires}->as_string_hash;
    my @test_pmfiles;
    for my $module (keys %$test_requirements) {
        push @test_pmfiles, $test_modules{$module} if $test_modules{$module};
    }
    my $additional_test_requirements = $class->_scan($scanner, $files, $distdir, \@test_pmfiles);
    for my $relationship (keys %$additional_test_requirements) {
        $uses{test}{$relationship} = ($uses{test}{$relationship})
            ? $uses{test}{$relationship}->add_requirements($additional_test_requirements->{$relationship})
            : $additional_test_requirements->{$relationship};
    }

    for my $phase (keys %uses) {
        for my $relationship (keys %{$uses{$phase}}) {
            my $requirements = $uses{$phase}{$relationship}->as_string_hash;
            for my $requirement (keys %$requirements) {
                if (
                    $skip{$requirement}
                    or $requirement =~ /^(?:inc|t)::/
                    or ($phase eq 'test' and $test_modules{$requirement})
                ) {
                    delete $requirements->{$requirement};
                }
            }
            if (%$requirements) {
                $uses{$phase}{$relationship} = $requirements;
            } else {
                delete $uses{$phase}{$relationship};
            }
        }
        delete $uses{$phase} unless %{$uses{$phase}};
    }

    $me->d->{uses} = \%uses;
    return;
}

sub _scan {
    my ($class, $scanner, $files_hash, $distdir, $files) = @_;

    my @methods = qw/requires recommends suggests noes/;
    my %reqs = map {$_ => CPAN::Meta::Requirements->new} @methods;
    for my $file (@$files) {
        my $ctx = $scanner->scan_file("$distdir/$file");

        # There may be broken files (intentionally, or unintentionally, esp in tests)
        if (@{$ctx->{errors} || []}) {
            my $error = join ',', @{$ctx->{errors}};
            $error =~ s/ at \S+ line \d+[^\n]*//gs;
            $error =~ s/Scan Error: //g;
            $files_hash->{$file}{scan_error} = $error;
        }

        if ($ctx->{perl6}) {
            $files_hash->{$file}{perl6} = 1;
            next;
        }
        for my $method (@methods) {
            my $requirements = $ctx->$method;
            my $hash = $requirements->as_string_hash;
            next unless %$hash;
            $files_hash->{$file}{$method} = $hash;
            $reqs{$method} = $reqs{$method}->add_requirements($requirements);
        }
    }
    return \%reqs;
}

##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators {
    return [
        {
            name => 'use_strict',
            error => q{This distribution does not 'use strict;' (or its equivalents) in all of its modules. Note that this is not about the actual strictness of the modules. It's bad if nobody can tell whether the modules are strictly written or not, without reading the source code of your favorite clever module that actually enforces strictness. In other words, it's bad if someone feels the need to add 'use strict' to your modules.},
            remedy => q{Add 'use strict' (or its equivalents) to all modules, or convince us that your favorite module is well-known enough and people can easily see the modules are strictly written.},
            ignorable => 1,
            code => sub {
                my $d       = shift;
                my $files = $d->{files_hash} || {};

                my $perl_version_with_implicit_stricture = version->new('5.011')->numify;
                my @no_strict;

                for my $file (keys %$files) {
                    next unless exists $files->{$file}{module};
                    next if $files->{$file}{unreadable};
                    next if $files->{$file}{perl6};
                    next if $file =~ /\.pod$/;
                    my $module = $files->{$file}{module};
                    my $requires = $files->{$file}{requires} || {};
                    my $required_perl = $requires->{perl};
                    if (defined $required_perl) {
                        $required_perl =~ s/_//;  # tweak 5.008_001 and the likes for silence
                        next if version->parse($required_perl)->numify >= $perl_version_with_implicit_stricture;
                    }

                    # There are lots of acceptable strict alternatives
                    push @no_strict, $module if none {exists $requires->{$_}} (@STRICT_EQUIV, @STRICT_WARNINGS_EQUIV);
                }
                if (@no_strict) {
                    $d->{error}{use_strict} = join ", ", sort @no_strict;
                    return 0;
                }
                return 1;
            },
            details => sub {
                my $d = shift;
                return "The following modules don't use strict (or equivalents): " . $d->{error}{use_strict};
            },
        },
        {
            name => 'use_warnings',
            error => q{This distribution does not 'use warnings;' (or its equivalents) in all of its modules. Note that this is not about that your modules actually warn when something bad happens. It's bad if nobody can tell if a module warns or not, without reading the source code of your favorite module that actually enforces warnings. In other words, it's bad if someone feels the need to add 'use warnings' to your modules.},
            is_extra => 1,
            ignorable => 1,
            remedy => q{Add 'use warnings' (or its equivalents) to all modules, or convince us that your favorite module is well-known enough and people can easily see the modules warn when something bad happens.},
            code => sub {
                my $d = shift;
                my $files = $d->{files_hash} || {};

                my @no_warnings;
                for my $file (keys %$files) {
                    next unless exists $files->{$file}{module};
                    next if $files->{$file}{unreadable};
                    next if $files->{$file}{perl6};
                    next if $file =~ /\.pod$/;
                    my $module = $files->{$file}{module};
                    my $requires = $files->{$file}{requires} || {};
                    push @no_warnings, $module if none {exists $requires->{$_}} (@WARNINGS_EQUIV, @STRICT_WARNINGS_EQUIV);
                }
                if (@no_warnings) {
                    $d->{error}{use_warnings} = join ", ", sort @no_warnings;
                    return 0;
                }
                return 1;
            },
            details => sub {
                my $d = shift;
                return "The following modules don't use warnings (or equivalents): " . $d->{error}{use_warnings};
            },
        },
    ];
}


q{Favourite record of the moment:
  Fat Freddys Drop: Based on a true story};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::Uses - Checks which modules are used

=head1 SYNOPSIS

Check which modules are actually used in the code.

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<100>.

=head3 analyse

C<MCK::Uses> uses C<Module::ExtractUse> to find all C<use> statements in code (actual code and tests).

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=over

=item * use_strict

=item * use_warnings

=back

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Thomas Klausner|https://metacpan.org/author/domm>

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2006, 2009 L<Thomas Klausner|https://metacpan.org/author/domm>

You may use and distribute this module according to the same terms
that Perl is distributed under.
