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

require 5.006;	# require 5.6.0
use strict;

# A command-line shell implementation. The code which invokes it is at the
# bottom of this file.
package PPMShell;
use base qw(PPM::Term::Shell);

use Data::Dumper;
use Text::Autoformat qw(autoformat form);
use Getopt::Long;

# These must come _after_ the options parsing.
require PPM::UI;
require PPM::Trace;
PPM::Trace->import(qw(trace));

my $NAME	= q{PPM - Programmer's Package Manager};
my $SHORT_NAME	= q{PPM};
our $VERSION	= '3.1';

sub dictsort(@);

#=============================================================================
# Output Methods
#
# PPM behaves differently under different calling circumstances. Here are the
# various classes of messages it prints out:
# 1. error/warning	- an error or "bad thing" has occurred
# 2. informational	- required information like search results
# 3. verbose		- verbose that's only needed in interactive mode
#
# Here are the cases:
# 1. PPM is in interactive mode: everything gets printed.
# 2. PPM is in batch mode: everything minus 'verbose' gets printed.
#=============================================================================
sub error {
    my $o = shift;
    return 1 unless $o->{SHELL}{output}{error};
    CORE::print STDERR @_;
}
sub errorf {
    my $o = shift;
    return 1 unless $o->{SHELL}{output}{error};
    CORE::printf STDERR @_;
}
sub warn { goto &error }
sub warnf { goto &errorf }
sub inform {
    my $o = shift;
    return 1 unless $o->{SHELL}{output}{inform};
    CORE::print @_;
}
sub informf {
    my $o = shift;
    return 1 unless $o->{SHELL}{output}{inform};
    CORE::printf @_;
}
sub verbose {
    my $o = shift;
    return 1 unless $o->{SHELL}{output}{verbose};
    CORE::print @_;
}
sub verbosef {
    my $o = shift;
    return 1 unless $o->{SHELL}{output}{verbose};
    CORE::printf @_;
}
sub assertw {
    my $o = shift;
    my $cond = shift;
    my $msg = shift;
    $o->warn("Warning: $msg\n") unless $cond;
    return $cond;
}
sub assert {
    my $o = shift;
    my $cond = shift;
    my $msg = shift;
    $o->error("Error: $msg\n") unless $cond;
    return $cond;
}

sub mode {
    my $o = shift;
    $o->{SHELL}{mode};
}
sub setmode {
    my $o = shift;
    my $newmode = shift || '';
    my $oldmode = $o->{SHELL}{mode};
    if ($newmode eq 'SHELL') {
	$o->{SHELL}{output}{error}   = 1;
	$o->{SHELL}{output}{inform}  = 1;
	$o->{SHELL}{output}{verbose} = 1;
    }
    elsif ($newmode eq 'BATCH') {
	$o->{SHELL}{output}{error}   = 1;
	$o->{SHELL}{output}{inform}  = 1;
	$o->{SHELL}{output}{verbose} = 0;
    }
    elsif ($newmode eq 'SCRIPT') {
	$o->{SHELL}{output}{error}   = 1;
	$o->{SHELL}{output}{inform}  = 1;
	$o->{SHELL}{output}{verbose} = 0;
    }
    elsif ($newmode eq 'SILENT') {
	$o->{SHELL}{output}{error}   = 1;
	$o->{SHELL}{output}{inform}  = 0;
	$o->{SHELL}{output}{verbose} = 0;
    }
    $o->{SHELL}{mode} = $newmode;
    return $oldmode;
}

# Older versions of PPM3 had one "Active" repository. This code reads
# $o->conf('repository') if it exists, and moves it into
# $o->conf('active_reps'), which is a list. The old one is deleted -- old PPMs
# will reset it if needed, but it will be ignored if 'active_reps' exists.
sub init_active_reps {
    my $o = shift;

    if ($o->conf('repository') and not $o->conf('active_reps')) {
	my @active = $o->conf('repository');
	delete $o->{SHELL}{conf}{DATA}{repository};
	$o->conf('active_reps', \@active);
    }
    elsif (not defined $o->conf('active_reps')) {
	my @active = $o->reps_all; # enable all repositories
	$o->conf('active_reps', \@active);
    }
}

sub init {
    my $o = shift;
    $o->cache_clear('query');
    $o->cache_clear('search');
    $o->{API}{case_ignore} = 1;

    # Load the configuration;
    $o->{SHELL}{conf} = PPM::Config::load_config_file('cmdline');
    $o->init_active_reps;

    # check whether there's a target in the parent's perl that hasn't been
    # installed in the "targets" file:
    my $ppmsitelib = $ENV{PPM3_PERL_SITELIB};
    if ($ppmsitelib and opendir(PPMDIR, "$ppmsitelib/ppm-conf")) {
        my @files = map  { "$ppmsitelib/ppm-conf/$_" }
	            grep { /^ppminst/i && !/(~|\.bak)\z/ } readdir PPMDIR;
	closedir PPMDIR;
	my $found = 0;
	if (@files == 1) {
	    my @targets = PPM::UI::target_list()->result_l;
	    for my $target (@targets) {
		my $info = PPM::UI::target_raw_info($target);
		next unless $info and $info->is_success;
		++$found and last
		    if path_under($info->result->{path}, $files[0]);
	    }
	    unless ($found) {
		# We're going to add a new target:
		# 1. if we can find ppm3-bin.cfg, use that
		# 2. if not, guess lots of stuff
		my $ppm3_bin_cfg = "$ENV{PPM3_PERL_PREFIX}/bin/ppm3-bin.cfg";
		my $r = PPM::UI::target_add(undef, From => $ppm3_bin_cfg)
		    if -f $ppm3_bin_cfg;
		unless ($r and $r->is_success) {
		    PPM::UI::target_add(
			'TEMP',
			type => 'Local',
			path => $files[0],
		    );
		}
	    }
	}
    }

    # set the initial target:
    if (defined $o->{API}{args}{target}) {
	my $t = $o->{API}{args}{target};
	my $prefix = $ENV{PPM3_PERL_PREFIX};
	if ($t ne 'auto') {
	    # A full name or number given:
	    $o->run('target', 'select', $o->{API}{args}{target});
	}
	elsif ($prefix) {
	    # Auto-select target, based on where we came from:
	    my @l = $o->conf('target');
	    push @l, PPM::UI::target_list()->result_l;
	    for my $target (@l) {
		next unless $target;
		my $info = PPM::UI::target_raw_info($target);
		next unless $info and $info->is_success;
		next unless path_under($info->result->{path}, "$prefix/");
		my $mode = $o->setmode('SILENT');
		$o->run('target', 'select', $target);
		$o->setmode($mode);
		last;
	    }
	}
    }
}

sub preloop {
    my $o = shift;

    if ($o->conf('verbose-startup') and $o->mode eq 'SHELL') {
	my $profile_track = $o->conf('profile-track');
	chomp (my $startup = <<END);
$NAME version $VERSION.
Copyright (c) 2001 ActiveState Corp. All Rights Reserved.
ActiveState is a devision of Sophos.

Entering interactive shell.
END

	my $file = PPM::Config::get_license_file();
	my $license;
	if (open (my $LICENSE, $file)) {
	    $license = do { local $/; <$LICENSE> };
	}
	my $aspn = $license =~ /ASPN/;
	my $profile_tracking_warning = ($profile_track || !$aspn) ? '' : <<'END';

Profile tracking is not enabled. If you save and restore profiles manually,
your profile may be out of sync with your computer. See 'help profile' for
more information.
END
	$o->inform($startup);
	$o->inform(<<END);
 Using $o->{API}{readline} as readline library.
$profile_tracking_warning
Type 'help' to get started.

END
    }
    else {
	$o->inform("$NAME ($VERSION). Type 'help' to get started.\n");
    }

    $o->term->SetHistory(@{$o->conf('history') || []})
	if $o->term->Features->{setHistory};
}

sub postloop {
    my $o = shift;
    trace(1, "PPM: exiting...\n");
    if ($o->mode eq 'SHELL' and $o->term->Features->{getHistory}) {
	my @h = $o->term->GetHistory;
	my $max_history = $o->conf('max_history') || 100;
	splice @h, 0, (@h - $max_history)
	    if @h > $max_history;
	my $old = $o->setmode('SILENT');
	$o->conf('history', \@h);
	$o->setmode($old);
    }
}

#============================================================================
# Cache of search and query results
#============================================================================
sub cache_set_current {
    my $o = shift;
    my $type = shift;
    my $set = shift;
    $set = $o->{SHELL}{CACHE}{$type}{current} unless defined $set;
    $o->{SHELL}{CACHE}{$type}{current} = $set;
    return $o->{SHELL}{CACHE}{$type}{current};
}

sub cache_set_index {
    my $o = shift;
    my $type = shift;
    my $index = shift;
    $index = $o->{SHELL}{CACHE}{$type}{index} unless defined $index;
    $o->{SHELL}{CACHE}{$type}{index} = $index;
    return $o->{SHELL}{CACHE}{$type}{index};
}

sub cache_set_add {
    my $o = shift;
    my $type = shift;
    my $query = shift;
    my $entries = shift;
    my $sort_field = $o->conf('sort-field');
    my @sorted = $o->sort_pkgs($sort_field, @$entries);
    my $set = {
		  query => $query,
		  raw => $entries,
		  $sort_field => \@sorted,
		};
    push @{$o->{SHELL}{CACHE}{$type}{sets}}, $set;
}

sub cache_entry {
    my $o = shift;
    my $type = shift;		# 'query' or 'cache';
    my $index = shift;		# defaults to currently selected index
    my $set = shift;		# defaults to currently selected set

    $index = $o->{SHELL}{CACHE}{$type}{index} unless defined $index;

    my $src = $o->cache_set($type, $set);
    return undef unless $src and bounded(0, $index, $#$src);

    # Make sure we display only valid entries:
    my $tar = $o->conf('target');
    $src->[$index]->make_complete($tar);
    return $src->[$index];
}

sub cache_set {
    my $o = shift;
    my $type = shift;		# 'query' or 'cache'
    my $set = shift;		# defaults to currently selected set
    my $entry = shift;		# defaults to 'results';

    $entry = $o->conf('sort-field') unless defined $entry;
    return undef unless grep { lc($entry) eq $_ } (sort_fields(), 'query');

    $set = $o->{SHELL}{CACHE}{$type}{current} unless defined $set;
    my $src = $o->{SHELL}{CACHE}{$type}{sets};

    return undef unless defined $set;
    return undef unless bounded(0, $set, $#$src);

    # We've changed sort-field at some point -- make sure the sorted data
    # exists, or else build it:
    unless (defined $src->[$set]{$entry}) {
	my $raw = $src->[$set]{raw};
	my @sorted = $o->sort_pkgs($entry, @$raw);
	$src->[$set]{$entry} = \@sorted;
    }
    
    return wantarray ? @{$src->[$set]{$entry}} : $src->[$set]{$entry};
}

sub cache_clear {
    my $o = shift;
    my $type = shift;		# 'query' or 'cache'
    $o->{SHELL}{CACHE}{$type}{sets} = [];
    $o->{SHELL}{CACHE}{$type}{current} = -1;
    $o->{SHELL}{CACHE}{$type}{index} = -1;
}

sub cache_sets {
    my $o = shift;
    my $type = shift;
    @{$o->{SHELL}{CACHE}{$type}{sets}};
}

# This sub searches for an entry in the cache whose name matches that thing
# passed in. It searches in the current cache first. If the name isn't found,
# it searches in all caches. If the name still isn't found, it returns undef.
sub cache_find {
    my $o = shift;
    my $type = shift;
    my $name = shift;

    my $ncaches = $o->cache_sets($type);
    my $current = $o->cache_set_current($type);

    # First, search the current set:
    my @pkgs = map { $_ ? $_->name : '' } $o->cache_set($type);
    my $ind  = find_index($name, 0, @pkgs);
    return ($current, $ind) if $ind >= 0;

    # Now try to find in all the sets:
    for my $s (0 .. $ncaches - 1) {
	next if $s == $current;
	@pkgs = map { $_ ? $_->name : '' } $o->cache_set($type, $s);
	$ind  = find_index($name, 0, @pkgs);
	return ($s, $ind) if $ind >= 0;
    }
    return (-1, -1);
}

# A pretty separator to print between logically separate items:
my $SEP;
BEGIN {
    $SEP = '=' x 20;
}

# Useful functions:
sub max (&@) {
    my $code = shift;
    my $max;
    local $_;
    for (@_) {
	my $res = $code->($_);
	$max = $res if not defined $max or $max < $res;
    }
    $max || 0;
}

sub min (&@) {
    my $code = shift;
    my $min;
    local $_;
    for (@_) {
	my $res = $code->($_);
	$min = $res if not defined $min or $min > $res;
    }
    $min || 0;
}

sub sum (&@) {
    my $code = shift;
    my $sum = 0;
    local $_;
    for (@_) {
	my $res = $code->($_);
	$sum += $res if defined $res;
    }
    $sum || 0;
}

#============================================================================
# Repository:
# rep			# displays repositories
# rep add http://...	# adds a new repository
# rep del <\d+>		# deletes the specified repository
# rep [set] 1		# sets the specified repository active
#============================================================================
sub smry_repository { "adds, removes, or sets repositories" }
sub help_repository { <<'END' }
repository -- Repository Control
  Synopsis
     rep                        Displays all repositories
     rep add [name] <location>  Adds a new repository; makes it active
     rep delete <name or num>   Deletes specified repository
     rep describe <name or num> Displays information about the specified
                                repository
     rep rename <name or num> <name>
                                Renames the specified repository to
                                the given name
     rep on <name>              Activates the specified repository
     rep off <name or num>      Removes the repository from the active list
     rep up <name or num>       Moves the specified repository up one
     rep down <name or num>     Moves the specified repository down one

    The <name> needs to be put inside doublequotes if it contains any
    spaces.

  Description
    The *repository* (or *rep*) command controls two lists or repositories:

    1   The list of "active" repositories. This is the list of repositories
        used by *search*, *install*, and *upgrade*.

    2   The list of all known repositories. You can designate a repository
        "inactive", which means PPM will not use it in any commands.

    If no arguments are given, the rep command will list the active
    repositories defined in the PPM settings. The order is significant: when
    installing a package, PPM will try the first repository, then the
    second, and so on, until it find the package you asked for. When
    searching, PPM merges the results of all the repositories together, so
    the order is less important (see the *search* command).

    For example, when you enter:

        rep

    PPM3 will return something resembling this:

        Repositories:
        [1] ActiveCD
        [2] ActiveState Package Repository
        [ ] An inactive repository

    In the example above, entering 'rep off 2' will disable the second
    repository (the ActiveStat Package Repository). To add another
    repository:

        rep add [options] <NAME> <LOCATION>

    The following options are available for the 'add' command:

    *   -username

    *   -password

    These options allow you to specify a username and password to be used
    when logging into a repository. Currently, these are only used for FTP
    and WWW repositories.

    For example:

        rep add "EZE" http://foo.com/MyPPMPackages

    with "EZE" being the name of the repository (for easy reference) and the
    location noted by the http location. If you were to enter the rep
    command again, you would see:

        ppm> rep
        Repositories:
        [1] ActiveCD
        [2] ActiveState Package Repository
        [3] EZE
        [ ] An inactive repository

    To move the new repository to the top of the Active list, you would
    type:

        ppm> rep up EZE
        Repositories:
        [1] ActiveCD
        [2] EZE
        [3] ActiveState Package Repository
        [ ] An inactive repository
        ppm> rep up EZE
        Repositories:
        [1] EZE
        [2] ActiveCD
        [3] ActiveState Package Repository
        [ ] An inactive repository

    To disable the ActiveCD repository temporarily, enter the following:

        ppm> rep off ActiveCD
        Repositories:
        [1] EZE
        [2] ActiveState Package Repository
        [ ] ActiveCD
        [ ] An inactive repository

    To describe a repository, refer to it either by name, or by the number
    displayed next to the repository in the Active Repositories list. You
    must refer to inactive repositories by their full name.

        ppm> rep describe 2
        Describing Active Repository 2:
            Name: ActiveState Package Repository
        Location: http://ppm.ActiveState.com/cgibin/PPM/...
            Type: PPMServer 2.00
        ppm> rep describe ActiveCD
        Describing Inactive Repository:
            Name: ActiveCD
        Location: F:\PPMPackages\5.8plus
            Type: Local Directory

    To re-activate the ActiveCD repository, use the *rep on* command. You
    must refer to inactive repositories by name, not number.

        ppm> rep on ActiveCD
        Active Repositories:
        [1] EZE
        [2] ActiveState Package Repository
        [3] ActiveCD
        [ ] An inactive repository

  Repository Types
    PPM3 supports several types of package repositories:

    1.  PPM Server 3

        ActiveState's SOAP-driven package server. Because all searches are
        done server-side, the server can deliver much richer information
        about packages than other repositories.

    2.  PPM Server 2

        The SOAP server designed for PPM version 2. PPM 3.1 ships with the
        PPM2 repository as well as the PPM3 repository, so you can use
        either. Simple searches are performed server-side. If your search is
        too complicated for the server, PPM 3.1 will download the package
        summary and search by itself.

    3.  Web Repositories

        Older versions of PPM used non-SOAP repositories (directories full
        of PPD files accessible using a web browser). Over the history of
        PPM, there have been several different ways of organising the files
        so that PPM can search for packages properly. PPM3 tries to download
        a summary file first -- if that fails, it gets the directory index.
        It parses the summary or the index, and caches it. Searches are done
        from the cache.

    4.  FTP Repositories

        FTP is another way of exposing a directory full of PPD files. PPM3
        consideres FTP repositories a subset of Web repositories. Treat them
        as identical: PPM3 downloads the summary or the "index" (file
        listing in this case), parses it, and then searches from it.

    5.  Local Repositories

        To support installing packages from the ActiveCD, a local directory
        can be a repository. PPM searches the files in the directory. All
        valid path formats are supported, including UNC paths.
END
sub comp_repository {
    my $o = shift;
    my ($word, $line, $start) = @_;
    my @words = $o->line_parsed($line);
    my $words = scalar @words;
    my @reps = PPM::UI::repository_list()->result_l;
    my $reps = @reps;
    my @compls = qw(add delete describe rename set select);
    push @compls, ($reps ? (1 .. $reps) : ()); 

    if ($words == 1 or $words == 2 and $start != length($line)) {
	return $o->completions($word, \@compls);
    }
    if ($words == 2 or $words == 3 and $start != length($line)) {
	return (readline::rl_filename_list($word))
	  if $words[1] eq 'add';
	return $o->completions($word, [1 .. $reps])
	  if $o->completions($words[1], [qw(delete describe rename set select)]) == 1;
    }
    ();
}
sub reps_all {
    my $o = shift;
    my $l = PPM::UI::repository_list();
    unless ($l->is_success) {
	$o->warn($l->msg);
	return () unless $l->ok;
    }
    $l->result_l;
}
sub reps_on {
    my $o = shift;
    return @{$o->conf('active_reps')};
}
sub reps_off {
    my $o = shift;
    my @reps = $o->reps_all;
    my @reps_on = $o->reps_on;
    my @off;
    for my $r (@reps) {
	push @off, $r unless grep { $_ eq $r } @reps_on;
    }
    @off;
}
sub rep_on {
    my $o = shift;
    my $rep = shift;
    my @reps = ($o->reps_on, $rep);
    my $m = $o->setmode('SILENT');
    $o->conf('active_reps', \@reps);
    $o->setmode($m);
}
sub rep_off {
    my $o = shift;
    my $rep = shift;
    my @reps = grep { $_ ne $rep } $o->reps_on;
    my $m = $o->setmode('SILENT');
    $o->conf('active_reps', \@reps);
    $o->setmode($m);
}
sub rep_ison {
    my $o = shift;
    my $rep = shift;
    scalar grep { $_ eq $rep } $o->reps_on;
}
sub rep_isoff {
    my $o = shift;
    my $rep = shift;
    scalar grep { $_ eq $rep } $o->reps_off;
}
sub rep_exists {
    my $o = shift;
    my $rep = shift;
    scalar grep { $_ eq $rep } $o->reps_all;
}
sub rep_uniq {
    my $o = shift;
    my $rep = shift;
    unless ($o->rep_exists($rep) or $rep =~ /^\d+$/) {
	/\Q$rep\E/i and return $_ for $o->reps_all;
    }
    $rep;
}
sub rep_up {
    my $o = shift;
    my $rep = shift;
    my @reps = $o->reps_on;
    my $ind = find_index($rep, 0, @reps);
    if (bounded(1, $ind, $#reps)) {
	@reps = (
	    @reps[0 .. $ind - 2],
	    $rep,
	    $reps[$ind - 1],
	    @reps[$ind + 1 .. $#reps]
	);
    }
    my $m = $o->setmode('SILENT');
    $o->conf('active_reps', \@reps);
    $o->setmode($m);
}
sub rep_down {
    my $o = shift;
    my $rep = shift;
    my @reps = $o->reps_on;
    my $ind = find_index($rep, 0, @reps);
    if (bounded(0, $ind, $#reps - 1)) {
	@reps = (
	    @reps[0 .. $ind - 1],
	    $reps[$ind + 1],
	    $rep,
	    @reps[$ind + 2 .. $#reps]
	);
    }
    my $m = $o->setmode('SILENT');
    $o->conf('active_reps', \@reps);
    $o->setmode($m);
}
sub run_repository {
    my $o = shift;
    my @args = @_;
    my (@reps, @reps_off, @reps_on);
    my $refresh = sub {
	@reps = $o->reps_all;
	@reps_off = $o->reps_off;
	@reps_on = $o->reps_on;
    };
    &$refresh;
    trace(1, "PPM: repository @args\n");

    if (@args) {
	my $cmd = shift @args;
	#=====================================================================
	# add, delete, describe, rename commands:
	#=====================================================================
	if (matches($cmd, "add")) {
	    # Support for usernames and passwords.
	    my ($user, $pass);
	    {
		local *ARGV;
		@ARGV = @args;
		GetOptions(
		    "username=s"	=> \$user,
		    "password=s"	=> \$pass,
		);
		@args = @ARGV;
	    }
	    $o->warn(<<END) and return unless @args;
repository: invalid 'add' command arguments. See 'help repository'.
END
	    my $url  = pop @args;
	    my $name = join(' ', @args);
	    unless ($name) {	# rep add http://...
		$name = 'Autonamed';
		for (my $i=1; $i<=@reps; $i++) {
		    my $tmp = "$name $i";
		    $name = $tmp and last
		      unless (grep { $tmp eq $_ } @reps);
		}
	    }
	    my $ok = PPM::UI::repository_add($name, $url, $user, $pass);
	    unless ($ok->is_success) {
		$o->warn($ok->msg);
		return unless $ok->ok;
	    }
	    $o->rep_on($name);
	    $o->cache_clear('search');
	}
	elsif (matches($cmd, "del|ete")) {
	    my $arg = join(' ', @args);
	    my $gonner = $arg;
	    if ($arg =~ /^\d+$/) {
		return unless $o->assert(
		    bounded(1, $arg, scalar @reps_on),
		    "no such active repository $arg"
		);
		$gonner = $reps_on[$arg - 1];
	    }
	    else {
		$gonner = $o->rep_uniq($gonner);
		return unless $o->assert(
		    $o->rep_exists($gonner),
		    "no such repository '$gonner'"
		);
	    }
	    my $ok = PPM::UI::repository_del($gonner);
	    unless ($ok->is_success) {
		$o->warn($ok->msg);
		return unless $ok->ok;
	    }
	    $o->rep_off($gonner);
	    $o->cache_clear('search');
	}
	elsif (matches($cmd, "des|cribe")) {
	    my $arg = join(' ', @args) || 1;
	    my $rep = $arg;
	    if ($arg =~ /^\d+$/) {
		return unless $o->assert(
		    bounded(1, $arg, scalar @reps_on),
		    "no such active repository $arg"
		);
		$rep = $reps_on[$arg - 1];
	    }
	    else {
		$rep = $o->rep_uniq($rep);
		return unless $o->assert(
		    $o->rep_exists($rep),
		    "no such repository '$rep'"
		);
	    }
	    my $info = PPM::UI::repository_info($rep);
	    unless ($info->is_success) {
		$o->warn($info->msg);
		return unless $info->ok;
	    }
	    my $type = $o->rep_ison($rep) ? "Active" : "Inactive";
	    my $num  = (
		$o->rep_ison($rep)
		? " " . find_index($rep, 1, @reps_on)
		: ""
	    );
	    my @info = $info->result_l;
	    my @keys = qw(Name Location Type);
	    push @keys, qw(Username) if @info >= 4;
	    push @keys, qw(Password) if @info >= 5;
	    $o->inform("Describing $type Repository$num:\n");
	    $o->print_pairs(\@keys, \@info);
	    return 1;
	}
	elsif (matches($cmd, 'r|ename')) {
	    my $name = pop @args;
	    my $arg = join(' ', @args);
	    my $rep = $arg;
	    if ($arg =~ /^\d+$/) {
		return unless $o->assert(
		    bounded(1, $arg, scalar @reps_on),
		    "no such active repository $arg"
		);
		$rep = $reps_on[$arg - 1];
	    }
	    else {
		$rep = $o->rep_uniq($rep);
		return unless $o->assert(
		    $o->rep_exists($rep),
		    "no such repository '$rep'"
		);
	    }
	    my $ok = PPM::UI::repository_rename($rep, $name);
	    unless ($ok->is_success) {
		$o->warn($ok->msg);
		return unless $ok->ok;
	    }
	    $o->rep_on($name) if $o->rep_ison($rep);
	    $o->rep_off($rep);
	    $o->cache_clear('search');
	}

	#=====================================================================
	# On, off, up, and down commands:
	#=====================================================================
	elsif (matches($cmd, 'on')) {
	    my $rep = $o->rep_uniq(join(' ', @args));
	    return unless $o->assert(
		$o->rep_isoff($rep),
		"no such inactive repository '$rep'"
	    );
	    $o->rep_on($rep);
	    $o->cache_clear('search');
	}
	elsif (matches($cmd, 'of|f')) {
	    my $arg = join(' ', @args);
	    my $rep = $arg;
	    if ($arg =~ /^\d+$/) {
		return unless $o->assert(
		    bounded(1, $arg, scalar @reps_on),
		    "no such active repository $arg"
		);
		$rep = $reps_on[$arg - 1];
	    }
	    else {
		$rep = $o->rep_uniq($rep);
		return unless $o->assert(
		    $o->rep_exists($rep),
		    "no such repository '$rep'"
		);
	    }
	    $o->rep_off($rep);
	    $o->cache_clear('search');
	}
	elsif (matches($cmd, 'up')) {
	    my $arg = join(' ', @args);
	    my $rep = $arg;
	    if ($arg =~ /^\d+$/) {
		return unless $o->assert(
		    bounded(1, $arg, scalar @reps_on),
		    "no such active repository $arg"
		);
		$rep = $reps_on[$arg - 1];
	    }
	    else {
		$rep = $o->rep_uniq($rep);
		return unless $o->assert(
		    $o->rep_exists($rep),
		    "no such repository '$rep'"
		);
	    }
	    $o->rep_up($rep);
	}
	elsif (matches($cmd, 'do|wn')) {
	    my $arg = join(' ', @args);
	    my $rep = $arg;
	    if ($arg =~ /^\d+$/) {
		return unless $o->assert(
		    bounded(1, $arg, scalar @reps_on),
		    "no such active repository $arg"
		);
		$rep = $reps_on[$arg - 1];
	    }
	    else {
		$rep = $o->rep_uniq($rep);
		return unless $o->assert(
		    $o->rep_exists($rep),
		    "no such repository '$rep'"
		);
	    }
	    $o->rep_down($rep);
	}

	else {
	    $o->warn(<<END) and return;
No such repository command '$cmd'; see 'help repository'.
END
	}
    }
    &$refresh;
    unless(@reps) {
	$o->warn("No repositories. Use 'rep add' to add a repository.\n");
    }
    else {
	my $i = 0;
	my $count = @reps_on;
	my $l = length($count);
	$o->inform("Repositories:\n");
	for my $r (@reps_on) {
	    my $n = sprintf("%${l}d", $i + 1);
	    $o->inform("[$n] $r\n");
	    $i++;
	}
	for my $r ($o->dictsort(@reps_off)) {
	    my $s = ' ' x $l;
	    $o->inform("[$s] $r\n");
	}
    }
    1;
}

#============================================================================
# Search:
# search		# displays previous searches
# search <\d+>		# displays results of previous search
# search <terms>	# executes a new search on the current repository
#============================================================================
sub smry_search { "searches for packages in a repository" }
sub help_search { <<'END' }
search -- Search for Packages
  Synopsis
     search                Displays list of previous searches
     search <number>       Displays results of search <number>
     search <glob pattern> Performs a new search
     search <field>=<glob> Searches for all packages matching the field.
     search *              Displays all packages in the current repository

    The available fields are 'ABSTRACT', 'NAME', 'TITLE', 'AUTHOR', and
    'VERSION'. 'NAME' is used when you do not specify a field.

  Description
    Use the search command to look through the repository for packages. PPM
    version 3 provides powerful search functionality. For example:

    1.  Search for 'CGI' anywhere in the name:

          search CGI

        Example results:

          Apache-CGI
          CGI-Application
          CGI-ArgChecker

    2.  Search for 'CGI' at the beginning of the name:

          search CGI*

        Example results:

          CGI-ArgChecker
          CGI-Application

    3.  Search for all modules authored by someone with 'smith' in their
        name or email:

          search AUTHOR=smith 

        Example results:

          Apache-ProxyPass
          Business-ISBN

    4.  Search for 'compress' anywhere in the abstract:

          search ABSTRACT=compress

        Example results:

          Apache-GzipChain
          IO-Zlib

    5.  Search for 'CGI' in the name, or 'web' in the abstract:

          search CGI or ABSTRACT=web

        Example results:

          CGI-XMLForm
          HTML-Clean

    6.  Search for 'XML' in the name and either 'parser' in the name or
        'pars' in the abstract, but not with 'XPath' in the name:

          search XML and (parser or ABSTRACT=pars) and not XPath

        Example results:

          XML-Node
          XML-Parser-EasyTree

    7.  PPM Server 3 repositories only: search by module name, even if
        unrelated to the containing package:

          search Data::Grove
                                
        Example results:

          libxml-perl

    8.  Browse all packages in the repository:

          search *

        Example results:

          Affix-Infix2Postfix
          AI-Fuzzy
          [many more...]

    Recall previous searches using the 'search <number>' command. PPM stores
    searches for each session until you exit PPM.

    Some package names or versions are too long to be displayed in the
    search results. If a name is too long, you will see a '~' (tilde) as the
    last visible character in the column. You can use *describe* to view
    detailed information about such packages.

  Search Results
    When you type a command like "search XML", PPM searches in each of the
    Active Repositories (see the *repository* command) for your package. The
    results are merged into one list, and duplicates (packages found in more
    than one repository) are hidden.

    You can control what fields PPM shows for each package. The fields each
    have a built-in weight, which is used to calculate how wide to make each
    field based on the width of your screen. Information that doesn't fit
    into a field is truncated, and a tilde ("~") character is displayed in
    the last column of the field.

    Let's get down to an example:

        ppm> search XML
        Searching in Active Repositories
            1. CGI-XMLForm           [0.10] Extension to CGI.pm which
            2. Data-DumpXML          [1.01] Dump arbitrary data structures
            3. DBIx-XML_RDB          [0.05] Perl extension for creating XML
            4. DBIx-XMLMessage       [0.03] XML Message exchange between DBI
            5. GoXML-XQI            [1.1.4] Perl extension for the XML Query
            6. Language-DATR-DATR2~ [0.901] manipulate DATR .dtr, XML, HTML,
            7. libxml-perl           [0.07] support for deeply nested
            8. Mail-FilterXML         [0.1] Undetermined
            9. Mail-XML              [0.03] Adds a toXML() method to
           10. Pod-XML               [0.93] Module to convert POD to XML

    As you can see, the three fields being displayed are:

    1   NAME

        The package name

    2   VERSION

        The package version

    3   ABSTRACT

        The package abstract

    You can customize the view somewhat. If you want to view the authors,
    but not the abstract, you can run the same *search* command after using
    *set* to change the fields:

        ppm> set fields="NAME VERSION AUTHOR"
        Setting 'fields' set to 'name version author'.
        ppm> search XML
        Using cached search result set 1.
            1. CGI-XMLForm         [0.10] Matt Sergeant (matt@sergeant.org)
            2. Data-DumpXML        [1.01] Gisle Aas (gisle@aas.no)
            3. DBIx-XML_RDB        [0.05] Matt Sergeant (matt@sergeant.org)
            4. DBIx-XMLMessage     [0.03] Andrei Nossov (andrein@andrein.com)
            5. GoXML-XQI          [1.1.4] Matthew MacKenzie (matt@goxml.com)
            6. Language-DATR-DAT~ [0.901] Lee Goddard (lgoddard@cpan.org)
            7. libxml-perl         [0.07] Ken MacLeod (ken@bitsko.slc.ut.us)
            8. Mail-FilterXML       [0.1] Matthew MacKenzie (matt@goxml.com)
            9. Mail-XML            [0.03] Matthew MacKenzie (matt@goxml.com)
           10. Pod-XML             [0.93] Matt Sergeant (matt@sergeant.org)

    You can change the order in which the results are sorted, and what
    columns are displayed. The settings *fields* and *sort-field* changes
    this. You can sort by any valid field name (even fields which are not
    displayed). See the *settings* command for the valid field names.

    PPM always hides "duplicate" results. It decides whether a result is
    duplicated based on the fields being displayed. If the same package is
    found in more than one repository, but you don't have the REPOSITORY
    field showing, PPM will only list the package once.
END
sub comp_search {()}
sub run_search {
    my $o = shift;
    my @args = @_;
    my $query = $o->raw_args || join ' ', @args;
    trace(1, "PPM: search @args\n\tquery='$query'\n");
    return unless $o->assert(
	scalar $o->reps_on,
	"you must activate a repository before searching."
    );

    # No args: show cached result sets
    unless (@args) {
	my @search_results = $o->cache_sets('search');
	my $search_result_current = $o->cache_set_current('search');
	if (@search_results) {
	    $o->inform("Search Result Sets:\n");
	    my $i = 0;
	    for (@search_results) {
		$o->informf("%s%2d",
		       $search_result_current == $i ? "*" : " ",
		       $i + 1);
		$o->inform(". $_->{query}\n");
		$i++;
	    }
	}
	else {
	    $o->warn("No search result sets -- provide a search term.\n");
	    return;
	}
    }

    # Args:
    else {
	# Show specified result set
	if ($query =~ /^\d+/) {
	    my $set = int($query);
	    my $s = $o->cache_set('search', $set - 1);
	    unless ($set > 0 and defined $s) {
		$o->warn("No such search result set '$set'.\n");
		return;
	    }

	    $query = $o->cache_set('search', $set-1, 'query');
	    $o->inform("Search Results Set $set ($query):\n");
	    $o->print_formatted($s, $o->cache_set_index('search'));
	    $o->cache_set_current('search', $set-1);
	    $o->cache_set_index('search', -1);
	}
       
	# Query is the same as a previous query on the same repository: 
	# Use cached results and set them as default
	elsif(grep { $_->{query} eq $query } $o->cache_sets('search')) {
	    my @entries = $o->cache_sets('search');
	    for (my $i=0; $i<@entries; $i++) {
		if ($o->cache_set('search', $i, 'query') eq $query) {
		    $o->inform("Using cached search result set ", $i+1, ".\n");
		    $o->cache_set_current('search', $i);
		    my $set = $o->cache_set('search');
		    $o->print_formatted($set);
		}
	    }
	}

	# Perform a new search
	else {
	    my @rlist = $o->reps_on;
	    my $targ = $o->conf('target');
	    my $case = not $o->conf('case-sensitivity');

	    $o->inform("Searching in Active Repositories\n");
	    my $ok = PPM::UI::search(\@rlist, $targ, $query, $case);
	    unless ($ok->is_success) {
		$o->warn($ok->msg);
		return unless $ok->ok;
	    }
	    my @matches = $ok->result_l;
	    unless (@matches) {
		$o->warn("No matches for '$query'; see 'help search'.\n");
		return 1;
	    }
	    $o->cache_set_index('search', -1);
	    $o->cache_set_add('search', $query, \@matches);
	    $o->cache_set_current('search', scalar($o->cache_sets('search')) - 1);
	    my @set = $o->cache_set('search');
	    $o->print_formatted(\@set);
	}
    }
    1;
}
sub alias_search { qw(s) }

#============================================================================
# tree
# tree		# shows the dependency tree for the default/current pkg
# tree <\d+>	# shows dep tree for numbered pkg in current search set
# tree <pkg>	# shows dep tree for given package
# tree <url>	# shows dep tree for package located at <url>
# tree <glob>	# searches for matches
#============================================================================
sub smry_tree { "shows package dependency tree" }
sub help_tree { <<'END' }
tree -- Show Dependency Tree for Packages
  Synopsis
     tree                Displays the dependency-tree of the current
                         or default package
     tree <number>       Displays the dependency-tree of the given <number>
     tree <range>        Displays a <range> of dependency-trees
     tree <package name> Displays the dependency-tree of the named package
     tree <url>          Displays the dependency-tree for the
                         package at <url>
     tree <glob pattern> Performs a new search using <glob pattern>

  Description
    The tree command is used to show the "dependency tree" of a given
    package (additional packages that are required by the current package).
    For example:

        tree SOAP-lite

    returns:

        ====================
        SOAP-Lite 0.51
        |__MIME-tools 5.316
        |   |__MailTools 1.15
        |   \__IO-stringy 1.216
        |
        \__MIME-Lite 2.105
        ====================

    SOAP-Lite requires four other packages.

    When tree is called without a <name> or <number> switch, the command
    will return the dependency tree of the first package in the default
    search result. If there is no default search, you will be requested to
    use search to find a package.
END
sub comp_tree { goto &comp_describe }
sub run_tree {
    my $o = shift;
    my @args = @_;
    trace(1, "PPM: tree @args\n");

    # Check for anything that looks like a query. If it does, just
    # send it to search() instead.
    my $query = $o->raw_args || join ' ', @args;
    $query ||= '';
    if ($query and not PPM::UI::is_pkg($args[0]) and not parse_range($query)) {
	$o->inform("Wildcards detected; using 'search' instead...\n");
	return $o->run('search', @_);
    }

    # No Args: describes current index of current result set, or 1.
    unless (@args) {
	my @search_results = $o->cache_sets('search');
	my $search_result_current = $o->cache_set_current('search');
	unless (@search_results and
		bounded(0, $search_result_current, $#search_results)) {
	    $o->warn("No search results to show dependency tree for -- " . 
	      "use 'search' to find a package.\n");
	    return;
	}
	else {
	    my @res = $o->cache_set('search');
	    my $npkgs = @res;
	    $o->inform("$SEP\n");
	    if ($o->cache_entry('search')) {
		my $n = $o->cache_set_index('search') + 1;
		$o->inform("Package $n:\n");
		$o->tree_pkg($o->cache_entry('search'));
	    }
	    elsif (defined $o->cache_entry('search', 0)) {
		$o->inform("Package 1:\n");
		$o->tree_pkg($o->cache_entry('search', 0));
		$o->cache_set_index('search', 0);
	    }
	    else {
		$o->inform("Search Results are empty -- use 'search' again.\n");
	    }
	    $o->inform("$SEP\n");
	}
    }

    # Args provided
    else {

	# Describe a particular number:
	if (my @r = parse_range(@args)) {
	    my @search_results = $o->cache_sets('search');
	    my $search_result_current = $o->cache_set_current('search');
	    unless (bounded(0, $search_result_current, $#search_results)) {
		$o->inform("No search results to show dependency tree for -- " . 
		  "use 'search' to find a package.\n");
		return;
	    }
	    else {
		for my $n (@r) {
		    my $sr = $o->cache_set('search');
		    $o->inform("$SEP\n");
		    if (bounded(1, $n, scalar @$sr)) {
			$o->inform("Package $n:\n");
			$o->tree_pkg($o->cache_entry('search', $n-1));
		    }
		    else {
			$o->inform("No such package $n in result set.\n");
		    }
		    $o->cache_set_index('search', $n - 1);
		}
		$o->inform("$SEP\n");
	    }
	}

	# Describe a particular package
	else {
	    return unless $o->assert(
		scalar $o->reps_on,
		"No repositories -- use 'rep add' to add a repository.\n"
	    );
	    my $pkg =
	      PPM::UI::describe([$o->reps_on], $o->conf('target'), $args[0]);
	    unless ($pkg->is_success) {
		$o->warn($pkg->msg);
		return unless $pkg->ok;
	    }
	    if ($pkg->ok) {
		$o->inform("$SEP\n");
		$o->tree_pkg($pkg->result);
		$o->inform("$SEP\n");
	    }
	}
    }
    1;
}

#============================================================================
# Describe:
# des		# describes default or current package
# des <\d+>	# describes numbered package in the current search set
# des <pkg>	# describes the named package (bypasses cached results)
# des <url>	# describes the package located at <url>
#============================================================================
sub smry_describe { "describes packages in detail" }
sub help_describe { <<'END' }
describe -- Describe Packages
  Synopsis
     des                Describes default/current package
     des <number>       Describes package <number> in the
                        current search set
     des <range>        Describes packages in the given 
                        <range> from the current search
     des <package name> Describes named package
     des <url>          Describes package located at <url>
     des <glob pattern> Performes a new search using <glob pattern>

  Description
    The describe command returns information about a package, including the
    name of the package, the author's name and a brief description (called
    an "Abstract") about the package. For example:

        describe libnet

    returns:

        ===============================
        Package 1
        Name: libnet
        Version: 1.07.03
        Author: Graham Barr
        Abstract: Collection of Network protocol modules
        Implementations:
                1.sun4-solaris-thread-multi
                2.i686-linux-thread-multi
                3.MSWIn32-x86-multi-thread
        ===============================

    There are two modifiers to the describe command:

    -ppd
        Displays the raw PPD of the package.

    -dump
        The same as -ppd.

    When the describe command is called without arguments, it returns
    information about the first package in the current search. If there is
    no default search set, you will be prompted to use the search command to
    find a package.

    If describe is called with a numeric argument, that number is set as the
    default package and the information about that package is returned. If
    the number given doesn't exist, you will be prompted to use search to
    find a package. Also, you can use describe to get descriptions of
    several packages. For example:

        describe 4-7

    will return descriptions of packages 4 through 7 in the current search
    request. You can also enter:

        describe 3-4,10

    to get information on packages 3, 4 and 10.

    If you specify a URL as the argument to describe, PPM will describe the
    package located at the URL. The URL must point to a PPD file. The URL
    can also point to a PPD file on your computer.

    When the describe command is given a name with a wildcard (such as "*"
    or "?") it executes the search command with the given argument. For
    example, describe Tk* will return the name(s) of any packages that match
    the search parameters.

  See Also
    properties
END
sub comp_describe {
    my $o = shift;
    my ($word, $line, $start) = @_;

    # If no search results
    my $n_results = $o->cache_sets('search');
    my $n_current = $o->cache_set_current('search');
    return ()
      unless ($n_results and bounded(0, $n_current, $n_results - 1));
    my @words = $o->line_parsed($line);

    # If the previous word isn't a number or the command, stop.
    return ()
      if ($#words > 0 and
	  $words[$#words] !~ /^\d+/ and
	  $start == length($line) or 
	  $#words > 1);

    # This is the most optimistic list:
    my @results = $o->cache_set('search');
    my $npkgs = @results;
    my @compls = (1 .. $npkgs);

    # If the previous word is a number, return only other numbers:
    return $o->completions($word, \@compls)
      if $words[$#words] =~ /^\d+/;

    # Either a number or the names of the packages
    push @compls, map { $_->name } @results;
    return $o->completions($word, \@compls);
}
sub run_describe {
    my $o = shift;
    my @args = @_;
    
    # Check for options:
    my $ppd;
    {
	local @ARGV = @args;
	GetOptions(ppd => \$ppd, dump => \$ppd);
	@args = @ARGV;
    }

    trace(1, "PPM: describe @args\n");

    # Check for anything that looks like a query. If it does, just
    # send it to search() instead.
    my $query = $o->raw_args || join ' ', @args;
    if ($query and not PPM::UI::is_pkg($args[0]) and not parse_range($query)) {
	$o->inform("Wildcards detected; using 'search' instead...\n");
	return $o->run('search', @_);
    }

    my $dumper = sub {
	my $o = shift;
	my $pkg_obj = shift;
	my $ppd = $pkg_obj->getppd($o->conf('target'))->result;
	$o->page($ppd);
    };
    my $displayer = $ppd ? $dumper : \&describe_pkg;

    # No Args: describes current index of current result set, or 1.
    unless (@args) {
	my @search_results = $o->cache_sets('search');
	my $search_result_current = $o->cache_set_current('search');
	unless (@search_results and
		bounded(0, $search_result_current, $#search_results)) {
	    $o->warn("No search results to describe -- " . 
	      "use 'search' to find a package.\n");
	    return;
	}
	else {
	    my @res = $o->cache_set('search');
	    my $npkgs = @res;
	    $o->inform("$SEP\n");
	    if ($o->cache_entry('search')) {
		my $n = $o->cache_set_index('search') + 1;
		$o->inform("Package $n:\n");
		$o->$displayer($o->cache_entry('search'));
	    }
	    elsif (defined $o->cache_entry('search', 0)) {
		$o->inform("Package 1:\n");
		$o->$displayer($o->cache_entry('search', 0));
		$o->cache_set_index('search', 0);
	    }
	    else {
		$o->warn("Search Results are empty -- use 'search' again.\n");
	    }
	    $o->inform("$SEP\n");
	}
    }

    # Args provided
    else {

	# Describe a particular number:
	if (my @r = parse_range(@args)) {
	    my @search_results = $o->cache_sets('search');
	    my $search_result_current = $o->cache_set_current('search');
	    unless (bounded(0, $search_result_current, $#search_results)) {
		$o->warn("No search results to describe -- " . 
		  "use 'search' to find a package.\n");
		return;
	    }
	    else {
		for my $n (@r) {
		    my $sr = $o->cache_set('search');
		    $o->inform("$SEP\n");
		    if (bounded(1, $n, scalar @$sr)) {
			$o->inform("Package $n:\n");
			$o->$displayer($o->cache_entry('search', $n-1));
		    }
		    else {
			$o->inform("No such package $n in result set.\n");
		    }
		    $o->cache_set_index('search', $n - 1);
		}
		$o->inform("$SEP\n");
	    }
	}

	# Describe a particular package
	else {
	    return unless $o->assert(
		scalar $o->reps_on,
		"No repositories -- use 'rep add' to add a repository.\n"
	    );
	    my ($set, $index) = $o->cache_find('search', $args[0]);
	    my ($ok, $pkg);
	    if ($index >= 0) {
		$o->cache_set_current('search', $set);
		$o->cache_set_index('search', $index);
		$pkg = $o->cache_entry('search');
	    }
	    else {
		$ok = PPM::UI::describe([$o->reps_on],
					$o->conf('target'), $args[0]);
		unless ($ok->is_success) {
		    $o->inform($ok->msg);
		    return unless $ok->ok;
		}
		$pkg = $ok->result;
		$o->cache_set_add('search', $args[0], [$pkg]);
		my $last = $o->cache_sets('search') - 1;
		$o->cache_set_current('search', $last);
		$o->cache_set_index('search', 0);
	    }
	    $o->inform("$SEP\n");
	    $o->$displayer($pkg);
	    $o->inform("$SEP\n");
	}
    }
    1;
}

#============================================================================
# Install:
# i		# installs default or current package
# i <\d+>	# installs numbered package in current search set
# i <pkg>	# installs named package
# i <url>	# installs the package at <url>
#============================================================================
sub smry_install { "installs packages" }
sub help_install { <<'END' }
install -- Install Packages
  Synopsis
     install           Installs default package
     install <number>  Installs packages by a specific <number>
     install <range>   Installs packages in the given numeric <range>
     install <name>    Installs named package
     install <url>     Installs the package located at <url>

  Description
    The install command is used to install packages from the repository.
    Install packages by name or number (the number is given by the
    repository or search request), or set a default package using the
    describe command. You can specify a full URL to a PPD file; the URL may
    point to a PPD file on your computer.

    If you have profile tracking enabled, (see 'help profile') the current
    profile will be updated to include the newly installed package(s).

    The following modifiers can be used with the install command:

    *   -force

    *   -noforce

    *   -follow

    *   -nofollow

    The force and follow switches determine how packages are installed:

     FORCE       FOLLOW          RESULT
     false       false           Checks to see if the package is installed and
                                 if it is, installation stops. If there are any
                                 missing prerequisites, the installation will
                                 fail.

     false       true            Checks to see if the package is installed and
                                 if it is, installation stops. If there are any
                                 missing prerequisites, they are automatically
                                 installed. NOTE: this is the default setting
                                 when PPM is first installed.

     true        false           If the package is installed, PPM will
                                 reinstall the package. If there are any
                                 missing prerequisites, the installation will
                                 fail.

     true        true            If the package is installed, PPM will
                                 reinstall the package. All prerequisites are
                                 installed, missing or not.
    
    If you do not specify any options, install uses the default settings.
    Set or view the current defaults using the 'settings' command.

    For example:

        install foo

    will install the package named "foo", using the default settings.
    Over-ride the defaults using the install modifiers described above.

    For example:

        install foo -force

    will install the "foo" package, even if it has already been installed.
    If both -force and -follow are set to "true", all the prerequisites for
    any package you install will also be installed. For example, the
    installation of a tk-related package, like "tk-ach" which is 8.4 kB will
    be preceded by the installation of Tk, which is 1.7 MB.

    You can also install by package number. Package numbers are based on the
    current repository or current search request. For example:

        install 6

    installs package number 6. You can install more than one package at one
    time:

        install 3-5

    installs packages 3, 4 and 5. You can also type install 3-6,8 to install
    packages 3,4,5,6 and 8.

  See Also
    profile
END
sub comp_install { goto &comp_describe }
sub run_install {
    my $o = shift;
    my @args = @_;
    trace(1, "PPM: install @args\n");

    # Get the install options
    my %opts = (
	force  => $o->conf('force-install'),
	follow => $o->conf('follow-install'),
	dryrun => 0,
    );
    {
	local @ARGV = @args;
	GetOptions('force!'  => \$opts{force},
		   'follow!' => \$opts{follow},
		   'dryrun'  => \$opts{dryrun},
		  );
	@args = @ARGV;
    }

    # No Args -- installs default package
    unless (@args) {
	my @search_results = $o->cache_sets('search');
	my $search_result_current = $o->cache_set_current('search');
	unless (@search_results and
		bounded(0, $search_result_current, $#search_results)) {
	    $o->warn("No search results to install -- " . 
	      "use 'search' to find a package.\n");
	    return;
	}
	else {
	    my @results = $o->cache_set('search');
	    my $npkgs = @results;
	    my $pkg;
	    if ($o->cache_entry('search')) {
		my $n = $o->cache_set_index('search') + 1;
		$o->inform("Package $n:\n");
		$pkg = $o->cache_entry('search');
	    }
	    else {
		$o->inform("Package 1:\n");
		$pkg = $o->cache_entry('search', 0);
	    }
	    return $o->install_pkg($pkg, \%opts);
	}
    }

    # Args provided
    else {

	# Install a particular number:
	if (my @r = parse_range(@args)) {
	    my @search_results = $o->cache_sets('search');
	    my $search_result_current = $o->cache_set_current('search');
	    unless (@search_results and
		    bounded(0, $search_result_current, $#search_results)) {
		$o->warn("No search results to install -- " . 
		  "use 'search' to find a package.\n");
		return;
	    }
	    else {
		my $ok = 0;
		for my $n (@r) {
		    my $sr = $o->cache_set('search');
		    if (bounded(1, $n, scalar @$sr)) {
			$o->inform("Package $n:\n");
			my $pkg = $sr->[$n-1];
			$ok++ if $o->install_pkg($pkg, \%opts);
		    }
		    else {
			$o->inform("No such package $n in result set.\n");
		    }
		}
		return unless $ok;
	    }
	}

	# Install a particular package
	else {
	    unless ($o->reps_all) {
		$o->warn("Can't install: no repositories defined.\n");
	    }
	    else {
		return $o->install_pkg($args[0], \%opts);
	    }
	    return;
	}
    }
    1;
}

#============================================================================
# Target:
# t		# displays a list of backend targets
# t [set] <\d+>	# sets numbered target as default backend target
# t des [<\d+>]	# describes the given (or default) target
#============================================================================
sub smry_targets { "views or sets target installer backends" }
sub help_targets { <<'END' }
targets -- View Target Installer Backends
  Synopsis
     target                      Displays a list of backend targets
     target <number>             Sets <number> as default backend target
     target [select] <name or num>
                                 Sets <name or num> as default backend target
     target describe [name or num]
                                 Describes the given (or default) target
     target set <key> <val>      Sets the target's <key> to <val> 
     target rename <name or num> <name>
                                 Renames the given target to <name>

  Description
    The target is the destination location of the install routine, such as
    the directory where the packages are installed when they're downloaded
    from the repository. For example:

        target

    returns:

        Targets:
          1. ActivePerl 618
        * 2. ActivePerl 629

    This shows that there are two available targets, and that the second
    target (ActivePerl 629) is currently the default (as shown by the
    asterisk). Using multiple targets, you can manage multiple installations
    of Perl from a single command-line.
END
sub comp_targets {
    my $o = shift;
    my ($word, $line, $start) = @_;
    my @words = $o->line_parsed($line);
    my $words = scalar @words;
    my @compls;
    my @targs = PPM::UI::target_list()->result_l;

    # only return 'set' and 'describe' when we're completing the second word
    if ($words == 1 or $words == 2 and $start != length($line)) {
	@compls = ('set', 'select', 'describe', 'rename', 1 .. scalar @targs);
	return $o->completions($word, \@compls);
    }

    if ($words == 2 or $words == 3 and $start != length($line)) {
	# complete 'set'
	if (matches($words[1], 's|et')) {
	    my $targ = $o->conf('target');
	    @compls = map { $_->[0] }
		      grep { $_->[1] }
		      PPM::UI::target_config_keys($targ)->result_l;
	    return $o->completions($word, \@compls);
	}
	# complete 'describe' and 'rename'
	elsif (matches($words[1], 'd|escribe')
	    or matches($words[1], 'r|ename')
	    or matches($words[1], 's|elect')) {
	    return $o->completions($word, [1 .. scalar @targs]);
	}
    }
    ();
}
sub run_targets {
    my $o = shift;
    my @args = @_;
    trace(1, "PPM: target @args\n");

    my @targets = PPM::UI::target_list()->result_l;
    my $targets = @targets;

    # No arguments: print targets
    if (@args) {
	my ($cmd, @rest) = @args;
	if ($cmd =~ /^\d+$/
	    or matches($cmd, 'se|lect')) {
	    my $num = 	$cmd =~ /^\d+$/		? $cmd		:
			$rest[0] =~ /^\d+$/	? $rest[0]	:
			do {
			    my $n = find_index($rest[0], 1, @targets);
			    if ($n < 1) {
				$o->warn("No such target '$rest[0]'.\n");
				return;
			    }
			    $n;
			};

	    # QA the number: is it too high/low?
	    unless(bounded(1, $num, $targets)) {
		$o->warn("No such target number '$num'.\n");
		return;
	    }
	    else {
		$o->conf('target', $targets[$num-1]);
		$o->cache_clear('query');
	    }
	}
	elsif (matches($cmd, 'r|ename')) {
	    my ($oldnum, $newname) = @rest;
	    $oldnum =	$oldnum =~ /^\d+$/ ? $oldnum :
			do {
			    my $n = find_index($oldnum, 1, @targets);
			    if ($n < 1) {
				$o->warn("No such target '$oldnum'.\n");
				return;
			    };
			    $n;
			};
	    unless (defined $oldnum && $oldnum =~ /^\d+$/) {
		$o->warn(<<END);
target: '$cmd' requires a numeric argument. See 'help $cmd'.
END
		return;
	    }
	    unless (bounded(1, $oldnum, $targets)) {
		$o->warn("No such target number '$oldnum'.\n");
		return;
	    }
	    unless (defined $newname and $newname) {
		$newname = '' unless defined $newname;
		$o->warn(<<END);
Target names must be non-empty: '$newname' is not a valid name.
END
		return;
	    }
	    
	    my $oldname = $targets[$oldnum - 1];
	    my $ret = PPM::UI::target_rename($oldname, $newname);
	    $o->warn($ret->msg) unless $ret->ok;
	    $o->conf('target', $newname)
	      if $o->conf('target') eq $oldname;
	    @targets = PPM::UI::target_list()->result_l;
	    $targets = scalar @targets;
	}
	elsif (matches($cmd, "s|et")) {
	    my ($key, $value) = @rest;
	    if (defined $key and $key =~ /=/ and not defined $value) {
		($key, $value) = split /=/, $key;
	    }
	    unless(defined($key) && $key) {
		$o->warn(<<END);
You must specify what option to set. See 'help target'.
END
		return;
	    }
	    unless(defined($value)) {
		$o->warn(<<END);
You must provide a value for the option. See 'help target'.
END
		return;
	    }
	    my $targ = $o->conf('target');
	    my %keys = map { @$_ }
			   PPM::UI::target_config_keys($targ)->result_l;
	    unless ($keys{$key}) {
		$o->warn("Invalid set key '$key'; these are the settable values:\n");
		$o->warn("    $_\n") for (grep { $keys{$_} } keys %keys);
		return;
	    }
	    my $ok = PPM::UI::target_config_set($targ, $key, $value);
	    unless ($ok->is_success) {
		$o->warn($ok->msg);
		return unless $ok->ok;
	    }
	    $o->inform("Target attribute '$key' set to '$value'\n");
	    return 1;
	}
	elsif (matches($cmd, "d|escribe")) {
	    my %opts = (exec => 1);
	    my $sel;
	    if (@rest) {
		local @ARGV = @rest;
		GetOptions(\%opts, 'exec!');
		@rest = @ARGV;
	    }
	    if (@rest) {
		$sel =	$rest[0] =~ /^\d+$/ ? $rest[0] :
			    do {
				my $n = find_index($rest[0], 1, @targets);
				if ($n < 1) {
				    $o->warn("No such target '$rest[0]'.\n");
				    return;
				};
				$n;
			    };
		unless(bounded(1, $sel, $targets)) {
		    $o->warn("No such target number '$sel'.\n");
		}
	    }
	    else {
		$sel = find_index($o->conf('target'), 1, @targets);
	    }
	    my $targ = $targets[$sel-1];
	    my (@keys, @vals);
	    my $res = $opts{exec}
		? PPM::UI::target_info($targ)
		: PPM::UI::target_raw_info($targ);
	    unless ($res->is_success) {
		$o->warn($res->msg);
		return unless $res->ok;
	    }
	    my %h = $res->result_h;
	    my @h = sort keys %h;
	    push @keys, @h;
	    push @vals, $h{$_} for @h;
	    if ($opts{exec}) {
		for (PPM::UI::target_config_info($targ)->result_l) {
		    push @keys, $_->[0];
		    push @vals, $_->[1];
		}
	    }
	    $_ = ucfirst $_ for @keys;
	    $o->inform("Describing target $sel ($targ):\n");
	    $o->print_pairs(\@keys, \@vals);
	    return 1;
	}
    }
    unless($targets) {
	$o->warn("No targets. Install a PPM target.\n");
	return;
    }
    else {
	$o->conf('target', $targets[0])
	    unless $o->conf('target');
	my $i = 0;
	$o->inform("Targets:\n");
	for (@targets) {
	    $o->informf(
		"%s%2d",
		$o->conf('target') eq $targets[$i] ? "*" : " ",
		$i + 1
	    );
	    $o->inform(". $_\n");
	    $i++;
	}
    }
    1;
}

#============================================================================
# Query:
# query		# displays list of previous queries
# query <\d+>	# displays results of previous query
# query <terms>	# performs a new query and displays results
#============================================================================
sub smry_query { "queries installed packages" }
sub help_query { <<'END' }
query -- Query Installed Packages
  Synopsis
     query                   Displays list of previous queries
     query <number>          Displays results of previous query
     query <glob pattern>    Performs a new query using <glob pattern>
     query *                 Displays a list of all installed packages

  Description
    The query command displays a list of all installed packages, or a list
    based on the <glob pattern> switch. You can also check the list of past
    queries, or the results of a past query.

    With PPM 3.1, you can now perform much more powerful queries. The syntax
    is identical to the 'search' command, and almost all the search switches
    are also available for querying installed packages.

    Recall previous queries with the 'query <number>' command. PPM3 stores
    all queries from the current PPM session.

    Note: Depending on the value of the "case-sensitivity" setting, the
    query may or may not be case-sensitive. See "help settings" for
    instructions on setting the default case sensitivity.

  See Also
    search, settings
END
sub comp_query {()}
sub run_query {
    my $o = shift;
    my $query = $o->raw_args || join ' ', @_;
    trace(1, "PPM: query @_\n\tquery='$query'\n");
    my @targets = PPM::UI::target_list()->result_l;
    my $target = $o->conf('target');
    my $case = not $o->conf('case-sensitivity');
    $o->warn("You must install an installation target before using PPM.\n")
      and return unless @targets;

    # No args: show cached query sets
    unless ($query =~ /\S/) {
	my @query_results = $o->cache_sets('query');
	my $query_result_current = $o->cache_set_current('query');
	if (@query_results) {
	    $o->inform("Query Result Sets:\n");
	    my $i = 0;
	    for (@query_results) {
		$o->informf("%s%2d",
		       $query_result_current == $i ? "*" : " ",
		       $i + 1);
		$o->inform(". $_->{query}\n");
		$i++;
	    }
	}
	else {
	    $o->warn("No query result sets -- provide a query term.\n");
	    return;
	}
    }

    # Args:
    else {
	# Show specified result set 
	if ($query =~ /^\d+/) {
	    my $set = int($query);
	    unless (defined $o->cache_set('query', $set-1)) {
		$o->warn("No such query result set '$set'.\n");
		return;
	    }

	    $query = $o->cache_set('query', $set-1, 'query');
	    $o->inform("Query Results Set $set ($query):\n");
	    $o->print_formatted([$o->cache_set('query', $set-1)],
				$o->cache_set_index('query'));
			    
	    $o->cache_set_current('query', $set-1);
	    $o->cache_set_index('query', -1);
	}

	# Query is the same a a previous query on the same target:
	# Use cached results and set them as default
	elsif (grep { $_->{query} eq $query } $o->cache_sets('query')) {
	    for (my $i=0; $i<$o->cache_sets('query'); $i++) {
		if ($o->cache_set('query', $i, 'query') eq $query) {
		    $o->inform("Using cached query result set ", $i+1, ".\n");
		    $o->cache_set_current('query', $i);
		    my $set = $o->cache_set('query');
		    $o->print_formatted($set);
		}
	    }
	}

	# Perform a new query.
	else {
	    my $num = find_index($target, 1, @targets);
	    $o->inform("Querying target $num (");
	    if (length($target) > 30) {
		$o->inform(substr($target, 0, 30), "...");
	    }
	    else {
		$o->inform($target);
	    }
	    $o->inform(")\n");

	    my $res = PPM::UI::query($target, $query, $case);
	    unless ($res->ok) {
		$o->inform($res->msg);
		return;
	    }
	    my @matches = $res->result_l;
	    if (@matches) {
		$o->cache_set_add('query', $query, \@matches);
		$o->cache_set_current('query', scalar($o->cache_sets('query')) - 1);
		my @set = $o->cache_set('query');
		$o->print_formatted(\@set);
	    }
	    else {
		$o->warn("No matches for '$query'; see 'help query'.\n");
	    }
	}
    }
    1;
}

#============================================================================
# Properties:
# prop		# describes default installed package
# prop <\d+>	# describes numbered installed package
# prop <pkg>	# describes named installed package
# prop <url>	# describes installed package at location <url>
#============================================================================
sub smry_properties { "describes installed packages in detail" }
sub help_properties { <<'END' }
properties -- Describe Installed Packages
  Synopsis
     prop                    Describes default installed package
     prop <number>           Describes installed package <number>
     prop <range>            Describes a <range> of installed packages
     prop <package name>     Describes named installed package
     prop <url>              Describes installed package located at <url>
     prop <glob pattern>     Performs a new query using <glob pattern>

  Description
    The properties command is an verbose form of the describe command. In
    addition to summary information, properties will display the
    installation date and a URL showing the location of the package within
    the repository.

    If you specify the package as a URL, PPM determines the package name
    from the URL and searches for that.

    When the properties command is used with wildcard arguments, the text
    entered at the PPM prompt is passed to the query command.

    For example, typing 'properties libnet' will give you:

        ====================
            Name: libnet
         Version: 1.07.03
          Author: Graham Barr
           Title: libnet
        Abstract: Collection of Network protocol modules
        InstDate: Fri Oct  2 16:15:15 1998
        Location: http://ppm.ActiveState.com/PPM/...
        ====================

  See Also
    describe
END
sub comp_properties {
    my $o = shift;
    my ($word, $line, $start) = @_;

    # If no query results
    my $n_results = scalar $o->cache_sets('query');
    my $n_current = $o->cache_set_current('query');
    unless ($n_results and bounded(0, $n_current, $n_results - 1)) {
	my $targ = $o->conf('target') or return ();
	my $r = PPM::UI::query($targ, '*');
	return () unless $r->ok;
	$o->cache_set_add('query', '*', $r->result);
	$o->cache_set_current('query', scalar($o->cache_sets('query')) - 1);
    }
    my @words = $o->line_parsed($line);

    # If the previous word isn't a number or the command, stop.
    return ()
      if ($#words > 0 and
	  $words[$#words] !~ /^\d+/ and
	  $start == length($line) or 
	  $#words > 1);

    # This is the most optimistic list:
    my @results = $o->cache_set('query');
    my $npkgs = @results;
    my @compls = (1 .. $npkgs);

    # If the previous word is a number, return only other numbers:
    return $o->completions($word, \@compls)
      if ($words[$#words] =~ /^\d+/);

    # Either a number or the names of the packages
    push @compls, map { $_->name } @results;
    return $o->completions($word, \@compls);
}
sub run_properties {
    my $o = shift;
    my @args = @_;
    my $args = $args[0];
    trace(1, "PPM: properties @args\n");

    # Check for anything that looks like a query. If it does, send it
    # to query instead.
    my $query = $o->raw_args || join ' ', @args;
    $query ||= '';
    if ($query and not PPM::UI::is_pkg($args[0]) and not parse_range($query)) {
	$o->inform("Wildcards detected; using 'query' instead.\n");
	return $o->run('query', @_);
    }
    
    # No Args: describes current index of current result set, or 1.
    my $n_results = $o->cache_sets('query');
    my $n_current = $o->cache_set_current('query');
    my $ind = $o->cache_set_index('query');
    unless (@args) {
	unless ($n_results and bounded(0, $n_current, $n_results - 1)) {
	    $o->inform("No query results to describe -- " . 
	      "use 'query' to find a package.\n");
	    return;
	}
	else {
	    my @results = $o->cache_set('query');
	    my $npkgs = @results;
	    $o->inform("$SEP\n");
	    if (bounded(0, $ind, $npkgs-1)) {
		my $n = $ind + 1;
		$o->inform("Package $n:\n");
		$o->describe_pkg($o->cache_entry('query', $ind));
	    }
	    else {
		$o->inform("Package 1:\n");
		$o->describe_pkg($results[0]);
		$o->cache_set_index('query', 0);
	    }
	    $o->inform("$SEP\n");
	}
    }

    # Args provided
    else {

	# Describe a particular number:
	if (my @r = parse_range(@args)) {
	    unless ($n_results and bounded(0, $n_current, $n_results - 1)) {
		$o->inform("No query results to describe -- " . 
		  "use 'query' to find a package.\n");
		return;
	    }
	    else {
		for my $n (@r) {
		    my @results = $o->cache_set('query');
		    my $npkgs = @results;
		    $o->inform("$SEP\n");
		    if (bounded(1, $n, $npkgs)) {
			$o->inform("Package $n:\n");
			$o->cache_set_index('query', $n-1);
			my $old = $o->cache_entry('query');
			my $prop =
			  PPM::UI::properties($o->conf('target'), $old->name);
			unless ($prop->is_success) {
			    $o->warn($prop->msg);
			    next unless $prop->ok;
			}
			my ($pkg, $idate, $loc) = $prop->result_l;
			$o->describe_pkg($pkg,
				     [qw(InstDate Location)],
				     [$idate, $loc],
				    );
		    }
		    else {
			$o->inform("No such package $n in result set.\n");
		    }
		}
		$o->inform("$SEP\n");
	    }
	}

	# Query a particular package
	else {
	    if ($o->conf('target')) {
		my $prop =
		  PPM::UI::properties($o->conf('target'), $args);
		unless ($prop->is_success) {
		    $o->warn($prop->msg);
		    return unless $prop->ok;
		}
		my ($pkg, $idate, $loc) = $prop->result_l;
		my ($s, $index) = $o->cache_find('query', $args);
		$o->inform("$SEP\n") if $pkg;
		$o->describe_pkg($pkg,
			     [qw(InstDate Location)],
			     [$idate, $loc],
			    )
		  if $pkg;
		$o->inform("$SEP\n") if $pkg;
		if ($index >= 0) {
		    $o->cache_set_current('query', $s);
		    $o->cache_set_index('query', $index);
		}
		elsif ($pkg) {
		    $o->cache_set_add('query', $args[0], [$pkg]);
		    my $last = $o->cache_sets('query') - 1;
		    $o->cache_set_current('query', $last);
		    $o->cache_set_index('query', 0);
		}
		$o->warn("Package '$args' not found; 'query' for it first.\n")
		  and return unless $pkg;
	    }
	    else {
		# XXX: Change this output.
		$o->warn(
		    "There are no targets installed.\n"
		);
		return;
	    }
	}
    }
    1;
}

#============================================================================
# Uninstall:
# uninst	# removes default installed package
# uninst <\d+>	# removes specified package
# uninst <pkg>	# removes specified package
# uninst <url>	# removes the package located at <url>
#============================================================================
sub smry_uninstall { "uninstalls packages" }
sub help_uninstall { <<'END' }
remove, uninstall -- Uninstalls Installed Packages
  Synopsis
     remove              Deletes default installed package
     remove <number>     Deletes installed package <number>
     remove <range>      Deletes a <range> of installed packages
     remove <name>       Deletes a packages by a specific name
     remove <url>        Deletes the package located at <url>

  Description
    The remove and uninstall commands function identically. They are used to
    delete packages from the current target (specified using the target
    command). If profile tracking is enabled, (see 'help profile') the
    current PPM profile on ASPN will be updated.

    Packages can be removed by package name, by their numerical listing, or
    by specifying a URL to a PPD file. For example:

        remove XML-DOM

    will delete the XML-DOM package from the target.

    To remove package by number:

        remove 6

    and the sixth package in your current query will be removed. If no
    queries have been run in the current PPM session, you will be prompted
    to use a query to find a package before deleting it. Remember that
    removing packages clears all previous query requests, since the
    numerical sequence stored in any query will no longer be true once
    package(s) have been removed.

    Packages can also be removed in groups. For example:

        remove 4-7

    will delete packages 4, 5, 6, and 7 from your target. You can also skip
    packages:

        remove 3-5, 7

    this will delete packages 3, 4, 5 and 7, but will leave 6 intact.
    Remember to run a new query whenever you remove a package from your
    target.

    If you specify the package as a URL, PPM determines the package name
    from the URL and removes that.

    Please note that wildcards like "*" or "?" cannot be used with the
    remove command.

  See Also
    profile
END
sub comp_uninstall { goto &comp_properties; }
sub run_uninstall {
    my $o = shift;
    my @args = @_;
    trace(1, "PPM: uninstall @args\n");

    # Get the force option:
    my ($force);
    {
	local @ARGV = @args;
	GetOptions(
		'force!' => \$force,
	);
	@args = @ARGV;
    }
    
    my $args = $args[0];

    # No Args -- removes default package
    my $n_results = $o->cache_sets('query');
    my $n_current = $o->cache_set_current('query');
    my $ind = $o->cache_set_index('query');
    unless (@args) {
	unless ($n_results and bounded(0, $n_current, $n_results - 1)) {
	    $o->warn("No query results to uninstall -- " . 
	      "use 'query' to find a package.\n");
	    return;
	}
	else {
	    my @results = $o->cache_set('query');
	    if (bounded(0, $ind, $#results)) {
		my $n = $ind + 1;
		$o->inform("Package $n:\n");
		$o->remove_pkg($o->cache_entry('query', $ind)->name, $force);
	    }
	    else {
		$o->inform("Package 1:\n");
		$o->remove_pkg($o->cache_entry('query', 0)->name, $force);
	    }
	}
    }

    # Args provided
    else {
	# Uninstall a particular number:
	if (my @r = parse_range(@args)) {
	    unless ($n_results and bounded(0, $n_current, $n_results - 1)) {
		$o->warn("No query results to uninstall -- " . 
		  "use 'query' to find a package.\n");
		return;
	    }
	    else {
		my @results = $o->cache_set('query');
		my $npkgs = @results;
		my $ok = 0;
		for my $n (@r) {
		    if (bounded(1, $n, $npkgs)) {
			$o->inform("Package $n:\n");
			$ok |=
			  $o->remove_pkg($o->cache_entry('query', $n-1)->name,
				     $force, 1);
		    }
		    else {
			$o->warn("No such package $n in result set.\n");
		    }
		}
		$o->cache_clear('query') if $ok;
	    }
	}

	# Uninstall a particular package
	else {
	    if ($o->conf('target')) {
		$o->remove_pkg($_, $force) for @args;
	    }
	    else {
		print
		  "No targets -- use 'rep add' to add a target.\n";
		return;
	    }
	}
    }
    1;
}
sub alias_uninstall { qw(remove) }

#============================================================================
# Settings:
#============================================================================
my (%lib_keys, @ui_keys);
my (@path_keys, @boolean_keys, @integer_keys);
my (%cache_clear_keys);
BEGIN {
    %lib_keys = ('download-chunksize' => 'downloadbytes',
		'tempdir' => 'tempdir',
		'rebuild-html' => 'rebuildhtml',
		'trace-file' => 'tracefile',
		'trace-level' => 'tracelvl',
		'profile-track' => 'profile_enable',
		);
    @ui_keys = qw(
	case-sensitivity
	pager
	fields
	follow-install
	force-install
	prompt-context
	prompt-slotsize
	prompt-verbose
	sort-field
	verbose-startup

	install-verbose
	upgrade-verbose
	remove-verbose
    );
    @boolean_keys = qw(case-sensitivity force-install follow-install
		       prompt-context prompt-verbose profile-track
		       verbose-startup install-verbose upgrade-verbose
		       remove-verbose rebuild-html
		      );
    @integer_keys = qw(download-chunksize prompt-slotsize trace-level);
    @path_keys = qw(tempdir pager trace-file);
    @cache_clear_keys{qw/
	case-sensitivity
    /} = ();
}
sub settings_getkeys {
    my $o = shift;
    my @keys = @ui_keys;
    push @keys, keys %lib_keys;
    @keys;
}
sub settings_getvals {
    my $o = shift;
    my @vals;
    push @vals, $o->settings_getkey($_) for $o->settings_getkeys;
    @vals;
}

sub conf {
    my $o   = shift;
    my $key = shift;
    my $val = shift;
    my $un  = shift;
    return $o->settings_setkey($key, $val, $un) if defined $val;
    return $o->settings_getkey($key);
}

sub settings_getkey {
    my $o = shift;
    my $key = shift;
    return PPM::UI::config_get($lib_keys{$key})->result if $lib_keys{$key};
    return $o->{SHELL}{conf}{DATA}{$key};
}
sub settings_setkey {
    my $o = shift;
    my ($key, $val, $un) = @_;
    if (grep { $key eq $_ } @boolean_keys) {
	$val = 0 if $un;
	unless ($val =~ /^\d+$/ && ($val == 0 || $val == 1)) {
	    $o->warn(<<END);
Setting '$key' must be boolean: '0' or '1'. See 'help settings'.
END
	    return;
	}
    }
    elsif (grep { $key eq $_ } @integer_keys) {
	$val = 0 if $un;
	unless ($val =~ /^\d+$/) {
	    $o->warn(<<END);
Setting '$key' must be numeric. See 'help settings'.
END
	    return;
	}
    }
    elsif ($key eq 'sort-field') {
	$val = 'name' if $un;
	my @fields = sort_fields();
	unless (grep { lc($val) eq $_ } @fields) {
	    $o->warn(<<END);
Error setting '$key' to '$val': should be one of:
@fields.
END
	    return;
	}
	else {
	    $val = lc($val);
	    $o->cache_set_index('search', -1); # invalidates current indices.
	    $o->cache_set_index('query', -1);
	}
    }
    elsif ($key eq 'fields') {
	$val = 'name version abstract' if $un;
	my @fields = sort_fields();
	my @vals = split ' ', $val;
	for my $v (@vals) {
	    unless (grep { lc $v eq lc $_ } @fields) {
		$o->warn(<<END);
Error adding field '$v': should be one of:
@fields.
END
		return;
	    }
	}
	$val = lc $val;
    }

    if ($un and $key eq 'tempdir') {
	$o->warn("Can't unset 'tempdir': use 'set' instead.\n");
	return;
    }

    # Check for any cache-clearing that needs to happen:
    if (exists $cache_clear_keys{$key}) {
	$o->cache_clear('search');
	$o->cache_clear('query');
    }

    if ($lib_keys{$key}) { PPM::UI::config_set($lib_keys{$key}, $val) }
    else {
	$o->{SHELL}{conf}{DATA}{$key} = $val;
	$o->{SHELL}{conf}->save;
    }
    $o->inform(<<END);
Setting '$key' set to '$val'.
END
}

sub smry_settings { "view or set PPM options" }
sub help_settings { <<'END' }
settings -- View or Set PPM Settings
  Synopsis
     set                 Displays current settings
     set <name>          Displays the current setting of the given <name>
     set <name> <value>  Sets <name> to <value>
     unset <name>        Sets <name> to a "false" value: '0' for boolean
                         settings, '' for others.

  Description
    The settings command is used to configure the default PPM environment.
    Settings such as the number of lines displayed per page,
    case-sensitivity, and the log file are configured using the settings
    command.

    Setting names may be abbreviated to uniqueness. For example, instead of
    typing 'case-sensitivity', you may type 'case'.

    Available settings:

     NAME                VALUE           DESCRIPTION
     case-sensitivity    1 or 0      If 1, searches and queries are
                                     case-sensitive.

     download-chunksize  integer     If this is set to a positive,
                                     non-zero integer, PPM updates the
                                     status after "integer" of bytes
                                     transferred during an install or
                                     upgrade.

     fields              fields      A space-separated list of fields to 
                                     display in the search results. Valid
                                     fields are:

                                       ABSTRACT
                                       AUTHOR
                                       NAME
                                       REPOSITORY
                                       TITLE
                                       VERSION

                                     Usually, NAME and TITLE have the same
                                     content.

     follow-install      1 or 0      See 'help install' for details.

     force-install       1 or 0      See 'help install' for details.

     install-verbose     1 or 0      If 0, suppresses most output when
                                     installing packages. If 1, PPM prints
                                     each file as it is installed.

     pager               path        The path to an external pager program
                                     used to page long displays. If blank,
                                     or set to 'internal', the internal
                                     pager is used. If 'none', paging
                                     is disabled.

     profile-track       1 or 0      If 1, PPM arranges to have the 
                                     ASPN server track your PPM profile. 
                                     This means that every time your install
                                     or remove a package, your profile is
                                     updated on the server. If 0, you must
                                     manually save your profile using
                                     'profile save'.

     prompt-context      1 or 0      If 1, enables the prompt to change
                                     based on the current state of PPM, i.e
                                     showing current target, query, etc.

     prompt-slotsize     integer     If prompt-verbose is 1, this defines
                                     the width of each slot in the prompt.
                                     For instance, 4 means to use 4 
                                     character-wide slots.

     prompt-verbose      1 or 0      If 0, uses numbers to represent the
                                     context in the prompt; much shorter.
                                     If prompt-context is set to 0, there
                                     will be no visible difference in the
                                     'prompt-verbose' settings.

     rebuild-html        1 or 0      If 0, suppresses regeneration of HTML
                                     documentation when packages are
                                     installed. If 1, enables HTML to be
                                     generated from POD documentation.
                                     Enabling this option may slow down
                                     package installation.

     remove-verbose      1 or 0      If 0, suppresses most output when
                                     removing packages. If 1, prints the
                                     name of each file as it is removed.

     sort-field          field       The field by which to sort search and
                                     query results. Valid fields are
                                     ABSTRACT, AUTHOR, NAME, TITLE
                                     and VERSION.

     tempdir             path        A temporary directory into which
                                     packages are downloaded and expanded
                                     during 'install' and 'upgrade'.

     trace-file          path        A file to which PPM will write tracing
                                     information.

     trace-level         integer     If 0 or negative, tracing is disabled.
                                     Positive, non-zero integers result in
                                     tracing information being written to
                                     'trace-file'. Higher settings of
                                     'trace-level' result in more trace
                                     information.

     upgrade-verbose     1 or 0      If 0, suppresses most output when
                                     upgrading packages. If 1, prints the
                                     name of each file as it is upgraded.

    For information about migrating options used by previous versions of
    PPM, see 'help ppm_migration'.

    When you assign a value to a setting, PPM saves the configuration.
    Therefore, setting values persist across sessions.
END
sub comp_settings {
    my $o = shift;
    my ($word, $line, $start) = @_;
    my @words = $o->line_parsed($line);

    # To please the users of Bash, we'll allow 'set foo=bar' to work as well,
    # since it's really easy to do:
    if (defined $words[1] and $words[1] =~ /=/ and not defined $words[2]) {
	my @kv = split '=', $words[1];
	splice(@words, 1, 1, @kv);
    }
    my $words = @words;
    my @compls;

    # return the keys when we're completing the second word
    if ($words == 1 or $words == 2 and $start != length($line)) {
	@compls = $o->settings_getkeys();
	return $o->completions($word, \@compls);
    }

    # Return no completions for 'unset'.
    return () if matches($o->{API}{cmd}{run}{name}, 'u|nset');

    # provide intelligent completion for arguments:
    if ($words ==2 or $words == 3 and $start != length($line)) {
	# Completion for boolean values:
	my @bool = $o->completions($words[1], \@boolean_keys);
	my @path = $o->completions($words[1], \@path_keys);
	if (@bool == 1) {
	    return $o->completions($word, [0, 1]);
	}
	elsif (@path == 1) {
	    @compls = readline::rl_filename_list($word);
	    return $o->completions($word, \@compls);
	}
	elsif (matches($words[1], 's|ort-field')) {
	    @compls = sort_fields();
	    return $o->completions(lc($word), \@compls);
	}
    }

    # Don't complete for anything else.
    ()
}
sub run_settings {
    my $o = shift;
    my @args = @_;
    my $key = $args[0];
    my $val = $args[1];

    # To please the users of Bash, we'll allow 'set foo=bar' to work as well,
    # since it's really easy to do:
    if (defined $key and $key =~ /=/ and not defined $val) {
	($key, $val) = split '=', $key;
    }

    trace(1, "PPM: settings @args\n");
    my $unset = matches($o->{API}{cmd}{run}{name}, 'u|nset');
    my @stuff = $o->completions($key, [$o->settings_getkeys()])
      if $key;
    my $fullkey = $stuff[0] if @stuff == 1;
    if (defined $key and defined $val) {
	# validate the key:
	unless ($fullkey) {
	    $key = '' unless defined $key;
	    $o->warn("Unknown or ambiguous setting '$key'. See 'help settings'.\n");
	    return;
	}
	$o->conf($fullkey, $val, $unset);
    }
    elsif (defined $key) {
	unless ($fullkey) {
	    $key = '' unless defined $key;
	    $o->warn("Unknown or ambiguous setting '$key'. See 'help settings'.\n");
	    return;
	}
	if ($unset) {
	    $o->conf($fullkey, '', $unset);
	}
	else {
	    my $val = $o->conf($fullkey);
	    $o->print_pairs([$fullkey], [$val]);
	}
    }
    else {
	my (@keys, @vals);
	@keys = $o->settings_getkeys();
	@vals = $o->settings_getvals();
	my %k;
	@k{@keys} = @vals;
	@keys = sort keys %k;
	@vals = map { $k{$_} } @keys;
	$o->print_pairs(\@keys, \@vals);
    }
}
sub alias_settings { qw(unset) }

sub help_help { <<'END' }
help -- General help, or help on specific commands.
  Synopsis
     help                Lists available commands and help topics
     help <command>      Lists detailed help about a specific command

  Description
    The help command provides a brief description of the commands available
    within PPM. For help on a specific command, enter help followed by the
    command name. For example, enter help settings or help set for a
    detailed description of the settings command.

    There are some extra help topics built into PPM. They can be accessed
    within the PPM environment as follows:

      help ppm_migration

    shows more details about the changes from previous versions of PPM

      help quickstart

    an easy-to-follow guide to getting started with PPM

      help prompt

    provides a detailed explanation about the PPM prompt
END

#============================================================================
# Version:
#============================================================================
sub smry_version { "displays the PPM version ($VERSION)" }
sub help_version { <<'END' }
version -- print the name and version of PPM.
    Prints the name and version of PPM3.
END
sub comp_version {()}
sub run_version {
    my $o = shift;
    if ($o->mode eq 'SHELL') {
	$o->inform("$NAME version $VERSION\n");
    }
    else {
	$o->inform("$SHORT_NAME $VERSION\n");
    }
    1;
}

#============================================================================
# Exit:
#============================================================================
sub help_exit { <<'END' }
exit, q, quit -- Exit the program
  Synopsis
     exit                Exit
     q                   Exit
     quit                Exit

  Description
    When you leave the PPM environment, the current settings are saved.
END
sub comp_exit {
    my $o = shift;
    return &comp_query
	if $o->{API}{cmd}{run}{name} eq 'q' and @_;
    ();
}
sub run_exit {
    my $o = shift;
    # Special case: 'q' with no arguments should mean 'quit', but 'q' with
    # arguments should mean 'query'.
    if ($o->{API}{cmd}{run}{name} eq 'q' and @_) {
	return $o->run('query', @_);
    }
    $o->stoploop;
}
sub alias_exit { qw(quit q) }

#============================================================================
# Upgrade
# upgrade	# lists upgrades available
# upgrade <\d+> # lists upgrades for specified package
# upgrade<pkg>	# lists upgrades for named package
#============================================================================
sub smry_upgrade { "shows availables upgrades for installed packages" }
sub help_upgrade { <<'END' }
upgrade -- List or install available upgrades
  Synopsis
     upgrade [*]         Lists upgrades available for all installed packages
     upgrade <number>    Lists upgrades for installed package <number>
     upgrade <range>     Lists upgrades for a <range> of installed packages
     upgrade <package>   Lists upgrades for the named <package>

  Description
    The upgrade command lists package upgrades that are available on the
    active repositories for packages installed on your system. To install
    available upgrades, use the '--install' option.

    If profile tracking is enabled, (see 'help profile'), your profile will
    be updated to reflect changes to any packages which are upgraded.

    There are several modifiers to the upgrade command:

    -install
        Installs, rather than lists, available upgrades

    -precious
        Allows upgrading of "precious" packages

    -force
        See 'help install'

    -follow
        See 'help install'

    By default, 'upgrade' typed by itself only lists the available upgrades.
    To actually install all available upgrades, enter

        upgrade -install

    To enable upgrading "precious" packages, enter

        upgrade -install -precious

  See Also
    profile
END
sub comp_upgrade { goto &comp_properties; }
sub run_upgrade {
    my $o = shift;
    my @args = @_;
    trace(1, "PPM: upgrade @args\n");

    # Get options:
    my %opts = (
	install => 0,
	doprecious => 0,
	dryrun => 0,
	force => $o->conf('force-install'),
	follow => $o->conf('follow-install'),
    );
    {
	local @ARGV = @args;
	GetOptions(install => \$opts{install},
		   precious => \$opts{doprecious},
		   'force!' => \$opts{force},
		   'follow!' => \$opts{follow},
		   dryrun => \$opts{dryrun},
		  );
	@args = @ARGV;
    }

    my $rlist = [$o->reps_on];
    my $targ  = $o->conf('target');
    my @pkgs;

    # Allow 'upgrade *';
    @args = grep { $_ ne '*' } @args;

    # List upgrades for a particular package
    if (@args) {
	my $pkg = $args[0];
	my @n = parse_range($o->raw_args);
	for my $n (@n) {
	    my $ppd = $o->cache_entry('query', $n-1);
	    unless($ppd) {
		$o->warn("No such query result '$pkg' in result set.\n");
		return;
	    }
	    else {
		push @pkgs, $ppd;
	    }
	}

	# The name of the package:
	unless (@n) {
	    my $ppd = PPM::UI::properties($o->conf('target'), $pkg);
	    unless ($ppd->is_success) {
		$o->warn($ppd->msg);
		return unless $ppd->ok;
	    }
	    my $real_ppd = ($ppd->result_l)[0];
	    push @pkgs, $real_ppd;
	}
    }
    # List upgrades for all packages
    else {
	@pkgs = PPM::UI::query($targ, '*', 0)->result_l;
	@pkgs = $o->sort_pkgs($o->conf('sort-field'), @pkgs);
    }

    my $verify = PPM::UI::verify_pkgs($rlist, $targ, @pkgs);
    unless ($verify->is_success) {
	$o->error("Error verifying packages: ", $verify->msg_raw, "\n");
	return;
    }
    my %bypackage;
    for my $result ($verify->result_l) {
	next unless $result->is_success; # ignore unfound packages
	my ($uptodate, $server_pkg, $inst_pkg, $b, $p) = $result->result_l;
	my $name = $server_pkg->name;
	my $nver = $server_pkg->version;
	my $over = $inst_pkg->version;
	my $repo = $server_pkg->repository->name;
	$bypackage{$name}{$repo} = {
	    uptodate => $uptodate,
	    oldver => $over,
	    newver => $nver,
	    repo => $repo,
	    bundled => $b,
	    precious => $p,
	    pkg => $server_pkg,
	};
    }
    for my $pkg (sort keys %bypackage) {
	my $default;
	my @updates;
	my $p = $bypackage{$pkg};
	for my $rep (sort { $p->{$b}{newver} cmp $p->{$a}{newver} } keys %$p) {
	    my $tmp = $default = $p->{$rep};
	    push @updates, [@$tmp{qw(oldver newver repo)}] unless $tmp->{uptodate};
	}
	my $upgrade = $opts{install} ? 1 : 0;
        for (@updates) {
	    $o->inform("$pkg $_->[0]: new version $_->[1] available in $_->[2]\n");
	}
	unless (@updates) {
	    $o->inform("$pkg $default->{oldver}: up to date.\n");
	    $upgrade &= $opts{force};
	}
	if ($upgrade) {
	    my @k = keys %$p;
	    my $ask = (@updates > 1 or @k > 1 and !@updates);
	    if ($ask) {
		# Which one do they want to install?
		$o->inform(<<MANY);

   Note: $pkg version $default->{oldver} is available from more than one place.
   Which repository would you like to upgrade from?

MANY
		my @repos = map { $_->[2] } @updates;
		$o->print_pairs([ 1 .. @repos ], \@repos, '. ');
		$o->inform("\n");
		my $rep = $o->prompt(
		    "Repository? [$default->{repo}] ",
		    $default->{repo},
		    [ 1 .. @repos, @repos ],
		);
		$rep = $repos[$rep - 1] if $rep =~ /^\d+$/;
		$default = $p->{$rep};
	    }
	    elsif (!@updates) {
		($default) = values %$p;
	    }
	    if (not $default->{precious} or $default->{precious} && $opts{doprecious}) {
		$o->upgrade_pkg($default->{pkg}, \%opts);
	    }
	    else {
		$o->warn(<<END);
Use '-precious' to force precious packages to be upgraded.
END
	    }
	}
    }
    1;
}

#============================================================================
# Profile:
# profile		# lists the profiles available on the repository
# profile N		# switches profiles
# profile add "name"	# adds a new profile
# profile delete N	# deletes the given profile
# profile describe N	# describes the given profile
# profile save		# saves the current state to the current profile
# profile restore	# restores the current profile
# profile rename	# renames the given profile
#============================================================================
sub smry_profiles { "manage PPM profiles" }
sub help_profiles { <<'END' }
profile -- Manage PPM Profiles
  Synopsis
     profile                     Lists profiles available on the repository
     profile <num>               Switches to the given profile
     profile add <name>          Creates a new profile on the repository
     profile delete <name or num>
                                 Deletes the given profile
     profile describe [name or num]
                                 Describes the current or given profile
     profile save                Saves the client state to the current profile
     profile restore             Restores the current profile
     profile rename <name or num> <name>
                                 Renames the given profile to <name>

  Description
    Profiles store information about packages that are installed on your
    system. If the 'profile-track' setting is enabled, your ASPN Profile
    will be updated with information about installed packages. Profiles
    allow you to easily migrate, reinstall, upgrade or restore PPM packages
    in one or more locations.

    To use profiles, you must have a license for ASPN. For license
    information, see http://www.ActiveState.com/ASPN/About Disable profile
    tracking by setting 'profile-track=0'.
END
sub comp_profiles {
    my $o = shift;
    my ($word, $line, $start) = @_;
    my @words = $o->line_parsed($line);
    my $words = scalar @words;
    my @profs = PPM::UI::profile_list();
    my @cmds = ('add', 'delete', 'describe', 'save', 'restore', 'rename');

    if ($words == 1 or $words == 2 and $start != length($line)) {
	my @compls = (@cmds, 1 .. scalar @profs);
	return $o->completions($word, \@compls);
    }
    if ($words == 2 or $words == 3 and $start != length($line)) {
	return ()
	  if ($o->completions($words[1], [qw(add save restore)])==1);
	return $o->completions($word, [1 .. scalar @profs])
	  if ($o->completions($words[1], [qw(delete describe rename)])==1);
    }
    ();
}
sub run_profiles {
    my $o = shift;
    my @args = @_;
    trace(1, "PPM: profile @args\n");

    my $ok = PPM::UI::profile_list();
    unless ($ok->is_success) {
	$o->warn($ok->msg);
	return unless $ok->ok;
    }
    my @profiles = dictsort $ok->result_l;
    $ok = PPM::UI::profile_get();
    unless ($ok->is_success) {
	$o->warn($ok->msg);
	return unless $ok->ok;
    }
    my $profile = $ok->result;
    my $which = find_index($profile, 0, @profiles);
    if ($which < 0 and @profiles) {
	$profile = $profiles[0];
	PPM::UI::profile_set($profile);
    }

    if (@args) {
	# Switch to profile N:
	if ($args[0] =~ /^\d+$/) {
	    my $num = $args[0];
	    if (bounded(1, $num, scalar @profiles)) {
		my $profile = $profiles[$num-1];
		PPM::UI::profile_set($profile);
	    }
	    else {
		$o->warn("No such profile number '$num'.\n");
		return;
	    }
	}

	# Describe profile N:
	elsif (matches($args[0], "des|cribe")) {
	    my $num = 	$args[1] =~ /^\d+$/ ? $args[1] :
			do {
			    my $n = find_index($args[1], 1, @profiles);
			    if ($n < 1) {
				$o->warn("No such profile '$args[1]'.\n");
				return;
			    }
			    $n;
			} if defined $args[1];
	    my $prof;
	    if (defined $num and $num =~ /^\d+$/) {
		if (bounded(1, $num, scalar @profiles)) {
		    $prof = $profiles[$num - 1];
		}
		else {
		    $o->warn("No such profile number '$num'.\n");
		    return;
		}
	    }
	    elsif (defined $num) {
		$o->warn("Argument to '$args[0]' must be numeric; see 'help profile'.\n");
		return;
	    }
	    else {
		$prof = $profile;
	    }

	    my $res = PPM::UI::profile_info($prof);
	    $o->warn($res->msg) and return unless $res->ok;
	    my @res = $res->result_l;
	    {
		my ($pkg, $version, $target);
		my $picture = <<'END';
[[[[[[[[[[[[[[[[[[[	[[[[[[[[[[[	[[[[[[[[[[[[[[[[[[[[[[
END
		($pkg, $version, $target) = qw(PACKAGE VERSION TARGET);
		my $text = '';
		$text .= form($picture, $pkg, $version, $target)
		  if @res;
		for my $entity (@res) {
		    ($pkg, $version, $target) = @$entity;
		    $version = "[$version]";
		    $text .= form($picture, $pkg, $version, $target);
		}
		if (@res) {
		    $o->inform("Describing Profile '$prof':\n");
		}
		else {
		    $o->inform("Profile '$prof' is empty.\n");
		}
		$o->page($text);
	    }
	    return 1;
	}

	# Add a profile "name":
	elsif (matches($args[0], "a|dd")) {
	    my $name = $args[1];
	    if ($name) {
		# Note: do some heavy-duty error-checking; XXX
		PPM::UI::profile_add($name);
		PPM::UI::profile_save($name)
		  if $o->conf('profile-track');
		PPM::UI::profile_set($name)
		  unless $which >= 0;
		@profiles = PPM::UI::profile_list()->result_l;
	    }
	    else {
		$o->warn("Invalid use of 'add' command; see 'help profile'.\n");
		return;
	    }
	}

	# Remove profile N:
	elsif (matches($args[0], "del|ete")) {
	    my $num =	$args[1] =~ /^\d+$/ ? $args[1] :
			do {
			    my $n = find_index($args[1], 1, @profiles);
			    if ($n < 1) {
				$o->inform("No such profile '$args[1]'.\n");
				return;
			    }
			    $n;
			} if defined $args[1];
	    if (defined $num and $num =~ /^\d+$/) {
		my $dead_profile = $profiles[$num-1];
		if (bounded(1, $num, scalar @profiles)) {
		    PPM::UI::profile_del($dead_profile);
		    @profiles = dictsort PPM::UI::profile_list()->result_l;
		    if (@profiles and $dead_profile eq $profile) {
			$profile = $profiles[0];
			PPM::UI::profile_set($profile);
		    }
		    elsif (not @profiles) {
			$o->conf('profile-track', 0);
			PPM::UI::profile_set('');
		    }
		}
		else {
		    $o->warn("No such profile '$num'.\n");
		    return;
		}
	    }
	    elsif (defined $num) {
		$o->warn(<<END);
Argument to '$args[0]' must be numeric; see 'help profile'.
END
		return;
	    }
	    else {
		$o->warn(<<END);
Invalid use of '$args[0]' command; see 'help profile'.
END
		return;
}
	}

	# Save current profile:
	elsif (matches($args[0], "s|ave")) {
	    unless (@profiles) {
		$o->warn(<<END);
No profiles on the server. Use 'profile add' to add a profile.
END
		return;
	    }
	    unless ($which >= 0) {
		$o->warn(<<END);
No profile selected. Use 'profile <number>' to select a profile.
END
		return;
	    }
	    my $ok = PPM::UI::profile_save($profile);
	    if ($ok->ok) {
		$o->inform("Profile '$profile' saved.\n");
	    }
	    else {
		$o->warn($ok->msg);
		return;
	    }
	    return 1;
	}

	# Rename profile:
	elsif (matches($args[0], "ren|ame")) {
	    unless (@profiles) {
		$o->warn(<<END);
No profiles on the server. Use 'profile add' to add a profile.
END
		return;
	    }

	    # Determine the old name:
	    my $num =	$args[1] =~ /^\d+$/ ? $args[1] :
			do {
			    my $n = find_index($args[1], 1, @profiles);
			    if ($n < 1) {
				$o->warn("No such profile '$args[1]'.\n");
				return;
			    };
			    $n;
			} if defined $args[1];
	    my $oldprof;
	    if (defined $num and $num =~ /^\d+$/) {
		if (bounded(1, $num, scalar @profiles)) {
		    $oldprof = $profiles[$num - 1];
		}
		else {
		    $o->warn("No such profile number '$num'.\n");
		    return;
		}
	    }
	    elsif (defined $num) {
		$o->warn("Argument to '$args[0]' must be numeric; see 'help profile'.\n");
		return;
	    }
	    else {
		$o->warn("profile: invalid use of '$args[0]' command: see 'help profile'.\n");
		return;
	    }

	    # Validate the new name:
	    my $newprof = $args[2];
	    unless (defined $newprof and length($newprof)) {
		$newprof = '' unless defined $newprof;
		$o->warn(<<END);
Profile names must be non-empty: '$newprof' is not a valid name.
END
		return;
	    }

	    # Actually do it:
	    my $ok = PPM::UI::profile_rename($oldprof, $newprof);
	    unless ($ok->is_success) {
		$o->warn($ok->msg);
		return unless $ok->ok;
	    }
	    if ($profile eq $oldprof) {
		$profile = $newprof;
		PPM::UI::profile_set($profile);
	    }
	    @profiles = dictsort PPM::UI::profile_list()->result_l;
	}

	# Restore current profile:
	elsif (matches($args[0], "res|tore")) {
	    unless (@profiles) {
		$o->warn(<<END);
No profiles on this server. Use 'profile add' to add a profile.
END
		return;
	    }
	    unless ($which >= 0) {
		$o->warn(<<END);
No profile selected. Use 'profile <number>' to select a profile.
END
		return;
	    }
	    my ($clean_packages, $dry) = (0, 0);
	    my ($force, $follow) = (1, 0);
	    {
		local @ARGV = @args;
		GetOptions('clean!' => \$clean_packages,
			   'force!' => \$force,
			   'follow!' => \$follow,
			   'dryrun' => \$dry,
			  );
		@args = @ARGV;
	    }
	    my $cb_inst = $dry ? \&dr_install : \&cb_install;
	    my $cb_rm   = $dry ? \&dr_remove  : \&cb_remove ;
	    my $ok = PPM::UI::profile_restore($profile, sub {$o->$cb_inst(@_)},
					      sub {$o->$cb_rm(@_)}, $force, $follow,
					      $dry, $clean_packages);
	    if ($ok->ok) {
		$o->cache_clear('query');
		$o->inform("Profile '$profile' restored.\n");
	    }
	    else {
		$o->warn($ok->msg);
		return;
	    }
	    return 1;
	}

	# Unrecognized subcommand:
	else {
	    $o->warn("No such profile command '$args[0]'; see 'help profile'.\n");
	    return;
	}
    }
    if (@profiles) {
	@profiles = dictsort @profiles;
	my $i = 0;
	$o->inform("Profiles:\n");
	my $profile = PPM::UI::profile_get()->result;
	for (@profiles) {
	    $o->informf("%s%2d", $profile eq $profiles[$i] ? "*" : " ", $i + 1);
	    $o->inform(". $_\n");
	    $i++;
	}
    }
    elsif (defined $args[0] and matches($args[0], "del|ete")) {
	# assume that we just deleted the last profile
	$o->warn(<<END);
Profile deleted; no remaining profiles on the server.
END
    }
    else {
	$o->warn(<<END);
No profiles. Use 'profile add' to add a profile.
END
    }
    1;
}

#============================================================================
# Help-only topics:
#============================================================================
sub smry_prompt { "how to interpret the PPM prompt" }
sub help_prompt { <<'END' }
prompt -- information about the PPM3 prompt
  Description
    The PPM prompt can tell you six things:

    1)  The current repository;

    2)  The current target;

    3)  The last search you made on the current repository;

    4)  The last query you made on the current target;

    5)  The last package you described from this repository; and,

    6)  The last package you described from this target.

    To enable the prompt to tell you this information, you must set
    'prompt-context' to '1'. The following examples all assume this setting.

  Examples
    1   Repository and Target:

        Set 'prompt-context' The prompt will resemble:

            ppm:1:1> 

        In this case, the first '1' means that the first repository is
        selected. The second '1' means the first target is selected. You can
        prove this by adding another repository and switching to it:

            ppm:1:1> rep add TEMP http://my/repository
            Repositories:
              1. ActiveState Package Repository
            * 2. TEMP
            ppm:1:1> rep 2
            Repositories:
              1. ActiveState Package Repository
            * 2. TEMP
            ppm:2:1> 

        The same is true for targets. If you have multiple versions of Perl
        installed, when you swtich to a different target the second number
        reflects the change.

        If you delete all the repositories, the repository number changes to
        '?'. The same goes for targets. If either item is indicated by a
        question mark, you must configure a repository or target before
        proceeding.

    2   Search and Query:

        PPM stores searches and search results from in the current session.
        The prompt displays the search number:

            ppm:1:1> search Text
            [results displayed here]
            ppm:1:1:s1> 

        The 's1' indicates that the last search you performed can be viewed
        again by entering 'search 1'. Type 'search' with no arguments to
        view the list of cached searches:

            ppm:1:1:s1> search
            Search Result Sets:
            * 1. Text

        If you then enter 'search 1', you will see the same results as when
        you typed 'search Text' earlier. If you search for something else
        ('search Parse') then the number will change to 's2':

            ppm:1:1:s1> search Parse
            [results displayed here]
            ppm:1:1:s2>

        The same indicators apply to the query command. When you run a
        query, a numerical indicator displays the current query:

            ppm:1:1:s1> query PPM
            [results displayed here]
            ppm:1:1:s1:q1> 

        You can view the past queries with 'query', and view results by
        querying a particular number.

    3   Describe and Properties:

        When you use the describe command with the numerical switch (to view
        package information based on the package number in the last search
        or query), PPM sets that index to the current index. If you use the
        desribe command with the name switch, and the name is found within
        the current result, the index is set to the current one. If no
        package is found, PPM creates a new search or query on-the-fly, and
        sets it as the current search or query.

        For example:

            ppm:1:1> search Text
            1. Convert-Context  [0.501]     an Attributed Text data type
            2. gettext          [1.01]      message handling functions
            3. HTML-FromText    [1.005]     mark up text as HTML
            4. HTML-Subtext     [1.03]      Perform text substitutions on an HTML
                                            template
            5. Locale-Maketext  [0.18]      framework for software localization
            ppm:1:1:s1>

            ppm:1:1:s1> describe 1
            ====================
            Package 1:
                Name: Convert-Context
             Version: 0.501
              Author: Martin Schwartz (martin@nacho.de)
            Abstract: an Attributed Text data type
            Implementations:
                   1. i686-linux-thread-multi
                   2. MSWin32-x86-multi-thread
                   3. sun4-solaris-thread-multi
            ====================
            ppm:1:1:s1:sp1> 

        The last prompt has an extra 'sp1'. That stands for 'search package
        1', and it means that PPM considers 'Convert-Context' to be the
        default package. If you now type 'describe' or 'install' with no
        arguments, PPM will apply your command to this package.

        If you go back to where you had no default package selected:

            ppm:1:1> search Text
            1. Convert-Context  [0.501]     an Attributed Text data type
            2. gettext          [1.01]      message handling functions
            3. HTML-FromText    [1.005]     mark up text as HTML
            4. HTML-Subtext     [1.03]      Perform text substitutions on an HTML
                                            template
            5. Locale-Maketext  [0.18]      framework for software localization
            ppm:1:1:s1>

        ...and you describe 'Locale-Maketext', you will see this:

            ppm:1:1:s1> describe Locale-Maketext
            ====================
                Name: Locale-Maketext
             Version: 0.18
              Author: Sean M. Burke (sburke@cpan.org)
            Abstract: framework for software localization
            Prerequisites:
                   1. I18N-LangTags 0.13
            Implementations:
                   1. i686-linux-thread-multi
                   2. MSWin32-x86-multi-thread
                   3. sun4-solaris-thread-multi
            ====================
            ppm:1:1:s1:sp5>

        Notice that the correct package got selected, even though you
        specified it by name.

    This behaviour also applies to the query and properties commands.

  See Also
    describe, properties, query, search
END

#sub run_quickstart  { $_[0]->run_help('quickstart') }
sub smry_quickstart { "a crash course in using PPM" }
sub help_quickstart { <<'END' }
quickstart -- a beginners' guide to PPM3
  Description
    PPM (Programmer's Package Manager) is a utility for managing software
    "packages". A package is a modular extension for a language or a
    software program. Packages reside in repositories. PPM can use three
    types of repositories:

     1) A directory on a CD-ROM or hard drive in your computer
     2) A website
     3) A remote Repository Server (such as ASPN)

    Common Commands:

    To view PPM help:

      help
      help <command>

    To view the name of the current repository:

      repository

    To search the current repository:

      search <keywords>

    To install a package:

      install <package_name>

    Most commands can be truncated; as long as the command is unambiguous,
    PPM will recognize it. For example, 'repository add foo' can be entered
    as 'rep add foo'.

    PPM features user profiles, which store information about installed
    packages. Profiles are stored as part of your ASPN account; thus, you
    can easily maintain package profiles for different languages, or
    configure one machine with your favorite packages, and then copy that
    installation to another machine by accessing your ASPN profile.

    For more information, type 'help profile' at the PPM prompt.
END

sub smry_ppm_migration { "guide for those familiar with PPM" }
sub help_ppm_migration { <<'END' }
ppm migration -- PPM Migration Guide
  Description
    Those familiar with PPM version 2 should appreciate the extended
    functionality of PPM version 3, including the command-line history,
    autocomplete and profiles. Some PPM version 2 commands are different in
    PPM version 3. Examples of command changes include:

    1   Adding a repository

        PPM2:

          set repository my_repository http://my/repository

        PPM3:

          repository add my_repository http://my/repository

    2   Removing a repository

        PPM2:

          set repository --remove my_repository

        PPM3:

          repository del my_repository

    3   Setting the temporary directory

        PPM2:

          set build DIRECTORY

        PPM3

          set tempdir DIRECTORY

    4   Setting frequency of download updates

        PPM2:

          set downloadstatus NUMBER

        PPM3:

          set download-chunksize NUMBER

    5   Changing the installation root directory:

        PPM2:

          set root DIRECTORY

        PPM3:

          target set root DIRECTORY

    6   Listing all installed packages:

        PPM2:

          query

        PPM3:

          query *

    7   Listing all packages on server:

        PPM2:

          search

        PPM3:

          search *

    8   Enabling HTML documentation generation:

        PPM2:

          set rebuildhtml 1

        PPM3:

          set rebuild-html 1
END

sub smry_unicode { "notes about unicode author names" }
sub help_unicode { <<'END' }
unicode -- Notes About Unicode Author Names
  Description
    CPAN author names are defined to be in Unicode. Unicode is an
    international standard ISO 10646, defining the *Universal Character Set
    (UCS)*. UCS contains all characters of all other character set
    standards. For more information about Unicode, see
    http://www.unicode.org/.

    The CPAN authors website is located at your local CPAN mirror under
    /authors/00whois.html. For example, you can view it at
    http://www.cpan.org/authors/00whois.html. This page can be rendered by
    Mozilla 0.9.8 and Internet Explorer 5.0, but you may have to install
    extra language packs to view all the author names.

    By default, PPM3 renders all characters as Latin1 when it prints them to
    your console. Characters outside the Latin1 range (0-255) are not
    printed at all.

    If your console can render UTF-8 characters, you can tell PPM3 not to
    recode characters by using one of the following environment variables:

    *   LC_ALL

    *   LC_CTYPE

    *   LANG

    *   PPM_LANG

    PPM3 requires one of these environment variables to contain the string
    'UTF-8'. For example, the following setting make PPM3 print
    beautifully-formatted authors in RedHat Linux 7.2 (assumes you're using
    a Bourne shell):

      $ PPM_LANG='en_US.UTF-8' xterm -u8 -e ppm3

    Linux and Solaris users should refer to xterm for more information about
    setting up xterm to display UTF-8 characters.
END

#============================================================================
# Utility Functions
#============================================================================
sub sort_fields { qw(name title author abstract version repository) }
sub sort_pkgs {
    my $o = shift;
    my $field = lc shift;
    my @pkgs = @_;
    my $targ = $o->conf('target');
    my $filt = sub { $_[0]->getppd_obj($targ)->result->$field };
    if ($field eq 'name') {
	return dictsort $filt, @pkgs;
    }
    if ($field eq 'title') {
	return dictsort $filt, @pkgs;
    }
    if ($field eq 'author') {
	return dictsort $filt, @pkgs;
    }
    if ($field eq 'abstract') {
	return dictsort $filt, @pkgs;
    }
    if ($field eq 'repository') {
	return dictsort sub { $_[0]->repository->name }, @pkgs;
    }
    if ($field eq 'version') {
	return sort {
	    my $pa = $a->getppd_obj($targ)->result;
	    my $pb = $b->getppd_obj($targ)->result;
	    $pb->uptodate($pa->version_osd) <=> $pa->uptodate($pb->version_osd)
	} @pkgs;
    }
    @pkgs;
}

sub find_index {
    my $entry = shift || '';
    my $index = shift;
    $index = 0 unless defined $index;
    for (my $i=0; $i<@_; $i++) {
	return $index + $i if $entry eq $_[$i];
    }
    return $index - 1;
}

sub bounded {
    my $lb = shift;
    my $d = shift;
    my $ub = shift;
    return ($d >= $lb and $d <= $ub);
}

sub dictsort(@) {
    my $o = shift if eval { $_[0]->isa("PPMShell") };
    my $filt = ref($_[0]) eq 'CODE' ? shift @_ : undef;
    return map { $_->[0] }
	   sort { lc $a->[1] cmp lc $b->[1] }
	   map { [ $_, $filt ? $filt->($_) : $_ ] } @_;
}

sub path_under {
    my $path = shift;
    my $cmp  = shift;
    if ($^O eq 'MSWin32') {
	$path =~ s#\\#/#g;
	$cmp  =~ s#\\#/#g;
	return $path =~ /^\Q$cmp\E/i;
    }
    else {
	return $path =~ /^\Q$cmp\E/;
    }
}

sub prompt_str {
    my $o = shift;

    # Hack: set the pager here, instead of in settings_setkey()
    $o->{API}{pager} = $o->conf('pager');

    my @search_results = $o->cache_sets('search');
    my $search_result_current = $o->cache_set_current('search');
    my $search_result_index = $o->cache_set_index('search');
    my @query_results = $o->cache_sets('query');
    my $query_result_current = $o->cache_set_current('query');
    my $query_result_index = $o->cache_set_index('query');

    # Make sure a profile is selected if they turned tracking on.
    my $profile_track = $o->conf('profile-track');
    my $profile       = PPM::UI::profile_get()->result;
    $o->setup_profile()
	if $profile_track and not $profile and $o->mode eq 'SHELL';

    my @targs = PPM::UI::target_list()->result_l;
    if (@targs and not find_index($o->conf('target'), 1, @targs)) {
	$o->conf('target', $targs[0]);
    }

    if ($o->conf('prompt-context')) {
	my ($targ, $rep, $s, $sp, $q, $qp);

	if ($o->conf('prompt-verbose')) {
	    my $sz = $o->conf('prompt-slotsize');
	    $targ = substr($o->conf('target'), 0, $sz);
	    $rep  = substr($o->conf('repository'), 0, $sz);

	    my $sq_tmp = $o->cache_set('search', undef, 'query');
	    my $ss_tmp = $o->cache_set('search');
	    my $sp_tmp = $o->cache_entry('search');
	    $s = (defined $sq_tmp)
		  ? ":" . substr($sq_tmp, 0, $sz)
		  : "";
	    $sp = ($s and defined $sp_tmp and
		   bounded(0, $search_result_index, $#$ss_tmp))
		  ? ":" . substr($sp_tmp->name, 0, $sz)
		  : "";

	    my $qq_tmp = $o->cache_set('query', undef, 'query');
	    my $qs_tmp = $o->cache_set('query');
	    my $qp_tmp = $o->cache_entry('query');
	    $q = (defined $qq_tmp)
		  ? ":" . substr($qq_tmp, 0, $sz)
		  : "";
	    $qp = ($q and defined $qp_tmp and
		   bounded(0, $query_result_index, $#$qs_tmp))
		  ? ":" . substr($qp_tmp->name, 0, $sz)
		  : "";
	}
	else {
	    # Target and Repository:
	    $targ = find_index($o->conf('target'), 1, @targs);
	    $targ = '?' if $targ == 0;
	
	    # Search number & package:
	    $s = @search_results ? ":s".($search_result_current + 1) : "";
	    my $sp_tmp = $o->cache_set('search');
	    $sp = ($s and defined $sp_tmp and 
		   bounded(0, $search_result_index, $#$sp_tmp))
		  ? ":sp".($search_result_index + 1)
		  : "";
	
	    # Query number & package:
	    $q = @query_results ? ":q".($query_result_current + 1) : "";
	    my $qp_tmp = $o->cache_set('query');
	    $qp = ($q and defined $qp_tmp and
		   bounded(0, $query_result_index, $#$qp_tmp))
		  ? ":qp".($query_result_index + 1)
		  : "";
	}
	return "ppm:$targ$s$sp$q$qp> ";
    }
    else {
	return "ppm> ";
    }
}

{
    # Weights for particular fields: these are stored in percentage of the
    # screen width, based on the number of columns they use on an 80 column
    # terminal. They also have a minimum and maximum.
    use constant MIN    => 0;
    use constant MAX    => 1;
    my %weight = (
	name     => [12, 20],
	title    => [12, 20],
	abstract => [12, 20],
	author   => [12, 20],
	repository => [12, 20],
	version  => [ 4,  9],
    );
    my %meth = (
	name     => 'name',
	title    => 'title',
	version  => 'version',
	abstract => 'abstract',
	author   => 'author',
	repository => sub {
	    my $o = shift;
	    my $rep = $o->repository or return "Installed";
	    my $name = $rep->name;
	    my $id   = $o->id || $name;
	    my $loc  = $rep->location;
	    "$name [$loc]"
	},
    );
    # These are Text::Autoformat justification marks. They're actually used to
    # build a printf() format string, since it's so much more efficient for a
    # non-line-wrapping case.
    my %just = (
	name     => '<',
	title    => '<',
	abstract => '<',
	author   => '<',
	repository => '<',
	version  => '>',
    );
    my %plus = (
	name     => '0',
	title    => '0',
	abstract => '0',
	author   => '0',
	repository => '0',
	version  => '2',
    );
    my %filt = (
	version => q{"[$_]"},
    );
    sub picture_optimized {
	my $o = shift;
	my @items = @{shift(@_)};
	unless ($o->conf('fields')) {
	    my $m = $o->setmode('SILENT');
	    $o->conf('fields', '', 1);
	    $o->setmode($m);
	}
	my @fields = split ' ', $o->conf('fields');
	$_ = lc $_ for @fields;
	my (%max_width, %width);
	my $cols = $o->termsize->{cols};
	for my $f (@fields) {
	    my $meth = $meth{$f};
	    $max_width{$f} = max { length($_->$meth) } @items;
	    $max_width{$f} += $plus{$f};
	    $width{$f} = $max_width{$f} / 80 * $cols;
	    my $max_f  = $weight{$f}[MAX] / 80 * $cols;
	    my $min_f  = $weight{$f}[MIN];
	    my $gw     = $width{$f};
	    $width{$f} = (
		$width{$f} > $max_width{$f} ? $max_width{$f} :
		$width{$f} > $max_f         ? $max_f         :
		$width{$f} < $min_f         ? $min_f         : $width{$f}
	    );
	}
	my $right = $fields[-1];
	my $index_sz = length( scalar(@items) ) + 3; # index spaces
	my $space_sz = @fields + 1; # separator spaces
	my $room = $cols - $index_sz - $space_sz;
	$width{$right} = $room - sum { $width{$_} } @fields[0 .. $#fields-1];
	while ($width{$right} > $max_width{$right}) {
	    my $smallest;
	    my $n;
	    for my $k (@fields[0 .. $#fields-1]) {
		my $max = $max_width{$k};
		my $sz  = $width{$k};
		$smallest = $k, $n = $max - $sz if $max - $sz > $n;
	    }
	    $width{$right}--;
	    $width{$smallest}++;
	}
	while ($width{$right} < $weight{$right}[MIN]) {
	    my $biggest;
	    my $n;
	    for my $k (@fields[0 .. $#fields-1]) {
		my $max = $max_width{$k};
		my $sz  = $width{$k};
		$biggest = $k, $n = $max - $sz if $max - $sz < $n;
	    }
	    $width{$right}++;
	    $width{$biggest}--;
	}
	my $picture;
	$picture = "\%${index_sz}s "; # printf picture
	$picture .= join ' ', map {
	    my $w = $width{$_};
	    my $c = $just{$_};
	    my $pad = $c eq '>' ? '' : '-';
	    "\%${pad}${w}s" # printf picture
	} @fields;
	($picture, \@fields, [@width{@fields}]);
    }

    sub print_formatted {
	my $o = shift;
	my $targ = $o->conf('target');
	my @items = map { $_->getppd_obj($targ)->result } @{shift(@_)};
	my $selected = shift;
	my $format;

	# Generate a picture and a list of fields for Text::Autoformat:
	my (@fields, %width);
	my ($picture, $f, $w) = $o->picture_optimized(\@items);
	$picture .= "\n";
	@fields = @$f;
	@width{@fields} = @$w;

	# The line-breaking sub: use '~' as hyphenation signal
	my $wrap = sub {
	    my ($str, $maxlen, $width) = @_;
	    my $field = substr($str, 0, $maxlen - 1) . '~';
	    my $left  = substr($str, $maxlen - 1);
	    ($field, $left);
	};

	my $lines = 0;
	my $i = 1;
	my @text;
	my %seen;
	for my $pkg (@items) {
	    my $star = (defined $selected and $selected == $i - 1) ? "*" : " ";
	    my $num  = "$star $i.";
	    my @vals = (
		map {
		    my $field  = $_;
		    my $method = $meth{$field};
		    local $_   = $pkg->$method;
		    my $val = defined $filt{$field} ? eval $filt{$field} : $_;
		    ($val) = $wrap->($val, $width{$field})
		        if length $val > $width{$field};
		    $val;
		}
		@fields
	    );
#	    my $key = join '', @vals;
#	    if (exists $seen{$key}) {
#		my $index = $seen{$key};
#		substr($text[$index], 0, 1) = '+';
#		next;
#	    }
#	    $seen{$key} = $i - 1;
	    (my $inc = sprintf $picture, $num, @vals) =~ s/[ ]+$//;
	    push @text, $inc;
	    $i++;
	}

	# And, page it.
	$o->page(join '', @text);
    }
}

sub tree_pkg {
    my $o = shift;
    my @rlist = $o->reps_on;
    my $tar = $o->conf('target');
    my $pkg = shift;
    my $ppd;
    if (eval { $pkg->isa('PPM::Package') }) {
	$ppd = $pkg->getppd_obj($tar);
	unless ($ppd->ok) {
	    $o->warn($ppd->msg);
	    return;
	}
	$ppd = $ppd->result;
    }
    else {
	my ($s, $i) = $o->cache_find('search', $pkg);
	if ($i >= 0) {
	    $ppd = $o->cache_entry('search', $i, $s);
	} 
	else {
	    my $ok = PPM::UI::describe(\@rlist, $tar, $pkg);
	    unless ($ok->is_success) {
		$o->warn($ok->msg);
		return unless $ok->ok;
	    }
	    $ppd = $ok->result->getppd_obj($tar);
	    unless ($ppd->ok) {
		$o->warn($ppd->msg);
		return;
	    }
	    $ppd = $ppd->result;
	}
    }

    my $pad = "\n";
    $o->inform($ppd->name, " ", $ppd->version);
    $o->Tree(\@rlist, $tar, $ppd->name, $pad, {});
    $o->inform($pad);
}

my ($VER, $HOR, $COR, $TEE, $SIZ) = ('|', '_', '\\', '|', ' ');

sub Tree {
    my $o = shift;
    my $reps = shift;
    my $tar = shift;
    my $pkg = shift;
    my $ind = shift;
    my $seen = shift;
    my $pad = $ind . "  " . $VER;

    my $ppd;
    if (exists $seen->{$pkg}) {
	$ppd = $seen->{$pkg};
    }
    else {
	my ($s, $i) = $o->cache_find('search', $pkg);
	if ($i >= 0) {
	    $ppd = $o->cache_entry('search', $i, $s);
	}
	else {
	    my $ok = PPM::UI::describe($reps, $tar, $pkg);
	    unless ($ok->is_success) {
		$o->inform(" -- package not found; skipping tree");
		return 0 unless $ok->ok;
	    }
	    $ppd = $ok->result;
	}
	$ppd->make_complete($tar);
	$ppd = $ppd->getppd_obj($tar);
	unless ($ppd->ok) {
	    $o->warn($ppd->msg);
	    return;
	}
	$ppd = $ppd->result;
	$seen->{$pkg} = $ppd;
    }

    my @impls   = $ppd->implementations;
    return 0 unless @impls;
    my @prereqs = $impls[0]->prereqs;
    return 0 unless @prereqs;
    my $nums = scalar @prereqs;

    for (1..$nums) {
	my $doneblank = 0;
	my $pre = $prereqs[$_-1];
	my $txt = $pre->name . " " . $pre->version;
	if ($_ == $nums) {
	    substr($pad, -1) = $COR;
	    $o->inform($pad, "$HOR$HOR", $txt);
	    substr($pad, -1) = ' ';
	}
	else {
	    substr($pad, -1) = $TEE;
	    $o->inform($pad, "$HOR$HOR", $txt);
	    substr($pad, -1) = $VER;
	}
	if ($o->Tree($reps, $tar, $pre->name, $pad, $seen) != 0 and
	    $doneblank == 0) {
	    $o->inform($pad); ++$doneblank;
	}
    }
    return $nums;
}

sub describe_pkg {
    my $o = shift;
    my $pkg = shift;
    my ($extra_keys, $extra_vals) = (shift || [], shift || []);
    my $n; 

    # Get the PPM::PPD object out of the PPM::Package object.
    my $pkg_des = $pkg->describe($o->conf('target'));
    unless ($pkg_des->ok) {
	$o->warn($pkg_des->msg);
	return;
    }
    $pkg_des = $pkg_des->result;

    # Basic information:
    $n = $o->print_pairs(
	[qw(Name Version Author Title Abstract), @$extra_keys],
	[(map { $pkg_des->$_ } qw(name version author title abstract)),
	 @$extra_vals],
	undef,	# separator
	undef,	# left
	undef,	# indent
	undef,	# length
	1,	# wrap (yes, please wrap)
    );

    # The repository:
    if (my $rep = $pkg_des->repository) {
	$o->print_pairs(
	    ["Location"],
	    [$rep->name],
	    undef,	# separator
	    undef,	# left
	    undef,	# indent
	    $n,		# length
	    1,		# wrap
	);
    }
    
    # Prerequisites:
    my @impls = grep { $_->architecture } $pkg_des->implementations;
    my @prereqs = @impls ? $impls[0]->prereqs : ();
    $o->inform("Prerequisites:\n") if @prereqs;
    $o->print_pairs(
	[ 1 .. @prereqs ],
	[ map { $_->name . ' ' . $_->version} @prereqs ],
	'. ',	# separator
	undef,	# left
	undef,	# indent
	$n,	# length
	0,	# wrap (no, please don't wrap)
    );
    
    # Implementations:
    $o->inform("Available Platforms:\n") if @impls;
    my @impl_strings;
    for (@impls) {
	my $arch  = $_->architecture;
	my $os    = $_->os;
	my $osver = $_->osversion;
	my $str   = $arch;
	$osver    =~ s/\Q(any version)\E//g;
	if ($os and $osver) {
	    $str .= ", $os $osver";
	}
	push @impl_strings, $str;
    }
    @impl_strings = dictsort @impl_strings;
    $o->print_pairs(
	[ 1 .. @impls ],
	[ @impl_strings ],
	'. ', undef, undef, $n
    );
}

sub remove_pkg {
    my $o = shift;
    my $package = shift;
    my $target = $o->conf('target');
    my $force = shift;
    my $quell_clear = shift;
    my $verbose = $o->conf('remove-verbose');
    my $ok = PPM::UI::remove($target, $package, $force, sub { $o->cb_remove(@_) }, $verbose);
    unless ($ok->is_success) {
	$o->warn($ok->msg);
	return 0 unless $ok->ok;
    }
    else {
	$o->warn_profile_change($ok);
    }
    $o->cache_clear('query') if ($ok->ok and not $quell_clear);
    1;
}

sub upgrade_pkg {
    push @_, 'upgrade';
    goto &install_pkg;
}
sub install_pkg {
    my $o = shift;
    my $pkg = shift;
    my $opts = shift;
    my $action = shift;
    my $quell_clear = shift;
    $action = 'install' unless defined $action;

    # Find the package:
    while (1) {
	# 1. Return if they specified a full filename or URL:
	last if PPM::UI::is_pkg($pkg);

	# 2. Check if whatever they specified returns 1 search result:
	my $search =
	  PPM::UI::search([$o->reps_on], $o->conf('target'), $pkg, 
			  $o->conf('case-sensitivity'));
	unless ($search->is_success) {
	    $o->warn($search->msg);
	    return unless $search->ok;
	}
	my @ret = $search->result_l;
	if (@ret > 1) {
	    $o->warn(<<END);
Searching for '$pkg' returned multiple results. Using 'search' instead...
END
	    $o->run_search($pkg);
	    return;
	}
	elsif (not @ret) {
	    $o->warn(<<END);
Searching for '$pkg' returned no results. Try a broader search first.
END
	    return;
	}
	$pkg = $ret[0]->name;
	last;
    }

    my $cb = (
	$opts->{dryrun}
	? $action eq 'install' ? \&dr_install : \&dr_upgrade
	: $action eq 'install' ? \&cb_install : \&cb_upgrade
    );

    # Now, do the install
    my $ok;
    my @rlist = $o->reps_on;
    my $targ = $o->conf('target');

    my $prop = PPM::UI::properties($targ, $pkg);
    if ($prop->ok) {
	my $name = ($prop->result_l)[0]->name;
	if (ref $pkg) {
	    $pkg->name($name);
	}
	else {
	    $pkg = $name;
	}
    }

    if ($action eq 'install') {
	$opts->{verbose} = $o->conf('install-verbose');
	my $pkgname = ref $pkg ? $pkg->name : $pkg;
	if ($prop->ok) {
	    $o->inform("Note: Package '$pkgname' is already installed.\n");
	    return unless $opts->{force};
	}
	$ok = PPM::UI::install(\@rlist, $targ, $pkg, $opts, sub {$o->$cb(@_)});
    }
    else {
	$opts->{verbose} = $o->conf('upgrade-verbose');
	$ok = PPM::UI::upgrade(\@rlist, $targ, $pkg, $opts, sub {$o->$cb(@_)});
    }

    unless ($ok->is_success) {
	$o->warn($ok->msg);
	return unless $ok->ok;
    }
    else {
	$o->warn_profile_change($ok);
	$o->cache_clear('query') unless $quell_clear;
    }
    1;
}

# The dry run callback; just prints out package name and version:
sub dr_install {
    my $o = shift;
    my $pkg = shift;
    my $version = shift;
    my $target_name = shift;
    $o->inform(<<END);
Dry run install '$pkg' version $version in $target_name.
END
}

sub dr_upgrade {
    my $o = shift;
    my $pkg = shift;
    my $version = shift;
    my $target_name = shift;
    $o->inform(<<END);
Dry run upgrade '$pkg' version $version in $target_name.
END
}

sub dr_remove {
    my $o = shift;
    my $pkg = shift;
    my $version = shift;
    my $target_name = shift;
    $o->inform(<<END);
Dry run remove '$pkg' version $version from $target_name.
END
}

sub cb_remove {
    my $o = shift;
    my $pkg = shift;
    my $version = shift;
    my $target_name = shift;
    my $status = shift;
    if ($status eq 'COMPLETE') {
	$o->inform(
	    "Successfully removed $pkg version $version from $target_name.\n"
	)
    }
    else {
	$o->inform(<<END);
$SEP
Remove '$pkg' version $version from $target_name.
$SEP
END
    }
}

sub cb_install {
    my $o = shift;
    unshift @_, $o, 'install';
    &cb_status;
}

sub cb_upgrade {
    my $o = shift;
    unshift @_, $o, 'upgrade';
    &cb_status;
}

sub cb_status {
    my $o = shift;
    my $ACTION = shift;
    my $pkg = shift;
    my $version = shift;
    my $target_name = shift;
    my $status = shift;
    my $bytes = shift;
    my $total = shift;
    my $secs = shift;

    my $cols = $ENV{COLUMNS} || 78;

    $o->inform(<<END) and return if ($status eq 'PRE-INSTALL');
$SEP
\u$ACTION '$pkg' version $version in $target_name.
$SEP
END

    # Print the output on one line, repeatedly:
    my ($line, $pad, $eol);
    if ($status eq 'DOWNLOAD') {
	if ($bytes < $total) {
	    $line = "Transferring data: $bytes/$total bytes.";
	    $eol = "\r";
	}
	else {
	    $line = "Downloaded $bytes bytes.";
	    $eol = "\n";
	}
    }
    elsif ($status eq 'PRE-EXPAND') {
	$line = ""; #"Extracting package. This may take a few seconds.";
	$eol = "\r";  #"\n";
    }
    elsif ($status eq 'EXPAND') {
	$line = "Extracting $bytes/$total: $secs";
	$eol = $bytes < $total ? "\r" : "\n";
    }
    elsif ($status eq 'COMPLETE') {
	my $verb = $ACTION eq 'install' ? 'installed' : 'upgraded';
	$o->inform(
	    "Successfully $verb $pkg version $version in $target_name.\n"
	);
	return;
    }
    $pad = ' ' x ($cols - length($line));
    $o->verbose($line, $pad, $eol);
}

sub warn_profile_change {
    my $o = shift;
    my $ok = shift;

    my $profile_track = $o->conf('profile-track');
    my $profile = PPM::UI::profile_get()->result;

    if ($profile_track) {
	$o->verbose(<<END);
Tracking changes to profile '$profile'.
END
    }
}

sub parse_range {
    my @numbers;
    my $arg;
    while ($arg = shift) {
      while ($arg) {
	if ($arg =~ s/^\s*,?\s*(\d+)\s*-\s*(\d+)//) {
	    push @numbers, ($1 .. $2);
	}
	elsif ($arg =~ s/^\s*,?\s*(\d+)//) {
	    push @numbers, $1;
	}
	else {
	    last;
	}
      }
    }
    @numbers;
}

sub raw_args {
    my $o = shift;
    strip($o->line_args);
}

sub strip {
    my $f = shift;
    $f =~ s/^\s*//;
    $f =~ s/\s*$//;
    $f;
}

# matches("neil", "ne|il") => 1
# matches("ne", "ne|il") => 1
# matches("n", "ne|il") => 0
sub matches {
    my $cmd = shift;
    my $pat = shift || "";

    my ($required, $extra) = split '\|', $pat;
    $extra ||= "";
    my $regex = "$required(?:";
    for (my $i=1; $i<=length($extra); $i++) {
	$regex .= '|' . substr($extra, 0, $i);
    }
    $regex .= ")";
    return $cmd =~ /^$regex$/i;
}

sub pause_exit {
    my $o = shift;
    my $exit_code = shift || 0;
    my $pause = shift || 0;
    if ($pause) {
	if ($o->have_readkey) {
	    $o->inform("Hit any key to exit...");
	}
	else {
	    $o->inform("Hit <ENTER> to exit...");
	}
	$o->readkey;
    }
    exit $exit_code;
}

#============================================================================
# Check if this is the first time we've ever used profiles. This can be
# guessed: if the 'profile' entry is not set, but the 'profile-track' flag
# is, then it's the first time profile-track has been set to '1'.
#============================================================================
sub setup_profile {
    my $o = shift;
    $o->inform(<<END);
$SEP
You have profile tracking turned on: now it's time to choose a profile name.
ActiveState's PPM 3 Server will track which packages you have installed on
your machine. This information is stored in a "profile", located on the
server.

Here are some features of profiles:
 o You can have as many profiles as you want;
 o Each profile can track an unlimited number of packages;
 o PPM defaults to "tracking" your profile (it updates your profile every time
   you add or remove a package;
 o You can disable profile tracking by modifying the 'profile-track' option;
 o You can manually select, save, and restore profiles;
 o You can view your profile from ASPN as well as inside PPM 3.
$SEP

END

    my $response = PPM::UI::profile_list();
    my @l;
    unless ($response->ok) {
	$o->warn("Failed to enable profile tracking: ".$response->msg);
	$o->warn(<<END);

You can still use PPM3, but profiles are not enabled. To try setting up
profiles again, enter 'set profile-track=1'. Or, you can set up profiles
by hand, using the 'profile add' command.

END
	$o->run('unset', 'profile-track');
	return;
    }
    else {
	@l = sort $response->result_l;
	$o->inform("It looks like you have profiles on the server already.\n")
	  if @l;
	$o->print_pairs([1 .. @l], \@l, '. ', 1, ' ');
	$o->inform("\n") if @l;
    }

    require PPM::Sysinfo;
    (my $suggest = PPM::Sysinfo::hostname()) =~ s/\..*$//;
    $suggest ||= "Default Profile";
    my $profile_name = $o->prompt(
	"What profile name would you like? [$suggest] ", $suggest, @l
    );

    my $select_existing = grep { $profile_name eq $_ } $response->result_l
      if $response->ok;
    if ($select_existing) {
	$o->inform("Selecting profile '$profile_name'...\n");
	PPM::UI::profile_set($profile_name);
	$o->inform(<<END);
You should probably run either 'profile save' or 'profile restore' to bring
the profile in sync with your computer.
END
    }
    elsif ($response->ok) {
	$o->inform("Creating profile '$profile_name'...\n");
	$o->run('profile', 'add', $profile_name);
	$o->inform("Saving profile '$profile_name'...\n");
	$o->run('profile', 'save');
	$o->inform(<<END);
Congratulations! PPM is now set up to track your profile.
END
    }
    else {
	$o->warn($response->msg);
	$o->warn(<<END);

You can still use PPM3, but profiles will not be enabled. To try setting up
profiles again, enter 'set profile-track=1'. Or, you can set up profiles
yourself using the 'profile add' command.

END
	$o->run('unset', 'profile-track');
    }
}

package main;
use Getopt::Long;
use Data::Dumper;

$ENV{PERL_READLINE_NOWARN} = "1";
$ENV{PERL_RL} = $^O eq 'MSWin32' ? "0" : "Perl";

my ($pause, $input_file, $target);

BEGIN {
    my ($shared_config_files, @fixpath, $gen_inst_key);

    Getopt::Long::Configure('pass_through');
    $target = 'auto';
    GetOptions(
	'file=s' => \$input_file,
	'shared' => \$shared_config_files,
	'target:s' => \$target,
	'fixpath=s' => \@fixpath,
	'generate-inst-key' => \$gen_inst_key,
	pause => \$pause,
    );
    Getopt::Long::Configure('no_pass_through');

    if ($shared_config_files) {
	$ENV{PPM3_shared_config} = 1;
    }

    if (@fixpath) {
	PPM::UI::target_fix_paths(@fixpath);
	exit;
    }
    if ($gen_inst_key) {
	require PPM::Config;
	PPM::Config::load_config_file('instkey');
	exit;
    }
}

# If we're being run from a file, tell Term::Shell about it:
if ($input_file) {
    my $line = 0;
    open SCRIPT, $input_file or die "$0: can't open $input_file: $!";
    my $shell = PPMShell->new(
	term => ['PPM3', \*SCRIPT, \*STDOUT],
	target => $target,
	pager => 'none',
    );
    $shell->setmode('SCRIPT');
    while (<SCRIPT>) {
	$line++;
	next if /^\s*#/ or /^\s*$/;
	my ($cmd, @args) = $shell->line_parsed($_);
	my $ret = $shell->run($cmd, @args);
	my $warn = <<END;
$0: $input_file:$line: fatal error: unknown or ambiguous command '$cmd'. 
END
	$shell->warn($warn) and $shell->pause_exit(2, $pause)
	    unless $shell->{API}{cmd}{run}{found};
	$shell->pause_exit(1, $pause) unless $ret;
    }
    close SCRIPT;
    $shell->pause_exit(0, $pause);
}

# If we've been told what to do from the command-line, do it right away:
elsif (@ARGV) {
    my $shell = PPMShell->new(target => $target, pager => 'none');
    $shell->setmode('BATCH');
    my $ret = $shell->run($ARGV[0], @ARGV[1..$#ARGV]);
    my $warn = <<END;
Unknown or ambiguous command '$ARGV[0]'; type 'help' for commands.
END
    $shell->warn($warn) and $shell->pause_exit(2, $pause)
	unless $shell->{API}{cmd}{run}{found};
    $shell->pause_exit(0, $pause) if $ret;
    $shell->pause_exit(1, $pause);
}

# Just run the command loop
if (-t STDIN and -t STDOUT) {
    my $shell = PPMShell->new(target => $target);
    $shell->setmode('SHELL');
    $shell->cmdloop;
}
else {
    die <<END;

Error:
    PPM3 cannot be run in interactive shell mode unless both STDIN and
    STDOUT are connected to a terminal or console. If you want to
    capture the output of a command, use PPM3 in batch mode like this:

       ppm3 search IO-stringy > results.txt

    Type 'perldoc ppm3' for more information.

END
}


=head1 NAME

ppm3-bin - ppm3 executable

=head1 SYNOPSIS

Do not run I<ppm3-bin> manually. It is meant to be called by the wrapper
program I<ppm3>. See L<ppm3>.

=head1 DESCRIPTION

I<ppm3> runs I<ppm3-bin> after setting up a few environment variables. You
should run I<ppm3> instead.

For information about I<ppm3> commands, see L<ppm3>.

=head1 SEE ALSO

See L<ppm3>.

=head1 AUTHOR

ActiveState Corporation (support@ActiveState.com)

=head1 COPYRIGHT

Copyright (C) 2001, 2002, ActiveState Corporation. All Rights Reserved.

=cut

__END__
:endofperl
