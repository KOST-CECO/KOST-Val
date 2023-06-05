package Module::CPANTS::Kwalitee::Prereq;
use warnings;
use strict;
use File::Spec::Functions qw(catfile);
use Text::Balanced qw/extract_bracketed/;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 100 }

##################################################################
# Analyse
##################################################################

sub analyse {
    my $class = shift;
    my $me = shift;

    $class->_from_meta($me) or
    $class->_from_cpanfile($me) or
    $class->_from_build_pl($me) or
    $class->_from_makefile_pl($me) or
    $class->_from_dist_ini($me);
}

sub _from_meta {
    my ($class, $me) = @_;
    my $meta = $me->d->{meta_yml};
    return unless $meta && ref $meta eq ref {};

    my $spec = $meta->{'meta-spec'};
    my %res;
    if ($spec && ref $spec eq ref {} && ($spec->{version} || 0) =~ /^(\d+)/ && $1 >= 2) {
        # meta spec ver2
        my $prereqs = $meta->{prereqs};

        %res = $class->_handle_prereqs_v2($meta->{prereqs});
    } else {
        # meta spec ver1
        my %map = (
            requires       => 'is_prereq',
            build_requires => 'is_build_prereq',
            recommends     => 'is_optional_prereq',
        );
        for my $rel (qw/requires recommends build_requires configure_requires conflicts/) {
            if ($meta->{$rel} && ref $meta->{$rel} eq ref {}) {
                my $prereqs_r = $meta->{$rel};
                next unless $prereqs_r && ref $prereqs_r eq ref {};
                for my $module (keys %$prereqs_r) {
                    my $type = $rel =~ /_/ ? $rel : "runtime_$rel";
                    push @{$res{$module} ||= []}, {
                        requires => $module,
                        version => $prereqs_r->{$module},
                        type => $type,
                        ($map{$rel} ? ($map{$rel} => 1) : ()),
                    };
                }
            }
        }

        # TODO: optional_features handling
    }

    return unless %res;
    $me->d->{prereq} = [sort {$a->{requires} cmp $b->{requires}} map {@$_} values %res];
    $me->d->{got_prereq_from} = 'META.yml';
}

sub _from_cpanfile {
    my ($class, $me) = @_;

    my $cpanfile = catfile($me->distdir, "cpanfile");
    return unless -f $cpanfile;
    eval { require Module::CPANfile; 1 };
    return if $@;
    my $prereqs = Module::CPANfile->load($cpanfile)->prereqs->as_string_hash;
    my %res = $class->_handle_prereqs_v2($prereqs);
    return unless %res;

    $me->d->{prereq} = [sort {$a->{requires} cmp $b->{requires}} map {@$_} values %res];
    $me->d->{got_prereq_from} = 'cpanfile';
}

sub _from_build_pl {
    my ($class, $me) = @_;

    my $build_pl_file = catfile($me->distdir, "Build.PL");
    return unless -f $build_pl_file;

    my $build_pl = do { local $/; open my $fh, '<', $build_pl_file; <$fh> };
    return unless $build_pl;

    my %map = (
        requires       => 'is_prereq',
        build_requires => 'is_build_prereq',
        test_requires  => 'is_build_prereq',
        recommends     => 'is_optional_prereq',
    );
    my %res;
    # TODO: auto_features
    while($build_pl =~ s/^.*?((?:(?:configure|build|test)_)?requires|recommends|conflicts)\s*=>\s*\{/{/s) {
        my $rel = $1;
        my ($block, $left) = extract_bracketed($build_pl, '{}');
        last unless $block;

        my $hashref = do { no strict; no warnings; eval $block }; ## no critic
        if ($hashref && ref $hashref eq ref {}) {
            for my $module (keys %$hashref) {
                my $type = $rel =~ /_/ ? $rel : "runtime_$rel";
                my ($version) = ($hashref->{$module} || 0) =~ /^([0-9.]+)/;
                push @{$res{$module} ||= []}, {
                    requires => $module,
                    version => $version,
                    type => $type,
                    ($map{$rel} ? ($map{$rel} => 1) : ()),
                };
            }
        }

        $build_pl = $left;
    }
    $me->d->{prereq} = [sort {$a->{requires} cmp $b->{requires}} map {@$_} values %res];
    $me->d->{got_prereq_from} = 'Build.PL';
}

sub _from_makefile_pl {
    my ($class, $me) = @_;

    my $distdir = $me->distdir;
    my %map = (
        PREREQ_PM      => 'is_prereq',
        BUILD_REQUIRES => 'is_build_prereq',
        TEST_REQUIRES  => 'is_build_prereq',
    );

    # There may be multiple Makefile.PLs in a distribution
    my %res;
    for my $file (@{$me->d->{files_array} || []}) {
        next unless $file =~ /Makefile\.PL$/;
        my $makefile_pl_file = catfile($distdir, $file);
        next unless -f $makefile_pl_file;

        my $makefile_pl = do { local $/; open my $fh, '<', $makefile_pl_file; <$fh> };
        next unless $makefile_pl;

        if ($makefile_pl =~ /use\s+inc::Module::Install/) {
            # Module::Install

            # TODO
            while($makefile_pl =~ s/(?:^|;).+?((?:(?:configure|build|test)_)?requires|recommends)\s*([^;]+);//s) {
                my ($rel, $tuple_text) = ($1, $2);
                my @tuples = do { no strict; no warnings; eval $tuple_text }; ## no critic
                my $type = $rel =~ /_/ ? $rel : "runtime_$rel";
                while(@tuples) {
                    my $module = shift @tuples or last;
                    my $version = shift @tuples || 0;
                        push @{$res{$module} ||= []}, {
                        requires => $module,
                        version => $version,
                        type => $type,
                        ($map{$rel} ? ($map{$rel} => 1) : ()),
                    };
                }
            }
        } else {
            # EUMM
            while($makefile_pl =~ s/^.*?((?:BUILD|TEST)_REQUIRES|PREREQ_PM)\s*=>\s*\{/{/s) {
                my $rel = $1;
                my ($block, $left) = extract_bracketed($makefile_pl, '{}');
                last unless $block;

                my $hashref = do { no strict; no warnings; eval $block }; ## no critic
                if ($hashref && ref $hashref eq ref {}) {
                    for my $module (keys %$hashref) {
                        my $type = $rel eq 'PREREQ_PM' ? "runtime_requires" : lc $rel;
                        push @{$res{$module} ||= []}, {
                            requires => $module,
                            version => $hashref->{$module},
                            type => $type,
                            ($map{$rel} ? ($map{$rel} => 1) : ()),
                        };
                    }
                }
                $makefile_pl = $left;
            }
        }
    }
    $me->d->{prereq} = [sort {$a->{requires} cmp $b->{requires}} map {@$_} values %res];
    $me->d->{got_prereq_from} = 'Makefile.PL';
}

# for META spec v2 and cpanfile
sub _handle_prereqs_v2 {
    my ($class, $prereqs) = @_;

    return unless $prereqs && ref $prereqs eq ref {};

    # XXX: this mapping is for backward compat only
    my %map = (
        runtime_requires   => 'is_prereq',
        build_requires     => 'is_build_prereq',
        test_requires      => 'is_build_prereq',
        runtime_recommends => 'is_optional_prereq',
        build_recommends   => 'is_optional_prereq',
        test_recommends    => 'is_optional_prereq',
        runtime_suggests   => 'is_optional_prereq',
        build_suggests     => 'is_optional_prereq',
        test_suggests      => 'is_optional_prereq',
    );

    my %res;
    for my $phase (keys %$prereqs) {
        my $prereqs_p = $prereqs->{$phase};
        next unless $prereqs_p && ref $prereqs_p eq ref {};
        for my $rel (keys %$prereqs_p) {
            my $prereqs_r = $prereqs_p->{$rel};
            next unless $prereqs_r && ref $prereqs_r eq ref {};
            for my $module (keys %$prereqs_r) {
                my $type = join '_', $phase, $rel;
                push @{$res{$module} ||= []}, {
                    requires => $module,
                    version => $prereqs_r->{$module},
                    type => $type,
                    ($map{$type} ? ($map{$type} => 1) : ()),
                };
            }
        }
    }
    %res;
}

sub _from_dist_ini {
    my ($class, $me) = @_;

    my $inifile = catfile($me->distdir, "dist.ini");
    return unless -f $inifile;

    eval { require Config::INI::Reader } or return;

    my $config = Config::INI::Reader->read_file($inifile);
    return unless $config && ref $config eq ref {};

    my %map = (
        runtime_requires   => 'is_prereq',
        build_requires     => 'is_build_prereq',
        test_requires      => 'is_build_prereq',
        runtime_recommends => 'is_optional_prereq',
        build_recommends   => 'is_optional_prereq',
        test_recommends    => 'is_optional_prereq',
        runtime_suggests   => 'is_optional_prereq',
        build_suggests     => 'is_optional_prereq',
        test_suggests      => 'is_optional_prereq',
    );
    my %res;
    for my $key (keys %$config) {
        next unless $key =~ /^Prereqs\b/;
        my ($phase, $rel) = qw(runtime requires);
        (undef, my $type) = split /\s*\/\s*/, $key, 2;
        if ($type) {
            if ($type =~ s/^(Configure|Build|Test|Runtime)//) {
                $phase = lc $1;
            }
            if ($type =~ s/^(Requires|Recommends|Suggests)//) {
                $rel = lc $1;
            }
        }
        my $conf = $config->{$key};
        next unless $conf && ref $conf eq ref {};
        if ($conf->{-phase}) {
            $phase = delete $conf->{-phase};
        }
        if ($conf->{-relationship}) {
            $rel = delete $conf->{-relationship};
        }
        for my $module (keys %$conf) {
            $type = join '_', $phase, $rel;
            push @{$res{$module} ||= []}, {
                requires => $module,
                version => $conf->{$module},
                type => $type,
                ($map{$type} ? ($map{$type} => 1) : ()),
            };
        }
    }
    $me->d->{prereq} = [sort {$a->{requires} cmp $b->{requires}} map {@$_} values %res];
    $me->d->{got_prereq_from} = 'dist.ini';
}

##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators{
    # NOTE: The metrics in this module have moved to
    # Module::CPANTS::SiteKwalitee because these require databases.

    return [];
}


q{Favourite record of the moment:
  Fat Freddys Drop: Based on a true story};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::Prereq - Checks listed prerequisites

=head1 SYNOPSIS

The metrics in this module have moved to L<Module::CPANTS::SiteKwalitee::Prereq|https://github.com/cpants/Module-CPANTS-SiteKwalitee>.

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<100>.

=head3 analyse

Find information on prerequisite distributions from meta files etc.

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
