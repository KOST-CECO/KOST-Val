package Module::CPANTS::Kwalitee::CpantsErrors;
use warnings;
use strict;
use version;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 1000 }

##################################################################
# Analyse
##################################################################

sub analyse {
    # NOTE: CPANTS error should be logged somewhere, but it
    # should not annoy people. If anything wrong or interesting
    # is found in the log, add some metrics (if it's worth),
    # or just fix our problems.

    # Note also that this stub should not be removed so that
    # this can replace the old ::CpantsErrors module, and the old
    # metrics will not be loaded while loading plugins.
}


##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators {
    # Older Test::Kwalitee (prior to 1.08) has hardcoded metrics
    # names in it, and if those metrics are gone from
    # Module::CPANTS::Kwalitee, it fails because the number of tests
    # is not as expected. This is not beautiful, but better than
    # to break others' distributions needlessly.
    if ($INC{"Test/Kwalitee.pm"}) {
        return [
            map {+{name => $_, code => sub {1}}}
            qw/extractable no_pod_errors
               has_test_pod has_test_pod_coverage/
        ] if version->parse(Test::Kwalitee->VERSION) < version->parse(1.08);
    }

    return [];
}


q{Listeing to: FM4 the early years};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::CpantsErrors - Check for CPANTS testing errors

=head1 SYNOPSIS

Checks if something strange happened during testing

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<1000>.

=head3 analyse

Uses C<IO::Capture::Stdout> to check for any strange things that might happen during testing

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Thomas Klausner|https://metacpan.org/author/domm>

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2006, 2009 L<Thomas Klausner|https://metacpan.org/author/domm>

You may use and distribute this module according to the same terms
that Perl is distributed under.
