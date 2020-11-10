#! /usr/local/bin/perl -ws

# TRY DOING *THIS* WITH format!

use Text::Reform qw(form break_with);

my $text = join "", map "line $_\n", (1..20);

@lines = form { 
	pagelen=>9,
	header => sub { "Page $_[0]\n"."="x50 },
	footer => sub { my ($pagenum, $lastpage) = @_;
			return "" if $lastpage;
			return "-"x50 . "\n" . form ">"x50, "...".($pagenum+1);
		      },
	pagefeed => "\n"x10
	},
"      [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[\n",
       \$text;

print @lines;

