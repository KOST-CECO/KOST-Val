package Perl::PrereqScanner::NotQuiteLite::Parser::Superclass;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    superclass => 'parse_superclass_args',
  },
}}

sub parse_superclass_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }

  my ($module, $version, $prev);
  for my $token (@$tokens) {
    last if $token eq '-norequire';
    if (ref $token) {
      last if $token->[0] eq '-norequire';
      $prev = $token->[0];
      next;
    }
    $prev = $token;

    if (is_module_name($token)) {
      if ($module) {
        $c->add($module => $version || 0);
      }
      $module = $token;
      next;
    }
    if (is_version($token)) {
      $c->add($module => $token);
    }
  }
  if ($module) {
    $c->add($module => 0);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Superclass

=head1 DESCRIPTION

This parser is to deal with module inheritance managed by
L<superclass>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
