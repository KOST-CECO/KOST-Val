package Module::CPANTS::Kwalitee::MetaYML;
use warnings;
use strict;
use File::Spec::Functions qw(catfile);
use CPAN::Meta::YAML;
use CPAN::Meta::Validator;
use CPAN::Meta::Converter;
use List::Util qw/first/;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 10 }

my $JSON_DECODER = _load_json_decoder() || do { require JSON::PP; JSON::PP->can('decode_json') };

##################################################################
# Analyse
##################################################################

sub analyse {
    my $class = shift;
    my $me = shift;
    my $distdir = $me->distdir;
    my $meta_yml   = catfile($distdir, 'META.yml');
    my $meta_json  = catfile($distdir, 'META.json');
    my $mymeta_yml = catfile($distdir, 'MYMETA.yml');

    # META.yml is not always the most preferred meta file,
    # but test it anyway because it may be broken sometimes.
    if (-f $meta_yml && -r _) {
        _analyse_yml($me, $meta_yml);
    }

    # check also META.json (if exists).
    if (-f $meta_json && -r _) {
        _analyse_json($me, $meta_json);
    }

    # If, and only if META.yml and META.json don't exist,
    # try MYMETA.yml
    if (!$me->d->{meta_yml} && -f $mymeta_yml && -r _) {
        _analyse_yml($me, $mymeta_yml);
    }

    if (!$me->d->{meta_yml}) {
        return;
    }

    # Theoretically it might be better to convert 1.* to 2.0.
    # However, converting 2.0 to 1.4 is much cheaper for CPANTS
    # website as it's much rarer as of this writing.
    if (($me->d->{meta_yml_spec_version} || '1.0') gt '1.4') {
        my $cmc = CPAN::Meta::Converter->new($me->d->{meta_yml});
        my $meta_14 = eval { $cmc->convert(version => '1.4') };
        if (!$@ && $meta_14) {
            $me->d->{meta_yml} = $meta_14;
        }
    }

    $me->d->{dynamic_config} = (!exists $me->d->{meta_yml}{dynamic_config} or $me->d->{meta_yml}{dynamic_config}) ? 1 : 0;
}

sub _analyse_yml {
    my ($me, $file) = @_;
    my @warnings;
    eval {
        # CPAN::Meta::YAML warns if it finds a duplicate key
        local $SIG{__WARN__} = sub { push @warnings, @_ };
        my $meta = CPAN::Meta::YAML->read($file) or die CPAN::Meta::YAML->errstr;
        # Broken META.yml may return a "YAML 1.0" string first.
        # eg. M/MH/MHASCH/Date-Gregorian-0.07.tar.gz
        if (@$meta > 1 or ref $meta->[0] ne ref {}) {
            $me->d->{meta_yml} = first { ref $_ eq ref {} } @$meta;
            $me->d->{error}{meta_yml_is_parsable} = "multiple parts found in META.yml";
        } else {
            $me->d->{meta_yml} = $meta->[0];
            $me->d->{meta_yml_is_parsable} = 1;
        }
    };
    if (my $error = $@) {
        $error =~ s/ at \S+ line \d+.+$//s;
        $me->d->{error}{meta_yml_is_parsable} = $error;
    }
    if ($me->d->{meta_yml}) {
        my ($spec, $error) = _validate_meta($me->d->{meta_yml});
        $me->d->{error}{meta_yml_conforms_to_known_spec} = $error if $error;
        $me->d->{meta_yml_spec_version} = $spec->{spec};
    }
    if (@warnings) {
        $me->d->{error}{meta_yml_has_duplicate_keys} = join ',', @warnings;
    }
}

sub _analyse_json {
    my ($me, $file) = @_;

    my $meta;
    eval {
        my $json = do { open my $fh, '<', $file or die "$file: $!"; local $/; <$fh> };
        $meta = $JSON_DECODER->($json);
        $me->d->{meta_json_is_parsable} = 1;
    };
    if (my $error = $@) {
        $error =~ s/ at \S+ line \d+.+$//s;
        $me->d->{error}{meta_json_is_parsable} = $error;
    }
    if ($meta) {
        my ($spec, $error) = _validate_meta($meta);
        $me->d->{error}{meta_json_conforms_to_known_spec} = $error if $error;
        $me->d->{meta_json_spec_version} = $spec->{spec};
    }
    if (!$me->d->{meta_yml}) {
        $me->d->{meta_yml} = $meta;
        $me->d->{meta_yml_spec_version} = $me->d->{meta_json_spec_version};
        $me->d->{meta_yml_is_meta_json} = 1;
    }
}

sub _load_json_decoder {
    my $json_class = $ENV{CPAN_META_JSON_BACKEND} || $ENV{PERL_JSON_BACKEND} || 'JSON::PP';
    eval "require $json_class; 1" or return;
    $json_class->can('decode_json');
}

sub _validate_meta {
    my $meta = shift;
    my $error;
    my $spec = eval { CPAN::Meta::Validator->new($meta) };
    if ($error = $@) {
        $error =~ s/ at \S+ line \d+.+$//s;
    } elsif (!$spec->is_valid) {
        $error = join ';', sort $spec->errors;
    }
    $error =~ s/(SCALAR|ARRAY|HASH|GLOB|REF)\(0x[0-9a-f]+\)/$1(...)/g;
    return ($spec, $error);
}

##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators{
    return [
        {
            name => 'meta_yml_is_parsable',
            error => q{The META.yml file of this distribution could not be parsed by the version of CPAN::Meta::YAML.pm CPANTS is using.},
            remedy => q{Upgrade your YAML generator so it produces valid YAML.},
            code => sub {
                my $d = shift;
                !$d->{error}{meta_yml_is_parsable} ? 1 : 0
            },
            details => sub {
                my $d = shift;
                $d->{error}{meta_yml_is_parsable};
            },
        },
        {
            name => 'meta_json_is_parsable',
            error => q{The META.json file of this distribution could not be parsed by the version of JSON parser CPANTS is using.},
            remedy => q{Upgrade your META.json generator so it produces valid JSON.},
            code => sub {
                my $d = shift;
                !$d->{error}{meta_json_is_parsable} ? 1 : 0
            },
            details => sub {
                my $d = shift;
                $d->{error}{meta_json_is_parsable};
            },
        },
        {
            name => 'meta_yml_has_provides',
            is_experimental => 1,
            error => q{This distribution does not have a list of provided modules defined in META.yml.},
            remedy => q{Add all modules contained in this distribution to the META.yml field 'provides'. Module::Build or Dist::Zilla::Plugin::MetaProvides do this automatically for you.},
            code => sub {
                my $d = shift;
                return 1 if !$d->{meta_yml};
                return 1 if $d->{meta_yml}{provides};
                return 0;
            },
            details => sub {
                my $d = shift;
                return "No META.yml." unless $d->{meta_yml};
                return q{No "provides" was found in META.yml.};
            },
        },
        {
            name => 'meta_yml_conforms_to_known_spec',
            error => q{META.yml does not conform to any recognised META.yml Spec.},
            remedy => q{Take a look at the META.yml Spec at https://metacpan.org/pod/CPAN::Meta::History::Meta_1_4 (for version 1.4) or https://metacpan.org/pod/CPAN::Meta::Spec (for version 2), and change your META.yml accordingly.},
            code => sub {
                my $d = shift;
                return 0 if $d->{error}{meta_yml_conforms_to_known_spec};
                return 1;
            },
            details => sub {
                my $d = shift;
                return "No META.yml." unless $d->{meta_yml};
                return "META.yml is broken." unless $d->{meta_yml_is_parsable};
                return $d->{error}{meta_yml_conforms_to_known_spec};
            },
        },
        {
            name => 'meta_json_conforms_to_known_spec',
            error => q{META.json does not conform to any recognised META Spec.},
            remedy => q{Take a look at the META.json Spec at https://metacpan.org/pod/CPAN::Meta::History::Meta_1_4 (for version 1.4) or https://metacpan.org/pod/CPAN::Meta::Spec (for version 2), and change your META.json accordingly.},
            code => sub {
                my $d = shift;
                return 0 if $d->{error}{meta_json_is_parsable};
                return 0 if $d->{error}{meta_json_conforms_to_known_spec};
                return 1;
            },
            details => sub {
                my $d = shift;
                return "META.json is broken." unless $d->{meta_json_is_parsable};
                return $d->{error}{meta_json_conforms_to_known_spec};
            },
        },
        {
            name => 'meta_yml_declares_perl_version',
            error => q{This distribution does not declare the minimum perl version in META.yml.},
            is_extra => 1,
            remedy => q{If you are using Build.PL define the {requires}{perl} = VERSION field. If you are using MakeMaker (Makefile.PL) you should upgrade ExtUtils::MakeMaker to 6.48 and use MIN_PERL_VERSION parameter. Perl::MinimumVersion can help you determine which version of Perl your module needs.},
            code => sub {
                my $d = shift;
                my $yaml = $d->{meta_yml};
                return 1 unless $yaml;
                return ref $yaml->{requires} eq ref {} && $yaml->{requires}{perl} ? 1 : 0;
            },
            details => sub {
                my $d = shift;
                my $yaml = $d->{meta_yml};
                return "No META.yml." unless $yaml;
                return q{No "requires" was found in META.yml.} unless ref $yaml->{requires} eq ref {};
                return q{No "perl" subkey was found in META.yml.} unless $yaml->{requires}{perl};
            },
        },
        {
            name => 'meta_yml_has_repository_resource',
            is_experimental => 1,
            error => q{This distribution does not have a link to a repository in META.yml.},
            remedy => q{Add a 'repository' resource to the META.yml via 'meta_add' accessor (for Module::Build) or META_ADD parameter (for ExtUtils::MakeMaker).},
            code => sub {
                my $d = shift;
                my $yaml = $d->{meta_yml};
                return 1 unless $yaml;
                return ref $yaml->{resources} eq ref {} && $yaml->{resources}{repository} ? 1 : 0;
            },
            details => sub {
                my $d = shift;
                my $yaml = $d->{meta_yml};
                return "No META.yml." unless $yaml;
                return q{No "resources" was found in META.yml.} unless ref $yaml->{resources} eq ref {};
                return q{No "repository" subkey was found in META.yml.} unless $yaml->{resources}{repository};
            },
        },
    ];
}

q{Barbies Favourite record of the moment:
  Nine Inch Nails: Year Zero};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::MetaYML - Checks data available in META.yml

=head1 SYNOPSIS

Checks various pieces of information in F<META.yml>

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<10>. C<MetaYML> should be checked earlier than C<Files> to
handle C<no_index> correctly.

=head3 analyse

C<MCK::MetaYML> checks C<META.yml>.

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=over

=item * meta_yml_is_parsable

=item * meta_yml_has_provides

=item * meta_yml_conforms_to_known_spec

=item * meta_yml_declares_perl_version

=item * meta_yml_has_repository_resource

=item * meta_json_is_parsable

=item * meta_json_conforms_to_known_spec

=back

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
