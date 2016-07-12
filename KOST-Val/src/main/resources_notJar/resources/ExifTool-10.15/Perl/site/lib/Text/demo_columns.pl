#! /usr/bin/perl -w

use Text::Reform qw(form columns);

# Easy when data already in columns...

@name  = qw(Tom Dick Harry);
@score = qw( 88   54    99);
@time  = qw( 15   13    18);

print form
'-----------------------------',   
'Name             Score   Time',   
'-----------------------------',   
'[[[[[[[[[[[[[[   |||||   ||||',
\@name,          \@score,\@time;

print "\n"x2;

# Not so easy when data in rows...

@data = (
	{ name=>'Tom',   score=>88, time=>15 },
	{ name=>'Dick',  score=>54, time=>13 },
	{ name=>'Harry', score=>99, time=>18 },
);


# The ugly way...

print form
'-----------------------------',   
'Name             Score   Time',   
'-----------------------------',   
'[[[[[[[[[[[[[[   |||||   ||||',
[map $$_{name},  @data],
[map $$_{score}, @data],
[map $$_{time} , @data];

print "\n"x2;

# The nice way...

print form
'-----------------------------',   
'Name             Score   Time',   
'-----------------------------',   
'[[[[[[[[[[[[[[   |||||   ||||',
columns qw(name score time), @data;


@data = (
	[ 15, 'Tom',   88 ],
	[ 13, 'Dick',  54 ],
	[ 18, 'Harry', 99 ],
);

print "\n"x2;


# Works for arrays too, and multiple lists...

print form
'--------------------------------------',   
'Name             Score   Time  | Total',   
'--------------------------------------',   
'[[[[[[[[[[[[[[   |||||   ||||  | |||||',
columns 1,2,0, @data,
	    2, @data;
