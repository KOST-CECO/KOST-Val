package Perl::PrereqScanner::NotQuiteLite::Parser::ClassAccessor;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register {{
  use => {
    'Class::Accessor'           => 'parse_class_accessor_args',
    'Class::Accessor::Fast'     => 'parse_class_accessor_args',
    'Class::Accessor::Faster'   => 'parse_class_accessor_args',
    'Class::XSAccessor::Compat' => 'parse_class_accessor_args',
  }
}}

sub parse_class_accessor_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }
  while(my $token = shift @$tokens) {
    if ($token =~ /^(?:antlers|moose-?like)$/i) {
      $c->register_keyword_parser(
        'extends',
        [$class, 'parse_extends_args', $used_module],
      );
      last;
    }
  }
}

sub parse_extends_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard extends, with;

  my $prev;
  for my $token (@$tokens) {
    if (!ref $token) {
      $c->add($token => 0);
      $prev = $token;
      next;
    }
    my $desc = $token->[1] || '';
    if ($desc eq '{}') {
      my @hash_tokens = @{$token->[0] || []};
      for(my $i = 0, my $len = @hash_tokens; $i < $len; $i++) {
        if ($hash_tokens[$i][0] eq '-version' and $i < $len - 2) {
          my $maybe_version_token = $hash_tokens[$i + 2];
          my $maybe_version = $maybe_version_token->[0];
          if (ref $maybe_version) {
            $maybe_version = $maybe_version->[0];
          }
          if ($prev and is_version($maybe_version)) {
            $c->add($prev => $maybe_version);
          }
        }
      }
    }
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::ClassAccessor

=head1 DESCRIPTION

This parser is to deal with modules loaded by C<extends>
from L<Class::Accessor> and its friends.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
