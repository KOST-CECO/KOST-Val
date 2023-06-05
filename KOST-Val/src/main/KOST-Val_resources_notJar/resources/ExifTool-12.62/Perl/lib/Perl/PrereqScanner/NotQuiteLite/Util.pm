package Perl::PrereqScanner::NotQuiteLite::Util;

use strict;
use warnings;
use Exporter 5.57 qw/import/;

our %FLAGS; BEGIN {
  my $i = 0;
  %FLAGS = map {$_ => 1 << $i++} qw/
  F_KEEP_TOKENS
  F_EVAL
  F_STRING_EVAL
  F_EXPECTS_BRACKET
  F_CONDITIONAL
  F_SIDEFF
  F_SCOPE_END
  F_STATEMENT_END
  F_EXPR_END
  F_EXPR
  /;
}

use constant \%FLAGS;
use constant {
  MASK_KEEP_TOKENS => ~(F_KEEP_TOKENS),
  MASK_EXPR_END => ~(F_EXPR_END|F_EXPR),
  MASK_STATEMENT_END => ~(F_KEEP_TOKENS|F_STATEMENT_END|F_EXPR|F_EXPR_END|F_SIDEFF),
  MASK_EVAL => ~(F_EVAL),
  MASK_SIDEFF => ~(F_SIDEFF),
  F_RESCAN => (F_KEEP_TOKENS|F_EVAL|F_STRING_EVAL|F_CONDITIONAL),
};

our @EXPORT = ((keys %FLAGS), qw/
  is_module_name
  is_version
  convert_string_tokens
  convert_string_token_list
  MASK_KEEP_TOKENS
  MASK_EXPR_END
  MASK_STATEMENT_END
  MASK_EVAL
  MASK_SIDEFF
  F_RESCAN
/);

sub is_module_name {
  my $name = shift or return;
  return 1 if $name =~ /^[A-Za-z_][A-Za-z0-9_]*(?:(?:::|')[A-Za-z0-9_]+)*$/;
  return;
}

sub is_version {
  my $version = shift;
  return unless defined $version;
  return 1 if $version =~ /\A
    (
      [0-9]+(?:\.[0-9]+)?
      |
      v[0-9]+(?:\.[0-9]+)*
      |
      [0-9]+(?:\.[0-9]+){2,}
    ) (?:_[0-9]+)?
  \z/x;
  return;
}

sub convert_string_tokens {
  my $org_tokens = shift;
  my @tokens;
  my @copied_tokens = @$org_tokens;
  my $prev = '';
  while(my $copied_token = shift @copied_tokens) {
    my ($token, $desc) = @$copied_token;
    if ($desc and $desc eq '()' and $prev ne 'WORD') {
      unshift @copied_tokens, @$token;
      next;
    }

    if (!$desc) {
      push @tokens, $copied_token;
    } elsif ($desc eq 'VERSION_STRING' or $desc eq 'NUMBER') {
      push @tokens, $token;
    } elsif ($desc eq 'STRING') {
      push @tokens, $token->[0];
    } elsif ($desc eq 'QUOTED_WORD_LIST') {
      push @tokens, grep {defined $_ and $_ ne ''} split /\s/, $token->[0];
    } else {
      push @tokens, $copied_token;
    }
    $prev = $desc;
  }
  \@tokens;
}

sub convert_string_token_list {
  my $org_tokens = shift;
  my @list;
  my @tokens;
  my @copied_tokens = @$org_tokens;
  my $prev = '';
  while(my $copied_token = shift @copied_tokens) {
    my ($token, $desc) = @$copied_token;
    if ($desc and $desc eq '()' and $prev ne 'WORD') {
      unshift @copied_tokens, @$token;
      next;
    }

    if (!$desc) {
      push @tokens, $copied_token;
    } elsif ($desc eq 'VERSION_STRING' or $desc eq 'NUMBER') {
      push @tokens, $token;
    } elsif ($desc eq 'STRING') {
      push @tokens, $token->[0];
    } elsif ($desc eq 'QUOTED_WORD_LIST') {
      push @list, grep {defined $_ and $_ ne ''} split /\s/, $token->[0];
    } elsif ($token eq ',' or $token eq '=>') {
      push @list, @tokens == 1 ? $tokens[0] : \@tokens;
      @tokens = ();
      $prev = '';
    } elsif ($desc eq ';') {
      last;
    } else {
      push @tokens, $copied_token;
    }
    $prev = $desc;
  }
  if (@tokens) {
    push @list, @tokens == 1 ? $tokens[0] : \@tokens;
  }

  \@list;
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Util

=head1 DESCRIPTION

This provides a few utility functions for internal use.

=head1 FUNCTIONS

=head2 is_module_name

takes a string and returns true if it looks like a module.

=head2 is_version

takes a string and returns true if it looks like a version.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
