#! /usr/local/bin/perl -w

use Text::Reform 'form';

$frmt = "[[[[[[[[[[[[[[[[[[[[[";
$data = "h  e\t \tl lo\nworld\t\t\t\t\t";

print +form $frmt, $data;
print "==========\n";

print +form {squeeze=>1}, $frmt, $data;
print "==========\n";

print +form {fill=>1}, $frmt, $data;
print "==========\n";

print +form {squeeze=>1, fill=>1}, $frmt, $data;
print "==========\n";

print length(form "[[[[[[[[[[", "short"), "\n";

print length(form {trim=>1}, "[[[[[[[[[[", "short"), "\n";

