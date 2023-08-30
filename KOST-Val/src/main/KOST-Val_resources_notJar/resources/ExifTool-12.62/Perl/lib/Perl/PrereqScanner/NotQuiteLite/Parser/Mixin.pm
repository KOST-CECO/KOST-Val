package Perl::PrereqScanner::NotQuiteLite::Parser::Mixin;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    mixin => 'parse_mixin_args',
  },
}}

sub parse_mixin_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }
  $c->add($_ => 0) for grep {!ref $_} @$tokens;
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Mixin

=head1 DESCRIPTION

This parser is to deal with module loading by C<mixin> module.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
