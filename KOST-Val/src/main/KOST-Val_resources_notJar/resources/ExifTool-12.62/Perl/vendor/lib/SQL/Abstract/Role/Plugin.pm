package SQL::Abstract::Role::Plugin;

use Moo::Role;

has sqla => (
  is => 'ro', init_arg => undef,
  handles => [ qw(
    expand_expr render_aqt join_query_parts
  ) ],
);

sub cb {
  my ($self, $method, @args) = @_;
  return sub {
    local $self->{sqla} = shift;
    $self->$method(@args, @_)
  };
}

sub register {
  my ($self, @pairs) = @_;
  my $sqla = $self->sqla;
  while (my ($method, $cases) = splice(@pairs, 0, 2)) {
    my @cases = @$cases;
    while (my ($name, $case) = splice(@cases, 0, 2)) {
      $sqla->$method($name, $self->cb($case));
    }
  }
  return $self;
}

sub apply_to {
  my ($self, $sqla) = @_;
  $self = $self->new unless ref($self);
  local $self->{sqla} = $sqla;
  $self->register_extensions($sqla);
}

requires 'register_extensions';

1;

__END__

=head1 NAME

SQL::Abstract::Role::Plugin - helpful methods for plugin authors

=head1 METHODS

=head2 apply_to

Applies the plugin to an L<SQL::Abstract> object.

=head2 register_extensions

Provided by the plugin, registers its extensions to the sqla object.

=head2 cb

Creates a callback to call a method on the plugin.

=head2 register

Calls methods on the sqla object with arguments wrapped as callbacks.

=head2 sqla

Available only during plugin callback executions, contains the currently
active L<SQL::Abstract> object.

=cut
