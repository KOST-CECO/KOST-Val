use v5.12.0;
use warnings;
package Email::MIME::Creator 1.953;
# ABSTRACT: obsolete do-nothing library

use parent q[Email::Simple::Creator];
use Email::MIME;
use Encode ();

sub _construct_part {
  my ($class, $body) = @_;

  my $is_binary = $body =~ /[\x00\x80-\xFF]/;

  my $content_type = $is_binary ? 'application/x-binary' : 'text/plain';

  Email::MIME->create(
    attributes => {
      content_type => $content_type,
      encoding     => ($is_binary ? 'base64' : ''),  # be safe
    },
    body => $body,
  );
}

1;

#pod =head1 SYNOPSIS
#pod
#pod You don't need to use this module for anything.
#pod
#pod =cut

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::MIME::Creator - obsolete do-nothing library

=head1 VERSION

version 1.953

=head1 SYNOPSIS

You don't need to use this module for anything.

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 AUTHORS

=over 4

=item *

Ricardo SIGNES <cpan@semiotic.systems>

=item *

Casey West <casey@geeknest.com>

=item *

Simon Cozens <simon@cpan.org>

=back

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2004 by Simon Cozens and Casey West.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
