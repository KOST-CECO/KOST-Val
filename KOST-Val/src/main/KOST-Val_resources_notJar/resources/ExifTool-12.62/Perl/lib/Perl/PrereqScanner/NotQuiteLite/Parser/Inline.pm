package Perl::PrereqScanner::NotQuiteLite::Parser::Inline;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    Inline => 'parse_inline_args',
  },
}}

sub parse_inline_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);

  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }
  if (ref $tokens->[0] and is_module_name($tokens->[0][0])) {
    my $module = (shift @$tokens)->[0];
    if ($module eq 'with') {
      $module = $tokens->[1];
      if (is_module_name($module)) {
        $c->add($module => 0);
      }
    } elsif ($module eq 'Config') {
      # Configuration only
    } else {
      $c->add("Inline::".$module => 0);
    }
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Inline

=head1 DESCRIPTION

This parser is to deal with a module loaded by L<Inline>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
