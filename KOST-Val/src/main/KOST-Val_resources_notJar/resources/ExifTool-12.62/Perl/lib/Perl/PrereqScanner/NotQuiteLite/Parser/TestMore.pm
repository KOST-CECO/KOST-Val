package Perl::PrereqScanner::NotQuiteLite::Parser::TestMore;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'Test::More' => 'parse_test_more_args',
  },
}}

sub register_fqfn { return +{
  'Test::More::done_testing' => 'parse_done_testing_args',
  'Test::More::plan' => 'parse_plan_args',
}}

sub parse_test_more_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->register_keyword_parser(
    'done_testing',
    [$class, 'parse_done_testing_args', $used_module],
  );

  $c->register_keyword_parser(
    'plan',
    [$class, 'parse_plan_args', $used_module],
  );
}

sub parse_done_testing_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->add($used_module => '0.88');
}

sub parse_plan_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard plan

  my $first_token = $tokens->[0] or return;
  $first_token = $first_token->[0] if ref $first_token;

  if ($first_token eq 'skip_all') {
    if (grep {$_->[0] eq '{' and $_->[2] eq 'BEGIN'} @{$c->{stack} || []}) {
      $c->{force_cond} = 1;
    }
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::TestMore

=head1 DESCRIPTION

This parser is to update the minimum version requirement of
L<Test::More> to 0.88 if C<done_testing> is found by the scanner.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
