package Perl::PrereqScanner::NotQuiteLite::Parser::Unless;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    unless => 'parse_unless_args',
  },
}}

sub parse_unless_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  while(my $token = shift @$raw_tokens) {
    last if $token->[1] eq 'COMMA';
  }

  my $tokens = convert_string_tokens($raw_tokens);
  my $module = shift @$tokens;
  if (ref $module and ($module->[1] eq 'WORD' or $module->[1] eq 'KEYWORD')) {
    $module = $module->[0];
  }
  if (is_module_name($module)) {
    if (is_version($tokens->[0])) {
      my $version = shift @$tokens;
      $c->add_recommendation($module => $version);
    } else {
      $c->add_recommendation($module => 0);
    }
  } else {
    push @{$c->{errors}}, "use unless module not found";
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Unless

=head1 DESCRIPTION

This parser is to deal with conditional loading by C<unless> module.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
