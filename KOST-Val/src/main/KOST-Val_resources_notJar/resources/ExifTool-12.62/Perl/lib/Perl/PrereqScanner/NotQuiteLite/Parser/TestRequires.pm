package Perl::PrereqScanner::NotQuiteLite::Parser::TestRequires;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'Test::Requires' => 'parse_test_requires_args',
  },
}}

sub parse_test_requires_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->register_keyword_parser(
    'test_requires',
    [$class, 'parse_test_requires_function_args', $used_module],
  );

  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }

  if (ref $tokens->[0] and $tokens->[0][1] and $tokens->[0][1] eq '{}') {
    my $tokens_in_hashref = convert_string_tokens($tokens->[0][0]);
    while(my ($key, undef, $value, undef) = splice @$tokens_in_hashref, 0, 4) {
      next unless is_module_name($key);
      next unless is_version($value);
      $c->add_recommendation($key => $value);
    }
  } else {
    for my $token (@$tokens) {
      next if ref $token;
      if ($token =~ /^v?5/) {
        $c->add_recommendation(perl => $token);
      } else {
        $c->add_recommendation($token => 0);
      }
    }
  }
}

sub parse_test_requires_function_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  $c->add_recommendation($_ => 0) for grep {!ref $_} @$tokens;
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::TestRequires

=head1 DESCRIPTION

This parser is to deal with conditional loading by C<Test::Requires>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
