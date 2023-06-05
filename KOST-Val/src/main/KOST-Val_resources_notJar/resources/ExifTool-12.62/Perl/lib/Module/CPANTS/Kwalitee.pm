package Module::CPANTS::Kwalitee;
use 5.006;
use strict;
use warnings;
use base qw(Class::Accessor::Fast);
use Carp;
use Module::Find qw(usesub);

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

__PACKAGE__->mk_accessors(qw(_available _total));

my @Plugins;
my @SearchPaths = ('Module::CPANTS::Kwalitee');
my @Indicators;
my %IndicatorHash;
my $Total;
my $Available;

sub import {
    my ($class, @search_paths) = @_;
    for my $path (@search_paths) {
        next unless $path =~ /^[A-Za-z][A-Za-z0-9_]*(::[A-Za-z][A-Za-z0-9_]*)*$/;
        push @SearchPaths, $path =~ /^Module::CPANTS::/ ? $path : "Module::CPANTS::$path";

        my %seen;
        @SearchPaths = grep {!$seen{$_}++} @SearchPaths;
    }
}

sub _load_plugins {
    my $class = shift;
    unless (@Plugins) {
        my %seen;
        @Plugins = sort {$a->order <=> $b->order or $a cmp $b}
                   grep {!$seen{$_}++}
                   map  {usesub $_} @SearchPaths;
        $class->_cache_indicators;
    }
}

# I suppose nobody wants to change the generators dynamically though
sub _cache_indicators {
    my $class = shift;
    @Indicators = ();
    $Total = $Available = 0;
    for my $plugin (@Plugins) {
        for my $indicator (@{$plugin->kwalitee_indicators}) {
            $indicator->{defined_in} = $plugin;
            $indicator->{is_core} = 1 if !$indicator->{is_extra} and !$indicator->{is_experimental};
            push @Indicators, $indicator;
            $Total++ unless $indicator->{is_experimental};
            $Available++ if $indicator->{is_core};
        }
    }
}

sub plugins { @Plugins }

sub new {
    my $class = shift;
    $class->_load_plugins;
    bless {}, $class;
}

sub generators {
    my $self = shift;
    return \@Plugins unless @_;
    @Plugins = @{$_[0]};
    $self->_cache_indicators;
    \@Plugins;
}

sub get_indicators {
    my ($self, $type) = @_;
    unless ($type) { # almost always true
        return wantarray ? @Indicators : \@Indicators;
    }

    $type = 'is_core' if $type eq 'core';
    $type = 'is_extra' if $type eq 'optional';
    $type = 'is_experimental' if $type eq 'experimental';

    my @indicators;
    for my $indicator (@Indicators) {
        next if !$indicator->{$type};
        push @indicators, $indicator;
    }

    return wantarray ? @indicators : \@indicators;
}

sub get_indicators_hash {
    my $self = shift;
    return \%IndicatorHash if %IndicatorHash;

    foreach my $ind (@Indicators) {
        $IndicatorHash{$ind->{name}} = $ind;
    }
    return \%IndicatorHash;
}

sub available_kwalitee { $Available }

sub total_kwalitee { $Total }

sub _indicator_names {
    my ($self, $coderef) = @_;
    my @names = map { $_->{name} } grep {$coderef->($_)} $self->get_indicators;
    return wantarray ? @names : \@names;
}

sub all_indicator_names { shift->_indicator_names(sub {1}) }

sub core_indicator_names {
    shift->_indicator_names(sub {$_->{is_core}});
}

sub optional_indicator_names {
    shift->_indicator_names(sub {$_->{is_extra}});
}

sub experimental_indicator_names {
    shift->_indicator_names(sub {$_->{is_experimental}});
}

q{Favourite record of the moment:
  Jahcoozi: Pure Breed Mongrel};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee - Interface to Kwalitee generators

=head1 SYNOPSIS

  my $mck = Module::CPANTS::Kwalitee->new;
  my @generators = $mck->generators;

=head1 DESCRIPTION

=head2 Methods

=head3 new

Plain old constructor.

Loads all Plugins.

=head3 get_indicators

Get the list of all Kwalitee indicators, either as an ARRAY or ARRAYREF.

=head3 get_indicators_hash

Get the list of all Kwalitee indicators as an HASHREF.

=head3 core_indicator_names

Get a list of core indicator names (NOT the whole indicator HASHREF).

=head3 optional_indicator_names

Get a list of optional indicator names (NOT the whole indicator HASHREF).

=head3 experimental_indicator_names

Get a list of experimental indicator names (NOT the whole indicator HASHREF).

=head3 all_indicator_names

Get a list of all indicator names (NOT the whole indicator HASHREF).

=head3 available_kwalitee

Get the number of available kwalitee points

=head3 total_kwalitee

Get the total number of kwalitee points. This is bigger the available_kwalitee as some kwalitee metrics are marked as 'extra' (e.g. C<is_prereq>).

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Thomas Klausner|https://metacpan.org/author/domm>

=head1 COPYRIGHT AND LICENSE

Copyright © 2003–2006, 2009 L<Thomas Klausner|https://metacpan.org/author/domm>

You may use and distribute this module according to the same terms
that Perl is distributed under.
