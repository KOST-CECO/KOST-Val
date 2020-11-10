# $Id: MD4.pm,v 1.2 2001/07/30 21:58:13 mikem Exp $
package Digest::MD4;

use strict;
use vars qw($VERSION @ISA @EXPORT);

require Exporter;
require DynaLoader;
require AutoLoader;

@ISA = qw(Exporter AutoLoader DynaLoader);
# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.
@EXPORT = qw(
	
);
$VERSION = '1.1';

bootstrap Digest::MD4 $VERSION;

# Preloaded methods go here.

sub addfile
{
    no strict 'refs';	# Countermand any strct refs in force so that we
			# can still handle file-handle names.

    my ($self, $handle) = @_;
    my ($package, $file, $line) = caller;
    my ($data) = '';

    if (!ref($handle))
    {
	# Old-style passing of filehandle by name. We need to add
	# the calling package scope qualifier, if there is not one
	# supplied already.

	$handle = $package . '::' . $handle unless ($handle =~ /(\:\:|\')/);
    }

    while (read($handle, $data, 1024))
    {
	$self->add($data);
    }
    return $self;
}

sub hexdigest
{
    my ($self) = shift;

    unpack("H*", ($self->digest()));
}

sub hash
{
    my ($self, $data) = @_;

    if (ref($self))
    {
	# This is an instance method call so reset the current context

	$self->reset();
    }
    else
    {
	# This is a static method invocation, create a temporary MD4 context

	$self = new Digest::MD4;
    }

    # Now do the hash

    $self->add($data);
    $self->digest();
}

sub hexhash
{
    my ($self, $data) = @_;

    unpack("H*", ($self->hash($data)));
}

# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__

=head1 NAME

Digest::MD4 - Perl interface to the RSA Data Security Inc. MD4 Message-Digest Algorithm

=head1 SYNOPSIS

    use Digest::MD4;
    
    $context = new Digest::MD4;
    $context->reset();
    
    $context->add(LIST);
    $context->addfile(HANDLE);
    
    $digest = $context->digest();
    $string = $context->hexdigest();

    $digest = Digest::MD4->hash(SCALAR);
    $string = Digest::MD4->hexhash(SCALAR);

=head1 DESCRIPTION

The B<Digest::MD4> module allows you to use the RSA Data Security Inc. MD4
Message Digest algorithm from within Perl programs.

A new MD4 context object is created with the B<new> operation.
Multiple simultaneous digest contexts can be maintained, if desired.
The context is updated with the B<add> operation which adds the
strings contained in the I<LIST> parameter. Note, however, that
C<add('foo', 'bar')>, C<add('foo')> followed by C<add('bar')> and
C<add('foobar')> should all give the same result.

The final message digest value is returned by the B<digest> operation
as a 16-byte binary string. This operation delivers the result of
B<add> operations since the last B<new> or B<reset> operation. Note
that the B<digest> operation is effectively a destructive, read-once
operation. Once it has been performed, the context must be B<reset>
before being used to calculate another digest value.

Several convenience functions are also provided. The B<addfile>
operation takes an open file-handle and reads it until end-of file in
1024 byte blocks adding the contents to the context. The file-handle
can either be specified by name or passed as a type-glob reference, as
shown in the examples below. The B<hexdigest> operation calls
B<digest> and returns the result as a printable string of hexdecimal
digits. This is exactly the same operation as performed by the
B<unpack> operation in the examples below.

The B<hash> operation can act as either a static member function (ie
you invoke it on the MD4 class as in the synopsis above) or as a
normal virtual function. In both cases it performs the complete MD4
cycle (reset, add, digest) on the supplied scalar value. This is
convenient for handling small quantities of data. When invoked on the
class a temporary context is created. When invoked through an already
created context object, this context is used. The latter form is
slightly more efficient. The B<hexhash> operation is analogous to
B<hexdigest>.

=head1 EXAMPLES

    use Digest::MD4;
    
    $md4 = new Digest::MD4;
    $md4->add('foo', 'bar');
    $md4->add('baz');
    $digest = $md4->digest();
    
    print("Digest is " . unpack("H*", $digest) . "\n");

The above example would print out the message

    Digest is 6df23dc03f9b54cc38a0fc1483df6e21

provided that the implementation is working correctly.

Remembering the Perl motto ("There's more than one way to do it"), the
following should all give the same result:

    use Digest::MD4;
    $md4 = new Digest::MD4;

    die "Can't open /etc/passwd ($!)\n" unless open(P, "/etc/passwd");

    seek(P, 0, 0);
    $md4->reset;
    $md4->addfile(P);
    $d = $md4->hexdigest;
    print "addfile (handle name) = $d\n";

    seek(P, 0, 0);
    $md4->reset;
    $md4->addfile(\*P);
    $d = $md4->hexdigest;
    print "addfile (type-glob reference) = $d\n";

    seek(P, 0, 0);
    $md4->reset;
    while (<P>)
    {
        $md4->add($_);
    }
    $d = $md4->hexdigest;
    print "Line at a time = $d\n";

    seek(P, 0, 0);
    $md4->reset;
    $md4->add(<P>);
    $d = $md4->hexdigest;
    print "All lines at once = $d\n";

    seek(P, 0, 0);
    $md4->reset;
    while (read(P, $data, (rand % 128) + 1))
    {
        $md4->add($data);
    }
    $d = $md4->hexdigest;
    print "Random chunks = $d\n";

    seek(P, 0, 0);
    $md4->reset;
    undef $/;
    $data = <P>;
    $d = $md4->hexhash($data);
    print "Single string = $d\n";

    close(P);

=head1 NOTE

The MD4 extension may be redistributed under the same terms as Perl.
The MD4 algorithm is defined in RFC1320. The basic C code implementing
the algorithm is derived from that in the RFC and is covered by the
following copyright:

=over 8

   Copyright (C) 1990-2, RSA Data Security, Inc. All rights reserved.

   License to copy and use this software is granted provided that it
   is identified as the "RSA Data Security, Inc. MD4 Message-Digest
   Algorithm" in all material mentioning or referencing this software
   or this function.

   License is also granted to make and use derivative works provided
   that such works are identified as "derived from the RSA Data
   Security, Inc. MD4 Message-Digest Algorithm" in all material
   mentioning or referencing the derived work.

   RSA Data Security, Inc. makes no representations concerning either
   the merchantability of this software or the suitability of this
   software for any particular purpose. It is provided "as is"
   without express or implied warranty of any kind.

   These notices must be retained in any copies of any part of this
   documentation and/or software.

=back

This copyright does not prohibit distribution of any version of Perl
containing this extension under the terms of the GNU or Artistic
licences.

=head1 AUTHOR

The MD4 interface was adapted by Mike McCauley
(C<mikem@open.com.au>), based entirely on MD5-1.7, written by Neil Winton
(C<N.Winton@axion.bt.co.uk>).

=head1 SEE ALSO

perl(1).

=cut
