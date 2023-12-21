package Email::Sender::Transport::Wrapper 2.600;
# ABSTRACT: a mailer to wrap a mailer for mailing mail

use Moo;
with 'Email::Sender::Transport';

use Email::Sender::Util;

#pod =head1 DESCRIPTION
#pod
#pod Email::Sender::Transport::Wrapper wraps a transport, provided as the
#pod C<transport> argument to the constructor.  It is provided as a simple way to
#pod use method modifiers to create wrapping classes.
#pod
#pod =cut

has transport => (
  is   => 'ro',
  does => 'Email::Sender::Transport',
  required => 1,
);

sub send_email {
  my $self = shift;

  $self->transport->send_email(@_);
}

sub is_simple {
  return $_[0]->transport->is_simple;
}

sub allow_partial_success {
  return $_[0]->transport->allow_partial_success;
}

sub BUILDARGS {
  my $self = shift;
  my $href = $self->SUPER::BUILDARGS(@_);

  if (my $class = delete $href->{transport_class}) {
    Carp::confess("given both a transport and transport_class")
      if $href->{transport};

    my %arg;
    for my $key (map {; /^transport_arg_(.+)$/ ? "$1" : () } keys %$href) {
      $arg{$key} = delete $href->{"transport_arg_$key"};
    }

    $href->{transport} = Email::Sender::Util->easy_transport($class, \%arg);
  }

  return $href;
}

no Moo;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Sender::Transport::Wrapper - a mailer to wrap a mailer for mailing mail

=head1 VERSION

version 2.600

=head1 DESCRIPTION

Email::Sender::Transport::Wrapper wraps a transport, provided as the
C<transport> argument to the constructor.  It is provided as a simple way to
use method modifiers to create wrapping classes.

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 AUTHOR

Ricardo Signes <cpan@semiotic.systems>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2022 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
