package Perl::PrereqScanner::NotQuiteLite::Parser::Plack;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'Plack::Builder' => 'parse_plack_builder_args',
  },
}}

sub parse_plack_builder_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  # TODO: support add_middleware(_if) methods?

  $c->register_keyword_parser(
    'enable',
    [$class, 'parse_enable_args', $used_module],
  );
  $c->register_keyword_parser(
    'enable_if',
    [$class, 'parse_enable_if_args', $used_module],
  );
}

sub parse_enable_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard 'enable' itself

  my $module = shift @$tokens or return;
  if ($module =~ s/^\+//) {
    $c->add($module => 0);
  } else {
    $module =~ s/^Plack::Middleware:://;
    $c->add("Plack::Middleware::".$module => 0);
  }
}

sub parse_enable_if_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  while(my $token = shift @$raw_tokens) {
    last if $token->[1] eq 'COMMA' or ref $token->[0];
  }
  shift @$raw_tokens if $raw_tokens->[0][1] eq 'COMMA';

  my $tokens = convert_string_tokens($raw_tokens);

  my $module = shift @$tokens or return;
  if ($module =~ s/^\+//) {
    $c->add($module => 0);
  } else {
    $module =~ s/^Plack::Middleware:://;
    $c->add("Plack::Middleware::".$module => 0);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::Plack

=head1 DESCRIPTION

This parser is to deal with Plack middlewares loaded by L<Plack::Builder>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
