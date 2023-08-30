package File::Find::Object::PathComp;
$File::Find::Object::PathComp::VERSION = '0.3.5';
use strict;
use warnings;

use integer;

use parent 'File::Find::Object::Base';

use Class::XSAccessor accessors => {
    (
        map { $_ => $_ } (
            qw(
                _actions
                _curr_file
                _files
                _last_dir_scanned
                _open_dir_ret
                _stat_ret
                _traverse_to
                )
        )
    )
    },
    getters => { _inodes     => '_inodes' },
    setters => { _set_inodes => '_inodes' },
    ;

use File::Spec;

__PACKAGE__->_make_copy_methods(
    [
        qw(
            _files
            _traverse_to
            )
    ]
);

sub _dev
{
    return shift->_stat_ret->[0];
}

sub _inode
{
    return shift->_stat_ret->[1];
}

sub _is_same_inode
{
    my $self = shift;

    # $st is an array ref with the return of perldoc -f stat .
    my $st = shift;

    # On MS-Windows, all inodes in stat are returned as 0, so we need to
    # check that both inodes are not zero. This is why there's the
    # $self->_inode() != 0 check at the end.
    return (   $self->_dev() == $st->[0]
            && $self->_inode() == $st->[1]
            && $self->_inode() != 0 );
}

sub _should_scan_dir
{
    my $self    = shift;
    my $dir_str = shift;

    if ( defined( $self->_last_dir_scanned() )
        && ( $self->_last_dir_scanned() eq $dir_str ) )
    {
        return;
    }
    else
    {
        $self->_last_dir_scanned($dir_str);
        return 1;
    }
}

sub _set_up_dir
{
    my $self    = shift;
    my $dir_str = shift;

    $self->_files( $self->_calc_dir_files($dir_str) );

    $self->_traverse_to( $self->_files_copy() );

    return $self->_open_dir_ret(1);
}

sub _calc_dir_files
{
    my $self    = shift;
    my $dir_str = shift;

    my $handle;
    my @files;
    if ( !opendir( $handle, $dir_str ) )
    {
        # Handle this error gracefully.
    }
    else
    {
        @files =
            ( sort { $a cmp $b } File::Spec->no_upwards( readdir($handle) ) );
        closedir($handle);
    }

    return \@files;
}

sub _component_open_dir
{
    my $self    = shift;
    my $dir_str = shift;

    if ( !$self->_should_scan_dir($dir_str) )
    {
        return $self->_open_dir_ret();
    }

    return $self->_set_up_dir($dir_str);
}

sub _next_traverse_to
{
    my $self = shift;

    return shift( @{ $self->_traverse_to() } );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

File::Find::Object::PathComp - base class for File::Find::Object's Path Components

=head1 VERSION

version 0.3.5

=head1 DESCRIPTION

This is the base class for F::F::O's path components. It only defines some
accessors, and is for File::Find::Object's internal use.

=head1 METHODS

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
