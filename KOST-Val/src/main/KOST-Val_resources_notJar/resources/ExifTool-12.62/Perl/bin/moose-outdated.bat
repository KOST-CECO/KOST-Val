@rem = '--*-Perl-*--
@set "ErrorLevel="
@if "%OS%" == "Windows_NT" @goto WinNT
@perl -x -S "%0" %1 %2 %3 %4 %5 %6 %7 %8 %9
@set ErrorLevel=%ErrorLevel%
@goto endofperl
:WinNT
@perl -x -S %0 %*
@set ErrorLevel=%ErrorLevel%
@if NOT "%COMSPEC%" == "%SystemRoot%\system32\cmd.exe" @goto endofperl
@if %ErrorLevel% == 9009 @echo You do not have Perl in your PATH.
@goto endofperl
@rem ';
#!/usr/bin/perl
#line 30

use strict;
use warnings;
# PODNAME: moose-outdated

# this script was generated with Dist::Zilla::Plugin::Conflicts 0.19

use Getopt::Long;
use Moose::Conflicts;

my $verbose;
GetOptions( 'verbose|v' => \$verbose );

if ($verbose) {
    Moose::Conflicts->check_conflicts;
}
else {
    my @conflicts = Moose::Conflicts->calculate_conflicts;
    print "$_\n" for map { $_->{package} } @conflicts;
    exit @conflicts;
}
__END__
:endofperl
@set "ErrorLevel=" & @goto _undefined_label_ 2>NUL || @"%COMSPEC%" /d/c @exit %ErrorLevel%
