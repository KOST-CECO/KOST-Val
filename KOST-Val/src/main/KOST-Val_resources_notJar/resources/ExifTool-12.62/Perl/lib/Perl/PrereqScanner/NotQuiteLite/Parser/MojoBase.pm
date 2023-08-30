package Perl::PrereqScanner::NotQuiteLite::Parser::MojoBase;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

my @MojoBaseLike = qw/
  Mojo::Base
  Mojo::Weixin::Base Mojo::Webqq::Base 
  Kelp::Base Rethinkdb::Base PMLTQ::Base
/;

sub register {
  my ($class, %args) = @_;
  my %mojo_base_like = map {$_ => 1} (@MojoBaseLike, @{$args{mojo_base_like} || []});
  my %mapping;
  for my $module (keys %mojo_base_like) {
    $mapping{use}{$module} = 'parse_mojo_base_args';
  }
  return \%mapping;
}

sub parse_mojo_base_args {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  my $tokens = convert_string_tokens($raw_tokens);
  if (is_version($tokens->[0])) {
    $c->add($used_module => shift @$tokens);
  }
  my $module = $tokens->[0];
  if (ref $module) {
    $module = $module->[0];
  }
  if (is_module_name($module)) {
    $module =~ s|'|::|g;
    $c->add($module => 0);
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Parser::MojoBase

=head1 DESCRIPTION

This parser is to deal with module inheritance by C<Mojo::Base>.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2017 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
