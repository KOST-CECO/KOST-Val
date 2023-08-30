package Perl::PrereqScanner::NotQuiteLite::Util::Prereqs;

use strict;
use warnings;
use Exporter 5.57 'import';

our @EXPORT = qw/dedupe_prereqs_and_features/;

sub dedupe_prereqs_and_features {
  my ($prereqs, $features) = @_;

  my @valid_features = grep defined, values %$features;
  for my $phase ($prereqs->phases) {
    my $requires = $prereqs->requirements_for($phase, 'requires');
    for my $type (qw/recommends suggests/) {
      my $target = $prereqs->requirements_for($phase, $type);
      _dedupe($requires, $target);
    }
    for my $feature (@valid_features) {
      for my $type (qw/requires recommends suggests/) {
        my $target = $feature->requirements_for($phase, $type);
        _dedupe($requires, $target);
      }
    }
    my $recommends = $prereqs->requirements_for($phase, 'recommends');
    for my $type (qw/suggests/) {
      my $target = $prereqs->requirements_for($phase, $type);
      _dedupe($recommends, $target);
    }
    for my $feature (@valid_features) {
      for my $type (qw/recommends suggests/) {
        my $target = $feature->requirements_for($phase, $type);
        _dedupe($recommends, $target);
      }
    }
    my $suggests = $prereqs->requirements_for($phase, 'suggests');
    for my $feature (@valid_features) {
      for my $type (qw/suggests/) {
        my $target = $feature->requirements_for($phase, $type);
        _dedupe($suggests, $target);
      }
    }
  }
}

sub _dedupe {
  my ($source, $target) = @_;
  my @modules = $source->required_modules;
  for my $module (@modules) {
    my $version = $target->requirements_for_module($module);
    next unless defined $version;
    next unless $version =~ /^[0-9._]+$/;
    next unless $source->accepts_module($module, $version);
    $target->clear_requirement($module);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Util::Prereqs

=head1 SYNOPSIS

=head1 DESCRIPTION

This is an internal utility to dedupe prereqs.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
