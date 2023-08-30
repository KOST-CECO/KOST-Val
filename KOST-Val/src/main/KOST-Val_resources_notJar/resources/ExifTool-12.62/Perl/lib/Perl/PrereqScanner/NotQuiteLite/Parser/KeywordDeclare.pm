package Perl::PrereqScanner::NotQuiteLite::Parser::KeywordDeclare;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'Keyword::Declare' => 'parse_keyword_declare_args',
  },
}}

sub parse_keyword_declare_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->register_sub_keywords(qw/keyword/);
  $c->register_keywords(qw/keytype is unkeyword/);
  $c->register_op_keywords(qw/is/);

  $c->register_sub_parser(
    'keyword',
    [$class, 'parse_keyword_args', $used_module],
  );
  $c->register_keyword_parser(
    'unkeyword',
    [$class, 'parse_unkeyword_args', $used_module],
  );
}

sub parse_keyword_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard keyword

  if (ref $tokens->[0] and $tokens->[0][1]) {
    $c->register_keywords($tokens->[0][1]);
  }
}

sub parse_unkeyword_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard unkeyword

  if (ref $tokens->[0] and $tokens->[0][1]) {
    $c->remove_keywords($tokens->[0][0]);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::KeywordDeclare

=head1 DESCRIPTION

This parser is to deal with keywords imported from L<Keyword::Declare>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2018 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
