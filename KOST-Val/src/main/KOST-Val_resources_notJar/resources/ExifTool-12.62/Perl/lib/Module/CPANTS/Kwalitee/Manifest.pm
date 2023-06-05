package Module::CPANTS::Kwalitee::Manifest;
use warnings;
use strict;
use File::Spec::Functions qw(catfile);
use Array::Diff;

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
    my $manifest_file = catfile($distdir, 'MANIFEST');

    if (-e $manifest_file) {
        # read manifest
        open(my $fh, '<', $manifest_file) or die "cannot read MANIFEST $manifest_file: $!";
        my %seen;
        while (<$fh>) {
            chomp;
            next if /^\s*#/; # discard pure comments
            if (s/^'(\\[\\']|.+)+'\s*.*/$1/) {
                s/\\([\\'])/$1/g;
            } else {
                s/\s.*$//;
            } # strip quotes and comments
            next unless $_; # discard blank lines
            $seen{$_}++;
        }
        close $fh;

        my @manifest = sort keys %seen;
        my @files = sort keys %{$me->d->{files_hash} || {}};
        my @dupes = grep {$seen{$_} > 1} @manifest;

        my $diff = Array::Diff->diff(\@manifest, \@files);
        if ($diff->count == 0 && !@dupes) {
            $me->d->{manifest_matches_dist} = 1;
        }
        else {
            $me->d->{manifest_matches_dist} = 0;
            my @error = (
                'MANIFEST ('.(@manifest + @dupes).') does not match dist ('.@files."):",
            );
            if (my @added = @{$diff->added}) {
                push @error, "Missing in MANIFEST: ".join(', ', @added);
            }
            if (my @deleted = @{$diff->deleted}) {
                push @error, "Missing in Dist: " . join(', ', @deleted);
            }
            if (@dupes) {
                push @error, "Duplicates in MANIFEST: " . join(', ', @dupes);
            }
            $me->d->{error}{manifest_matches_dist} = \@error;
        }

        # Tweak symlinks error for a local distribution (RT #97858)
        if ($me->d->{is_local_distribution} && $me->d->{error}{symlinks}) {
            my %manifested = map {$_ => 1} @manifest;
            my @symlinks = grep {$manifested{$_}} split ',', $me->d->{error}{symlinks};
            if (@symlinks) {
                $me->d->{error}{symlinks} = join ',', @symlinks;
            } else {
                delete $me->d->{error}{symlinks};
            }
        }
    }
    else {
        $me->d->{manifest_matches_dist} = 0;
        $me->d->{error}{manifest_matches_dist} = q{Cannot find MANIFEST in dist.};
    }
}

##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators {
    return [
        {
            name => 'manifest_matches_dist',
            error => q{MANIFEST does not match the contents of this distribution.},
            remedy => q{Run a proper command ("make manifest" or "./Build manifest", maybe with a force option), or use a distribution builder to generate the MANIFEST. Or update MANIFEST manually.},
            code => sub { shift->{manifest_matches_dist} ? 1 : 0 },
            details => sub {
                my $d = shift;
                my $error = $d->{error}{manifest_matches_dist};
                return $error unless ref $error;
                return join "\n", @$error;
            },
        }
    ];
}


q{Listening to: YAPC::Europe 2007};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::Manifest - Check MANIFEST

=head1 SYNOPSIS

Check if MANIFEST and dist contents match.

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<100>.

=head3 analyse

Check if MANIFEST and dist contents match.

=head3 kwalitee_indicators

Returns the Kwalitee Indicators data structure.

=over

=item * manifest_matches_dist

=back

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

Thomas Klausner, <domm@cpan.org>, https://domm.plix.at/

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2006, 2009 L<Thomas Klausner|https://metacpan.org/author/domm>

You may use and distribute this module according to the same terms
that Perl is distributed under.
