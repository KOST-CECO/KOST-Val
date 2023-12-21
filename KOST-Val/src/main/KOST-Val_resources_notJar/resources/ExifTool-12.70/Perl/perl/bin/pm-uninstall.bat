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
use warnings;
use App::pmuninstall;

App::pmuninstall->new->run(@ARGV);

__END__

=head1 NAME

  pm-uninstall - Uninstall modules

=head1 SYNOPSIS

  pm-uninstall [options] Module ...

  options:
      -v,--verbose                  Turns on chatty output
      -f,--force                    Uninstalls without prompts
      -c,--checkdeps                Check dependencies (defaults to on)
      -n,--no-checkdeps             Don't check dependencies
      -q,--quiet                    Suppress some messages
      -h,--help                     This help message
      -V,--version                  Show version
      -l,--local-lib                Additional module path
      -L,--local-lib-contained      Additional module path (don't include non-core modules)

=cut
__END__
:endofperl
@set "ErrorLevel=" & @goto _undefined_label_ 2>NUL || @"%COMSPEC%" /d/c @exit %ErrorLevel%
