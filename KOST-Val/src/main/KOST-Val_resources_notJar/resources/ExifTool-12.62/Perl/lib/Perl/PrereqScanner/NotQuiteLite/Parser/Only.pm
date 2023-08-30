package Perl::PrereqScanner::NotQuiteLite::Parser::Only;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    only => 'parse_only_args',
  },
}}

sub parse_only_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);

  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }
  while(my $token = shift @$tokens) {
    my $module = $token;
    if (ref $module) {
      $module = $module->[0];
    }
    next unless is_module_name($module);
    my $version = shift @$tokens;
    $version = shift @$tokens if ref $version;
    if (is_version($version)) {
      $c->add($module => $version);
    } else {
      $c->add($module => 0); # Can't determine a version
    }
    last;
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Only

=head1 DESCRIPTION

This parser is to deal with a module loaded by L<only>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
