package Email::Sender::Success::Partial 2.600;
# ABSTRACT: a report of partial success when delivering

use Moo;
extends 'Email::Sender::Success';

use MooX::Types::MooseLike::Base qw(InstanceOf);

#pod =head1 DESCRIPTION
#pod
#pod These objects indicate that some delivery was accepted for some recipients and
#pod not others.  The success object's C<failure> attribute will return a
#pod L<Email::Sender::Failure::Multi> describing which parts of the delivery failed.
#pod
#pod =cut

use Email::Sender::Failure::Multi;

has failure => (
  is  => 'ro',
  isa => InstanceOf['Email::Sender::Failure::Multi'],
  required => 1,
);

no Moo;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Sender::Success::Partial - a report of partial success when delivering

=head1 VERSION

version 2.600

=head1 DESCRIPTION

These objects indicate that some delivery was accepted for some recipients and
not others.  The success object's C<failure> attribute will return a
L<Email::Sender::Failure::Multi> describing which parts of the delivery failed.

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
