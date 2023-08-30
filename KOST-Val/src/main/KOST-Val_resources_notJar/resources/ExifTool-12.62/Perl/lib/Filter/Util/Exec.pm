package Filter::Util::Exec ;

require 5.006 ;
require XSLoader;
our $VERSION = "1.60" ;

XSLoader::load('Filter::Util::Exec');
1 ;
__END__

=head1 NAME

Filter::Util::Exec - exec source filter

=head1 SYNOPSIS
 
    use Filter::Util::Exec;

=head1 DESCRIPTION

This module is provides the interface to allow the creation of I<Source
Filters> which use a Unix coprocess.

See L<Filter::exec>, L<Filter::cpp> and L<Filter::sh> for examples of
the use of this module.

Note that the size of the buffers is limited to 32-bit.

=head2 B<filter_add()>

The function, C<filter_add> installs a filter. It takes one
parameter which should be a reference. The kind of reference used will
dictate which of the two filter types will be used.

If a CODE reference is used then a I<closure filter> will be assumed.

If a CODE reference is not used, a I<method filter> will be assumed.
In a I<method filter>, the reference can be used to store context
information. The reference will be I<blessed> into the package by
C<filter_add>.

See L<Filter::Util::Call> for examples of using context information
using both I<method filters> and I<closure filters>.

=head1 AUTHOR

Paul Marquess 

=head1 DATE

11th December 1995.

=cut

