package Perl::PrereqScanner::NotQuiteLite::Parser::ClassAutouse;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'Class::Autouse' => 'parse_class_autouse_args',
  },
}}

sub parse_class_autouse_args {
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
    if (is_module_name($module)) {
      $c->add_recommendation($module => 0);
    }
  }

  $c->register_method_parser(
    'autouse',
    [$class, 'parse_autouse_method_args', $used_module],
  );
}

sub parse_autouse_method_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;
  my $tokens = convert_string_tokens($raw_tokens);

  # Check class
  my ($klass, $arrow, $method, @args) = @$tokens;
  return unless $klass and ref $klass and $klass->[0] eq $used_module;
  return unless $method and ref $method and $method->[0] eq 'autouse';
  for my $arg (@args) {
    next if ref $arg;
    $c->add_recommendation($arg => 0);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::ClassAutouse

=head1 DESCRIPTION

This parser is to deal with modules loaded dynamically by
C<Class::Autouse>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
