package Perl::PrereqScanner::NotQuiteLite::Parser::TestClassMost;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'Test::Class::Most' => 'parse_test_class_most_args',
  },
}}

sub parse_test_class_most_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_token_list($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }

  while(my ($key, $value) = splice @$tokens, 0, 2) {
    my $keystr = ref $key ? $key->[0] : $key;
    if ($keystr eq 'parent') {
      if (!ref $value) {
        $c->add($value => 0);
      } elsif ($value->[1] eq '[]') {
        my $tokens_inside = convert_string_token_list($value->[0]);
        $c->add($_ => 0) for @$tokens_inside;
      }
    }
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::TestClassMost

=head1 DESCRIPTION

This parser is to deal with conditional loading by C<Test::Class::Most>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
