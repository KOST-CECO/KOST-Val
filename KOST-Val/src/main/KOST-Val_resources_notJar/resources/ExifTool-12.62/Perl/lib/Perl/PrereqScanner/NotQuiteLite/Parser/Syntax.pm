package Perl::PrereqScanner::NotQuiteLite::Parser::Syntax;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

my %Unsupported = map {$_ => 1} qw(
);


sub register { return {
  use => {
    syntax => 'parse_syntax_args',
  },
}}

sub parse_syntax_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);

  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }

  return if ref $tokens->[0];

  my $feature_name = $tokens->[0];

  my $name =
    join '::',
    map ucfirst,
    split m{/},
    join '',
    map ucfirst,
    split qr{_}, $feature_name;

  my $feature_module = "Syntax::Feature::$name";
  if (is_module_name($feature_module)) {
    $c->add($feature_module => 0);
  }

  if ($feature_name =~ /^q[sil]$/) {
    $c->register_quotelike_keywords($feature_name, 'q'.$feature_name);
  }

  # Some of the features change syntax too much
  if ($Unsupported{$feature_name}) {
    $c->{aborted} = "syntax '$feature_name'";
    $c->{ended} = 1;
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Syntax

=head1 DESCRIPTION

This parser is to deal with L<syntax> features.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
