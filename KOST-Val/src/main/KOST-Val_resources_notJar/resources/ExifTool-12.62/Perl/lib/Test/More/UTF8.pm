package Test::More::UTF8;

use warnings;
use strict;
use Test::More ();
use Carp;

=head1 NAME

Test::More::UTF8 - Enhancing Test::More for UTF8-based projects

=cut

our $VERSION = '0.05';

=head1 SYNOPSIS

	use Test::More;
	use Test::More::UTF8;

	# now we can easily use flagged strings without warnings like "Wide character in print ..."
	is("\x{410}","\x{420}"); # got a failure message without warnings

=head1 LIMITATIONS

	This module have reason only for perl 5.8 and higher

=head1 FEATURES

This module also switch on by default utf8 pragma. To disable this, add "-utf8" option

	use Test::More::UTF8 qw(-utf8);

By default binmode ':utf8' will be done on all output handles: failure_output, todo_output, output. It is possible to choose only some of them

	use Test::More::UTF8 qw(failure); # enable :utf8 only on failure_output
	use Test::More::UTF8 qw(todo); # enable :utf8 only on todo_output
	use Test::More::UTF8 qw(out); # enable :utf8 only on output

=cut

sub import {
	my $pkg = shift;
	my %args = map {$_ => 1} @_;
	my ($do_utf8,@h) = (1);
	push @h, 'failure_output' if delete $args{failure} or delete $args{failure_output};
	push @h, 'todo_output' if delete $args{todo} or delete $args{todo_output};
	push @h, 'output' if delete $args{out} or delete $args{output};
	$do_utf8 = 0 if delete $args{-utf8} or delete $args{-utf};
	%args and croak "Unsupported options to $pkg: ".join ', ', keys %args;
	@h or @h = qw(failure_output todo_output output);
	binmode Test::More->builder->$_, ':utf8' for @h;
	if ($do_utf8) {
		require utf8;
		@_ = ('utf8');
		goto &utf8::import;
	}
	return;
}

=head1 AUTHOR

Mons Anderson, <mons@cpan.org>

=head1 BUGS

None known

=head1 COPYRIGHT & LICENSE

Copyright 2009 Mons Anderson, all rights reserved.

This program is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.


=cut

1; # End of Test::More::UTF8
