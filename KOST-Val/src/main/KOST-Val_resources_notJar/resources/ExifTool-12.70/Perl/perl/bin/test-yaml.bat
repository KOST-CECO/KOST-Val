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
#!/usr/bin/bash

USAGE=<<...
Usage:

  test-yaml <test> <options>

Tests:

  load                  # Load YAML input and write data output
  dump                  # Load data input and write YAML output
  yny                   # Load→Dump→Load roundtrip
  nyn                   # Dump→Load→Dump roundtrip
  parse                 # Parse YAML input and write YAML events
  emit                  # Emit YAML events and write YAML output

Options:

  --in=<file>           # Input file
  --out=<file>          # Output file
  --from=<format>       # Input format
  --to=<format>         # Output format
  --yaml=<framework>    # Which YAML implementations to use
  --test=<tag-spec>     # Tags of test cases to use

Formats:
  json                  # JSON input or output
  perl                  # Perl code producing or representing data

Examples:

  test-yaml load --in=file.yaml --to=json --yaml=perl
  test-yaml dump --in=prog.pl --tag=dump --yaml=perl

See:

  For more complete doc, run:

    > man test-yaml

...
__END__
:endofperl
@set "ErrorLevel=" & @goto _undefined_label_ 2>NUL || @"%COMSPEC%" /d/c @exit %ErrorLevel%
