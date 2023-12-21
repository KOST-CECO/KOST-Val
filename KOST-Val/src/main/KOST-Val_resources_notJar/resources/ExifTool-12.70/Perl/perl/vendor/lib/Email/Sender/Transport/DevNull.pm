package Email::Sender::Transport::DevNull 2.600;
# ABSTRACT: happily throw away your mail

use Moo;
with 'Email::Sender::Transport';

#pod =head1 DESCRIPTION
#pod
#pod This class implements L<Email::Sender::Transport>.  Any mail sent through a
#pod DevNull transport will be silently discarded.
#pod
#pod =cut

sub send_email { return $_[0]->success }

no Moo;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Sender::Transport::DevNull - happily throw away your mail

=head1 VERSION

version 2.600

=head1 DESCRIPTION

This class implements L<Email::Sender::Transport>.  Any mail sent through a
DevNull transport will be silently discarded.

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
