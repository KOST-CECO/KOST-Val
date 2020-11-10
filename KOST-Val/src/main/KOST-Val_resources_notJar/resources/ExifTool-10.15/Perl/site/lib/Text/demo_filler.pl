#! /usr/local/bin/perl -w

use Text::Reform;

print +form { filler=>'*' },
	"Pay bearer: ^^^^^^^^^^^^^^^^^^^",
	'$123.45';

print +form { filler=>'-->' },
	"Pay bearer: ]]]]]]]]]]]]]]]]]]]",
	['$1234.50', '$123.45', '$12.34'];

print +form { filler=>{left=>'->', right=>'*'} },
	"Pay bearer: <<<<<<<<<<<<<<<<<<",
	'$123.45',
	"Pay bearer: >>>>>>>>>>>>>>>>>>",
	'$123.45',
	"Pay bearer: ^^^^^^^^^^^^^^^^^^",
	'$123.45';
