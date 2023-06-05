package Perl::PrereqScanner::NotQuiteLite::Parser::PackageVariant;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register {{
  use => {
    'Package::Variant' => 'parse_package_variant_args',
  },
}}

sub parse_package_variant_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);

  while(my $token = shift @$tokens) {
    if (ref $token and $token->[0] eq 'importing') {
      shift @$tokens if @$tokens && $tokens->[0][1] eq 'COMMA';
      my $next_token = shift @$tokens or last;
      if (!ref $next_token) {
        my $module = $next_token;
        if (is_module_name($module)) {
          $c->add($module);
          if ($c->has_callback_for(use => $module)) {
            $c->run_callback_for('use', $module, [["use", "KEYWORD"], [$module, "WORD"], [";", ";"]]);
          }
        }
      }
      elsif ($next_token->[1] eq '[]') {
        my $modules = convert_string_token_list($next_token->[0]);
        while(my $module = shift @$modules) {
          next unless is_module_name($module);
          $c->add($module);
          if ($c->has_callback_for(use => $module)) {
            $c->run_callback_for('use', $module, [["use", "KEYWORD"], [$module, "WORD"], [";", ";"]]);
          }
        }
      } elsif ($next_token->[1] eq '{}') {
        my $hash_tokens = convert_string_token_list($next_token->[0]);
        while(my $module = shift @$hash_tokens) {
          my $arg = shift @$hash_tokens;
          my @args = $arg->[1] eq '[]' ? @{$arg->[0]} : $arg;
          $c->add($module);
          if ($c->has_callback_for(use => $module)) {
            $c->run_callback_for('use', $module, [["use", "KEYWORD"], [$module, "WORD"], @args, [";", ";"]]);
          }
        }
      }
    }
    elsif (ref $token && !ref $token->[0] && $token->[1] eq 'WORD') {
      shift @$tokens if @$tokens && $tokens->[0][1] eq 'COMMA';
      shift @$tokens if @$tokens;
    }
    shift @$tokens if @$tokens && ref $tokens->[0] && $tokens->[0][1] eq 'COMMA';
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::PackageVariant

=head1 DESCRIPTION

This parser is to deal with modules loaded by L<Package::Variant>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2018 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
