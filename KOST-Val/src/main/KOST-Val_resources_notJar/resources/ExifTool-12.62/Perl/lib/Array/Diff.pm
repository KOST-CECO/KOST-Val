package Array::Diff;
$Array::Diff::VERSION = '0.09';
use strict;
use warnings;
use base qw/Class::Accessor::Fast/;

use Algorithm::Diff 1.19;
eval q{ use Algorithm::Diff::XS; };


__PACKAGE__->mk_accessors(qw/added deleted count diff_class/);

=head1 NAME

Array::Diff - Find the differences between two arrays

=head1 SYNOPSIS

    my @old = ( 'a', 'b', 'c' );
    my @new = ( 'b', 'c', 'd' );

    my $diff = Array::Diff->diff( \@old, \@new );

    $diff->count   # 2
    $diff->added   # [ 'd' ];
    $diff->deleted # [ 'a' ];

=head1 DESCRIPTION

This module compares two B<pre-sorted> arrays
and returns the added or deleted elements in two separate arrays.
It's a simple wrapper around L<Algorithm::Diff>.

B<Note>: the arrays must be sorted before you call C<diff>.

And if you need more complex array tools, check L<Array::Compare>.

=head1 METHODS

=over 4

=item new ()

Create a new C<Array::Diff> object.

=cut

sub new {
    my $self = shift->SUPER::new(@_);

    $self->{diff_class} ||= $INC{'Algorithm/Diff/XS.pm'} ? 'Algorithm::Diff::XS' : 'Algorithm::Diff';

    $self;
}

=item diff ( OLD, NEW )

Compute the differences between two arrays.  The results are stored
in the C<added>, C<deleted>, and C<count> properties that may be
examined using the corresponding methods.

This method may be invoked as an object method, in which case it will
recalculate the differences and repopulate the C<count>, C<added>, and
C<removed> properties, or as a static method, in which case it will
return a newly-created C<Array::Diff> object with the properties
set appropriately.

=cut

sub diff {
    my ( $self, $old, $new ) = @_;
    $self = $self->new unless ref $self;

    $self->added(   [] );
    $self->deleted( [] );
    $self->count( 0 );

    my $diff = $self->diff_class->new( $old, $new );
    while ( $diff->Next ) {
        next if $diff->Same;

        my @deleted = $diff->Items(1);
        my @added = $diff->Items(2);

        $self->{count} += @added + @deleted;
        push @{$self->{deleted}}, @deleted if @deleted;
        push @{$self->{added}}, @added if @added;
    }

    $self;
}

=item added ( [VALUES ] )

Get or set the elements present in the C<NEW> array and absent in
the C<OLD> one at the comparison performed by the last C<diff()>
invocation.

=item deleted ( [VALUES] )

Get or set the elements present in the C<OLD> array and absent in
the C<NEW> one at the comparison performed by the last C<diff()>
invocation.

=item count ( [VALUE] )

Get or set the total number of added or deleted elements at
the comparison performed by the last C<diff()> invocation.
This count should be equal to the sum of the number of elements in
the C<added> and C<deleted> properties.

=back

=head1 SEE ALSO

L<Array::Compare> - performs the same function as this module,
but has options for controlling how it works.

L<List::Compare> - similar functionality, but again with more options.

L<Algorithm::Diff> - the underlying implementation of the diff algorithm.
If you've got L<Algorithm::Diff::XS> installed, that will be used.

L<YAML::Diff> - find difference between two YAML documents.

L<HTML::Differences> - find difference between two HTML documents.
This uses a more sane approach than L<HTML::Diff>.

L<XML::Diff> - find difference between two XML documents.

L<Hash::Diff> - find the differences between two Perl hashes.

L<Data::Diff> - find difference between two arbitrary data structures.

L<Text::Diff> - can find difference between two inputs, which can be
data structures or file names.

=head1 AUTHOR

Daisuke Murase <typester@cpan.org>

=head1 COPYRIGHT AND LICENSE

Copyright (c) 2009 by Daisuke Murase.

This program is free software; you can redistribute
it and/or modify it under the same terms as Perl itself.

The full text of the license can be found in the
LICENSE file included with this module.

=cut

1;
