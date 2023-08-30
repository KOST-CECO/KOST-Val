package Filter::m4;

use Filter::Util::Exec;
use strict;
use warnings;

our $VERSION = '1.60';

my $m4;
my $sep;
if ($^O eq 'MSWin32') {
    $m4 = 'm4.exe';
    $sep = ';';
}
else {
    ($m4) = 'm4';
    $sep = ':';
}

if (!$m4) {
    require Carp;
    Carp::croak("Cannot find m4\n");
}

# Check whether m4 is installed.
if (!-x $m4) {
    my $foundM4 = 0;
    foreach my $dir (split($sep, $ENV{PATH}), '') {
        if (-x "$dir/$m4") {
            $foundM4 = 1;
            last;
        }
    }

    if (!$foundM4) {
        require Carp;
        Carp::croak("Cannot find m4\n");
    }
}


sub import 
{ 
    my ($self, @args) = @_;

    my $m4arg = '';
    foreach my $arg (@args) {
        if ($arg eq 'prefix') {
            $m4arg = '-P';
        }
        else {
            require Carp;
            Carp::croak("Unrecognized argument $arg\n");
        }
    }

    if ($^O eq 'MSWin32') {
        Filter::Util::Exec::filter_add($self, 'cmd', '/c',
                "m4.exe $m4arg 2>nul");
    }
    else {
        Filter::Util::Exec::filter_add ($self, 'sh', '-c',
                "m4 $m4arg 2>/dev/null");
    }
}

1;
__END__


=head1 NAME

Filter::m4 - M4 source filter


=head1 SYNOPSIS

    use Filter::m4;
    use Filter::m4 'prefix';


=head1 DESCRIPTION

This source filter pipes the current source file through the M4 macro
processor (C<m4>) if it is available.

As with all source filters its scope is limited to the current source file
only.  Every file you want to be processed by the filter must have the
following line near the top.

    use Filter::m4;


=head1 EXAMPLE

Here is a small example that shows how to define and use an M4 macro:

    use Filter::m4;

    define(`foo', `$1 =~ s/bar/baz/r')

    $a = "foobar";
    print "a = " . foo(`$a') . "\n";

The output of the above example:

    a = foobaz


=head1 NOTES

By default, M4 uses ` and ' as quotes; however, this is configurable using
M4's C<changequote> builtin.

M4 uses C<$1>, C<$2>, etc., to indicate arguments in macros.  To avoid
clashes with Perl regex syntax it is recommended to use Perl's alternative
forms C<${1}>, C<${1}>, etc.

The following keywords in M4 and Perl are identical:

    eval
    format
    index
    mkstemp
    shift
    substr

If you need such keywords in your Perl code you have to use one of the
following three solutions.

=over

=item *

Protect the keyword with M4 quotes, for example C<`shift'>.

=item *

Redefine the problematic M4 builtin using C<defn>, as outlined in section
I<Renaming macros> of the M4 info manual.

=item *

Use the C<prefix> option.  This adds the prefix C<m4_> to all M4 builtins
(but not to user-defined macros).  For example, you will have to use
C<m4_shift> instead of C<shift>.

=back


=head1 AUTHOR

Werner Lemberg


=head1 DATE

17th March 2018.

=cut
