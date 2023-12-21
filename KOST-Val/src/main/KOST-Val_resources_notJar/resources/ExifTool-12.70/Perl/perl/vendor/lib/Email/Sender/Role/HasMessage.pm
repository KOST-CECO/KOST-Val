package Email::Sender::Role::HasMessage 2.600;
# ABSTRACT: an object that has a message

use Moo::Role;

#pod =attr message
#pod
#pod This attribute is a message associated with the object.
#pod
#pod =cut

has message => (
  is       => 'ro',
  required => 1,
);

no Moo::Role;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Sender::Role::HasMessage - an object that has a message

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

This attribute is a message associated with the object.

=head1 AUTHOR

Ricardo Signes <cpan@semiotic.systems>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2022 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
