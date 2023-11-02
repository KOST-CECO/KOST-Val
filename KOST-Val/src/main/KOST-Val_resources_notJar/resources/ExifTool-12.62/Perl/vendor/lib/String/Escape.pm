=head1 NAME

String::Escape - Backslash escapes, quoted phrase, word elision, etc.

=cut

package String::Escape;

use strict;
use warnings;
use Carp;

use vars qw( $VERSION );
$VERSION = 2010.002;

########################################################################

=head1 SYNOPSIS

This module provides a flexible calling interface to some frequently-performed string conversion functions, including applying and removing backslash escapes like \n and \t, wrapping and removing double-quotes, and truncating to fit within a desired length.

  use String::Escape qw( printable unprintable );
  # Convert control, high-bit chars to \n or \xxx escapes
  $output = printable($value);
  # Convert escape sequences back to original chars
  $value = unprintable($input);

  use String::Escape qw( elide );
  # Shorten strings to fit, if necessary
  foreach (@_) { print elide( $_, 79 ) . "\n"; }

  use String::Escape qw( string2list list2string );
  # Pack and unpack simple lists by quoting each item
  $list = list2string( @list );
  @list = string2list( $list );

  use String::Escape qw( escape );
  # Defer selection of escaping routines until runtime
  $escape_name = $use_quotes ? 'qprintable' : 'printable';
  @escaped = escape($escape_name, @values);

=cut


########################################################################

=head1 INTERFACE

All of the public functions described below are available as optional exports.

You can either import the specific functions you want, or import only the C<escape()> function and pass it the names of the functions to invoke.

=cut

use Exporter;

use vars qw( @ISA @EXPORT_OK );

push @ISA, qw( Exporter );
push @EXPORT_OK, qw(
	quote unquote quote_non_words singlequote unsinglequote
	backslash unbackslash qqbackslash unqqbackslash
	printable unprintable qprintable unqprintable
	unquotemeta
	elide
	escape
	string2list string2hash list2string list2hash hash2string hash2list
);


########################################################################

=head2 Quoting

Each of these functions takes a single simple scalar argument and
returns its escaped (or unescaped) equivalent.

=over 4

=item quote($value) : $escaped

Add double quote characters to each end of the string.

=item unquote($value) : $escaped

If the string both begins and ends with double quote characters, they are removed, otherwise the string is returned unchanged.

=item quote_non_words($value) : $escaped

As above, but only quotes empty, punctuated, and multiword values; simple values consisting of alphanumerics without special characters are not quoted.

=item singlequote($value) : $escaped

Add single quote characters to each end of the string.

=item unsinglequote($value) : $escaped

If the string both begins and ends with single quote characters, they are removed, otherwise the string is returned unchanged.

=back

=cut

# $with_surrounding_quotes = quote( $string_value );
sub quote ($) {
	'"' . $_[0] . '"'
}

# $remove_surrounding_quotes = quote( $string_value );
sub unquote ($) {
	( $_[0] =~ m/ \A ["] (.*) ["] \Z /sx ) ? $1 : $_[0];
}

# $word_or_phrase_with_surrounding_quotes = quote( $string_value );
sub quote_non_words ($) {
	( ! length $_[0] or $_[0] =~ /[^\w\_\-\/\.\:\#]/ ) ? '"'.$_[0].'"' : $_[0]
}

# $with_surrounding_quotes = singlequote( $string_value );
sub singlequote ($) {
	'\'' . $_[0] . '\''
}

# $remove_surrounding_quotes = singlequote( $string_value );
sub unsinglequote ($) {
	( $_[0] =~ m/ \A ['] (.*) ['] \Z /sx ) ? $1 : $_[0];
}


########################################################################

=head2 Backslash Escaping Functions

Each of these functions takes a single simple scalar argument and
returns its escaped (or unescaped) equivalent.

These functions recognize common whitespace sequences C<\r>, C<\n>, and C<\t>, as well as hex escapes C<\x4F> and ocatal C<\020>. 

When escaping, alphanumeric characters and most punctuation is passed through unchanged; only the return, newline, tab, backslash, dollar, at sign and unprintable control and high-bit characters are escaped.

=over 4

=item backslash($value) : $escaped

Converts special characters to their backslash-escaped equivalents.

=item unbackslash($value) : $escaped

Converts backslash escape sequences in a string back to their original characters.

=item qqbackslash($value) : $escaped

Converts special characters to their backslash-escaped equivalents and then wraps the results with double quotes.

=item unqqbackslash($value) : $escaped

Strips surrounding double quotes then converts backslash escape sequences back to their original characters.

=back

Here are a few examples:

=over 4

=item *

  print backslash( "\tNow is the time\nfor all good folks\n" );

  \tNow is the time\nfor all good folks\n

=item *

  print unbackslash( '\\tNow is the time\\nfor all good folks\\n' );

  	Now is the time
  for all good folks

=back

=cut

use vars qw( %Backslashed %Interpolated );

# Earlier definitions are preferred to later ones, thus we output \n not \x0d
_define_backslash_escapes(
	( map { $_ => $_ } ( '\\', '"', '$', '@' ) ),
	( 'r' => "\r", 'n' => "\n", 't' => "\t" ),
	( map { 'x' . unpack('H2', chr($_)) => chr($_) } (0..255) ),
	( map { sprintf('%03o', $_) => chr($_) } (0..255) ),
);

sub _define_backslash_escapes {
	%Interpolated = @_;
	%Backslashed  = reverse @_;
}

# $special_characters_escaped = backslash( $source_string );
sub backslash ($) {
	local $_ = ( defined $_[0] ? $_[0] : '' );
	# Preserve only printable ASCII characters other than \, ", $, and @
	s/([^\x20\x21\x24\x25-\x39\x41-\x5b\x5d-\x7e])/\\$Backslashed{$1}/gs;
	return $_;
}

# $original_string = unbackslash( $special_characters_escaped );
sub unbackslash ($) {
	local $_ = ( defined $_[0] ? $_[0] : '' );
	s/ (\A|\G|[^\\]) [\\] ( [0]\d\d | [x][\da-fA-F]{2} | . ) / $1 . ( $Interpolated{lc($2) }) /gsxe;
	return $_;
}

# quoted_and_escaped = qqbackslash( $source_string );
sub qqbackslash ($) { quote backslash $_[0] }

# $original_string = unqqbackslash( quoted_and_escaped );
sub unqqbackslash ($) { unbackslash unquote $_[0] }


########################################################################

=head2 Legacy Backslash Functions

In addition to the four functions listed above, there is a corresponding set which use a slightly different set of escape sequences.

These functions do not support as many escape sequences and use a non-standard
format for hex escapes. In general, the above C<backslash()> functions are
recommended, while these functions are retained for legacy compatibility
purposes.

=over 4

=item printable($value) : $escaped

Converts return, newline, tab, backslash and unprintable
characters to their backslash-escaped equivalents.

=item unprintable($value) : $escaped

Converts backslash escape sequences in a string back to their original value.

=item qprintable($value) : $escaped

Converts special characters to their backslash-escaped equivalents and then wraps the results with double quotes.

(Note that this is I<not> MIME quoted-printable encoding.)

=item unqprintable($value) : $escaped

Strips surrounding double quotes then converts backslash escape sequences back to their original value.

=back

=cut

use vars qw( %Printable %Unprintable );
%Printable = (
	( map { chr($_), unpack('H2', chr($_)) } (0..255) ),
	( "\\"=>'\\', "\r"=>'r', "\n"=>'n', "\t"=>'t', ),
	( map { $_ => $_ } ( '"' ) )
);
%Unprintable = ( reverse %Printable );

# $special_characters_escaped = printable( $source_string );
sub printable ($) {
	local $_ = ( defined $_[0] ? $_[0] : '' );
	s/([\r\n\t\"\\\x00-\x1f\x7F-\xFF])/ '\\' . $Printable{$1} /gsxe;
	return $_;
}

# $original_string = unprintable( $special_characters_escaped );
sub unprintable ($) {
	local $_ = ( defined $_[0] ? $_[0] : '' );
	s/((?:\A|\G|[^\\]))\\([rRnNtT\"\\]|[x]?[\da-fA-F]{2})/ $1 . $Unprintable{lc($2)} /gsxe;
	return $_;
}

# quoted_and_escaped = qprintable( $source_string );
sub qprintable ($) { quote_non_words printable $_[0] }

# $original_string = unqprintable( quoted_and_escaped );
sub unqprintable ($) { unprintable unquote $_[0] }


########################################################################

=head2 Other Backslash Functions

In addition to the functions listed above, there is also one function that mirrors the behavior of Perl's built-in C<quotemeta()> function.

=over 4

=item unquotemeta($value) : $escaped

Strips out backslashes before any character.

=back

=cut

sub unquotemeta ($) {
	local $_ = ( defined $_[0] ? $_[0] : '' );
	s/ (\A|\G|[^\\]) [\\] (.) / $1 . $2 /gsex;
	return $_;
}


########################################################################

=head2 Elision Function

This function extracts the leading portion of a provided string and appends ellipsis if it's longer than the desired maximum excerpt length.

=over 4

=item elide($string) : $elided_string

=item elide($string, $length) : $elided_string

=item elide($string, $length, $word_boundary_strictness) : $elided_string

=item elide($string, $length, $word_boundary_strictness, $elipses) : $elided_string

Return a single-quoted, shortened version of the string, with ellipsis.

If the original string is shorter than $length, it is returned unchanged. At most $length characters are returned; if called with a single argument, $length defaults to $DefaultLength.

Up to $word_boundary_strictness additional characters may be ommited in order to make the elided portion end on a word boundary; you can pass 0 to ignore word boundaries. If not provided, $word_boundary_strictness defaults to $DefaultStrictness.

=item $Elipses

The string of characters used to indicate the end of the excerpt. Initialized to '...'.

=item $DefaultLength

The default target excerpt length, used when the elide function is called with a single argument. Initialized to 60.

=item $DefaultStrictness

The default word-boundary flexibility, used when the elide function is called without the third argument. Initialized to 10.

=back

Here are a few examples:

=over 4

=item *

  $string = 'foo bar baz this that the other';

  print elide( $string, 12 );
  # foo bar...

  print elide( $string, 12, 0 );
  # foo bar b...

  print elide( $string, 100 );
  # foo bar baz this that the other

=back

=cut

use vars qw( $Elipses $DefaultLength $DefaultStrictness );
$Elipses = '...';
$DefaultLength = 60;
$DefaultStrictness = 10;

# $elided_string = elide($string);
# $elided_string = elide($string, $length);
# $elided_string = elide($string, $length, $word_boundary_strictness);
# $elided_string = elide($string, $length, $word_boundary_strictness, $elipses);
sub elide ($;$$) {
	my $source     = shift;
	my $length     = scalar(@_) ? shift() : $DefaultLength;
	my $word_limit = scalar(@_) ? shift() : $DefaultStrictness;
	my $elipses    = scalar(@_) ? shift() : $Elipses;
	
	# If the source is already short, we don't need to do anything
	return $source if (length($source) < $length);
	
	# Leave room for the elipses and make sure we include at least one character.
	$length -= length( $elipses );
	$length = 1 if ( $length < 1 );
	
	my $excerpt;
	
	# Try matching $length characters or less at a word boundary.
	$excerpt = ( $source =~ /^(.{0,$length})(?:\s|\Z)/ )[0] if ( $word_limit );
	
	# If that fails or returns much less than we wanted, ignore boundaries
	$excerpt = substr($source, 0, $length) if ( 
		! defined $excerpt or
		length($excerpt) < length($source) and
			! length($excerpt) || abs($length - length($excerpt)) > $word_limit
	);
	
	return $excerpt . $elipses;
}


########################################################################

=head2 escape()

These functions provide for the registration of string-escape specification
names and corresponding functions, and then allow the invocation of one or
several of these functions on one or several source string values.

=over 4

=item escape($escapes, $value) : $escaped_value

=item escape($escapes, @values) : @escaped_values

Returns an altered copy of the provided values by looking up the escapes string in a registry of string-modification functions.

If called in a scalar context, operates on the single value passed in; if
called in a list contact, operates identically on each of the provided values.

Space-separated compound specifications like 'quoted uppercase' are expanded to a list of functions to be applied in order.

Valid escape specifications are:

=over 4

=item one of the keys defined in %Escapes

The coresponding specification will be looked up and used.

=item a sequence of names separated by whitespace,

Each name will be looked up, and each of the associated functions will be applied successively, from left to right.

=item a reference to a function

The provided function will be called on with each value in turn.

=item a reference to an array

Each item in the array will be expanded as provided above.

=back

A fatal error will be generated if you pass an unsupported escape specification, or if the function is called with multiple values in a scalar context.

=item String::Escape::names() : @defined_escapes

Returns a list of defined escape specification strings.

=item String::Escape::add( $escape_name, \&escape_function );

Add a new escape specification and corresponding function.

=back

By default, all of the public functions described below are available as named escape commands, as well as the following built-in functions:

=over 4

=item *

none: Return the string unchanged.

=item *

uppercase: Calls the built-in uc function.

=item *

lowercase: Calls the built-in lc function.

=item *

initialcase: Calls the built-in lc and ucfirst functions.

=back

Here are a few examples:

=over 4

=item *

C<print escape('qprintable', "\tNow is the time\nfor all good folks\n" );>

  "\tNow is the time\nfor all good folks\n"

=item *

C<print escape('uppercase qprintable', "\tNow is the time\nfor all good folks\n" );>

  "\tNOW IS THE TIME\nFOR ALL GOOD FOLKS\n"

=item *

C<print join '--', escape('printable', "\tNow is the time\n", "for all good folks\n" );>

  \tNow is the time\n--for all good folks\n

=item *

You can add more escaping functions to the supported set by calling add().

C<String::Escape::add( 'html', \&HTML::Entities::encode_entities );>

C<print escape('html', "AT&T" );>

  AT&amp;T

=back

=cut

# %Escapes - escaper function references by name
use vars qw( %Escapes );

# String::Escape::add( $name, $subroutine );
sub add {
	while ( @_ ) {
		my ( $name, $func ) = ( shift, shift );
		$Escapes{ $name } = $func
	}
}

# @defined_names = String::Escape::names();
sub names {
	keys(%Escapes)
}

# $escaped = escape($escape_spec, $value);
# @escaped = escape($escape_spec, @values);
sub escape {
	my ($escape_spec, @values) = @_;
	
	my @escapes = _expand_escape_spec($escape_spec);
	
	foreach my $value ( @values ) {
		foreach my $escaper ( @escapes ) {
			$value = &$escaper( $value );
		}
	}
	
	if ( wantarray ) {
		@values
	} elsif ( @values > 1 ) {
		croak "escape called with multiple values but in scalar context"
	} else {
		$values[0]
	}
}

# @escape_functions = _expand_escape_spec($escape_spec);
sub _expand_escape_spec {
	my $escape_spec = shift;
	
	if ( ref($escape_spec) eq 'CODE' ) {
		return $escape_spec;
	} elsif ( ref($escape_spec) eq 'ARRAY' ) {
		return map { _expand_escape_spec($_) } @$escape_spec;
	} elsif ( ! ref($escape_spec) ) {
		return map {
			_expand_escape_spec($_)
		} map {
			$Escapes{$_} or _unsupported_escape_spec( $_ )
		} split(/\s+/, $escape_spec);
	} else {
	_unsupported_escape_spec( $escape_spec );
	}
}

# _unsupported_escape_spec($escape_spec);
sub _unsupported_escape_spec {
	my $escape_spec = shift;
	
		croak(
		"unsupported escape specification " .
		( defined($escape_spec) ? "'$_'" : 'undef' ) . "; " .
		"should be one of " . join(', ', names())
	)
}

add(
	'none'            => sub ($) { $_[0]; },
	
	'uppercase'       => sub ($) { uc $_[0] },
	'lowercase'       => sub ($) { lc $_[0] },
	'initialcase'     => sub ($) { ucfirst lc $_[0] },

	'quote'           => \&quote,
	'unquote'         => \&unquote,
	'quote_non_words' => \&quote_non_words,
	'singlequote'     => \&singlequote,
	'unsinglequote'   => \&unsinglequote,

	'backslash'       => \&backslash,
	'unbackslash'     => \&unbackslash,
	'qqbackslash'     => \&qqbackslash, #b
	'unqqbackslash'   => \&unqqbackslash,

	'printable'       => \&printable,
	'unprintable'     => \&unprintable,
	'qprintable'      => \&qprintable,
	'unqprintable'    => \&unqprintable,

	'quotemeta'       => sub ($) { quotemeta $_[0] },
	'unquotemeta'     => \&unquotemeta,

	'elide'           => \&elide,
);


########################################################################

=head2 Space-separated Lists and Hashes

=over 4

=item @words = string2list( $space_separated_phrases );

Converts a space separated string of words and quoted phrases to an array;

=item $space_sparated_string = list2string( @words );

Joins an array of strings into a space separated string of words and quoted phrases;

=item %hash = string2hash( $string );

Converts a space separated string of equal-sign-associated key=value pairs into a simple hash.

=item $string = hash2string( %hash );

Converts a simple hash into a space separated string of equal-sign-associated key=value pairs.

=item %hash = list2hash( @words );

Converts an array of equal-sign-associated key=value strings into a simple hash.

=item @words = hash2list( %hash );

Converts a hash to an array of equal-sign-associated key=value strings.

=back

Here are a few examples:

=over 4

=item *

C<print list2string('hello', 'I move next march');>

  hello "I move next march"

=item *

C<@list = string2list('one "second item" 3 "four\nlines\nof\ntext"');>

C<print $list[1];>

  second item

=item *

C<print hash2string( 'foo' =E<gt> 'Animal Cities', 'bar' =E<gt> 'Cheap' );>

  foo="Animal Cities" bar=Cheap

=item *

C<%hash = string2hash('key=value "undefined key" words="the cat in the hat"');>

C<print $hash{'words'};>

  the cat in the hat

C<print exists $hash{'undefined_key'} and ! defined $hash{'undefined_key'};>

  1

=back

=cut

# @words = string2list( $space_separated_phrases );
sub string2list {
	my $text = shift;
	
	carp "string2list called with a non-text argument, '$text'" if (ref $text);
	
	my @words;
	my $word = '';
	
	while ( length $text ) {
		if ($text =~ s/\A(?: ([^\"\s\\]+) | \\(.) )//mx) {
			$word .= $1;
		} elsif ($text =~ s/\A"((?:[^\"\\]|\\.)*)"//mx) {
			$word .= $1;
		} elsif ($text =~ s/\A\s+//m){
			push(@words, unprintable($word));
			$word = '';
		} elsif ($text =~ s/\A"//) {
			carp "string2list found an unmatched quote at '$text'";
			return;
		} else {
			carp "string2list parse exception at '$text'";
			return;
		}
	}
	push(@words, unprintable($word));
	
	return @words;
}

# $space_sparated_string = list2string( @words );
sub list2string {
	join ( ' ', map qprintable($_), @_ );
}

# %hash = list2hash( @words );
sub list2hash {
	my @pairs;
	foreach (@_) {
		my ($key, $val) = m/\A(.*?)(?:\=(.*))?\Z/s;
		push @pairs, $key, $val;
	}
	return @pairs;
}

# @words = hash2list( %hash );
sub hash2list {
	my @words;
	while ( scalar @_ ) {
		my ($key, $value) = ( shift, shift );
		push @words, qprintable($key) . '=' . qprintable($value)
	}
	return @words;
}

# %hash = string2hash( $string );
sub string2hash {
	return list2hash( string2list( shift ) );
}

# $string = hash2string( %hash );
sub hash2string {
	join ( ' ', hash2list( @_ ) );
}


########################################################################

=head1 SEE ALSO

Numerous modules provide collections of string escaping functions for specific contexts.

The string2list function is similar to to the quotewords function in the standard distribution; see L<Text::ParseWords>.

Use other packages to stringify more complex data structures; see L<Storable>, L<Data::Dumper>, or other similar package.

=cut


########################################################################


=head1 BUGS

The following issues or changes are under consideration for future releases:

=over 4

=item *

Does this problem with the \r character only show up on Windows? (And is it, in fact, a feature rather than a bug?)

  http://rt.cpan.org/Public/Bug/Display.html?id=19766

=item *

Consider changes to word parsing in string2list: Perhaps use \b word-boundary test in elide's regular expression rather than \s|\Z? Perhaps quotes embedded in a word (eg: a@"!a) shouldn't cause phrase breaks?

=item *

Check for possible problems in the use of printable escaping functions and list2hash. For example, are the encoded strings for hashes with high-bit characters in their keys properly unquoted and unescaped?

=item *

We should allow escape specifications to contain = signs and optional arguments, so that users can request certain string lengths with C<escape("lowercase elide=20 quoted", @_>.

=back


=head1 VERSION

This is version 2010.002.


=head1 INSTALLATION

This package should run on any standard Perl 5 installation.

To install this package, download the distribution from a CPAN mirror,
unpack the archive file, and execute the standard "perl Makefile.PL",
"make test", "make install" sequence or your local equivalent.


=head1 SUPPORT

Once installed, this module's documentation is available as a
manual page via C<perldoc String::Escape> or on CPAN sites
such as C<http://search.cpan.org/dist/String-Escape>.

If you have questions or feedback about this module, please feel free to
contact the author at the address shown below. Although there is no formal
support program, I do attempt to answer email promptly.  Bug reports that
contain a failing test case are greatly appreciated, and suggested patches
will be promptly considered for inclusion in future releases.

You can report bugs and request features via the CPAN web tracking system
at C<http://rt.cpan.org/NoAuth/ReportBug.html?Queue=String-Escape> or by 
sending mail to C<bug-string-escape at rt.cpan.org>.

If you've found this module useful or have feedback about your
experience with it, consider sharing your opinion with other Perl users
by posting your comment to CPAN's ratings system
(C<http://cpanratings.perl.org/rate/?distribution=String-Escape>).

For more general discussion, you may wish to post a message on PerlMonks
(C<http://perlmonks.org/?node=Seekers%20of%20Perl%20Wisdom>) or on the
comp.lang.perl.misc newsgroup
(C<http://groups.google.com/group/comp.lang.perl.misc/topics>).



=head1 AUTHOR

Matthew Simon Cavalletto, C<< <simonm at cavalletto.org> >>

Initial versions developed at Evolution Online Systems with Eleanor J. Evans and Jeremy G. Bishop.


=head1 LICENSE

Copyright 2010, 2002 Matthew Simon Cavalletto.

Portions copyright 1996, 1997, 1998, 2001 Evolution Online Systems, Inc.

You may use, modify, and distribute this software under the same terms as Perl.

See http://dev.perl.org/licenses/ for more information.


=cut

########################################################################

1; # End of String::Escape
