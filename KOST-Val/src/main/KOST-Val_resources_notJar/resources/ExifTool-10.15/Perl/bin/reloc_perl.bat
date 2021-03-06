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
#!perl -w
#line 15

use strict;
use ActiveState::RelocateTree qw(relocate spongedir rel2abs);
use Config;
use Getopt::Std;
use vars qw(
    $opt_a $opt_b $opt_d $opt_e $opt_f $opt_i $opt_t $opt_r $opt_v
    *OLDERR
);

my $logname;

BEGIN {
    # If we're being run via wperl, redirect the output streams to a log file.
    if ($^O eq 'MSWin32' and $^X =~ /\bwperl(.exe)?\z/i) {
	my $tmp = $ENV{TEMP} || $ENV{tmp} || "$ENV{SystemDrive}/" || "c:/temp";
	$logname = "$tmp/ActivePerlInstall.log";
	open(STDERR, ">> $logname");
	open(STDOUT, ">&STDERR");
    }
}

my $frompath_default = $Config{prefix};

getopts('abde:f:itrv') or usage('');

my $topath      = shift || usage('');
my $frompath    = shift || $frompath_default;
# MSI insists on handing us paths with backslashes at the end
if ($^O eq 'MSWin32') {
    $topath =~ s{\\\z}{};
    $frompath =~ s{\\\z}{};
}
my $destpath    = $opt_e || $topath;
my $filelist    = $opt_f || '';

usage("$destpath is longer than $frompath")
    if length($destpath) > length($frompath) and ! $opt_a;
usage("$destpath is longer than " . spongedir('thisperl'))
    if length($destpath) > length(spongedir('thisperl')) and ! $opt_t;

if (-d $topath) {
    if (not -d $frompath) {
	#warn "Will do inplace edit of `$topath'\n";
	$opt_i++;
    }
}
elsif ($opt_i) {
    usage("Directory `$topath' doesn't exist, can't do inplace edit");
}

sub usage {
    my $msg = shift;
    warn <<EOT;
    $msg
    Usage:
        $0 [-a] [-b] [-d] [-e destpath] [-f logfile] [-i] [-t] [-r] [-v]
           topath [frompath]

        -a              allow destpath to be longer than frompath
        -b              don't delete backups after edit
        -d              delete source tree after relocation
        -e destpath     edit files to contain this path instead of `frompath'
                          (defaults to `topath')
        -f logfile      creates `logfile' and writes the full path name of
                          each file that was modified (one line per file)
        -i              edit perl installation at `topath' insitu
                          (makes no attempt to move tree, -d is ignored)
        -t              only edit text files
        -r              do not run `ranlib' on *.a files that were edited
        -v              verbose messages

    'destpath' defaults to `topath'

    'frompath' defaults to '$frompath_default'

    'destpath' must be shorter than 'frompath' unless the -a option is
    specified.

    'destpath' must shorter than the path built into this Perl binary,
    unless the -t option is given. The -a switch cannot override this
    restriction.

    -i is assumed if `topath' exists, is a directory, and `frompath'
    doesn't exist.
EOT
    exit(1);
}

relocate(
    to		=> $topath,
    from	=> $frompath,
    replace	=> $destpath,
    verbose	=> $opt_v,
    filelist	=> $filelist,
    ranlib	=> (not $opt_r),
    textonly	=> $opt_t,
    savebaks	=> $opt_b,
    inplace	=> $opt_i,
    killorig	=> $opt_d,
    usenlink	=> 0, # don't use nlink: broken on HP-UX.
);

__END__

=head1 NAME

reloc_perl - relocate a perl installation

=head1 SYNOPSIS

  reloc_perl [-a] [-b] [-d] [-e destpath] [-f file] [-i] [-t] [-r] [-v]
             topath [frompath]

=head1 DESCRIPTION

This tool will move a perl installation wholesale to a new location.

Edits path names in binaries (e.g., a2p, perl, libperl.a) to reflect the
new location, but preserves the size of strings by null padding them as
necessary.

Edits text files by simple substitution.

'destpath' cannot be longer than 'frompath'.

If 'frompath' is not found in any files, no changes whatsoever are made.

Running the tool without arguments provides more help.

=head1 COPYRIGHT

(c) 1999-2001 ActiveState Tool Corp.  All rights reserved.

=cut


__END__
:endofperl
