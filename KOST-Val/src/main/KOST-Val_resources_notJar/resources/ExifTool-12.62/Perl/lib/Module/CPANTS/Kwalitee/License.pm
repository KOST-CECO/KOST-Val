package Module::CPANTS::Kwalitee::License;
use warnings;
use strict;
use File::Spec::Functions qw(catfile);
use Software::LicenseUtils;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 100 }

##################################################################
# Analyse
##################################################################

sub analyse {
    my $class = shift;
    my $me = shift;
    my $distdir = $me->distdir;

    # check META.yml
    my $yaml = $me->d->{meta_yml};
    $me->d->{license} = '';
    if ($yaml) {
        if ($yaml->{license} and $yaml->{license} ne 'unknown') {
            my $license = $yaml->{license};
            $license = join ',', @$license if ref $license eq 'ARRAY';
            $me->d->{license_from_yaml} = $license;
            $me->d->{license} = $license.' defined in META.yml';
        }
    }
    # use "files_array" to exclude files listed in "no_index".
    my $files = $me->d->{files_array} || [];

    # check if there's a LICEN[CS]E file
    # (also accept LICENSE.txt etc; RT #114247)
    if (my ($file) = grep {$_ =~ /^(?:LICEN[CS]E|COPYING)\b/} @$files) {
        $me->d->{license} .= " defined in $file";
        $me->d->{external_license_file} = $file;
    }

    # check pod
    my %licenses;
    foreach my $file (grep { /\.(?:pm|pod|pl|PL)$/ } sort @$files ) {
        next if $file =~ /(?:Makefile|Build)\.PL$/;
        my $path = catfile($distdir, $file);
        next unless -r $path; # skip if not readable
        open my $fh, '<', $path or next;
        my $in_pod = 0;
        my $pod = '';
        my $pod_head = '';
        my @possible_licenses;
        my @unknown_license_texts;
        my $uc_head;
        while(<$fh>) {
            my $first_four = substr($_, 0, 4);
            if ($first_four eq '=hea' && (($uc_head = uc $_) =~ /(?:LICEN[CS]E|LICEN[CS]ING|COPYRIGHT|LEGAL)/)) {
                $me->d->{license_in_pod} = 1;
                $me->d->{license} ||= "defined in POD ($file)";
                if ($in_pod) {
                    my @guessed = Software::LicenseUtils->guess_license_from_pod("=head1 LICENSE\n$pod\n\n=cut\n");
                    if (@guessed) {
                        push @possible_licenses, @guessed;
                    } else {
                        push @unknown_license_texts, "$pod_head$pod";
                    }
                }

                $in_pod = 1;
                $pod_head = $_;
                $pod = '';
            }
            elsif ($first_four eq '=hea' or $first_four eq '=cut') {
                if ($in_pod) {
                    my @guessed = Software::LicenseUtils->guess_license_from_pod("=head1 LICENSE\n$pod\n\n=cut\n");
                    if (@guessed) {
                        push @possible_licenses, @guessed;
                    } else {
                        push @unknown_license_texts, "$pod_head$pod";
                    }
                }
                $in_pod = 0;
                $pod = '';
            }
            elsif ($in_pod) {
                $pod .= $_;
            }
        }
        if ($pod) {
            my @guessed = Software::LicenseUtils->guess_license_from_pod("=head1 LICENSE\n$pod\n\n=cut\n");
            if (@guessed) {
                push @possible_licenses, @guessed;
            } else {
                push @unknown_license_texts, "$pod_head$pod";
            }
        }
        if (@possible_licenses) {
            @possible_licenses = map { s/^Software::License:://; $_ } @possible_licenses;
            push @{$licenses{$_} ||= []}, $file for @possible_licenses;
            $me->d->{files_hash}{$file}{license} = join ',', @possible_licenses;
        } else {
            $me->d->{unknown_license_texts}{$file} = join "\n", @unknown_license_texts if @unknown_license_texts;
        }
    }
    if (%licenses) {
        $me->d->{licenses} = \%licenses;
        my @possible_licenses = keys %licenses;
        if (@possible_licenses == 1) {
            my ($type) = @possible_licenses;
            $me->d->{license_type} = $type;
            $me->d->{license_file} = join ',', @{$licenses{$type}};
        }
    }

    return;
}

##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators{
    return [
        {
            name => 'meta_yml_has_license',
            error => q{This distribution does not have a license defined in META.yml.},
            remedy => q{Define the license if you are using in Build.PL. If you are using MakeMaker (Makefile.PL) you should upgrade to ExtUtils::MakeMaker version 6.31.},
            is_extra => 1,
            code => sub {
                my $d = shift;
                my $yaml = $d->{meta_yml};
                ($yaml->{license} and $yaml->{license} ne 'unknown') ? 1 : 0 },
            details => sub {
                my $d = shift;
                my $yaml = $d->{meta_yml};
                return "No META.yml." unless $yaml;
                return "No license was found in META.yml." unless $yaml->{license};
                return "Unknown license was found in META.yml.";
            },
        },
        {
            name => 'has_human_readable_license',
            error => q{This distribution does not have a license defined in the documentation or in a file called LICENSE},
            remedy => q{Add a section called "LICENSE" to the documentation, or add a file named LICENSE to the distribution.},
            code => sub {
                my $d = shift;
                return $d->{external_license_file} || $d->{license_in_pod} ? 1 : 0;
            },
            details => sub {
                my $d = shift;
                return "Neither LICENSE file nor LICENSE section in pod was found.";
            },
        },
        {
            name => 'has_separate_license_file',
            error => q{This distribution does not have a LICENSE or LICENCE file in its root directory.},
            remedy => q{This is not a critical issue. Currently mainly informative for the CPANTS authors. It might be removed later.},
            is_experimental => 1,
            code => sub { shift->{external_license_file} ? 1 : 0 },
            details => sub {
                my $d = shift;
                return "LICENSE file was found.";
            },
        },
        {
            name => 'has_license_in_source_file',
            error => q{Does not have license information in any of its source files},
            remedy => q{Add =head1 LICENSE and the text of the license to the main module in your code.},
            code => sub {
                my $d = shift;
                return $d->{license_in_pod} ? 1 : 0;
            },
            details => sub {
                my $d = shift;
                return "LICENSE section was not found in the pod.";
            },
        },
        {
            name => 'has_known_license_in_source_file',
            error => q{Does not have license information in any of its source files, or the information is not recognized by Software::License},
            remedy => q{Add =head1 LICENSE and/or the proper text of the well-known license to the main module in your code.},
            is_extra => 1,
            code => sub {
                my $d = shift;
                return 0 unless $d->{license_in_pod};
                my @files_with_licenses = grep {$d->{files_hash}{$_}{license}} keys %{$d->{files_hash}};
                return @files_with_licenses ? 1 : 0;
            },
            details => sub {
                my $d = shift;
                return "LICENSE section was not found in the pod, or the license information was not recognized by Software::License.";
            },
        },
    ];
}


q{Favourite record of the moment:
  Lili Allen - Allright, still};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::License - Checks if there is a license

=head1 SYNOPSIS

Checks if the distribution specifies a license.

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<100>.

=head3 analyse

C<MCK::License> checks if there's a C<license> field C<META.yml>. Additionally, it looks for a file called LICENSE and a POD section named LICENSE

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=over

=item * meta_yml_has_license

=item * has_known_license_in_source_file

=item * has_license_in_source_file

=item * has_human_readable_license

=item * has_separate_license_file

=back

=head2 License information

Places where the license information is taken from:

Has a LICENSE file   file_license 1|0

Content of LICENSE file matches License X from Software::License

License in META.yml

License in META.yml matches one of the known licenses

License in source files recognized by Software::LicenseUtils
For each file keep where is was it recognized.

Has license or copyright entry in pod (that might not be recognized by Software::LicenseUtils)

# has_license

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Thomas Klausner|https://metacpan.org/author/domm>
and L<Gábor Szabó|https://metacpan.org/author/szabgab>

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2009 L<Thomas Klausner|https://metacpan.org/author/domm>

Copyright © 2006–2008 L<Gábor Szabó|https://metacpan.org/author/szabgab>

You may use and distribute this module according to the same terms
that Perl is distributed under.
