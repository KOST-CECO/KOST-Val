package Email::Sender::Transport::Print 2.600;
# ABSTRACT: print email to a filehandle (like stdout)

use Moo;
with 'Email::Sender::Transport';

#pod =head1 DESCRIPTION
#pod
#pod When this transport is handed mail, it prints it to a filehandle.  By default,
#pod it will print to STDOUT, but it can be given any L<IO::Handle> object to print
#pod to as its C<fh> attribute.
#pod
#pod =cut

use IO::Handle;
use MooX::Types::MooseLike::Base qw(InstanceOf);

has 'fh' => (
  is       => 'ro',
  isa      => InstanceOf['IO::Handle'],
  required => 1,
  default  => sub { IO::Handle->new_from_fd(fileno(STDOUT), 'w') },
);

sub send_email {
  my ($self, $email, $env) = @_;

  my $fh = $self->fh;

  $fh->printf("ENVELOPE TO  : %s\n", join(q{, }, @{ $env->{to} }) || '-');
  $fh->printf("ENVELOPE FROM: %s\n", defined $env->{from} ? $env->{from} : '-');
  $fh->print(q{-} x 10 . " begin message\n");

  $fh->print( $email->as_string );

  $fh->print(q{-} x 10 . " end message\n");

  return $self->success;
}

no Moo;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Sender::Transport::Print - print email to a filehandle (like stdout)

=head1 VERSION

version 2.600

=head1 DESCRIPTION

When this transport is handed mail, it prints it to a filehandle.  By default,
it will print to STDOUT, but it can be given any L<IO::Handle> object to print
to as its C<fh> attribute.

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
