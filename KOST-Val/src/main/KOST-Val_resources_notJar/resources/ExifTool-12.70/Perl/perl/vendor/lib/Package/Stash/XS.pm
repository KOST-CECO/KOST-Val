package Package::Stash::XS; # git description: v0.29-21-gea0807b
use strict;
use warnings;
use 5.008001;
# ABSTRACT: Faster and more correct implementation of the Package::Stash API

our $VERSION = '0.30';

use XSLoader;
XSLoader::load(__PACKAGE__, $VERSION);

__END__

=pod

=encoding UTF-8

=head1 NAME

Package::Stash::XS - Faster and more correct implementation of the Package::Stash API

=head1 VERSION

version 0.30

=head1 SYNOPSIS

  use Package::Stash;

=head1 DESCRIPTION

This is a backend for L<Package::Stash>, which provides the functionality in a
way that's less buggy and much faster. It will be used by default if it's
installed, and should be preferred in all environments with a compiler.

=head1 BUGS

No known bugs (but see the BUGS section in L<Package::Stash>).

=head1 SEE ALSO

L<Package::Stash>

=for Pod::Coverage add_symbol
get_all_symbols
get_or_add_symbol
get_symbol
has_symbol
list_all_symbols
name
namespace
new
remove_glob
remove_symbol

=head1 SUPPORT

Bugs may be submitted through L<the RT bug tracker|https://rt.cpan.org/Public/Dist/Display.html?Name=Package-Stash-XS>
(or L<bug-Package-Stash-XS@rt.cpan.org|mailto:bug-Package-Stash-XS@rt.cpan.org>).

=head1 AUTHORS

=over 4

=item *

Stevan Little <stevan.little@iinteractive.com>

=item *

Jesse Luehrs <doy@tozt.net>

=back

=head1 CONTRIBUTORS

=for stopwords Karen Etheridge Florian Ragwitz Dave Rolsky Justin Hunter Tim Bunce

=over 4

=item *

Karen Etheridge <ether@cpan.org>

=item *

Florian Ragwitz <rafl@debian.org>

=item *

Dave Rolsky <autarch@urth.org>

=item *

Justin Hunter <justin.d.hunter@gmail.com>

=item *

Tim Bunce <Tim.Bunce@pobox.com>

=back

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2022 by Jesse Luehrs.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
