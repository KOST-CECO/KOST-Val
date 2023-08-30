package common::sense;

our $VERSION = 3.75;

# overload should be included

sub import {
   local $^W; # work around perl 5.16 spewing out warnings for next statement
   # use warnings
   ${^WARNING_BITS} ^= ${^WARNING_BITS} ^ "\x0c\x3f\x33\x00\x03\xf0\x0f\xc0\xf0\xfc\x33\x00\x00\x00\x0c\x00\x00\x00\x00";
   # use strict, use utf8; use feature;
   $^H |= 0x1c820fc0;
   @^H{qw(feature___SUB__ feature_evalbytes feature_fc feature_indirect feature_say feature_state feature_switch feature_unicode)} = (1) x 8;
}

1
