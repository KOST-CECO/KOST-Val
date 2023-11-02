package Email::Sender 2.600;
# ABSTRACT: a library for sending email

use Moo::Role;
requires 'send';

#pod =head1 SYNOPSIS
#pod
#pod   my $message = Email::MIME->create( ... );
#pod   # produce an Email::Abstract compatible message object,
#pod   # e.g. produced by Email::Simple, Email::MIME, Email::Stuff
#pod
#pod   use Email::Sender::Simple qw(sendmail);
#pod   use Email::Sender::Transport::SMTP qw();
#pod   use Try::Tiny;
#pod
#pod   try {
#pod     sendmail(
#pod       $message,
#pod       {
#pod         from => $SMTP_ENVELOPE_FROM_ADDRESS,
#pod         transport => Email::Sender::Transport::SMTP->new({
#pod             host => $SMTP_HOSTNAME,
#pod             port => $SMTP_PORT,
#pod         })
#pod       }
#pod     );
#pod   } catch {
#pod       warn "sending failed: $_";
#pod   };
#pod
#pod =head1 OVERVIEW
#pod
#pod Email::Sender replaces the old and sometimes problematic Email::Send library,
#pod which did a decent job at handling very simple email sending tasks, but was not
#pod suitable for serious use, for a variety of reasons.
#pod
#pod Most users will be able to use L<Email::Sender::Simple> to send mail.  Users
#pod with more specific needs should look at the available Email::Sender::Transport
#pod classes.
#pod
#pod Documentation may be found in L<Email::Sender::Manual>, and new users should
#pod start with L<Email::Sender::Manual::QuickStart>.
#pod
#pod =head1 IMPLEMENTING
#pod
#pod Email::Sender itself is a Moo role.  Any class that implements Email::Sender
#pod is required to provide a method called C<send>.  This method should accept any
#pod input that can be understood by L<Email::Abstract>, followed by a hashref
#pod containing C<to> and C<from> arguments to be used as the envelope.  The method
#pod should return an L<Email::Sender::Success> object on success or throw an
#pod L<Email::Sender::Failure> on failure.
#pod
#pod =cut

no Moo::Role;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Sender - a library for sending email

=head1 VERSION

version 2.600

=head1 SYNOPSIS

  my $message = Email::MIME->create( ... );
  # produce an Email::Abstract compatible message object,
  # e.g. produced by Email::Simple, Email::MIME, Email::Stuff

  use Email::Sender::Simple qw(sendmail);
  use Email::Sender::Transport::SMTP qw();
  use Try::Tiny;

  try {
    sendmail(
      $message,
      {
        from => $SMTP_ENVELOPE_FROM_ADDRESS,
        transport => Email::Sender::Transport::SMTP->new({
            host => $SMTP_HOSTNAME,
            port => $SMTP_PORT,
        })
      }
    );
  } catch {
      warn "sending failed: $_";
  };

=head1 OVERVIEW

Email::Sender replaces the old and sometimes problematic Email::Send library,
which did a decent job at handling very simple email sending tasks, but was not
suitable for serious use, for a variety of reasons.

Most users will be able to use L<Email::Sender::Simple> to send mail.  Users
with more specific needs should look at the available Email::Sender::Transport
classes.

Documentation may be found in L<Email::Sender::Manual>, and new users should
start with L<Email::Sender::Manual::QuickStart>.

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 IMPLEMENTING

Email::Sender itself is a Moo role.  Any class that implements Email::Sender
is required to provide a method called C<send>.  This method should accept any
input that can be understood by L<Email::Abstract>, followed by a hashref
containing C<to> and C<from> arguments to be used as the envelope.  The method
should return an L<Email::Sender::Success> object on success or throw an
L<Email::Sender::Failure> on failure.

=head1 AUTHOR

Ricardo Signes <cpan@semiotic.systems>

=head1 CONTRIBUTORS

=for stopwords Alex Efros Aristotle Pagaltzis Bernhard Graf Christian Walde David Golden Steinbrunner Hans Dieter Pearcey HIROSE Masaaki James E Keenan Justin Hunter Karen Etheridge Kenichi Ishigaki kga Kris Matthews Marc Bradshaw Ricardo Signes Stefan Hornburg (Racke) William Blunn

=over 4

=item *

Alex Efros <powerman@powerman.name>

=item *

Aristotle Pagaltzis <pagaltzis@gmx.de>

=item *

Bernhard Graf <augensalat@gmail.com>

=item *

Christian Walde <walde.christian@googlemail.com>

=item *

David Golden <dagolden@cpan.org>

=item *

David Steinbrunner <dsteinbrunner@pobox.com>

=item *

Hans Dieter Pearcey <hdp@cpan.org>

=item *

HIROSE Masaaki <hirose31@gmail.com>

=item *

James E Keenan <jkeenan@cpan.org>

=item *

Justin Hunter <justin.d.hunter@gmail.com>

=item *

Karen Etheridge <ether@cpan.org>

=item *

Kenichi Ishigaki <ishigaki@cpan.org>

=item *

kga <watrty@gmail.com>

=item *

Kris Matthews <kris@tigerlms.com>

=item *

Marc Bradshaw <marc@marcbradshaw.net>

=item *

Ricardo Signes <rjbs@semiotic.systems>

=item *

Stefan Hornburg (Racke) <racke@linuxia.de>

=item *

William Blunn <zgpmax@cpan.org>

=back

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2022 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
