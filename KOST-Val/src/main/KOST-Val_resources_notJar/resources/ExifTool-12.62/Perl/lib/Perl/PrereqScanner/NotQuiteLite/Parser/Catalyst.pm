package Perl::PrereqScanner::NotQuiteLite::Parser::Catalyst;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    Catalyst => 'parse_catalyst_args',
  },
}}

sub parse_catalyst_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my @copied_tokens = @$raw_tokens;
  if (($copied_tokens[0][1] || '') eq '()') {
    my $token = shift @copied_tokens;
    unshift @copied_tokens, @{$token->[0]};
  }
  if (is_version($copied_tokens[0])) {
    $c->add($used_module => shift @copied_tokens);
  }

  my @plugins;
  for my $token (@copied_tokens) {
    my $desc = $token->[1] or next;
    if ($desc eq 'STRING') {
      push @plugins, $token->[0][0];
    } elsif ($desc eq 'QUOTED_WORD_LIST') {
      push @plugins, split /\s/, $token->[0][0];
    }
  }

  for my $plugin (@plugins) {
    next if $plugin =~ /^\-/;
    $plugin = "Catalyst::Plugin::$plugin" unless $plugin =~ s/^\+//;
    $c->add($plugin => 0) if is_module_name($plugin);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Catalyst

=head1 DESCRIPTION

This parser is to deal with module inheritance by C<Catalyst>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
