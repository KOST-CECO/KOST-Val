package Unicode::String;

# Copyright 1997-1999, Gisle Aas.

use strict;
use vars qw($VERSION @ISA @EXPORT_OK $UTF7_OPTIONAL_DIRECT_CHARS);
use Carp;

require Exporter;
require DynaLoader;
@ISA = qw(Exporter DynaLoader);

@EXPORT_OK = qw(utf16 utf8 utf7 ucs2 ucs4 latin1 uchr uhex byteswap2 byteswap4);

$VERSION = '2.07'; # $Id: String.pm,v 1.27 2000/05/19 12:00:45 gisle Exp $

$UTF7_OPTIONAL_DIRECT_CHARS ||= 1;

bootstrap Unicode::String $VERSION;

use overload '""'   => \&as_string,
	     'bool' => \&as_bool,
	     '0+'   => \&as_num,
	     '.='   => \&append,
             '.'    => \&concat,
             'x'    => \&repeat,
	     '='    => \&copy,
             'fallback' => 1;

my %stringify = (
   unicode => \&utf16,
   utf16   => \&utf16,
   ucs2    => \&utf16,
   utf8    => \&utf8,
   utf7    => \&utf7,
   ucs4    => \&ucs4,
   latin1  => \&latin1,
  'hex'    => \&hex,
);

my $stringify_as = \&utf8;

# some aliases
*ucs2 = \&utf16;
*uhex = \&hex;
*uchr = \&chr;

sub new
{
    #_dump_arg("new", @_);
    my $class = shift;
    my $str;
    my $self = bless \$str, $class;
    &$stringify_as($self, shift) if @_;
    $self;
}


sub repeat
{
    my($self, $count) = @_;
    my $class = ref($self);
    my $str = $$self x $count;
    bless \$str, $class;
}


sub _dump_arg
{
    my $func = shift;
    print "$func(";
    print join(",", map { if (defined $_) {
                             my $x = overload::StrVal($_);
			     $x =~ s/\n/\\n/g;
			     $x = '""' unless length $x;
			     $x;
			 } else {
			     "undef"
			 }
                        } @_);
    print ")\n";
}


sub concat
{
    #_dump_arg("concat", @_);
    my($self, $other, $reversed) = @_;
    my $class = ref($self);
    unless (UNIVERSAL::isa($other, 'Unicode::String')) {
	$other = Unicode::String->new($other);
    }
    my $str = $reversed ? $$other . $$self : $$self . $$other;
    bless \$str, $class;
}


sub append
{
    #_dump_arg("append", @_);
    my($self, $other) = @_;
    unless (UNIVERSAL::isa($other, 'Unicode::String')) {
	$other = Unicode::String->new($other);
    }
    $$self .= $$other;
    $self;
}


sub copy
{
    my($self) = @_;
    my $class = ref($self);
    my $copy = $$self;
    bless \$copy, $class;
}


sub as_string
{
    #_dump_arg("as_string", @_);
    &$stringify_as($_[0]);
}


sub as_bool
{
    # This is different from perl's normal behaviour by not letting
    # a U+0030  ("0") be false.
    my $self = shift;
    $$self ? 1 : "";
}


sub as_num
{
    # Should be able to use the numeric property from Unidata
    # in order to parse a large number of numbers.  Currently we
    # only convert it to a plain string and let perl's normal
    # num-converter do the job.
    my $self = shift;
    my $str = $self->utf8;
    $str + 0;
}


sub stringify_as
{
    my $class;
    if (@_ > 1) {
	$class = shift;
	$class = ref($class) if ref($class);
    } else {
	$class = "Unicode::String";
    }
    my $old = $stringify_as;
    if (@_) {
	my $as = shift;
	croak("Don't know how to stringify as '$as'")
	    unless exists $stringify{$as};
	$stringify_as = $stringify{$as};
    }
    $old;
}


sub utf16
{
    my $self = shift;
    unless (ref $self) {
	my $u = new Unicode::String;
	$u->utf16($self);
	return $u;
    }
    my $old = $$self;
    if (@_) {
	$$self = shift;
	if ((length($$self) % 2) != 0) {
	    warn "Uneven UTF16 data" if $^W;
	    $$self .= "\0";
	}
	if ($$self =~ /^\xFF\xFE/) {
	    # the string needs byte swapping
	    $$self = pack("n*", unpack("v*", $$self));
	}
    }
    $old;
}



sub utf7   # rfc1642
{
    my $self = shift;
    unless (ref $self) {
	# act as ctor
	my $u = new Unicode::String;
	$u->utf7($self);
	return $u;
    }
    my $old;
    if (defined wantarray) {
	# encode into $old
	$old = "";
	pos($$self) = 0;
	my $len = length($$self);
	while (pos($$self) < $len) {
            if (($UTF7_OPTIONAL_DIRECT_CHARS &&
		 $$self =~ /\G((?:\0[A-Za-z0-9\'\(\)\,\-\.\/\:\?\!\"\#\$\%\&\*\;\<\=\>\@\[\]\^\_\`\{\|\}\s])+)/gc)
	        || $$self =~ /\G((?:\0[A-Za-z0-9\'\(\)\,\-\.\/\:\?\s])+)/gc)
            {
		#print "Plain ", utf16($1)->latin1, "\n";
		$old .= utf16($1)->latin1;
	    }
            elsif (($UTF7_OPTIONAL_DIRECT_CHARS &&
                    $$self =~ /\G((?:[^\0].|\0[^A-Za-z0-9\'\(\)\,\-\.\/\:\?\!\"\#\$\%\&\*\;\<\=\>\@\[\]\^\_\`\{\|\}\s])+)/gsc)
                   || $$self =~ /\G((?:[^\0].|\0[^A-Za-z0-9\'\(\)\,\-\.\/\:\?\s])+)/gsc)
            {
		#print "Unplain ", utf16($1)->hex, "\n";
		if ($1 eq "\0+") {
		    $old .= "+-";
		} else {
		    require MIME::Base64;
		    my $base64 = MIME::Base64::encode($1, '');
		    $base64 =~ s/=+$//;
		    $old .= "+$base64-";
		    # XXX should we determine when the final "-" is
		    # unnecessary? depends on next char not being part
		    # of the base64 char set.
		}
	    } else {
		die "This should not happen, pos=" . pos($$self) .
                                            ":  "  . $self->hex . "\n";
	    }
	}
    }
    
    if (@_) {
	# decode
	my $len = length($_[0]);
	$$self = "";
	while (pos($_[0]) < $len) {
	    if ($_[0] =~ /\G([^+]+)/gc) {
		$self->append(latin1($1));
	    } elsif ($_[0] =~ /\G\+-/gc) {
		$$self .= "\0+";
	    } elsif ($_[0] =~ /\G\+([A-Za-z0-9+\/]+)-?/gc) {
		my $base64 = $1;
		my $pad = length($base64) % 4;
		$base64 .= "=" x (4 - $pad) if $pad;
		require MIME::Base64;
		$$self .= MIME::Base64::decode($base64);
		if ((length($$self) % 2) != 0) {
		    warn "Uneven UTF7 base64-data" if $^W;
		    chop($$self); # correct it
		}
            } elsif ($_[0] =~ /\G\+/gc) {
		warn "Bad UTF7 data escape" if $^W;
		$$self .= "\0+";
	    } else {
		die "This should not happen " . pos($_[0]);
	    }
	}
    }
    $old;
}


sub hex
{
    my $self = shift;
    unless (ref $self) {
	my $u = new Unicode::String;
	$u->hex($self);
	return $u;
    }
    my $old;
    if (defined($$self) && defined wantarray) {
	$old = unpack("H*", $$self);
	$old =~ s/(....)/U+$1 /g;
	$old =~ s/\s+$//;
    }
    if (@_) {
	my $new = shift;
	$new =~ tr/0-9A-Fa-f//cd;  # leave only hex chars
	croak("Hex string length must be multiple of four")
	    unless (length($new) % 4) == 0;
	$$self = pack("H*", $new);
    }
    $old;
}


sub length
{
    my $self = shift;
    int(length($$self) / 2);
}

sub byteswap
{
   my $self = shift;
   byteswap2($$self);
   $self;
}

sub unpack
{
    my $self = shift;
    unpack("n*", $$self)
}


sub pack
{
    my $self = shift;
    $$self = pack("n*", @_);
    $self;
}


sub ord
{
    my $self = shift;
    return () unless defined $$self;

    my $array = wantarray;
    my @ret;
    my @chars;
    if ($array) {
        @chars = CORE::unpack("n*", $$self);
    } else {
	@chars = CORE::unpack("n2", $$self);
    }

    while (@chars) {
	my $first = shift(@chars);
	if ($first >= 0xD800 && $first <= 0xDFFF) { 	# surrogate
	    my $second = shift(@chars);
	    #print "F=$first S=$second\n";
	    if ($first >= 0xDC00 || $second < 0xDC00 || $second > 0xDFFF) {
		carp(sprintf("Bad surrogate pair (U+%04x U+%04x)",
			     $first, $second));
		unshift(@chars, $second);
		next;
	    }
	    push(@ret, ($first-0xD800)*0x400 + ($second-0xDC00) + 0x10000);
	} else {
	    push(@ret, $first);
	}
	last unless $array;
    }
    $array ? @ret : $ret[0];
}


sub name
{
    my $self = shift;
    require Unicode::CharName;
    if (wantarray) {
	return map { Unicode::CharName::uname($_) } $self->ord;
    } else {
        return Unicode::CharName::uname(scalar($self->ord));
    }
}


sub chr
{
    my($self,$val) = @_;
    unless (ref $self) {
	# act as ctor
	my $u = new Unicode::String;
	return $u->uchr($self);
    }
    if ($val > 0xFFFF) {
	# must be represented by a surrogate pair
	return undef if $val > 0x10FFFF;  # Unicode limit
	$val -= 0x10000;
	my $h = int($val / 0x400) + 0xD800;
	my $l = ($val % 0x400) + 0xDC00;
	$$self = CORE::pack("n2", $h, $l);
    } else {
	$$self = CORE::pack("n", $val);
    }
    $self;
}


sub substr
{
    my($self, $offset, $length, $substitute) = @_;
    $offset ||= 0;
    $offset *= 2;
    my $substr;
    if (defined $substitute) {
	unless (UNIVERSAL::isa($substitute, 'Unicode::String')) {
	    $substitute = Unicode::String->new($substitute);
	}
	if (defined $length) {
	    $substr = substr($$self, $offset, $length*2) = $$substitute;
	} else {
	    $substr = substr($$self, $offset) = $$substitute;
	}
    } else {
	if (defined $length) {
	    $substr = substr($$self, $offset, $length*2);
	} else {
	    $substr = substr($$self, $offset);
	}
    }
    bless \$substr, ref($self);
}


sub index
{
    my($self, $other, $pos) = @_;
    $pos ||= 0;
    $pos *= 2;
    $other = Unicode::String->new($other) unless ref($other);
    $pos++ while ($pos = index($$self, $$other, $pos)) > 0 && ($pos%2) != 0;
    $pos /= 2 if $pos > 0;
    $pos;
}


sub rindex
{
    my($self, $other, $pos) = @_;
    $pos ||= 0;
    die "NYI";
}


sub chop
{
    my $self = shift;
    if (CORE::length $$self) {
	my $chop = chop($$self);
	$chop = chop($$self) . $chop;
	return bless \$chop, ref($self);
    }
    undef;
}


# XXX: Ideas to be implemented
sub scan;
sub reverse;

sub lc;
sub lcfirst;
sub uc;
sub ucfirst;

sub split;
sub sprintf;
sub study;
sub tr;


1;

__END__

=head1 NAME

Unicode::String - String of Unicode characters (UCS2/UTF16)

=head1 SYNOPSIS

 use Unicode::String qw(utf8 latin1 utf16);
 $u = utf8("The Unicode Standard is a fixed-width, uniform ");
 $u .= utf8("encoding scheme for written characters and text");

 # convert to various external formats
 print $u->ucs4;      # 4 byte characters
 print $u->utf16;     # 2 byte characters + surrogates
 print $u->utf8;      # 1-4 byte characters
 print $u->utf7;      # 7-bit clean format
 print $u->latin1;    # lossy
 print $u->hex;       # a hexadecimal string

 # all these can be used to set string value or as constructor
 $u->latin1("Å være eller å ikke være");
 $u = utf16("\0Å\0 \0v\0æ\0r\0e");

 # string operations
 $u2 = $u->copy;
 $u->append($u2);
 $u->repeat(2);
 $u->chop;

 $u->length;
 $u->index($other);
 $u->index($other, $pos);

 $u->substr($offset);
 $u->substr($offset, $length);
 $u->substr($offset, $length, $substitute);

 # overloading
 $u .= "more";
 $u = $u x 100;
 print "$u\n";

 # string <--> array of numbers
 @array = $u->unpack;
 $u->pack(@array);

 # misc
 $u->ord;
 $u = uchr($num);

=head1 DESCRIPTION

A I<Unicode::String> object represents a sequence of Unicode
characters.  The Unicode Standard is a fixed-width, uniform encoding
scheme for written characters and text.  This encoding treats
alphabetic characters, ideographic characters, and symbols
identically, which means that they can be used in any mixture and with
equal facility.  Unicode is modeled on the ASCII character set, but
uses a 16-bit encoding to support full multilingual text.

Internally a I<Unicode::String> object is a string of 2 byte values in
network byte order (big-endian).  The class provide various methods to
convert from and to various external formats, and all string
manipulations are made on strings in this the internal 16-bit format.

The functions utf16(), utf8(), utf7(), ucs2(), ucs4(), latin1(),
uchr() can be imported from the I<Unicode::String> module and will
work as constructors initializing strings of the corresponding
encoding.  The ucs2() and utf16() are really aliases for the same
function.

The I<Unicode::String> objects overload various operators, so they
will normally work like plain 8-bit strings in Perl.  This includes
conversions to strings, numbers and booleans as well as assignment,
concatenation and repetition.

=head1 METHODS

The following methods are available:

=over 4

=item Unicode::String->stringify_as( [$enc] )

This class method specify which encoding will be used when
I<Unicode::String> objects are implicitly converted to and from plain
strings.  It define which encoding to assume for the argument of the
I<Unicode::String> constructor new().  Without an encoding argument,
stringify_as() returns the current encoding ctor function.  The
encoding argument ($enc) is a string with one of the following values:
"ucs4", "ucs2", "utf16", "utf8", "utf7", "latin1", "hex".  The default
is "utf8".

=item $us = Unicode::String->new( [$initial_value] )

This is the customary object constructor.  Without argument, it
creates an empty I<Unicode::String> object.  If an $initial_value
argument is given, it is decoded according to the specified
stringify_as() encoding and used to initialize the newly created
object.

Normally you create I<Unicode::String> objects by importing some of
the encoding methods below as functions into your namespace and
calling them with an appropriate encoded argument.

=item $us->ucs4( [$newval] )

The UCS-4 encoding use 32 bits per character.  The main benefit of this
encoding is that you don't have to deal with surrogate pairs.  Encoded
as a Perl string we use 4-bytes in network byte order for each
character.

The ucs4() method always return the old value of $us and if given an
argument decodes the UCS-4 string and set this as the new value of $us.
The characters in $newval must be in the range 0x0 .. 0x10FFFF.
Characters outside this range is ignored.

=item $us->ucs2( [$newval] )

=item $us->utf16( [$newval] )

The ucs2() and utf16() are really just different names for the same
method.  The UCS-2 encoding use 16 bits per character.  The UTF-16
encoding is identical to UCS-2, but includes the use of surrogate
pairs.  Surrogates make it possible to encode characters in the range
0x010000 .. 0x10FFFF with the use of two consecutive 16-bit chars.
Encoded as a Perl string we use 2-bytes in network byte order for each
character (or surrogate code).

The ucs2() method always return the old value of $us and if given an
argument set this as the new value of $us.

=item $us->utf8( [$newval] )

The UTF-8 encoding use 8-bit for the encoding of characters in the
range 0x0 .. 0x7F, 16-bit for the encoding of characters in the range
0x80 .. 0x7FF, 24-bit for the encoding of characters in the range
0x800 .. 0xFFFF and 32-bit for characters in the range 0x01000
.. 0x10FFFF.  Americans like this encoding, because plain US-ASCII
characters are still US-ASCII.  Another benefit is that the character
'\0' only occurs as the encoding of 0x0, thus the normal
NUL-terminated strings (popular in the C programming language) can
still be used.

The utf8() method always return the old value of $us encoded using
UTF-8 and if given an argument decodes the UTF-8 string and set this as
the new value of $us.

=item $us->utf7( [$newval] )

The UTF-7 encoding only use plain US-ASCII characters for the
encoding.  This makes it safe for transport through 8-bit stripping
protocols.  Characters outside the US-ASCII range are base64-encoded
and '+' is used as an escape character.  The UTF-7 encoding is
described in RFC1642.

The utf7() method always return the old value of $us encoded using
UTF-7 and if given an argument decodes the UTF-7 string and set this as
the new value of $us.

If the (global) variable $Unicode::String::UTF7_OPTIONAL_DIRECT_CHARS
is TRUE, then a wider range of characters are encoded as themselves.
It is even TRUE by default.  The characters affected by this are:

   ! " # $ % & * ; < = > @ [ ] ^ _ ` { | }

=item $us->latin1( [$newval] )

The first 256 codes of Unicode is identical to the ISO-8859-1 8-bit
encoding, also known as Latin-1.  The latin1() method always return
the old value of $us and if given an argument set this as the new
value of $us.  Characters outside the 0x0 .. 0xFF range are ignored
when returning a Latin-1 string.  If you want more control over the
mapping from Unicode to Latin-1, use the I<Unicode::Map8> class.  This
is also the way to deal with other 8-bit character sets.

=item $us->hex( [$newval] )

This method() return a plain ASCII string where each Unicode character
is represented by the "U+XXXX" string and separated by a single space
character.  This format can also be used to set the value of $us (in
which case the "U+" is optional).

=item $us->as_string;

Converts a I<Unicode::String> to a plain string according to the
setting of stringify_as().  The default stringify_as() method is
"utf8".

=item $us->as_num;

Converts a I<Unicode::String> to a number.  Currently only the digits
in the range 0x30 .. 0x39 are recognized.  The plan is to eventually
support all Unicode digit characters.

=item $us->as_bool;

Converts a I<Unicode::String> to a boolean value.  Only the empty
string is FALSE.  A string consisting of only the character U+0030 is
considered TRUE, even if Perl consider "0" to be FALSE.

=item $us->repeat( $count );

Returns a new I<Unicode::String> where the content of $us is repeated
$count times.  This operation is also overloaded as:

  $us x $count

=item $us->concat( $other_string );

Concatenates the string $us and the string $other_string.  If
$other_string is not an I<Unicode::String> object, then it is first
passed to the Unicode::String->new constructor function.  This
operation is also overloaded as:

  $us . $other_string


=item $us->append( $other_string );

Appends the string $other_string to the value of $us.  If
$other_string is not an I<Unicode::String> object, then it is first
passed to the Unicode::String->new constructor function.  This
operation is also overloaded as:

  $us .= $other_string

=item $us->copy;

Returns a copy of the current I<Unicode::String> object.  This
operation is overloaded as the assignment operator.

=item $us->length;

Returns the length of the I<Unicode::String>.  Surrogate pairs are
still counted as 2.

=item $us->byteswap;

This method will swap the bytes in the internal representation of the
I<Unicode::String> object.

Unicode reserve the character U+FEFF character as a byte order mark.
This works because the swapped character, U+FFFE, is reserved to not
be valid.  For strings that have the byte order mark as the first
character, we can guaranty to get the byte order right with the
following code:

   $ustr->byteswap if $ustr->ord == 0xFFFE;

=item $us->unpack;

Returns a list of integers each representing an UTF-16 character code.

=item $us->pack( @uchr );

Sets the value of $us as a sequence of UTF-16 characters with the
characters codes given as parameter.

=item $us->ord;

Returns the character code of the first character in $us.  The ord()
method deals with surrogate pairs, which gives us a result-range of
0x0 .. 0x10FFFF.  If the $us string is empty, undef is returned.

=item $us->chr( $code );

Sets the value of $us to be a string containing the character assigned
code $code.  The argument $code must be an integer in the range 0x0
.. 0x10FFFF.  If the code is greater than 0xFFFF then a surrogate pair
created.

=item $us->name

In scalar context returns the official Unicode name of the first
character in $us.  In array context returns the name of all characters
in $us.  Also see L<Unicode::CharName>.

=item $us->substr( $offset, [$length, [$subst]] )

Returns a sub-string of $us.  Works similar to the builtin substr
function, but because we can't make LVALUE subs yet, you have to pass
the string you want to assign to the sub-string as the 3rd parameter.

=item $us->index( $other, [$pos] );

Locates the position of $other within $us, possibly starting the
search at position $pos.

=item $us->chop;

Chops off the last character of $us and returns it (as a
I<Unicode::String> object).

=back

=head1 FUNCTIONS

The following utility functions are provided.  They will be exported
on request.

=over 4

=item byteswap2($str, ...)

This function will swap 2 and 2 bytes in the strings passed as
arguments.  This can be used to fix up UTF-16 or UCS-2 strings from
litle-endian systems.  If this function is called in void context,
then it will modify its arguments in-place.  Otherwise, then swapped
strings are returned.

=item byteswap4($str, ...)

The byteswap4 function works similar to byteswap2, but will reverse
the order of 4 and 4 bytes.  Can be used to fix litle-endian UCS-4
strings.

=back

=head1 SEE ALSO

L<Unicode::CharName>,
L<Unicode::Map8>,
http://www.unicode.org/

=head1 COPYRIGHT

Copyright 1997-2000 Gisle Aas.

This library is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=cut


#
# Some old code that is not used any more (because the methods are
# now implemented as XS) and which I did not want to throw away yet.
#

sub ucs4_inperl
{
    my $self = shift;
    unless (ref $self) {
	my $u = new Unicode::String;
	$u->ucs4($self);
	return $u;
    }
    my $old = pack("N*", $self->ord);
    if (@_) {
	$$self = "";
	for (unpack("N*", shift)) {
	    $self->append(uchr($_));
	}
    }
    $old;
}


sub utf8_inperl
{
    my $self = shift;
    unless (ref $self) {
	# act as ctor
	my $u = new Unicode::String;
	$u->utf8($self);
	return $u;
    }

    my $old;
    if (defined($$self) && defined wantarray) {
	# encode UTF-8
	my $uc;
	for $uc (unpack("n*", $$self)) {
	    if ($uc < 0x80) {
		# 1 byte representation
		$old .= chr($uc);
	    } elsif ($uc < 0x800) {
		# 2 byte representation
		$old .= chr(0xC0 | ($uc >> 6)) .
                        chr(0x80 | ($uc & 0x3F));
	    } else {
		# 3 byte representation
		$old .= chr(0xE0 | ($uc >> 12)) .
		        chr(0x80 | (($uc >> 6) & 0x3F)) .
			chr(0x80 | ($uc & 0x3F));
	    }
	}
    }

    if (@_) {
	if (defined $_[0]) {
	    $$self = "";
	    my $bytes = shift;
	    $bytes =~ s/^[\200-\277]+//;  # can't start with 10xxxxxx
	    while (length $bytes) {
		if ($bytes =~ s/^([\000-\177]+)//) {
		    $$self .= pack("n*", unpack("C*", $1));
		} elsif ($bytes =~ s/^([\300-\337])([\200-\277])//) {
		    my($b1,$b2) = (ord($1), ord($2));
		    $$self .= pack("n", (($b1 & 0x1F) << 6) | ($b2 & 0x3F));
		} elsif ($bytes =~ s/^([\340-\357])([\200-\277])([\200-\277])//) {
		    my($b1,$b2,$b3) = (ord($1), ord($2), ord($3));
		    $$self .= pack("n", (($b1 & 0x0F) << 12) |
                                        (($b2 & 0x3F) <<  6) |
				         ($b3 & 0x3F));
		} else {
		    croak "Bad UTF-8 data";
		}
	    }
	} else {
	    $$self = undef;
	}
    }

    $old;
}




sub latin1_inperl
{
    my $self = shift;
    unless (ref $self) {
	# act as ctor
	my $u = new Unicode::String;
	$u->latin1($self);
	return $u;
    }

    my $old;
    # XXX: should really check that none of the chars > 256
    $old = pack("C*", unpack("n*", $$self)) if defined $$self;

    if (@_) {
	# set the value
	if (defined $_[0]) {
	    $$self = pack("n*", unpack("C*", $_[0]));
	} else {
	    $$self = undef;
	}
    }
    $old;
}
