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
#!perl
#line 30
use strict;
BEGIN {
my $old = select STDERR; $|++;  # turn on autoflush
select $old;             $|++;  # turn on autoflush
$0 = shift(@ARGV);              # rename the script
my $rv = do($0);                # execute the file
die $@ if $@;                   # die on parse/execute error
}
### XXX 'do' returns last statement evaluated, which may be
### undef as well. So don't die in that case.
#die $! if not defined $rv;      # die on execute error
__END__
:endofperl
@set "ErrorLevel=" & @goto _undefined_label_ 2>NUL || @"%COMSPEC%" /d/c @exit %ErrorLevel%
