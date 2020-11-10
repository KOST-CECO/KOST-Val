@ECHO OFF
SETLOCAL

CALL %~dp0/Perl/bin/perl.exe %~dp0/exiftool.pl %*
