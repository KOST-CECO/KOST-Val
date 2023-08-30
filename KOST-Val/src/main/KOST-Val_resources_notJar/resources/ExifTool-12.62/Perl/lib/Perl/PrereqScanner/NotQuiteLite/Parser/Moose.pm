package Perl::PrereqScanner::NotQuiteLite::Parser::Moose;

use strict;
use warnings;
use Perl::PrereqScanner::NotQuiteLite::Util;

# There are so many Moose-like variants

# Like Moose; modules that are not listed here but have Moose
# in their name are implicitly treated like these as well
my @ExportsExtendsAndWith = qw/
  Moose Moo Mouse MooX Moo::Lax Moos
  MooseX::App MooseX::Singleton MooseX::SingletonMethod

  HTML::FormHandler::Moose Test::Class::Moose

  App::GHPT::Wrapper::OurMoose App::wmiirc::Plugin Ark
  Bot::Backbone::Service Bubblegum::Class CatalystX::Declare
  Cogwheel CPAN::Testers::Backend::Base Dancer2::Plugin
  Data::Object::Class DBICx::Modeler::Model Digital::Driver
  Elastic::Doc Fey::ORM::Table Form::Factory::Processor
  Jedi::App Momo Moonshine::Magic Moxie Nile::Base
  Parse::FixedRecord Pcore Reaction::Class Reaction::UI::WidgetClass
  Squirrel Statocles::Base TAEB::OO Test::Able Test::Roo
  Web::Simple XML::Rabbit
/;

# Like Moose::Role; modules that are not listed here but have Role
# in their name are implicitly treated like these as well

my @ExportsWith = qw/
  Moose::Role Moo::Role Mouse::Role
  MooseX::Role::Parameterized
  Mason::PluginRole Mojo::RoleTiny MooX::Cmd
  Role::Basic Role::Tiny Role::Tiny::With Reflex::Role
  Template::Caribou Test::Routine App::SimulateReads::Base
/;

# Like Mo
my @ExportsExtends = qw/
  Mo
  Lingy::Base OptArgs2::Mo Parse::SAMGov::Mo Pegex::Base
  Sub::Mage TestML::Base Type::Utils VSO
/;

sub register {
  my ($class, %args) = @_;

  # Make sure everything is unique
  my %exports_extends_and_with = map {$_ => 1} (@ExportsExtendsAndWith, @{$args{exports_extends_and_with} || []});
  my %exports_extends = map {$_ => 1} (@ExportsExtends, @{$args{exports_extends} || []});
  my %exports_with = map {$_ => 1} (@ExportsWith, @{$args{exports_with} || []});

  for my $module (keys %exports_with) {
    if (exists $exports_extends_and_with{$module}) {
      delete $exports_with{$module};
      next;
    }
    if (exists $exports_extends{$module}) {
      $exports_extends_and_with{$module} = 1;
      delete $exports_with{$module};
      next;
    }
  }
  for my $module (keys %exports_extends) {
    if (exists $exports_extends_and_with{$module}) {
      delete $exports_extends{$module};
      next;
    }
  }

  my %mapping;
  for my $module (keys %exports_with) {
    $mapping{use}{$module} = 'register_with';
    $mapping{no}{$module}  = 'remove_with';
  }
  for my $module (keys %exports_extends) {
    $mapping{use}{$module} = 'register_extends';
    $mapping{no}{$module}  = 'remove_extends';
  }
  for my $module (keys %exports_extends_and_with) {
    $mapping{use}{$module} = 'register_extends_and_with';
    $mapping{no}{$module}  = 'remove_extends_and_with';
  }

  return \%mapping;
}

sub register_extends_and_with {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->register_keyword_parser(
    'extends',
    [$class, 'parse_extends_args', $used_module],
  );
  $c->register_keyword_parser(
    'with',
    [$class, 'parse_with_args', $used_module],
  );
}

sub register_with {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->register_keyword_parser(
    'with',
    [$class, 'parse_with_args', $used_module],
  );
}

sub register_extends {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->register_keyword_parser(
    'extends',
    [$class, 'parse_extends_args', $used_module],
  );
}

sub remove_extends_and_with {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->remove_keyword('extends');
  $c->remove_keyword('with');
}

sub remove_with {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->remove_keyword('with');
}

sub remove_extends {
  my ($class, $c, $used_module, $raw_tokens) = @_;

  $c->remove_keyword('extends');
}

sub parse_extends_args { shift->_parse_loader_args(@_) }
sub parse_with_args { shift->_parse_loader_args(@_) }

sub _parse_loader_args {
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

Perl::PrereqScanner::NotQuiteLite::Parser::Moose

=head1 DESCRIPTION

This parser is to deal with modules loaded by C<extends> and/or
C<with> from L<Moose> and its friends.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
