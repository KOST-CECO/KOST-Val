package Net::DNS::Text;

use strict;
use warnings;

our $VERSION = (qw$Id: Text.pm 1894 2023-01-12 10:59:08Z willem $)[2];


=head1 NAME

Net::DNS::Text - DNS text representation

=head1 SYNOPSIS

    use Net::DNS::Text;

    $object = Net::DNS::Text->new('example');
    $string = $object->string;

    $object = Net::DNS::Text->decode( \$data, $offset );
    ( $object, $next ) = Net::DNS::Text->decode( \$data, $offset );

    $data = $object->encode;
    $text = $object->value;

=head1 DESCRIPTION

The C<Net::DNS::Text> module implements a class of text objects
with associated class and instance methods.

Each text object instance has a fixed identity throughout its
lifetime.

=cut


use integer;
use Carp;


use constant ASCII => ref eval {
	require Encode;
	Encode::find_encoding('ascii');
};

use constant UTF8 => scalar eval {	## not UTF-EBCDIC  [see Unicode TR#16 3.6]
	Encode::encode_utf8( chr(182) ) eq pack( 'H*', 'C2B6' );
};


=head1 METHODS

=head2 new

    $object = Net::DNS::Text->new('example');

Creates a text object which encapsulates a single character
string component of a resource record.

Arbitrary single-byte characters can be represented by \ followed
by exactly three decimal digits. Such characters are devoid of
any special meaning.

A character preceded by \ represents itself, without any special
interpretation.

=cut

my ( %escape, %escapeUTF8, %unescape );	## precalculated escape tables

sub new {
	my $self = bless [], shift;
	local $_ = &_encode_utf8;

	s/^\042(.*)\042$/$1/s;					# strip paired quotes

	s/\134([\060-\071]{3})/$unescape{$1}/eg;		# restore numeric escapes
	s/\134([^\134])/$1/g;					# restore character escapes
	s/\134\134/\134/g;					# restore escaped escapes

	while ( length $_ > 255 ) {
		my $chunk = substr( $_, 0, 255 );		# carve into chunks
		$chunk =~ s/[\300-\377][\200-\277]*$//;
		push @$self, $chunk;
		substr( $_, 0, length $chunk ) = '';
	}
	push @$self, $_;

	return $self;
}


=head2 decode

    $object = Net::DNS::Text->decode( \$buffer, $offset );

    ( $object, $next ) = Net::DNS::Text->decode( \$buffer, $offset );

Creates a text object which represents the decoded data at the
indicated offset within the data buffer.

The argument list consists of a reference to a scalar containing
the wire-format data and offset of the text data.

The returned offset value indicates the start of the next item in
the data buffer.

=cut

sub decode {
	my $class  = shift;
	my $buffer = shift;					# reference to data buffer
	my $offset = shift || 0;				# offset within buffer
	my $size   = shift;					# specify size of unbounded text

	unless ( defined $size ) {
		$size = unpack "\@$offset C", $$buffer;
		$offset++;
	}

	my $next = $offset + $size;
	croak 'corrupt wire-format data' if $next > length $$buffer;

	my $self = bless [unpack( "\@$offset a$size", $$buffer )], $class;

	return wantarray ? ( $self, $next ) : $self;
}


=head2 encode

    $data = $object->encode;

Returns the wire-format encoded representation of the text object
suitable for inclusion in a DNS packet buffer.

=cut

sub encode {
	my $self = shift;
	return join '', map { pack( 'C a*', length $_, $_ ) } @$self;
}


=head2 raw

    $data = $object->raw;

Returns the wire-format encoded representation of the text object
without the explicit length field.

=cut

sub raw {
	my $self = shift;
	return join '', map { pack( 'a*', $_ ) } @$self;
}


=head2 value

    $value = $text->value;

Character string representation of the text object.

=cut

sub value {
	return unless defined wantarray;
	my $self = shift;
	return _decode_utf8( join '', @$self );
}


=head2 string

    $string = $text->string;

Conditionally quoted RFC1035 zone file representation of the text object.

=cut

sub string {
	my $self = shift;

	my @s = map { split '', $_ } @$self;			# escape special and ASCII non-printable
	my $s = _decode_utf8( join '', map { $escape{$_} } @s );
	return $s =~ /[ \t\n\r\f(),;]|^$/ ? qq("$s") : $s;	# quote special characters and empty string
}


=head2 unicode

    $string = $text->unicode;

Conditionally quoted Unicode representation of the text object.

=cut

sub unicode {
	my $self = shift;

	my @s = map { split '', $_ } @$self;			# escape special and non-printable
	my $s = _decode_utf8( join '', map { $escapeUTF8{$_} } @s );
	return $s =~ /[ \t\n\r\f();]|^$/ ? qq("$s") : $s;	# quote special characters and empty string
}


########################################

# perlcc: address of encoding objects must be determined at runtime
my $ascii = ASCII ? Encode::find_encoding('ascii') : undef;	# Osborn's Law:
my $utf8  = UTF8  ? Encode::find_encoding('utf8')  : undef;	# Variables won't; constants aren't.


sub _decode_utf8 {			## UTF-8 to perl internal encoding
	local $_ = shift;

	# partial transliteration for non-ASCII character encodings
	tr
	[\040-\176\000-\377]
	[ !"#$%&'()*+,\-./0-9:;<=>?@A-Z\[\\\]^_`a-z{|}~?] unless ASCII;

	my $z = length($_) - length($_);			# pre-5.18 taint workaround
	return ASCII ? substr( ( UTF8 ? $utf8 : $ascii )->decode($_), $z ) : $_;
}


sub _encode_utf8 {			## perl internal encoding to UTF-8
	local $_ = shift;
	croak 'argument undefined' unless defined $_;

	# partial transliteration for non-ASCII character encodings
	tr
	[ !"#$%&'()*+,\-./0-9:;<=>?@A-Z\[\\\]^_`a-z{|}~]
	[\040-\176] unless ASCII;

	my $z = length($_) - length($_);			# pre-5.18 taint workaround
	return ASCII ? substr( ( UTF8 ? $utf8 : $ascii )->encode($_), $z ) : $_;
}


%escape = eval {			## precalculated ASCII escape table
	my %table = map { ( chr($_) => chr($_) ) } ( 0 .. 127 );

	foreach my $n ( 0 .. 31, 34, 92, 127 .. 255 ) {		# numerical escape
		my $codepoint = sprintf( '%03u', $n );

		# transliteration for non-ASCII character encodings
		$codepoint =~ tr [0-9] [\060-\071];

		$table{chr($n)} = pack 'C a3', 92, $codepoint;
	}

	return %table;
};

%escapeUTF8 = eval {			## precalculated UTF-8 escape table
	my @octet = UTF8 ? ( 128 .. 191, 194 .. 254 ) : ();
	return ( %escape, map { ( chr($_) => chr($_) ) } @octet );
};


%unescape = eval {			## precalculated numeric escape table
	my %table;

	foreach my $n ( 0 .. 255 ) {
		my $key = sprintf( '%03u', $n );

		# transliteration for non-ASCII character encodings
		$key =~ tr [0-9] [\060-\071];

		$table{$key} = pack 'C', $n;
	}
	$table{"\060\071\062"} = pack 'C2', 92, 92;		# escaped escape

	return %table;
};


1;
__END__


########################################

=head1 BUGS

Coding strategy is intended to avoid creating unnecessary argument
lists and stack frames. This improves efficiency at the expense of
code readability.

Platform specific character coding features are conditionally
compiled into the code.


=head1 COPYRIGHT

Copyright (c)2009-2011 Dick Franks.

All rights reserved.


=head1 LICENSE

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose and without fee is hereby granted, provided
that the original copyright notices appear in all copies and that both
copyright notice and this permission notice appear in supporting
documentation, and that the name of the author not be used in advertising
or publicity pertaining to distribution of the software without specific
prior written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.


=head1 SEE ALSO

L<perl> L<Net::DNS>
L<RFC1035|https://tools.ietf.org/html/rfc1035>
L<RFC3629|https://tools.ietf.org/html/rfc3629>

=cut

