@rem = '--*-Perl-*--
@echo off
if "%OS%" == "Windows_NT" goto WinNT
perl -x -S "%0" %1 %2 %3 %4 %5 %6 %7 %8 %9
goto endofperl
:WinNT
perl -x -S %0 %*
if NOT "%COMSPEC%" == "%SystemRoot%\system32\cmd.exe" goto endofperl
if %errorlevel% == 9009 echo You do not have Perl in your PATH.
if errorlevel 1 goto script_failed_so_exit_with_non_zero_val 2>nul
goto endofperl
@rem ';
#!perl
#line 15
    eval 'exec C:\Perl\bin\perl.exe -S $0 ${1+"$@"}'
	if $running_under_some_shell;
#!/usr/local/bin/perl -w

use strict;
use IO::File;
use ExtUtils::Packlist;
use ExtUtils::Installed;

use vars qw($Inst @Modules);

################################################################################

sub do_module($)
{
my ($module) = @_;
my $help = <<EOF;
Available commands are:
   f [all|prog|doc]   - List installed files of a given type
   d [all|prog|doc]   - List the directories used by a module
   v                  - Validate the .packlist - check for missing files
   t <tarfile>        - Create a tar archive of the module
   q                  - Quit the module
EOF
print($help);
while (1)
   {
   print("$module cmd? ");
   my $reply = <STDIN>; chomp($reply);
   CASE:
      {
      $reply =~ /^f\s*/ and do
         {
         my $class = (split(' ', $reply))[1];
         $class = 'all' if (! $class);
         my @files;
         if (eval { @files = $Inst->files($module, $class); })
            {
            print("$class files in $module are:\n   ",
                  join("\n   ", @files), "\n");
            last CASE;
            }
         else
            { print($@); }
         };
      $reply =~ /^d\s*/ and do
         {
         my $class = (split(' ', $reply))[1];
         $class = 'all' if (! $class);
         my @dirs;
         if (eval { @dirs = $Inst->directories($module, $class); })
            {
            print("$class directories in $module are:\n   ",
                  join("\n   ", @dirs), "\n");
            last CASE;
            }
         else
            { print($@); }
         };
      $reply =~ /^t\s*/ and do
         {
         my $file = (split(' ', $reply))[1];
         my $tmp = "/tmp/inst.$$";
         if (my $fh = IO::File->new($tmp, "w"))
            {
            $fh->print(join("\n", $Inst->files($module)));
            $fh->close();
            system("tar cvf $file -I $tmp");
            unlink($tmp);
            last CASE;
            }
         else { print("Can't open $file: $!\n"); }
         last CASE;
         };
      $reply eq 'v' and do
         {
         if (my @missing = $Inst->validate($module))
            {
            print("Files missing from $module are:\n   ",
                  join("\n   ", @missing), "\n");
            }
         else
            {
            print("$module has no missing files\n");
            }
         last CASE;
         };
      $reply eq 'q' and do
         {
         return;
         };
      # Default
         print($help);
      }
   }
}

################################################################################

sub toplevel()
{
my $help = <<EOF;
Available commands are:
   l            - List all installed modules
   m <module>   - Select a module
   q            - Quit the program
EOF
print($help);
while (1)
   {
   print("cmd? ");
   my $reply = <STDIN>; chomp($reply);
   CASE:
      {
      $reply eq 'l' and do
         {
         print("Installed modules are:\n   ", join("\n   ", @Modules), "\n");
         last CASE;
         };
      $reply =~ /^m\s+/ and do
         {
         do_module((split(' ', $reply))[1]);
         last CASE;
         };
      $reply eq 'q' and do
         {
         exit(0);
         };
      # Default
         print($help);
      }
   }
}

################################################################################

$Inst = ExtUtils::Installed->new();
@Modules = $Inst->modules();
toplevel();

################################################################################

__END__
:endofperl
