package Perl::PrereqScanner::NotQuiteLite::Parser::MooseXDeclare;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

sub register { return {
  use => {
    'MooseX::Declare' => 'parse_moosex_declare_args',
  },
}}

sub parse_moosex_declare_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->register_sub_parser(
    'class',
    [$class, 'parse_class_args', $used_module],
  );
  $c->register_sub_parser(
    'role',
    [$class, 'parse_role_args', $used_module],
  );

  $c->register_keyword_parser(
    'extends',
    [$class, 'parse_extends_args', $used_module],
  );
  $c->register_keyword_parser(
    'with',
    [$class, 'parse_with_args', $used_module],
  );

  $c->register_keyword_parser(
    'namespace',
    [$class, 'parse_namespace_args', $used_module],
  );

  $c->register_sub_keywords(qw/
    class method role
    before after around override augment
  /);

  $c->prototype_re(qr{\G(\((?:[^\\\(\)]*(?:\\.[^\\\(\)]*)*)\))});
}

sub parse_class_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $namespace = $c->stash->{moosex_declare}{namespace} || '';

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard class

  my $class_name = (shift @$tokens || [])->[0] or return;
  if ($class_name eq '::') {
    my $name = (shift @$tokens || [])->[0];
    $class_name = $namespace . '::' . $name;
  }

  my $prev = '';
  while(my $token = shift @$tokens) {
    if ($token->[0] eq 'extends' or $token->[0] eq 'with') {
      while(1) {
        my $name = (shift @$tokens || [])->[0];
        if ($name eq '::') {
          $name = $namespace . '::' . (shift @$tokens || [])->[0];
        }
        $c->add($name => 0) if is_module_name($name);
        last if !@$tokens;
        my $next_token = $tokens->[0];
        last if $next_token->[0] ne ',';
        shift @$tokens;
      }
    }
  }
}

sub parse_role_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $namespace = $c->stash->{moosex_declare}{namespace} || '';

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard role

  my $class_name = (shift @$tokens)->[0];
  if ($class_name eq '::') {
    my $name = (shift @$tokens)->[0];
    $class_name = $namespace . '::' . $name;
  }

  my $prev = '';
  while(my $token = shift @$tokens) {
    if ($token->[0] eq 'with') {
      while(1) {
        my $name = (shift @$tokens)->[0];
        if ($name eq '::') {
          $name = $namespace . '::' . (shift @$tokens)->[0];
        }
        $c->add($name => 0) if is_module_name($name);
        last if !@$tokens;
        my $next_token = $tokens->[0];
        last if $next_token->[0] ne ',';
        shift @$tokens;
      }
    }
  }
}

sub parse_namespace_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard namespace

  my $first_token = (shift @$tokens)->[0];
  if (is_module_name($first_token)) {
    $c->stash->{moosex_declare}{namespace} = $first_token;
  }
}

sub parse_extends_args { shift->_parse_loader_args(@_) }
sub parse_with_args { shift->_parse_loader_args(@_) }

sub _parse_loader_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $namespace = $c->stash->{moosex_declare}{namespace} || '';

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # discard extends, with;

  my $prev;
  while(my $token = shift @$tokens) {
    if (!ref $token) {
      if ($token =~ /^::/) {
        $token = $namespace . $token;
      }
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

Perl::PrereqScanner::NotQuiteLite::Parser::MooseXDeclare

=head1 DESCRIPTION

This parser is to deal with modules loaded by C<extends> and/or
C<with> from L<MooseX::Declare>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
