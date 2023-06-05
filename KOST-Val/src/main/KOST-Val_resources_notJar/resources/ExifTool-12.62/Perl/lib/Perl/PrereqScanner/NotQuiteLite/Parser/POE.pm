package Perl::PrereqScanner::NotQuiteLite::Parser::POE;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    POE => 'parse_poe_args',
  },
}}

sub parse_poe_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;
  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }
  $c->add($_ eq "POE" ? $_ : "POE::".$_ => 0) for grep {!ref $_} @$tokens;
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::POE

=head1 DESCRIPTION

This parser is to deal with modules loaded by L<POE>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
