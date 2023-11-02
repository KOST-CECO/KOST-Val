package Email::Sender::Failure 2.600;
# ABSTRACT: a report of failure from an email sending transport

use Moo;
extends 'Throwable::Error';

use Carp ();
use MooX::Types::MooseLike::Base qw(ArrayRef);

#pod =attr message
#pod
#pod This method returns the failure message, which should describe the failure.
#pod Failures stringify to this message.
#pod
#pod =attr code
#pod
#pod This returns the numeric code of the failure, if any.  This is mostly useful
#pod for network protocol transports like SMTP.  This may be undefined.
#pod
#pod =cut

has code => (
  is => 'ro',
);

#pod =attr recipients
#pod
#pod This returns a list of addresses to which the email could not be sent.
#pod
#pod =cut

has recipients => (
  isa     => ArrayRef,
  default => sub {  []  },
  writer  => '_set_recipients',
  reader  => '__get_recipients',
  is      => 'rw',
  accessor => undef,
);

sub __recipients { @{$_[0]->__get_recipients} }

sub recipients {
  my ($self) = @_;
  return $self->__recipients if wantarray;
  return if ! defined wantarray;

  Carp::carp("recipients in scalar context is deprecated and WILL BE REMOVED");
  return $self->__get_recipients;
}

#pod =method throw
#pod
#pod This method can be used to instantiate and throw an Email::Sender::Failure
#pod object at once.
#pod
#pod   Email::Sender::Failure->throw(\%arg);
#pod
#pod Instead of a hashref of args, you can pass a single string argument which will
#pod be used as the C<message> of the new failure.
#pod
#pod =cut

sub BUILD {
  my ($self) = @_;
  Carp::confess("message must contain non-space characters")
    unless $self->message =~ /\S/;
}

#pod =head1 SEE ALSO
#pod
#pod =over
#pod
#pod =item * L<Email::Sender::Permanent>
#pod
#pod =item * L<Email::Sender::Temporary>
#pod
#pod =item * L<Email::Sender::Multi>
#pod
#pod =back
#pod
#pod =cut

no Moo;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Sender::Failure - a report of failure from an email sending transport

=head1 VERSION

version 2.600

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 ATTRIBUTES

=head2 message

This method returns the failure message, which should describe the failure.
Failures stringify to this message.

=head2 code

This returns the numeric code of the failure, if any.  This is mostly useful
for network protocol transports like SMTP.  This may be undefined.

=head2 recipients

This returns a list of addresses to which the email could not be sent.

=head1 METHODS

=head2 throw

This method can be used to instantiate and throw an Email::Sender::Failure
object at once.

  Email::Sender::Failure->throw(\%arg);

Instead of a hashref of args, you can pass a single string argument which will
be used as the C<message> of the new failure.

=head1 SEE ALSO

=over

=item * L<Email::Sender::Permanent>

=item * L<Email::Sender::Temporary>

=item * L<Email::Sender::Multi>

=back

=head1 AUTHOR

Ricardo Signes <cpan@semiotic.systems>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2022 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
