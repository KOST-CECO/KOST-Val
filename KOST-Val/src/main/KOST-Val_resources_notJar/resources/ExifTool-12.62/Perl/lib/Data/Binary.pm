package Data::Binary;

use strict;
use warnings;

our $VERSION = 0.01;

use base qw(Exporter);

use Encode qw(decode_utf8);

our @EXPORT_OK = qw(is_text is_binary);

sub is_text {
	my ($string) = @_;

	if (length($string) > 512) {
		$string = substr($string, 0, 512);
	}

	return '' if (index($string, "\c@") != -1);
	my $length = length($string);
	my $odd = ($string =~ tr/\x01\x02\x03\x04\x05\x06\x07\x09\x0b\x0c\x0e\x0f\x10\x11\x12\x13\x14\x15\x16\x17\x18\x19\x1a\x1b\x1c\x1d\x1e\x1f//d);

	# Detecting >=128 and non-UTF-8 is interesting. Note that all UTF-8 >=128 has several bytes with
	# >=128 set, so a quick test is possible by simply checking if any are >=128. However, the count
	# from that is typically wrong, if this is binary data, it'll not have been decoded. So we do this
	# in two steps.

	my $copy = $string;
	if (($copy =~ tr[\x80-\xff][]d) > 0) {
		my $modified = decode_utf8($string, Encode::FB_DEFAULT);
		my $substitions = ($modified =~ tr/\x{fffd}//d);
		$odd += $substitions;
	}

	return '' if ($odd * 3 > $length);

	return 1;
}

sub is_binary {
	my ($string) = @_;
	return ! is_text($string);
}

1;

=head1 NAME

Data::Binary - Simple detection of binary versus text in strings

=head1 SYNOPSIS

 use Data::Binary qw(is_text is_binary);
 my $text = File::Slurp::read_file("test1.doc");
 my $is_text = is_text($text); # equivalent to -T "test1.doc"
 my $is_binary = is_binary($text); # equivalent to -B "test1.doc"

=head1 DESCRIPTION

This simple module provides string equivalents to the -T / -B operators. Since
these only work on file names and file handles, this module provides the same
functions but on strings.

Note that the actual implementation is currently different, basically because 
the -T / -B functions are in C/XS, and this module is written in pure Perl. 
For now, anyway. 

=head1 FUNCTIONS

=head2 is_text($string)

Uses the same kind of heuristics in -T, but applies them to a string. Returns true
if the string is basically text.

=head2 is_binary($string)

Uses the same kind of heuristics in -B, but applies them to a string. Returns true
if the string is basically binary.

=head1 AUTHOR

Stuart Watt, stuart@morungos.com

=head1 COPYRIGHT

Copyright (c) 2014 Stuart Watt. All rights reserved.

=cut