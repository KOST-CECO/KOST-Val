use strict;
use warnings;
package Test::Kwalitee; # git description: v1.27-5-ge8333c9
# vim: set ts=8 sts=4 sw=4 tw=115 et :
# ABSTRACT: Test the Kwalitee of a distribution before you release it
# KEYWORDS: testing tests kwalitee CPANTS quality lint errors critic

our $VERSION = '1.28';

use Cwd ();
use Test::Builder 0.88;
use Module::CPANTS::Analyse 0.92;

use parent 'Exporter';
our @EXPORT_OK = qw(kwalitee_ok);

my $Test;
BEGIN { $Test = Test::Builder->new }

sub import
{
    my ($class, @args) = @_;

    # back-compatibility mode!
    if (@args % 2 == 0)
    {
        $Test->level($Test->level + 1);
        my %args = @args;
        my $result = kwalitee_ok(@{$args{tests}});
        $Test->done_testing;
        return $result;
    }

    # otherwise, do what a regular import would do...
    $class->export_to_level(1, @_);
}

sub kwalitee_ok
{
    my (@tests) = @_;

    warn "These tests should not be running unless AUTHOR_TESTING=1 and/or RELEASE_TESTING=1!\n"
        # this setting is internal and for this distribution only - there is
        # no reason for you to need to circumvent this check in any other context.
        # Please DO NOT enable this test to run for users, as it can fail
        # unexpectedly as parts of the toolchain changes!
        unless $ENV{_KWALITEE_NO_WARN} or $ENV{AUTHOR_TESTING} or $ENV{RELEASE_TESTING}
            or (caller)[1] =~ m{^(?:\.[/\\])?xt\b}
            or ((caller)[0]->isa(__PACKAGE__) and (caller(1))[1] =~ m{^(?:\.[/\\])?xt\b});

    my @run_tests = grep { /^[^-]/ } @tests;
    my @skip_tests = map { s/^-//; $_ } grep { /^-/ } @tests;

    # These don't really work unless you have a tarball, so skip them
    push @skip_tests, qw(extractable extracts_nicely no_generated_files
        has_proper_version has_version manifest_matches_dist);

    # MCA has a patch to add 'needs_tarball', 'no_build' as flags
    my @skip_flags = qw(is_extra is_experimental needs_db);

    my $basedir = Cwd::cwd;

    my $analyzer = Module::CPANTS::Analyse->new({
        distdir => $basedir,
        dist    => $basedir,
        # for debugging..
        opts => { no_capture => 1 },
    });

    my $ok = 1;

    for my $generator (@{ $analyzer->mck->generators })
    {
        $generator->analyse($analyzer);

        for my $indicator (sort { $a->{name} cmp $b->{name} } @{ $generator->kwalitee_indicators })
        {
            next if grep { $indicator->{$_} } @skip_flags;

            next if @run_tests and not grep { $indicator->{name} eq $_ } @run_tests;

            next if grep { $indicator->{name} eq $_ } @skip_tests;

            my $result = _run_indicator($analyzer->d, $indicator);
            $ok &&= $result;
        }
    }

    return $ok;
}

sub _run_indicator
{
    my ($dist, $metric) = @_;

    my $subname = $metric->{name};
    my $ok = 1;

    $Test->level($Test->level + 1);
    if (not $Test->ok( $metric->{code}->($dist), $subname))
    {
        $ok = 0;
        $Test->diag('Error: ', $metric->{error});

        # NOTE: this is poking into the analyse structures; we really should
        # have a formal API for accessing this.

        # attempt to print all the extra information we have
        my @details;
        push @details, $metric->{details}->($dist)
            if $metric->{details} and ref $metric->{details} eq 'CODE';
        push @details,
            (ref $dist->{error}{$subname}
                ? @{$dist->{error}{$subname}}
                : $dist->{error}{$subname})
            if defined $dist->{error} and defined $dist->{error}{$subname};
        $Test->diag("Details:\n", join("\n", @details)) if @details;

        $Test->diag('Remedy: ', $metric->{remedy});
    }
    $Test->level($Test->level - 1);

    return $ok;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Test::Kwalitee - Test the Kwalitee of a distribution before you release it

=head1 VERSION

version 1.28

=head1 SYNOPSIS

In a separate test file:

  use Test::More;
  use strict;
  use warnings;
  BEGIN {
      plan skip_all => 'these tests are for release candidate testing'
          unless $ENV{RELEASE_TESTING};
  }

  use Test::Kwalitee 'kwalitee_ok';
  kwalitee_ok();
  done_testing;

=head1 DESCRIPTION

=for stopwords CPANTS

Kwalitee is an automatically-measurable gauge of how good your software is.
That's very different from quality, which a computer really can't measure in a
general sense.  (If you can, you've solved a hard problem in computer science.)

In the world of the CPAN, the CPANTS project (CPAN Testing Service; also a
funny acronym on its own) measures Kwalitee with several metrics.  If you plan
to release a distribution to the CPAN -- or even within your own organization
-- testing its Kwalitee before creating a release can help you improve your
quality as well.

C<Test::Kwalitee> and a short test file will do this for you automatically.

=head1 USAGE

Create a test file as shown in the synopsis.  Run it.  It will run all of the
potential Kwalitee tests on the current distribution, if possible.  If any
fail, it will report those as regular diagnostics.

If you ship this test, it will not run for anyone else, because of the
C<RELEASE_TESTING> guard. (You can omit this guard if you move the test to
xt/release/, which is not run automatically by other users.)

=head1 FUNCTIONS

=head2 kwalitee_ok

With no arguments, runs all standard metrics.

To run only a handful of tests, pass their name(s) to the C<kwalitee_ok>
function:

  kwalitee_ok(qw( use_strict has_tests ));

To disable a test, pass its name with a leading minus (C<->):

  kwalitee_ok(qw( -use_strict has_readme ));

=head1 BACK-COMPATIBILITY MODE

Previous versions of this module ran tests directly via the C<import> sub, like so:

    use Test::Kwalitee;
    # and that's it!

...but this is problematic if you need to perform some setup first, as you
would need to do that in a C<BEGIN> block, or manually call C<import>. This is
messy!

However, this calling path is still available, e.g.:

  use Test::Kwalitee tests => [ qw( use_strict has_tests ) ];

=head1 METRICS

The list of each available metric currently available on your
system can be obtained with the C<kwalitee-metrics> command (with
descriptions, if you pass C<--verbose> or C<-v>, but
as of L<Module::CPANTS::Analyse> 0.97_03, the tests include:

=over 4

=item *

has_abstract_in_pod

Does the main module have a C<=head1 NAME> section with a short description of the distribution?

=item *

has_buildtool

Does the distribution have a build tool file?

=for stopwords changelog

=item *

has_changelog

Does the distribution have a changelog?

=item *

has_humanreadable_license

Is there a C<LICENSE> section in documentation, and/or a F<LICENSE> file
present?

=item *

has_license_in_source_file

Is there license information in any of the source files?

=item *

has_manifest

Does the distribution have a F<MANIFEST>?

=item *

has_meta_yml

Does the distribution have a F<META.yml> file?

=item *

has_readme

Does the distribution have a F<README> file?

=item *

has_tests

Does the distribution have tests?

=item *

manifest_matches_dist

Do the F<MANIFEST> and distribution contents match?

=item *

meta_json_conforms_to_known_spec

Does META.json conform to the recognised META.json specification?
(For specs see L<CPAN::Meta::Spec>)

=item *

meta_json_is_parsable

Can the F<META.json> be parsed?

=item *

meta_yml_conforms_to_known_spec

=for stopwords recognised

Does META.yml conform to any recognised META.yml specification?
(For specs see
L<http://module-build.sourceforge.net/META-spec-current.html>)

=item *

meta_yml_is_parsable

Can the F<META.yml> be parsed?

=item *

no_broken_auto_install

Is the distribution using an old version of L<Module::Install>? Versions of
L<Module::Install> prior to 0.89 do not detect correctly that C<CPAN>/C<CPANPLUS>
shell is used.

=item *

no_broken_module_install

Does the distribution use an obsolete version of L<Module::Install>?
Versions of L<Module::Install> prior to 0.61 might not work on some systems at
all. Additionally if the F<Makefile.PL> uses the C<auto_install()>
feature, you need at least version 0.64. Also, 1.04 is known to be broken.

=item *

no_symlinks

Does the distribution have no symlinks?

=item *

use_strict

Does the distribution files all use strict?

=back

=head1 ACKNOWLEDGEMENTS

=for stopwords Klausner Dolan

With thanks to CPANTS and Thomas Klausner, as well as test tester Chris Dolan.

=head1 SEE ALSO

=over 4

=item *

L<kwalitee-metrics> (in this distribution)

=item *

L<Module::CPANTS::Analyse>

=item *

L<App::CPANTS::Lint>

=item *

L<Test::Kwalitee::Extra>

=item *

L<Dist::Zilla::Plugin::Test::Kwalitee>

=item *

L<Dist::Zilla::Plugin::Test::Kwalitee::Extra>

=item *

L<Dist::Zilla::App::Command::kwalitee>

=back

=head1 SUPPORT

Bugs may be submitted through L<the RT bug tracker|https://rt.cpan.org/Public/Dist/Display.html?Name=Test-Kwalitee>
(or L<bug-Test-Kwalitee@rt.cpan.org|mailto:bug-Test-Kwalitee@rt.cpan.org>).

There is also a mailing list available for users of this distribution, at
L<http://lists.perl.org/list/perl-qa.html>.

There is also an irc channel available for users of this distribution, at
L<C<#perl> on C<irc.perl.org>|irc://irc.perl.org/#perl-qa>.

=head1 AUTHORS

=over 4

=item *

chromatic <chromatic@wgz.org>

=item *

Karen Etheridge <ether@cpan.org>

=back

=head1 CONTRIBUTORS

=for stopwords David Steinbrunner Gavin Sherlock Kenichi Ishigaki Nathan Haigh Zoffix Znet Daniel Perrett

=over 4

=item *

David Steinbrunner <dsteinbrunner@pobox.com>

=item *

Gavin Sherlock <sherlock@cpan.org>

=item *

Kenichi Ishigaki <ishigaki@cpan.org>

=item *

Nathan Haigh <nathanhaigh@ukonline.co.uk>

=item *

Zoffix Znet <cpan@zoffix.com>

=item *

Daniel Perrett <perrettdl@googlemail.com>

=back

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2005 by chromatic.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
