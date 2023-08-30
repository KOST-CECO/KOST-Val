package Perl::PrereqScanner::NotQuiteLite::Parser::SyntaxCollector;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'Syntax::Collector' => 'parse_syntax_collector_args',
  },
}}

sub parse_syntax_collector_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_token_list($raw_tokens);

  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }

  my $spec;
  if (!(@$tokens % 2)) {
    while(my ($key, $value) = splice @$tokens, 0, 2) {
      my $keystr = ref $key ? $key->[0] : $key;
      if ($keystr eq '-collect') {
        $spec = $value;
        last;
      }
    }
  } else {
    $spec = $tokens->[0];
  }
  if (ref $spec) {
    $spec = $spec->[0];
  }
  return unless $spec;

  my @features =
    map {
      m{^
        (use|no) \s+      # "use" or "no"
        (\S+) \s+         # module name
        ([\d\._v]+)       # module version
        (?:               # everything else
          \s* (.+)
        )?                #    ... perhaps
        [;] \s*           # semicolon
      $}x
        ? [$1, $2, $3, [ defined($4) ? eval "($4)" : ()] ]
        : die("Line q{$_} doesn't conform to 'use MODULE VERSION [ARGS];'")
    }
    grep { ! m/^#/ }                # not a comment
    grep { m/[A-Z0-9]/i }           # at least one alphanum
    map  { s/(^\s+)|(\s+$)//; $_ }  # trim
    map  { split /(\r?\n|\r)/ }     # split lines
    $spec;

  for my $feature (@features) {
    $c->add($feature->[1], $feature->[2]);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::SyntaxCollector

=head1 DESCRIPTION

This parser is to deal with modules loading by L<Syntax::Collector> module.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
