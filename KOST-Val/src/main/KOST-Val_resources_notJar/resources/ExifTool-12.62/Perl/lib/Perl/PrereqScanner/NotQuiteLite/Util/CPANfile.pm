package Perl::PrereqScanner::NotQuiteLite::Util::CPANfile;

use strict;
use warnings;
use parent 'Module::CPANfile';
use Perl::PrereqScanner::NotQuiteLite::Util::Prereqs;

sub load_and_merge {
  my ($class, $file, $prereqs, $features) = @_;

  $prereqs = $prereqs->as_string_hash unless ref $prereqs eq 'HASH';

  my $self;
  if (-f $file) {
    $self = $class->load($file);
    $self->_merge_prereqs($prereqs);
  } else {
    $self = $class->from_prereqs($prereqs);
  }

  if ($features) {
    for my $identifier (keys %$features) {
      my $feature = $features->{$identifier};
      next unless $feature->{prereqs};
      $self->_merge_prereqs($feature->{prereqs}, $identifier) or next;
      $self->{_prereqs}->add_feature($identifier, $feature->{description});
    }
  }

  $self->_dedupe;

  $self;
}

sub features {
    my $self = shift;
    map $self->feature($_), sort $self->{_prereqs}->identifiers;  # TWEAKED
}

sub _merge_prereqs {
  my ($self, $prereqs, $feature_id) = @_;
  $prereqs = $prereqs->as_string_hash unless ref $prereqs eq 'HASH';

  my $current = CPAN::Meta::Prereqs->new($self->{_prereqs}->specs($feature_id));
  my $merged = $current->with_merged_prereqs(CPAN::Meta::Prereqs->new($prereqs));

  $self->__replace_prereqs($merged, $feature_id);
}

sub __replace_prereqs {
  my ($self, $prereqs, $feature_id) = @_;
  $prereqs = $prereqs->as_string_hash unless ref $prereqs eq 'HASH';

  @{$self->{_prereqs}{prereqs}{$feature_id || ''}} = ();
  my $added = 0;
  for my $phase (keys %$prereqs) {
    for my $type (keys %{$prereqs->{$phase}}) {
      while (my($module, $requirement) = each %{$prereqs->{$phase}{$type}}) {
        $self->{_prereqs}->add(
          feature => $feature_id,
          phase => $phase,
          type  => $type,
          module => $module,
          requirement => Module::CPANfile::Requirement->new(name => $module, version => $requirement),
        );
        $added++
      }
    }
  }
  delete $self->{_prereqs}{cpanmeta} unless $feature_id;  # to rebuild cpanmeta
  $added;
}

sub _dedupe {
  my $self = shift;
  my $prereqs = $self->prereqs;
  my %features = map {$_ => $self->feature($_)->{prereqs} } $self->{_prereqs}->identifiers;

  dedupe_prereqs_and_features($prereqs, \%features);

  $self->__replace_prereqs($prereqs);
  for my $feature_id (keys %features) {
    $self->__replace_prereqs($features{$feature_id}, $feature_id);
  }
}

sub _dump_prereqs {
  my($self, $prereqs, $include_empty, $base_indent) = @_;

  my $code = '';
  my @x_phases = sort grep {/^x_/i} keys %$prereqs; # TWEAKED
  for my $phase (qw(runtime configure build test develop), @x_phases) {
    my $indent = $phase eq 'runtime' ? '' : '    ';
    $indent = (' ' x ($base_indent || 0)) . $indent;

    my($phase_code, $requirements);
    $phase_code .= "on $phase => sub {\n" unless $phase eq 'runtime';

    my @x_types = sort grep {/^x_/i} keys %{$prereqs->{$phase}}; # TWEAKED
    for my $type (qw(requires recommends suggests conflicts), @x_types) {
      for my $mod (sort keys %{$prereqs->{$phase}{$type}}) {
        my $ver = $prereqs->{$phase}{$type}{$mod};
        $phase_code .= $ver eq '0'
          ? "${indent}$type '$mod';\n"
          : "${indent}$type '$mod', '$ver';\n";
        $requirements++;
      }
    }

    $phase_code .= "\n" unless $requirements;
    $phase_code .= "};\n" unless $phase eq 'runtime';

    $code .= $phase_code . "\n" if $requirements or $include_empty;
  }

  $code =~ s/\n+$/\n/s;
  $code;
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Util::CPANfile

=head1 SYNOPSIS

=head1 DESCRIPTION

This is a wrapper of L<Module::CPANfile>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
