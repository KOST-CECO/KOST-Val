package Email::MIME::Kit::Role::Component 3.000007;
# ABSTRACT: things that are kit components

use v5.20.0;
use Moose::Role;

#pod =head1 DESCRIPTION
#pod
#pod All (or most, anyway) components of an Email::MIME::Kit will perform this role.
#pod Its primary function is to provide a C<kit> attribute that refers back to the
#pod Email::MIME::Kit into which the component was installed.
#pod
#pod =cut

has kit => (
  is  => 'ro',
  isa => 'Email::MIME::Kit',
  required => 1,
  weak_ref => 1,
);

no Moose::Role;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::MIME::Kit::Role::Component - things that are kit components

=head1 VERSION

version 3.000007

=head1 DESCRIPTION

All (or most, anyway) components of an Email::MIME::Kit will perform this role.
Its primary function is to provide a C<kit> attribute that refers back to the
Email::MIME::Kit into which the component was installed.

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 AUTHOR

Ricardo Signes <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2023 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
