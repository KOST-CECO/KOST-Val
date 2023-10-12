package Email::MIME::Kit::Role::Validator 3.000007;
# ABSTRACT: things that validate assembly parameters

use v5.20.0;
use Moose::Role;

#pod =head1 IMPLEMENTING
#pod
#pod This role also performs L<Email::MIME::Kit::Role::Component>.
#pod
#pod Classes implementing this role are used to validate that the arguments passed
#pod to C<< $mkit->assemble >> are valid.  Classes must provide a C<validate> method
#pod which will be called with the hashref of values passed to the kit's C<assemble>
#pod method.  If the arguments are not valid for the kit, the C<validate> method
#pod should raise an exception.
#pod
#pod =cut

with 'Email::MIME::Kit::Role::Component';

requires 'validate';

no Moose::Role;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::MIME::Kit::Role::Validator - things that validate assembly parameters

=head1 VERSION

version 3.000007

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 IMPLEMENTING

This role also performs L<Email::MIME::Kit::Role::Component>.

Classes implementing this role are used to validate that the arguments passed
to C<< $mkit->assemble >> are valid.  Classes must provide a C<validate> method
which will be called with the hashref of values passed to the kit's C<assemble>
method.  If the arguments are not valid for the kit, the C<validate> method
should raise an exception.

=head1 AUTHOR

Ricardo Signes <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2023 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
