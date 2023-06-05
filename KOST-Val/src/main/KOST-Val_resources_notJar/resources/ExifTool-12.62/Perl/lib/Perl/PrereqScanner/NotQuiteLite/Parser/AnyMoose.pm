package Perl::PrereqScanner::NotQuiteLite::Parser::AnyMoose;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register {{
  use => {
    'Any::Moose' => 'parse_any_moose_args',
  },
  no => {
    'Any::Moose' => 'remove_extends_and_with',
  },
}}

sub parse_any_moose_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }
  while(my $token = shift @$tokens) {
    next if ref $token;

    # As Any::Moose falls back to Mouse, it's nice to have
    # a Mouse variant, but that should not be required.
    my $module = "Mouse$token";
    $c->add_recommendation($module => 0) if is_module_name($module);
  }

  $c->register_keyword_parser(
    'extends',
    [$class, 'parse_extends_args', $used_module],
  );
  $c->register_keyword_parser(
    'with',
    [$class, 'parse_with_args', $used_module],
  );
}

sub remove_extends_and_with {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->remove_keyword('extends');
  $c->remove_keyword('with');
}

sub parse_extends_args { shift->_parse_loader_args(@_) }
sub parse_with_args { shift->_parse_loader_args(@_) }

sub _parse_loader_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard extends, with;

  my $prev;
  my $saw_any_moose;
  while(my $token = shift @$tokens) {
    if (!ref $token) {
      if ($saw_any_moose) {
        my $module = "Mouse$token";
        $c->add_recommendation($module => 0);
        $prev = $module;
      } else {
        $c->add($token => 0);
        $prev = $token;
      }
      next;
    }
    if ($token->[0] eq 'any_moose') {
      $saw_any_moose = 1;
      next;
    }
    my $desc = $token->[1] || '';
    if ($desc eq '{}') {
      my @hash_tokens = @{$token->[0] || []};
      for(my $i = 0, my $len = @hash_tokens; $i < $len; $i++) {
        if ($hash_tokens[$i][0] eq '-version' and $i < $len - 2) {
          my $maybe_version_token = $hash_tokens[$i + 2];
          my $maybe_version = $maybe_version_token->[0];
          if (ref $maybe_version) {
            $maybe_version = $maybe_version->[0];
          }
          if ($prev and is_version($maybe_version)) {
            if ($saw_any_moose) {
              $c->add_recommendation($prev => $maybe_version);
            } else {
              $c->add($prev => $maybe_version);
            }
          }
        }
      }
    }
    if ($saw_any_moose and $desc eq '()') {
      my $tokens_in_parentheses = convert_string_tokens($token->[0]);
      for my $token_in_parentheses (@$tokens_in_parentheses) {
        next if ref $token_in_parentheses;
        my $module = "Mouse$token_in_parentheses";
        $c->add_recommendation($module => 0) if is_module_name($module);
      }
    }
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::AnyMoose

=head1 DESCRIPTION

This parser is to deal with modules loaded by C<extends>
from L<Any::Moose> and its friends.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
