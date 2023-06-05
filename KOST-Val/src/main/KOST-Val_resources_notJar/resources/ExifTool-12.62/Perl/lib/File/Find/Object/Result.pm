package File::Find::Object::Result;
$File::Find::Object::Result::VERSION = '0.3.5';
use strict;
use warnings;
use integer;

use Class::XSAccessor accessors => {
    (
        map { $_ => $_ } (
            qw(
                base
                basename
                is_dir
                is_file
                is_link
                path
                dir_components
                stat_ret
                )
        )
    )
};

use Fcntl qw(:mode);

sub new
{
    my $class = shift;
    my $self  = shift;

    bless $self, $class;

    return $self;
}

sub full_components
{
    my $self = shift;

    return [
        @{ $self->dir_components() },
        ( $self->is_dir() ? () : $self->basename() ),
    ];
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

File::Find::Object::Result - a result class for File::Find::Object

=head1 VERSION

version 0.3.5

=head1 DESCRIPTION

This is a class returning a single L<File::Find::Object> result as returned
by its next_obj() method.

=head1 METHODS

=head2 File::Find::Object::Result->new({%args});

Initializes a new object from %args. For internal use.

=head2 $result->base()

Returns the base directory from which searching began.

=head2 $result->path()

Returns the full path of the result. As such C<< $ffo->next_obj()->path() >>
is equivalent to C<< $ffo->next() >> .

=head2 $result->is_dir()

Returns true if the result refers to a directory.

=head2 $result->is_file()

Returns true if the result refers to a plain file (equivalent to the Perl
C<-f> operator).

=head2 $result->is_link()

Returns true if the result is a symbolic link.

=head2 $result->dir_components()

The components of the directory part of the path starting from base()
(also the full path if the result is a directory) as an array reference.

=head2 $result->basename()

Returns the basename of the file (if it is a file and not a directory.)
Otherwise - undef().

=head2 $result->full_components()

Returns the full components of the result with the basename if it is
a file.

Returns a single array reference.

=head2 $result->stat_ret()

The return value of L<perlfunc/stat> for the result, placed
inside an array reference. This is calculated by L<File::Find::Object> and
kept here for convenience and for internal use.

=head1 SEE ALSO

L<File::Find::Object>

=head1 LICENSE

Copyright (C) 2005, 2006 by Olivier Thauvin

This package is free software; you can redistribute it and/or modify it under
the following terms:

1. The GNU General Public License Version 2.0 -
http://www.opensource.org/licenses/gpl-license.php

2. The Artistic License Version 2.0 -
http://www.perlfoundation.org/legal/licenses/artistic-2_0.html

3. At your option - any later version of either or both of these licenses.

=for :stopwords cpan testmatrix url bugtracker rt cpants kwalitee diff irc mailto metadata placeholders metacpan

=head1 SUPPORT

=head2 Websites

The following websites have more information about this module, and may be of help to you. As always,
in addition to those websites please use your favorite search engine to discover more resources.

=over 4

=item *

MetaCPAN

A modern, open-source CPAN search engine, useful to view POD in HTML format.

L<https://metacpan.org/release/File-Find-Object>

=item *

Search CPAN

The default CPAN search engine, useful to view POD in HTML format.

L<http://search.cpan.org/dist/File-Find-Object>

=item *

RT: CPAN's Bug Tracker

The RT ( Request Tracker ) website is the default bug/issue tracking system for CPAN.

L<https://rt.cpan.org/Public/Dist/Display.html?Name=File-Find-Object>

=item *

CPAN Ratings

The CPAN Ratings is a website that allows community ratings and reviews of Perl modules.

L<http://cpanratings.perl.org/d/File-Find-Object>

=item *

CPANTS

The CPANTS is a website that analyzes the Kwalitee ( code metrics ) of a distribution.

L<http://cpants.cpanauthors.org/dist/File-Find-Object>

=item *

CPAN Testers

The CPAN Testers is a network of smoke testers who run automated tests on uploaded CPAN distributions.

L<http://www.cpantesters.org/distro/F/File-Find-Object>

=item *

CPAN Testers Matrix

The CPAN Testers Matrix is a website that provides a visual overview of the test results for a distribution on various Perls/platforms.

L<http://matrix.cpantesters.org/?dist=File-Find-Object>

=item *

CPAN Testers Dependencies

The CPAN Testers Dependencies is a website that shows a chart of the test results of all dependencies for a distribution.

L<http://deps.cpantesters.org/?module=File::Find::Object>

=back

=head2 Bugs / Feature Requests

Please report any bugs or feature requests by email to C<bug-file-find-object at rt.cpan.org>, or through
the web interface at L<https://rt.cpan.org/Public/Bug/Report.html?Queue=File-Find-Object>. You will be automatically notified of any
progress on the request by the system.

=head2 Source Code

The code is open to the world, and available for you to hack on. Please feel free to browse it and play
with it, or whatever. If you want to contribute patches, please send me a diff or prod me to pull
from your repository :)

L<https://github.com/shlomif/perl-file-find-object>

  git clone git://github.com/shlomif/perl-file-find-object.git

=head1 AUTHOR

Shlomi Fish <shlomif@cpan.org>

=head1 BUGS

Please report any bugs or feature requests on the bugtracker website
L<https://github.com/shlomif/perl-file-find-object/issues>

When submitting a bug or request, please include a test-file or a
patch to an existing test-file that illustrates the bug or desired
feature.

=head1 COPYRIGHT AND LICENSE

This software is Copyright (c) 2000 by Olivier Thauvin and others.

This is free software, licensed under:

  The Artistic License 2.0 (GPL Compatible)

=cut
