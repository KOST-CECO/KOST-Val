use v5.12.0;
use warnings;
package Email::Stuffer 0.020;
# ABSTRACT: A more casual approach to creating and sending Email:: emails

use Scalar::Util qw(blessed);

#pod =head1 SYNOPSIS
#pod
#pod   # Prepare the message
#pod   my $body = <<'AMBUSH_READY';
#pod   Dear Santa
#pod
#pod   I have killed Bun Bun.
#pod
#pod   Yes, I know what you are thinking... but it was actually a total accident.
#pod
#pod   I was in a crowded line at a BayWatch signing, and I tripped, and stood on
#pod   his head.
#pod
#pod   I know. Oops! :/
#pod
#pod   So anyways, I am willing to sell you the body for $1 million dollars.
#pod
#pod   Be near the pinhole to the Dimension of Pain at midnight.
#pod
#pod   Alias
#pod
#pod   AMBUSH_READY
#pod
#pod   # Create and send the email in one shot
#pod   Email::Stuffer->from     ('cpan@ali.as'             )
#pod                 ->to       ('santa@northpole.org'     )
#pod                 ->bcc      ('bunbun@sluggy.com'       )
#pod                 ->text_body($body                     )
#pod                 ->attach_file('dead_bunbun_faked.gif' )
#pod                 ->send;
#pod
#pod =head1 DESCRIPTION
#pod
#pod B<The basics should all work, but this module is still subject to
#pod name and/or API changes>
#pod
#pod Email::Stuffer, as its name suggests, is a fairly casual module used
#pod to stuff things into an email and send them. It is a high-level module
#pod designed for ease of use when doing a very specific common task, but
#pod implemented on top of the light and tolerable Email:: modules.
#pod
#pod Email::Stuffer is typically used to build emails and send them in a single
#pod statement, as seen in the synopsis. And it is certain only for use when
#pod creating and sending emails. As such, it contains no email parsing
#pod capability, and little to no modification support.
#pod
#pod To re-iterate, this is very much a module for those "slap it together and
#pod fire it off" situations, but that still has enough grunt behind the scenes
#pod to do things properly.
#pod
#pod =head2 Default Transport
#pod
#pod Although it cannot be relied upon to work, the default behaviour is to
#pod use C<sendmail> to send mail, if you don't provide the mail send channel
#pod with either the C<transport> method, or as an argument to C<send>.
#pod
#pod (Actually, the choice of default is delegated to
#pod L<Email::Sender::Simple>, which makes its own choices.  But usually, it
#pod uses C<sendmail>.)
#pod
#pod =head2 Why use this?
#pod
#pod Why not just use L<Email::Simple> or L<Email::MIME>? After all, this just adds
#pod another layer of stuff around those. Wouldn't using them directly be better?
#pod
#pod Certainly, if you know EXACTLY what you are doing. The docs are clear enough,
#pod but you really do need to have an understanding of the structure of MIME
#pod emails. This structure is going to be different depending on whether you have
#pod text body, HTML, both, with or without an attachment etc.
#pod
#pod Then there's brevity... compare the following roughly equivalent code.
#pod
#pod First, the Email::Stuffer way.
#pod
#pod   Email::Stuffer->to('Simon Cozens<simon@somewhere.jp>')
#pod                 ->from('Santa@northpole.org')
#pod                 ->text_body("You've been good this year. No coal for you.")
#pod                 ->attach_file('choochoo.gif')
#pod                 ->send;
#pod
#pod And now doing it directly with a knowledge of what your attachment is, and
#pod what the correct MIME structure is.
#pod
#pod   use Email::MIME;
#pod   use Email::Sender::Simple;
#pod   use IO::All;
#pod
#pod   Email::Sender::Simple->try_to_send(
#pod     Email::MIME->create(
#pod       header => [
#pod           To => 'simon@somewhere.jp',
#pod           From => 'santa@northpole.org',
#pod       ],
#pod       parts => [
#pod           Email::MIME->create(
#pod             body => "You've been a good boy this year. No coal for you."
#pod           ),
#pod           Email::MIME->create(
#pod             body => io('choochoo.gif'),
#pod             attributes => {
#pod                 filename => 'choochoo.gif',
#pod                 content_type => 'image/gif',
#pod             },
#pod          ),
#pod       ],
#pod     );
#pod   );
#pod
#pod Again, if you know MIME well, and have the patience to manually code up
#pod the L<Email::MIME> structure, go do that, if you really want to.
#pod
#pod Email::Stuffer as the name suggests, solves one case and one case only:
#pod generate some stuff, and email it to somewhere, as conveniently as
#pod possible. DWIM, but do it as thinly as possible and use the solid
#pod Email:: modules underneath.
#pod
#pod =head1 COOKBOOK
#pod
#pod Here is another example (maybe plural later) of how you can use
#pod Email::Stuffer's brevity to your advantage.
#pod
#pod =head2 Custom Alerts
#pod
#pod   package SMS::Alert;
#pod   use base 'Email::Stuffer';
#pod
#pod   sub new {
#pod     shift()->SUPER::new(@_)
#pod            ->from('monitor@my.website')
#pod            # Of course, we could have pulled these from
#pod            # $MyConfig->{support_tech} or something similar.
#pod            ->to('0416181595@sms.gateway')
#pod            ->transport('SMTP', { host => '123.123.123.123' });
#pod   }
#pod
#pod Z<>
#pod
#pod   package My::Code;
#pod
#pod   unless ( $Server->restart ) {
#pod           # Notify the admin on call that a server went down and failed
#pod           # to restart.
#pod           SMS::Alert->subject("Server $Server failed to restart cleanly")
#pod                     ->send;
#pod   }
#pod
#pod =head1 METHODS
#pod
#pod As you can see from the synopsis, all methods that B<modify> the
#pod Email::Stuffer object returns the object, and thus most normal calls are
#pod chainable.
#pod
#pod However, please note that C<send>, and the group of methods that do not
#pod change the Email::Stuffer object B<do not> return the object, and thus
#pod B<are not> chainable.
#pod
#pod =cut

use Carp                   qw(croak);
use File::Basename         ();
use Params::Util 1.05      qw(_INSTANCE _INSTANCEDOES);
use Email::MIME 1.943      ();
use Email::MIME::Creator   ();
use Email::Sender::Simple  ();
use Module::Runtime        qw(require_module);

#####################################################################
# Constructor and Accessors

#pod =method new
#pod
#pod Creates a new, empty, Email::Stuffer object.
#pod
#pod You can pass a hashref of properties to set, including:
#pod
#pod =for :list
#pod * to
#pod * from
#pod * cc
#pod * bcc
#pod * reply_to
#pod * subject
#pod * text_body
#pod * html_body
#pod * transport
#pod
#pod The to, cc, bcc, and reply_to headers properties may be provided as array
#pod references.  The array's contents will be used as the list of arguments to the
#pod setter.
#pod
#pod =cut

my %IS_INIT_ARG = map {; $_ => 1 } qw(
  to from cc bcc reply_to subject text_body html_body transport
);

my %IS_ARRAY_ARG = map {; $_ => 1 } qw(
  to cc bcc reply_to
  transport
);

sub new {
  Carp::croak("new method called on Email::Stuffer instance") if ref $_[0];

  my ($class, $arg) = @_;

  my $self = bless {
    parts      => [],
    email      => Email::MIME->create(
      header => [],
      parts  => [],
    ),
  }, $class;

  my @init_args = keys %{ $arg || {} };
  if (my @bogus = grep {; ! $IS_INIT_ARG{$_} } @init_args) {
    Carp::croak("illegal arguments to Email::Stuffer->new: @bogus");
  }

  for my $init_arg (@init_args) {
    my @args = $arg->{$init_arg};
    if ($IS_ARRAY_ARG{$init_arg} && ref $args[0] && ref $args[0] eq 'ARRAY') {
      @args = @{ $args[0] };
    }

    $self->$init_arg(@args);
  }

  $self;
}

sub _self {
  my $either = shift;
  ref($either) ? $either : $either->new;
}

#pod =method header_names
#pod
#pod Returns, as a list, all of the headers currently set for the Email
#pod For backwards compatibility, this method can also be called as B[headers].
#pod
#pod =cut

sub header_names {
  shift()->{email}->header_names;
}

sub headers {
  shift()->{email}->header_names; ## This is now header_names, headers is depreciated
}

#pod =method parts
#pod
#pod Returns, as a list, the L<Email::MIME> parts for the Email
#pod
#pod =cut

sub parts {
  grep { defined $_ } @{shift()->{parts}};
}

#####################################################################
# Header Methods

#pod =method header
#pod
#pod   $stuffer->header($header_name = $value)
#pod
#pod This method sets a named header in the email. Multiple calls with the same
#pod C<$header_name> will overwrite previous calls C<$value>.
#pod
#pod =cut

sub header {
  my $self = shift()->_self;
  return unless @_;
  $self->{email}->header_str_set(ucfirst shift, shift);
  return $self;
}

#pod =method to
#pod
#pod   $stuffer->to(@addresses)
#pod
#pod This method sets the To header in the email.
#pod
#pod =cut

sub _assert_addr_list_ok {
  my ($self, $header, $allow_empty, $list) = @_;

  Carp::croak("$header is a required field")
    unless $allow_empty or @$list;

  for (@$list) {
    Carp::croak("list of $header headers contains undefined values")
      unless defined;

    Carp::croak("list of $header headers contains unblessed references")
      if ref && ! blessed $_;
  }
}

sub to {
  my $self = shift()->_self;
  $self->_assert_addr_list_ok(to => 0 => \@_);
  $self->{email}->header_str_set(To => (@_ > 1 ? \@_ : @_));
  return $self;
}

#pod =method from
#pod
#pod   $stuffer->from($address)
#pod
#pod
#pod This method sets the From header in the email.
#pod
#pod =cut

sub from {
  my $self = shift()->_self;
  $self->_assert_addr_list_ok(from => 0 => \@_);
  Carp::croak("only one address is allowed in the from header") if @_ > 1;
  $self->{email}->header_str_set(From => shift);
  return $self;
}

#pod =method reply_to
#pod
#pod   $stuffer->reply_to($address)
#pod
#pod This method sets the Reply-To header in the email.
#pod
#pod =cut

sub reply_to {
  my $self = shift()->_self;
  $self->_assert_addr_list_ok('reply-to' => 0 => \@_);
  Carp::croak("only one address is allowed in the reply-to header") if @_ > 1;
  $self->{email}->header_str_set('Reply-To' => shift);
  return $self;
}

#pod =method cc
#pod
#pod   $stuffer->cc(@addresses)
#pod
#pod This method sets the Cc header in the email.
#pod
#pod =cut

sub cc {
  my $self = shift()->_self;
  $self->_assert_addr_list_ok(cc => 1 => \@_);
  $self->{email}->header_str_set(Cc => (@_ > 1 ? \@_ : @_));
  return $self;
}

#pod =method bcc
#pod
#pod   $stuffer->bcc(@addresses)
#pod
#pod This method sets the Bcc header in the email.
#pod
#pod =cut

sub bcc {
  my $self = shift()->_self;
  $self->_assert_addr_list_ok(bcc => 1 => \@_);
  $self->{email}->header_str_set(Bcc => (@_ > 1 ? \@_ : @_));
  return $self;
}

#pod =method subject
#pod
#pod   $stuffer->subject($text)
#pod
#pod This method sets the Subject header in the email.
#pod
#pod =cut

sub subject {
  my $self = shift()->_self;
  Carp::croak("subject is a required field") unless defined $_[0];
  $self->{email}->header_str_set(Subject => shift);
  return $self;
}

#####################################################################
# Body and Attachments

#pod =method text_body
#pod
#pod   $stuffer->text_body($body, %attributes);
#pod
#pod Sets the text body of the email. Appropriate headers are set for you.
#pod You may override MIME attributes as needed. See the C<attributes>
#pod parameter to L<Email::MIME/create> for the headers you can set.
#pod
#pod If C<$body> is undefined, this method will do nothing.
#pod
#pod Prior to Email::Stuffer version 0.015 text body was marked as flowed,
#pod which broke all pre-formated body text.  Empty space at the beggining
#pod of the line was dropped and every new line character could be changed
#pod to one space (and vice versa).  Version 0.015 (and later) does not set
#pod flowed format automatically anymore and so text body is really plain
#pod text.  If you want to use old behavior of "advanced" flowed formatting,
#pod set flowed format manually by: C<< text_body($body, format => 'flowed') >>.
#pod
#pod =cut

sub text_body {
  my $self = shift()->_self;
  my $body = defined $_[0] ? shift : return $self;
  my %attr = (
    # Defaults
    content_type => 'text/plain',
    charset      => 'utf-8',
    encoding     => 'quoted-printable',
    # Params overwrite them
    @_,
    );

  # Create the part in the text slot
  $self->{parts}->[0] = Email::MIME->create(
    attributes => \%attr,
    body_str   => $body,
    );

  $self;
}

#pod =method html_body
#pod
#pod   $stuffer->html_body($body, %attributes);
#pod
#pod Sets the HTML body of the email. Appropriate headers are set for you.
#pod You may override MIME attributes as needed. See the C<attributes>
#pod parameter to L<Email::MIME/create> for the headers you can set.
#pod
#pod If C<$body> is undefined, this method will do nothing.
#pod
#pod =cut

sub html_body {
  my $self = shift()->_self;
  my $body = defined $_[0] ? shift : return $self;
  my %attr = (
    # Defaults
    content_type => 'text/html',
    charset      => 'utf-8',
    encoding     => 'quoted-printable',
    # Params overwrite them
    @_,
    );

  # Create the part in the HTML slot
  $self->{parts}->[1] = Email::MIME->create(
    attributes => \%attr,
    body_str   => $body,
    );

  $self;
}

#pod =method attach
#pod
#pod   $stuffer->attach($contents, %attributes)
#pod
#pod Adds an attachment to the email. The first argument is the file contents
#pod followed by (as for text_body and html_body) the list of headers to use.
#pod Email::Stuffer will I<try> to guess the headers correctly, but you may wish
#pod to provide them anyway to be sure. Encoding is Base64 by default. See
#pod the C<attributes> parameter to L<Email::MIME/create> for the headers you
#pod can set.
#pod
#pod =cut

sub _detect_content_type {
  my ($filename, $body) = @_;

  if (defined($filename)) {
    if ($filename =~ /\.([a-zA-Z]{3,4})\z/) {
      my $content_type = {
        'gif'  => 'image/gif',
        'png'  => 'image/png',
        'jpg'  => 'image/jpeg',
        'jpeg' => 'image/jpeg',
        'txt'  => 'text/plain',
        'htm'  => 'text/html',
        'html' => 'text/html',
        'css'  => 'text/css',
        'csv'  => 'text/csv',
        'pdf'  => 'application/pdf',
        'wav'  => 'audio/wav',
      }->{lc($1)};
      return $content_type if defined $content_type;
    }
  }
  if ($body =~ /
    \A(?:
        (GIF8)          # gif
      | (\xff\xd8)      # jpeg
      | (\x89PNG)       # png
      | (%PDF-)         # pdf
    )
  /x) {
    return 'image/gif'  if $1;
    return 'image/jpeg' if $2;
    return 'image/png'  if $3;
    return 'application/pdf' if $4;
  }
  return 'application/octet-stream';
}

sub attach {
  my $self = shift()->_self;
  my $body = defined $_[0] ? shift : return undef;
  my %attr = (
    # Cheap defaults
    encoding => 'base64',
    # Params overwrite them
    @_,
    );

  # The more expensive defaults if needed
  unless ( $attr{content_type} ) {
    $attr{content_type} = _detect_content_type($attr{filename}, $body);
  }

  ### MORE?

  # Determine the slot to put it at
  my $slot = scalar @{$self->{parts}};
  $slot = 3 if $slot < 3;

  # Create the part in the attachment slot
  $self->{parts}->[$slot] = Email::MIME->create(
    attributes => \%attr,
    body       => $body,
    );

  $self;
}

#pod =method attach_file
#pod
#pod   $stuffer->attach_file($file, %attributes)
#pod
#pod Attachs a file that already exists on the filesystem to the email.
#pod C<attach_file> will attempt to auto-detect the MIME type, and use the
#pod file's current name when attaching. See the C<attributes> parameter to
#pod L<Email::MIME/create> for the headers you can set.
#pod
#pod C<$file> can be a filename or an IO::All::File object.
#pod
#pod =cut

sub attach_file {
  my $self = shift;
  my $body_arg = shift;
  my $name = undef;
  my $body = undef;

  # Support IO::All::File arguments
  if ( Params::Util::_INSTANCE($body_arg, 'IO::All::File') ) {
    $body_arg->binmode;
    $name = $body_arg->name;
    $body = $body_arg->all;

  # Support file names
  } elsif ( defined $body_arg and Params::Util::_STRING($body_arg) ) {
    croak "No such file '$body_arg'" unless -f $body_arg;
    $name = $body_arg;
    $body = _slurp( $body_arg );

  # That's it
  } else {
    my $type = ref($body_arg) || "<$body_arg>";
    croak "Expected a file name or an IO::All::File derivative, got $type";
  }

  # Clean the file name
  $name = File::Basename::basename($name);

  croak("basename somehow returned undef") unless defined $name;

  # Now attach as normal
  $self->attach( $body, name => $name, filename => $name, @_ );
}

# Provide a simple _slurp implementation
sub _slurp {
  my $file = shift;
  local $/ = undef;

  open my $slurp, '<:raw', $file or croak("error opening $file: $!");
  my $source = <$slurp>;
  close( $slurp ) or croak "error after slurping $file: $!";
  \$source;
}

#pod =method transport
#pod
#pod   $stuffer->transport( $moniker, @options )
#pod
#pod or
#pod
#pod   $stuffer->transport( $transport_obj )
#pod
#pod The C<transport> method specifies the L<Email::Sender> transport that
#pod you want to use to send the email, and any options that need to be
#pod used to instantiate the transport.  C<$moniker> is used as the transport
#pod name; if it starts with an equals sign (C<=>) then the text after the
#pod sign is used as the class.  Otherwise, the text is prepended by
#pod C<Email::Sender::Transport::>.
#pod
#pod Alternatively, you can pass a complete transport object (which must be
#pod an L<Email::Sender::Transport> object) and it will be used as is.
#pod
#pod =cut

sub transport {
  my $self = shift()->_self;

  if ( @_ ) {
    # Change the transport
    if ( _INSTANCEDOES($_[0], 'Email::Sender::Transport') ) {
      $self->{transport} = shift;
    } else {
      my ($moniker, @arg) = @_;
      my $class = $moniker =~ s/\A=//
                ? $moniker
                : "Email::Sender::Transport::$moniker";
      require_module($class);
      my $transport = $class->new(@arg);
      $self->{transport} = $transport;
    }
  }

  $self;
}

#####################################################################
# Output Methods

#pod =method email
#pod
#pod   my $email_mime = $stuffer->email;
#pod
#pod This method creates and returns the full L<Email::MIME> object for the email.
#pod
#pod =cut

sub email {
  my $self  = shift;
  my @parts = $self->parts;

  ### Lyle Hopkins, code added to Fix single part, and multipart/alternative
  ### problems
  if (scalar(@{ $self->{parts} }) >= 3) {
    ## multipart/mixed
    $self->{email}->parts_set(\@parts);
  } elsif (scalar(@{ $self->{parts} })) {
    ## Check we actually have any parts
    if ( _INSTANCE($parts[0], 'Email::MIME')
      && _INSTANCE($parts[1], 'Email::MIME')
    ) {
      ## multipart/alternate
      $self->{email}->header_set('Content-Type' => 'multipart/alternative');
      $self->{email}->parts_set(\@parts);
    } elsif (_INSTANCE($parts[0], 'Email::MIME')) {
      ## As @parts is $self->parts without the blanks, we only need check
      ## $parts[0]
      ## single part text/plain
      _transfer_headers($self->{email}, $parts[0]);
      $self->{email} = $parts[0];
    }
  }

  $self->{email};
}

# Support coercion to an Email::MIME
sub __as_Email_MIME { shift()->email }

# Quick any routine
sub _any (&@) {
        my $f = shift;
        return if ! @_;
        for (@_) {
                return 1 if $f->();
        }
        return 0;
}

# header transfer from one object to another
sub _transfer_headers {
        # $_[0] = from, $_[1] = to
        my @headers_move = $_[0]->header_names;
        my @headers_skip = $_[1]->header_names;
        foreach my $header_name (@headers_move) {
                next if _any { $_ eq $header_name } @headers_skip;
                my @values = $_[0]->header($header_name);
                $_[1]->header_str_set( $header_name, @values );
        }
}

#pod =method as_string
#pod
#pod   my $email_document = $stuffer->as_string;
#pod
#pod Returns the string form of the email.  Identical to (and uses behind the
#pod scenes) C<< Email::MIME->as_string >>.
#pod
#pod =cut

sub as_string {
  shift()->email->as_string;
}

#pod =method send
#pod
#pod   $stuffer->send;
#pod
#pod or
#pod
#pod   $stuffer->send({ to => [ $to_1, $to_2 ], from => $sender });
#pod
#pod Sends the email via L<Email::Sender::Simple>.
#pod L<Envelope information|Email::Sender::Manual::QuickStart/"envelope information">
#pod can be specified in a hash reference.
#pod
#pod On failure, returns false.
#pod
#pod =cut

sub send {
  my $self = shift;
  my $arg  = shift;
  my $email = $self->email or return undef;

  my $transport = $self->{transport};

  Email::Sender::Simple->try_to_send(
    $email,
    {
      ($transport ? (transport => $transport) : ()),
      $arg ? %$arg : (),
    },
  );
}

#pod =method send_or_die
#pod
#pod   $stuffer->send_or_die;
#pod
#pod or
#pod
#pod   $stuffer->send_or_die({ to => [ $to_1, $to_2 ], from => $sender });
#pod
#pod Sends the email via L<Email::Sender::Simple>.
#pod L<Envelope information|Email::Sender::Manual::QuickStart/"envelope information">
#pod can be specified in a hash reference.
#pod
#pod On failure, throws an exception.
#pod
#pod =cut

sub send_or_die {
  my $self = shift;
  my $arg  = shift;
  my $email = $self->email or return undef;

  my $transport = $self->{transport};

  Email::Sender::Simple->send(
    $email,
    {
      ($transport ? (transport => $transport) : ()),
      $arg ? %$arg : (),
    },
  );
}

1;

#pod =head1 TO DO
#pod
#pod =for :list
#pod * Fix a number of bugs still likely to exist
#pod * Write more tests.
#pod * Add any additional small bit of automation that isn't too expensive
#pod
#pod =head1 SEE ALSO
#pod
#pod L<Email::MIME>, L<Email::Sender>, L<http://ali.as/>
#pod
#pod =cut

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Stuffer - A more casual approach to creating and sending Email:: emails

=head1 VERSION

version 0.020

=head1 SYNOPSIS

  # Prepare the message
  my $body = <<'AMBUSH_READY';
  Dear Santa

  I have killed Bun Bun.

  Yes, I know what you are thinking... but it was actually a total accident.

  I was in a crowded line at a BayWatch signing, and I tripped, and stood on
  his head.

  I know. Oops! :/

  So anyways, I am willing to sell you the body for $1 million dollars.

  Be near the pinhole to the Dimension of Pain at midnight.

  Alias

  AMBUSH_READY

  # Create and send the email in one shot
  Email::Stuffer->from     ('cpan@ali.as'             )
                ->to       ('santa@northpole.org'     )
                ->bcc      ('bunbun@sluggy.com'       )
                ->text_body($body                     )
                ->attach_file('dead_bunbun_faked.gif' )
                ->send;

=head1 DESCRIPTION

B<The basics should all work, but this module is still subject to
name and/or API changes>

Email::Stuffer, as its name suggests, is a fairly casual module used
to stuff things into an email and send them. It is a high-level module
designed for ease of use when doing a very specific common task, but
implemented on top of the light and tolerable Email:: modules.

Email::Stuffer is typically used to build emails and send them in a single
statement, as seen in the synopsis. And it is certain only for use when
creating and sending emails. As such, it contains no email parsing
capability, and little to no modification support.

To re-iterate, this is very much a module for those "slap it together and
fire it off" situations, but that still has enough grunt behind the scenes
to do things properly.

=head2 Default Transport

Although it cannot be relied upon to work, the default behaviour is to
use C<sendmail> to send mail, if you don't provide the mail send channel
with either the C<transport> method, or as an argument to C<send>.

(Actually, the choice of default is delegated to
L<Email::Sender::Simple>, which makes its own choices.  But usually, it
uses C<sendmail>.)

=head2 Why use this?

Why not just use L<Email::Simple> or L<Email::MIME>? After all, this just adds
another layer of stuff around those. Wouldn't using them directly be better?

Certainly, if you know EXACTLY what you are doing. The docs are clear enough,
but you really do need to have an understanding of the structure of MIME
emails. This structure is going to be different depending on whether you have
text body, HTML, both, with or without an attachment etc.

Then there's brevity... compare the following roughly equivalent code.

First, the Email::Stuffer way.

  Email::Stuffer->to('Simon Cozens<simon@somewhere.jp>')
                ->from('Santa@northpole.org')
                ->text_body("You've been good this year. No coal for you.")
                ->attach_file('choochoo.gif')
                ->send;

And now doing it directly with a knowledge of what your attachment is, and
what the correct MIME structure is.

  use Email::MIME;
  use Email::Sender::Simple;
  use IO::All;

  Email::Sender::Simple->try_to_send(
    Email::MIME->create(
      header => [
          To => 'simon@somewhere.jp',
          From => 'santa@northpole.org',
      ],
      parts => [
          Email::MIME->create(
            body => "You've been a good boy this year. No coal for you."
          ),
          Email::MIME->create(
            body => io('choochoo.gif'),
            attributes => {
                filename => 'choochoo.gif',
                content_type => 'image/gif',
            },
         ),
      ],
    );
  );

Again, if you know MIME well, and have the patience to manually code up
the L<Email::MIME> structure, go do that, if you really want to.

Email::Stuffer as the name suggests, solves one case and one case only:
generate some stuff, and email it to somewhere, as conveniently as
possible. DWIM, but do it as thinly as possible and use the solid
Email:: modules underneath.

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 METHODS

As you can see from the synopsis, all methods that B<modify> the
Email::Stuffer object returns the object, and thus most normal calls are
chainable.

However, please note that C<send>, and the group of methods that do not
change the Email::Stuffer object B<do not> return the object, and thus
B<are not> chainable.

=head2 new

Creates a new, empty, Email::Stuffer object.

You can pass a hashref of properties to set, including:

=over 4

=item *

to

=item *

from

=item *

cc

=item *

bcc

=item *

reply_to

=item *

subject

=item *

text_body

=item *

html_body

=item *

transport

=back

The to, cc, bcc, and reply_to headers properties may be provided as array
references.  The array's contents will be used as the list of arguments to the
setter.

=head2 header_names

Returns, as a list, all of the headers currently set for the Email
For backwards compatibility, this method can also be called as B[headers].

=head2 parts

Returns, as a list, the L<Email::MIME> parts for the Email

=head2 header

  $stuffer->header($header_name = $value)

This method sets a named header in the email. Multiple calls with the same
C<$header_name> will overwrite previous calls C<$value>.

=head2 to

  $stuffer->to(@addresses)

This method sets the To header in the email.

=head2 from

  $stuffer->from($address)

This method sets the From header in the email.

=head2 reply_to

  $stuffer->reply_to($address)

This method sets the Reply-To header in the email.

=head2 cc

  $stuffer->cc(@addresses)

This method sets the Cc header in the email.

=head2 bcc

  $stuffer->bcc(@addresses)

This method sets the Bcc header in the email.

=head2 subject

  $stuffer->subject($text)

This method sets the Subject header in the email.

=head2 text_body

  $stuffer->text_body($body, %attributes);

Sets the text body of the email. Appropriate headers are set for you.
You may override MIME attributes as needed. See the C<attributes>
parameter to L<Email::MIME/create> for the headers you can set.

If C<$body> is undefined, this method will do nothing.

Prior to Email::Stuffer version 0.015 text body was marked as flowed,
which broke all pre-formated body text.  Empty space at the beggining
of the line was dropped and every new line character could be changed
to one space (and vice versa).  Version 0.015 (and later) does not set
flowed format automatically anymore and so text body is really plain
text.  If you want to use old behavior of "advanced" flowed formatting,
set flowed format manually by: C<< text_body($body, format => 'flowed') >>.

=head2 html_body

  $stuffer->html_body($body, %attributes);

Sets the HTML body of the email. Appropriate headers are set for you.
You may override MIME attributes as needed. See the C<attributes>
parameter to L<Email::MIME/create> for the headers you can set.

If C<$body> is undefined, this method will do nothing.

=head2 attach

  $stuffer->attach($contents, %attributes)

Adds an attachment to the email. The first argument is the file contents
followed by (as for text_body and html_body) the list of headers to use.
Email::Stuffer will I<try> to guess the headers correctly, but you may wish
to provide them anyway to be sure. Encoding is Base64 by default. See
the C<attributes> parameter to L<Email::MIME/create> for the headers you
can set.

=head2 attach_file

  $stuffer->attach_file($file, %attributes)

Attachs a file that already exists on the filesystem to the email.
C<attach_file> will attempt to auto-detect the MIME type, and use the
file's current name when attaching. See the C<attributes> parameter to
L<Email::MIME/create> for the headers you can set.

C<$file> can be a filename or an IO::All::File object.

=head2 transport

  $stuffer->transport( $moniker, @options )

or

  $stuffer->transport( $transport_obj )

The C<transport> method specifies the L<Email::Sender> transport that
you want to use to send the email, and any options that need to be
used to instantiate the transport.  C<$moniker> is used as the transport
name; if it starts with an equals sign (C<=>) then the text after the
sign is used as the class.  Otherwise, the text is prepended by
C<Email::Sender::Transport::>.

Alternatively, you can pass a complete transport object (which must be
an L<Email::Sender::Transport> object) and it will be used as is.

=head2 email

  my $email_mime = $stuffer->email;

This method creates and returns the full L<Email::MIME> object for the email.

=head2 as_string

  my $email_document = $stuffer->as_string;

Returns the string form of the email.  Identical to (and uses behind the
scenes) C<< Email::MIME->as_string >>.

=head2 send

  $stuffer->send;

or

  $stuffer->send({ to => [ $to_1, $to_2 ], from => $sender });

Sends the email via L<Email::Sender::Simple>.
L<Envelope information|Email::Sender::Manual::QuickStart/"envelope information">
can be specified in a hash reference.

On failure, returns false.

=head2 send_or_die

  $stuffer->send_or_die;

or

  $stuffer->send_or_die({ to => [ $to_1, $to_2 ], from => $sender });

Sends the email via L<Email::Sender::Simple>.
L<Envelope information|Email::Sender::Manual::QuickStart/"envelope information">
can be specified in a hash reference.

On failure, throws an exception.

=head1 COOKBOOK

Here is another example (maybe plural later) of how you can use
Email::Stuffer's brevity to your advantage.

=head2 Custom Alerts

  package SMS::Alert;
  use base 'Email::Stuffer';

  sub new {
    shift()->SUPER::new(@_)
           ->from('monitor@my.website')
           # Of course, we could have pulled these from
           # $MyConfig->{support_tech} or something similar.
           ->to('0416181595@sms.gateway')
           ->transport('SMTP', { host => '123.123.123.123' });
  }

Z<>

  package My::Code;

  unless ( $Server->restart ) {
          # Notify the admin on call that a server went down and failed
          # to restart.
          SMS::Alert->subject("Server $Server failed to restart cleanly")
                    ->send;
  }

=head1 TO DO

=over 4

=item *

Fix a number of bugs still likely to exist

=item *

Write more tests.

=item *

Add any additional small bit of automation that isn't too expensive

=back

=head1 SEE ALSO

L<Email::MIME>, L<Email::Sender>, L<http://ali.as/>

=head1 AUTHORS

=over 4

=item *

Adam Kennedy <adamk@cpan.org>

=item *

Ricardo SIGNES <cpan@semiotic.systems>

=back

=head1 CONTRIBUTORS

=for stopwords Aaron W. Swenson adam adamk@cpan.org adam@phase-n.com Alastair Douglas Aristotle Pagaltzis Chase Whitener CosmicNet Dan Book fREW Schmidt John Napiorkowski Josh Stompro Kevin Tew Kieren Diment Kris Matthews Lee Johnson Manni Heumann Pali Ricardo Signes Ross Attrill Russell Jenkins Shawn Sorichetti Steve Dondley tokuhirom

=over 4

=item *

Aaron W. Swenson <aaron.w.swenson@gmail.com>

=item *

adam <adam@88f4d9cd-8a04-0410-9d60-8f63309c3137>

=item *

adamk@cpan.org <adamk@cpan.org@88f4d9cd-8a04-0410-9d60-8f63309c3137>

=item *

adam@phase-n.com <adam@phase-n.com@88f4d9cd-8a04-0410-9d60-8f63309c3137>

=item *

Alastair Douglas <altreus@altre.us>

=item *

Aristotle Pagaltzis <pagaltzis@gmx.de>

=item *

Chase Whitener <chase.whitener@infotechfl.com>

=item *

CosmicNet <webmaster@cosmicperl.com>

=item *

Dan Book <grinnz@gmail.com>

=item *

fREW Schmidt <frioux@gmail.com>

=item *

John Napiorkowski <jjn1056@yahoo.com>

=item *

Josh Stompro <github@stompro.org>

=item *

Kevin Tew <tewk@tan.tewk.com>

=item *

Kieren Diment <kd@fenchurch.local>

=item *

Kris Matthews <kris@tigerlms.com>

=item *

Lee Johnson <lee@givengain.ch>

=item *

Manni Heumann <github@lxxi.org>

=item *

Pali <pali@cpan.org>

=item *

Ricardo Signes <rjbs@semiotic.systems>

=item *

Ross Attrill <ross.attrill@gmail.com>

=item *

Russell Jenkins <russell.jenkins@strategicdata.com.au>

=item *

Shawn Sorichetti <shawn@coloredblocks.com>

=item *

Steve Dondley <s@dondley.com>

=item *

tokuhirom <tokuhirom@gmail.com>

=back

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2004 by Adam Kennedy and Ricardo SIGNES.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
