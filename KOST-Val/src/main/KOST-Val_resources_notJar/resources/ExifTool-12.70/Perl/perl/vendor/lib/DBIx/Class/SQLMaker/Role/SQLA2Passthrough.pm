package DBIx::Class::SQLMaker::Role::SQLA2Passthrough;

use strict;
use warnings;
use Exporter 'import';

our @EXPORT = qw(on);

sub on (&) {
  my ($on) = @_;
  sub {
    my ($args) = @_;
    $args->{self_resultsource}
         ->schema->storage->sql_maker
         ->expand_join_condition(
             $on->($args),
             $args
           );
  }
}

use Role::Tiny;

around select => sub {
  my ($orig, $self, $table, $fields, $where, $rs_attrs, $limit, $offset) = @_;

  $fields = \[ $self->render_expr({ -list => [
    grep defined,
    map +(ref($_) eq 'HASH'
          ? do {
              my %f = %$_;
              my $as = delete $f{-as};
              my ($f, $rhs) = %f;
              my $func = +{ ($f =~ /^-/ ? $f : "-${f}") => $rhs };
              ($as
                ? +{ -op => [ 'as', $func, { -ident => [ $as ] } ] }
                : $func)
            }
          : $_), ref($fields) eq 'ARRAY' ? @$fields : $fields
  ] }, -ident) ];

  if (my $gb = $rs_attrs->{group_by}) {
    $rs_attrs = {
      %$rs_attrs,
      group_by => \[ $self->render_expr({ -list => $gb }, -ident) ]
    };
  }
  $self->$orig($table, $fields, $where, $rs_attrs, $limit, $offset);
};

sub expand_join_condition {
  my ($self, $cond, $args) = @_;
  my ($type, %known) = do {
    if (my $obj = $args->{self_result_object}) {
      (self => $obj->get_columns)
    } elsif (my $val = $args->{foreign_values}) {
      (foreign => %$val)
    } else {
      ('')
    }
  };
  my $maybe = $type ? 1 : 0;
  my $outside;
  my $wrap = sub {
    my ($orig) = @_;
    $outside = $orig;
    sub {
      my $res = $orig->(@_);
      my ($name, $col) = @{$res->{-ident}};
      if ($name eq 'self' or $name eq 'foreign') {
        if ($type eq $name) {
          $maybe = 0 unless exists $known{$col};
        }
        return { -ident => [ $args->{"${name}_alias"}, $col ] };
      }
      return $res;
    };
  };
  my $sqla = $self->clone->wrap_op_expander(ident => $wrap);
  my $aqt = $sqla->expand_expr($cond, -ident);
  return $aqt unless $maybe;
  my $inner_wrap = sub {
    my $res = $outside->(@_);
    my ($name, $col) = @{$res->{-ident}};
    if ($name eq 'self' or $name eq 'foreign') {
      if ($type eq $name) {
        return { -bind => [ $args->{"${name}_alias"}.'.'.$col, $known{$col} ] };
      }
      return { -ident => [ $args->{"${name}_alias"}, $col ] };
    }
    return $res;
  };
  $sqla->op_expander(ident => $inner_wrap);
  my $inner_aqt = $self->_collapsify($sqla->expand_expr($cond, -ident));
  return ($aqt, $inner_aqt);
}

sub _collapsify {
  my ($self, $aqt) = @_;
  return $aqt unless my @opargs = @{$aqt->{-op}};
  my ($logop, @args) = @opargs;
  return $aqt unless $logop eq 'and';
  my %collapsed = map {
    my $q = $_;
    return $aqt unless my @opargs = @{$q->{-op}};
    my ($op, $lhs, @rest) = @opargs;
    return $aqt unless my @ident = @{$lhs->{-ident}};
    (join('.', @ident), { $op => \@rest });
  } @args;
  return \%collapsed;
}

1;

__END__

=head1 NAME

DBIx::Class::SQLMaker::Role::SQLA2Passthrough - A test of future possibilities

=head1 SYNOPSIS

=over 4

=item * select and group_by options are processed using the richer SQLA2 code

=item * expand_join_condition is provided to more easily express rich joins

=back

See C<examples/sqla2passthrough.pl> for a small amount of running code.

=head1 SETUP

  (on_connect_call => sub {
     my ($storage) = @_;
     $storage->sql_maker
             ->with::roles('DBIx::Class::SQLMaker::Role::SQLA2Passthrough');
  })

=head2 expand_join_condition

  __PACKAGE__->has_many(minions => 'Blah::Person' => sub {
    my ($args) = @_;
    $args->{self_resultsource}
         ->schema->storage->sql_maker
         ->expand_join_condition(
             $args
           );
  });

=head2 on

  __PACKAGE__->has_many(minions => 'Blah::Person' => on {
    { 'self.group_id' => 'foreign.group_id',
      'self.rank' => { '>', 'foreign.rank' } }
  });

Or with ParameterizedJoinHack,

  __PACKAGE__->parameterized_has_many(
      priority_tasks => 'MySchema::Result::Task',
      [['min_priority'] => sub {
          my $args = shift;
          return +{
              "$args->{foreign_alias}.owner_id" => {
                  -ident => "$args->{self_alias}.id",
              },
              "$args->{foreign_alias}.priority" => {
                  '>=' => $_{min_priority},
              },
          };
      }],
  );

becomes

  __PACKAGE__->parameterized_has_many(
      priority_tasks => 'MySchema::Result::Task',
      [['min_priority'] => on {
        { 'foreign.owner_id' => 'self.id',
          'foreign.priority' => { '>=', { -value => $_{min_priority} } } }
      }]
  );

Note that foreign/self can appear in such a condition on either side, BUT
if you want L<DBIx::Class> to be able to use a join-less version you must
ensure that the LHS is all foreign columns, i.e.

  on {
    +{
      'foreign.x' => 'self.x',
      'self.y' => { -between => [ 'foreign.y1', 'foreign.y2' ] }
    }
  }

is completely valid but DBIC will insist on doing a JOIN even if you
have a fully populated row object to call C<search_related> on - to avoid
the spurious JOIN, you must specify it with explicit LHS foreign cols as:

  on {
    +{
      'foreign.x' => 'self.x',
      'foreign.y1' => { '<=', 'self.y' },
      'foreign.y2' => { '>=', 'self.y' },
    }
  }

=cut
