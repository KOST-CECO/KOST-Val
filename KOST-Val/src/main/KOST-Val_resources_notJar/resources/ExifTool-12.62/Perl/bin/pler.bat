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

use 5.00503;
use strict;

# On some platforms (mostly Windows), we get errors because
# of Term::Cap issues. To avoid this, set TERM=dumb if the
# user does not have a TERM value already.
# This doesn't remove all possible errors, just the most
# annoying and common ones.
BEGIN {
	$ENV{TERM} ||= 'dumb';
}

use vars qw{$VERSION};
BEGIN {
	$VERSION = '1.06';
}

use pler ();

unless ( $VERSION eq $pler::VERSION ) {
	die "Error: Version mismatch (launch script $VERSION using module $pler::VERSION)";
}
pler::main(@ARGV);

exit(0);
__END__
:endofperl
@set "ErrorLevel=" & @goto _undefined_label_ 2>NUL || @"%COMSPEC%" /d/c @exit %ErrorLevel%
