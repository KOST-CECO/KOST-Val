package Perl::PrereqScanner::NotQuiteLite::Parser::ClassLoad;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

my %known_functions = map {$_ => 1} qw/
  load_class try_load_class load_optional_class
  load_first_existing_class
/;

sub register { return {
  use => {
    'Class::Load' => 'parse_class_load_args',
  },
}}

sub register_fqfn { return {
  map { "Class::Load::".$_ => "parse_".$_."_args" }
  keys %known_functions
}}

sub parse_class_load_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }

  for my $token (@$tokens) {
    next if ref $token;

    if ($known_functions{$token}) {
      $c->register_keyword_parser(
        $token,
        [$class, 'parse_'.$token.'_args', $used_module],
      );
    } elsif ($token eq ':all') {
      for my $func (keys %known_functions) {
        $c->register_keyword_parser(
          $func,
          [$class, 'parse_'.$func.'_args', $used_module],
        );
      }
    }
  }
}

sub parse_load_class_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # function
  my ($module, undef, $options) = @$tokens;
  my $version = 0;
  if (ref $options and $options->[1] eq '{}') {
    my $tokens_in_hashref = convert_string_tokens($options->[0]);
    while(my ($key, undef, $value, undef) = splice @$tokens_in_hashref, 0, 4) {
      if (ref $key and $key->[0] eq '-version' and is_version($value)) {
        $version = $value;
      }
    }
  }
  $c->add_conditional($module => $version);
}

sub parse_try_load_class_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # function
  my ($module, undef, $options) = @$tokens;
  my $version = 0;
  if (ref $options and $options->[1] eq '{}') {
    my $tokens_in_hashref = convert_string_tokens($options->[0]);
    while(my ($key, undef, $value, undef) = splice @$tokens_in_hashref, 0, 4) {
      if (ref $key and $key->[0] eq '-version' and is_version($value)) {
        $version = $value;
      }
    }
  }
  $c->add_suggestion($module => $version);
}

sub parse_load_optional_class_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $class->parse_try_load_class_args($c, $used_module, $raw_tokens);
}

sub parse_load_first_existing_class_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  shift @$tokens; # function
  my ($module, $version);
  for my $token (@$tokens) {
    if (is_module_name($token)) {
      if ($module) {
        $c->add_suggestion($module => $version);
      }
      $module = $token;
      $version = 0;
      next;
    }
    if (ref $token and ($token->[1] || '') eq '{}') {
      my $tokens_in_hashref = convert_string_tokens($token->[0]);
      while(my ($key, undef, $value, undef) = splice @$tokens_in_hashref, 0, 4) {
        if (ref $key and $key->[0] eq '-version' and is_version($value)) {
          $version = $value;
        }
      }
    }
  }
  if ($module) {
    $c->add_suggestion($module => $version);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::ClassLoad

=head1 DESCRIPTION

This parser is to deal with module loading by C<Class::Load>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
