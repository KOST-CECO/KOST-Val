package SQL::Abstract::Plugin::BangOverrides;

use Moo;

with 'SQL::Abstract::Role::Plugin';

sub register_extensions {
  my ($self, $sqla) = @_;
  foreach my $stmt ($sqla->statement_list) {
    $sqla->wrap_expander($stmt => sub {
      my ($orig) = @_;
      sub {
        my ($self, $name, $args) = @_;
        my %args = (
          %$args,
          (ref($args->{order_by}) eq 'HASH'
            ? %{$args->{order_by}}
            : ())
        );
        my %overrides;
        foreach my $clause (map /^!(.*)$/, keys %args) {
          my $override = delete $args{"!${clause}"};
          $overrides{$clause} = (
            ref($override) eq 'CODE'
              ? $self->$override($args{$clause})
              : $override
          );
        }
        $self->$orig($name, { %$args, %overrides });
      }
    });
  }
}

1;

__END__

=head1 NAME

SQL::Abstract::Plugin::BangOverrides

=head2 SYNOPSIS

  $sqla->plugin('+BangOverrides');
  ...
  profit();

=head1 METHODS

=head2 register_extensions

Wraps all currently existing clause based statements such that when a clause
of '!name' is encountered, if its value is a coderef, it's called with the
original value of the 'name' clause and expected to return a replacement, and
if not, it's simply used as a direct replacement.

This allows for passing data through existing systems that attempt to have
their own handling for thing but whose capabilities are now superceded by
L<SQL::Abstract>, and is primarily useful to provide access to experimental
feature bundles such as L<SQL::Abstract::Plugin::ExtraClauses>.

As an example of such a thing, given an appropriate DBIC setup
(see C<examples/bangdbic.pl>):

  $s->storage->sqlmaker->plugin('+ExtraClauses')->plugin('+BangOverrides');

  my $rs2 = $s->resultset('Foo')->search({
    -op => [ '=', { -ident => 'outer.y' }, { -ident => 'me.x' } ]
  });
  # (SELECT me.x, me.y, me.z FROM foo me WHERE ( outer.y = me.x ))

  my $rs3 = $rs2->search({}, {
    '!from' => sub { my ($sqla, $from) = @_;
      my $base = $sqla->expand_expr({ -old_from => $from });
      return [ $base, -join => [ 'wub', on => [ 'me.z' => 'wub.z' ] ] ];
    }
  });
  # (SELECT me.x, me.y, me.z FROM foo me JOIN wub ON me.z = wub.z WHERE ( outer.y = me.x ))

  my $rs4 = $rs3->search({}, {
    '!with' => [ [ qw(wub x y z) ], $s->resultset('Bar')->as_query ],
  });
  # (WITH wub(x, y, z) AS (SELECT me.a, me.b, me.c FROM bar me) SELECT me.x, me.y, me.z FROM foo me JOIN wub ON me.z = wub.z WHERE ( outer.y = me.x ))

  my $rs5 = $rs->search({}, { select => [ { -coalesce => [ { -ident => 'x' }, { -value => 7 } ] } ] });
  # (SELECT -COALESCE( -IDENT( x ), -VALUE( 7 ) ) FROM foo me WHERE ( z = ? ))

  my $rs6 = $rs->search({}, { '!select' => [ { -coalesce => [ { -ident => 'x' }, { -value => 7 } ] } ] });
  # (SELECT COALESCE(x, ?) FROM foo me WHERE ( z = ? ))

=cut
