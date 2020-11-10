#! /usr/local/bin/perl -w

use Text::Reform;

my @values   = (1..12);

my @squares  = map { sprintf "%.6g", $_**2    } @values;
my @roots    = map { sprintf "%.6g", sqrt($_) } @values;
my @logs     = map { sprintf "%.6g", log($_)  } @values;
my @inverses = map { sprintf "%.6g", 1/$_     } @values;

print +form
"  N      N**2    sqrt(N)      log(N)      1/N",
"=====================================================",
"| [[  |  [[[  |  [[[[[[[[[[ | [[[[[[[[[ | [[[[[[[[[ |
-----------------------------------------------------",
\@values,  \@squares,    \@roots,       \@logs,      \@inverses;
