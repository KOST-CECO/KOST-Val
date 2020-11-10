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
use Win32::TieRegistry;

my $R = $Registry;
$R->Delimiter('/');

# Note: the double slashes in front of InstallLocation are *not* a typo:
# they indicate that InstallLocation is a named value under the PPM key.
my $key1 = 'HKEY_LOCAL_MACHINE/SOFTWARE/ActiveState/PPM//InstallLocation';
my $key2 = 'HKEY_CURRENT_USER/SOFTWARE/ActiveState/PPM//InstallLocation';
my $own  = "$Config{binexp}\\ppm3-bin";
my $opt_exe = -f $own			? $own		: 
	      defined $R->{$key1}	? $R->{$key1}	:
	      defined $R->{$key2}	? $R->{$key2}	: undef;

die "Error: neither '$key1' nor '$key2' found in registry"
  unless defined $opt_exe;

# Tell ppm3-bin where it was launched from. (Used when auto-selecting what
# target to use.)
use Config;
$ENV{PPM3_PERL_PREFIX} = $Config{prefix};  # should really be 'prefixexp'
					   # but most versions on windows don't
					   # have it set right
$ENV{PPM3_PERL_SITELIB} = $Config{sitelibexp};

# If $shared is true, then tell PPM3 to look at the shared config.
# If $user   is true, then tell PPM3 to look at the user's config.
# If $tree   is true, then tell PPM3 to look at the perl tree's config.
# Note: if both of these are true, PPM3 will not work properly!
$ENV{PPM3_SHARED} = 1 if $opt_shared;
$ENV{PPM3_USER}   = 1 if $opt_user;

system($opt_exe, @ARGV);
exit ($? >> 8);

=head1 NAME

ppm3 - Programmer's Package Manager, version 3.1

=head1 SYNOPSIS

ppm3 [-file=f] [-shared] [-target[=t]] [-pause] [command [args]]

When run with no arguments, PPM3 starts up an interactive shell where you can
enter commands. You may specify command-line arguments to run a single
command.

For example, the following two sessions are equivalent:

   $ ppm
   ppm> search Tk
   [results displayed]
   ppm> exit

   $ ppm search Tk
   [results displayed]

In the first case, PPM returns you to the prompt, where you can enter more
commands.

=head1 DESCRIPTION

The program I<ppm3> is intended to simplify the tasks of locating, installing,
upgrading and removing Perl packages.

I<ppm3> runs in one of three modes: an interactive shell from which commands
may be entered; a script interpreter, reading sequential commands from a
file; and command-line mode, in which one specific action is performed per
invocation of the program.

=head1 COMMANDS

The following sections describe each command available in PPM. The following
help is also available via the online help feature by typing 'help' in PPM3.


=head1 describe -- Describe Packages

=head2 Synopsis

 des                Describes default/current package
 des <number>       Describes package <number> in the
                    current search set
 des <range>        Describes packages in the given 
                    <range> from the current search
 des <package name> Describes named package
 des <url>          Describes package located at <url>
 des <glob pattern> Performes a new search using <glob pattern>

=head2 Description

The describe command returns information about a package, including
the name of the package, the author's name and a brief description
(called an "Abstract") about the package. For example:

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

=over 4

=item -ppd

Displays the raw PPD of the package.

=item -dump

The same as -ppd.

=back

When the describe command is called without arguments, it returns
information about the first package in the current search. If there is no
default search set, you will be prompted to use the search command
to find a package.

If describe is called with a numeric argument, that number is set as the
default package and the information about that package is returned. If the
number given doesn't exist, you will be prompted to use search to find a
package. Also, you can use describe to get descriptions of several
packages.  For example:

    describe 4-7

will return descriptions of packages 4 through 7 in the current search
request. You can also enter:

    describe 3-4,10

to get information on packages 3, 4 and 10.

If you specify a URL as the argument to describe, PPM will describe the
package located at the URL. The URL must point to a PPD file. The URL
can also point to a PPD file on your computer.

When the describe command is given a name with a wildcard (such as "*" or
"?") it executes the search command with the given argument. For example,
describe Tk* will return the name(s) of any packages that match the
search parameters.

=head2 See Also

properties

=head1 exit, q, quit -- Exit the program

=head2 Synopsis

 exit                Exit
 q                   Exit
 quit                Exit

=head2 Description

When you leave the PPM environment, the current settings are saved.

=head1 help -- General help, or help on specific commands.

=head2 Synopsis

 help                Lists available commands and help topics
 help <command>      Lists detailed help about a specific command

=head2 Description

The help command provides a brief description of the commands available
within PPM. For help on a specific command, enter help followed by the
command name. For example, enter help settings or help set for a detailed
description of the settings command.

There are some extra help topics built into PPM. They can be accessed
within the PPM environment as follows:

  help ppm_migration

shows more details about the changes from previous versions of PPM

  help quickstart

an easy-to-follow guide to getting started with PPM

  help prompt

provides a detailed explanation about the PPM prompt

=head1 install -- Install Packages

=head2 Synopsis

 install           Installs default package
 install <number>  Installs packages by a specific <number>
 install <range>   Installs packages in the given numeric <range>
 install <name>    Installs named package
 install <url>     Installs the package located at <url>

=head2 Description

The install command is used to install packages from the repository.  Install
packages by name or number (the number is given by the repository or search
request), or set a default package using the describe command. You can specify
a full URL to a PPD file; the URL may point to a PPD file on your computer.

If you have profile tracking enabled, (see 'help profile') the current profile
will be updated to include the newly installed package(s).

The following modifiers can be used with the install command:

=over 4

=item

-force

=item

-noforce

=item

-follow

=item

-nofollow

=back

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

will install the "foo" package, even if it has already been installed. If
both -force and -follow are set to "true", all the prerequisites for any
package you install will also be installed. For example, the installation
of a tk-related package, like "tk-ach" which is 8.4 kB will be preceded
by the installation of Tk, which is 1.7 MB.

You can also install by package number. Package numbers are 
based on the current repository or current search request. For example:

    install 6

installs package number 6. You can install more than one package at one
time:

    install 3-5

installs packages 3, 4 and 5. You can also type install 3-6,8 to install 
packages 3,4,5,6 and 8.

=head2 See Also

profile

=head1 profile -- Manage PPM Profiles

=head2 Synopsis

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

=head2 Description

Profiles store information about packages that are installed on your
system. If the 'profile-track' setting is enabled, your ASPN Profile will
be updated with information about installed packages. Profiles allow you
to easily migrate, reinstall, upgrade or restore PPM packages in one or
more locations.

To use profiles, you must have a license for ASPN. For license
information, see http://www.ActiveState.com/ASPN/About 
Disable profile tracking by setting 'profile-track=0'.

=head1 properties -- Describe Installed Packages

=head2 Synopsis

 prop                    Describes default installed package
 prop <number>           Describes installed package <number>
 prop <range>            Describes a <range> of installed packages
 prop <package name>     Describes named installed package
 prop <url>              Describes installed package located at <url>
 prop <glob pattern>     Performs a new query using <glob pattern>

=head2 Description

The properties command is an verbose form of the describe command. In
addition to summary information, properties will display
the installation date and a URL showing the location of the package
within the repository.

If you specify the package as a URL, PPM determines the package name
from the URL and searches for that.

When the properties command is used with wildcard arguments,
the text entered at the PPM prompt is passed to the query command.

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

=head2 See Also

describe

=head1 query -- Query Installed Packages

=head2 Synopsis

 query                   Displays list of previous queries
 query <number>          Displays results of previous query
 query <glob pattern>    Performs a new query using <glob pattern>
 query *                 Displays a list of all installed packages

=head2 Description

The query command displays a list of all installed packages, or a
list based on the <glob pattern> switch. You can also check
the list of past queries, or the results of a past query.

With PPM 3.1, you can now perform much more powerful queries. The syntax
is identical to the 'search' command, and almost all the search switches
are also available for querying installed packages.

Recall previous queries with the 'query <number>' command. PPM3
stores all queries from the current PPM session.

Note: Depending on the value of the "case-sensitivity" setting,
the query may or may not be case-sensitive. See "help settings" for
instructions on setting the default case sensitivity.

=head2 See Also

search, settings

=head1 repository -- Repository Control

=head2 Synopsis

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

The <name> needs to be put inside doublequotes if it contains any spaces.

=head2 Description

The I<repository> (or I<rep>) command controls two lists or repositories:

=over 4

=item 1

The list of "active" repositories. This is the list of repositories used by
I<search>, I<install>, and I<upgrade>.

=item 2

The list of all known repositories. You can designate a repository "inactive",
which means PPM will not use it in any commands.

=back

If no arguments are given, the rep command will list the active
repositories defined in the PPM settings. The order is significant: when
installing a package, PPM will try the first repository, then the
second, and so on, until it find the package you asked for. When
searching, PPM merges the results of all the repositories together, so the
order is less important (see the I<search> command).

For example, when you enter:

    rep

PPM3 will return something resembling this:

    Repositories:
    [1] ActiveCD
    [2] ActiveState Package Repository
    [ ] An inactive repository

In the example above, entering 'rep off 2' will disable the second repository
(the ActiveStat Package Repository). To add another repository:

    rep add [options] <NAME> <LOCATION>

The following options are available for the 'add' command:

=over 4

=item

-username 

=item

-password

=back

These options allow you to specify a username and password to be used
when logging into a repository. Currently, these are only used for FTP
and WWW repositories.

For example:

    rep add "EZE" http://foo.com/MyPPMPackages

with "EZE" being the name of the repository (for easy reference)
and the location noted by the http location. If you were to enter the
rep command again, you would see:

    ppm> rep
    Repositories:
    [1] ActiveCD
    [2] ActiveState Package Repository
    [3] EZE
    [ ] An inactive repository

To move the new repository to the top of the Active list, you would type:

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
displayed next to the repository in the Active Repositories list. You must
refer to inactive repositories by their full name.

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

To re-activate the ActiveCD repository, use the I<rep on> command. You
must refer to inactive repositories by name, not number.

    ppm> rep on ActiveCD
    Active Repositories:
    [1] EZE
    [2] ActiveState Package Repository
    [3] ActiveCD
    [ ] An inactive repository

=head2 Repository Types

PPM3 supports several types of package repositories:

=over 4

=item 1.

PPM Server 3

ActiveState's SOAP-driven package server. Because all searches are
done server-side, the server can deliver much richer information about
packages than other repositories.
       
=item 2.

PPM Server 2

The SOAP server designed for PPM version 2. PPM 3.1 ships with the
PPM2 repository as well as the PPM3 repository, so you can use
either. Simple searches are performed server-side. If your search is
too complicated for the server, PPM 3.1 will download the package
summary and search by itself.

=item 3.

Web Repositories

Older versions of PPM used non-SOAP repositories (directories full of
PPD files accessible using a web browser). Over the history of PPM,
there have been several different ways of organising the files so that
PPM can search for packages properly. PPM3 tries to download a summary
file first -- if that fails, it gets the directory index. It parses the
summary or the index, and caches it. Searches are done from the
cache.

=item 4.

FTP Repositories

FTP is another way of exposing a directory full of PPD files. PPM3
consideres FTP repositories a subset of Web repositories. Treat them as
identical: PPM3 downloads the summary or the "index" (file listing in
this case), parses it, and then searches from it.

=item 5.

Local Repositories

To support installing packages from the ActiveCD, a local directory can
be a repository. PPM searches the files in the directory. All valid
path formats are supported, including UNC paths.

=back

=head1 search -- Search for Packages

=head2 Synopsis

 search                Displays list of previous searches
 search <number>       Displays results of search <number>
 search <glob pattern> Performs a new search
 search <field>=<glob> Searches for all packages matching the field.
 search *              Displays all packages in the current repository

The available fields are 'ABSTRACT', 'NAME', 'TITLE', 'AUTHOR', and 'VERSION'.
'NAME' is used when you do not specify a field.

=head2 Description

Use the search command to look through the repository for packages.
PPM version 3 provides powerful search functionality. For example:

=over 4

=item 1.

Search for 'CGI' anywhere in the name:

  search CGI

Example results:

  Apache-CGI
  CGI-Application
  CGI-ArgChecker

=item 2.

Search for 'CGI' at the beginning of the name:

  search CGI*

Example results:

  CGI-ArgChecker
  CGI-Application

=item 3.

Search for all modules authored by someone with 'smith' in their name or
email:

  search AUTHOR=smith 

Example results:

  Apache-ProxyPass
  Business-ISBN

=item 4.

Search for 'compress' anywhere in the abstract:

  search ABSTRACT=compress

Example results:

  Apache-GzipChain
  IO-Zlib

=item 5.

Search for 'CGI' in the name, or 'web' in the abstract:

  search CGI or ABSTRACT=web

Example results:

  CGI-XMLForm
  HTML-Clean

=item 6.

Search for 'XML' in the name and either 'parser' in the name or 'pars' in the
abstract, but not with 'XPath' in the name:

  search XML and (parser or ABSTRACT=pars) and not XPath

Example results:

  XML-Node
  XML-Parser-EasyTree

=item 7.

PPM Server 3 repositories only: search by module name, even if unrelated to
the containing package:

  search Data::Grove
                                
Example results:

  libxml-perl

=item 8.

Browse all packages in the repository:

  search *

Example results:

  Affix-Infix2Postfix
  AI-Fuzzy
  [many more...]

=back

Recall previous searches using the 'search <number>' command. PPM stores
searches for each session until you exit PPM.

Some package names or versions are too long to be displayed in the search
results. If a name is too long, you will see a '~' (tilde) as the last visible
character in the column. You can use I<describe> to view detailed information
about such packages.

=head2 Search Results

When you type a command like C<search XML>, PPM searches in each of the Active
Repositories (see the I<repository> command) for your package. The results are
merged into one list, and duplicates (packages found in more than one
repository) are hidden.

You can control what fields PPM shows for each package. The fields each have a
built-in weight, which is used to calculate how wide to make each field based
on the width of your screen. Information that doesn't fit into a field is
truncated, and a tilde (C<~>) character is displayed in the last column of the
field.

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

=over 4

=item 1

NAME

The package name

=item 2

VERSION

The package version

=item 3

ABSTRACT

The package abstract

=back

You can customize the view somewhat. If you want to view the authors, but not
the abstract, you can run the same I<search> command after using I<set> to
change the fields:

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

You can change the order in which the results are sorted, and what columns are
displayed. The settings I<fields> and I<sort-field> changes this. You can
sort by any valid field name (even fields which are not displayed). See the
I<settings> command for the valid field names.

PPM always hides "duplicate" results. It decides whether a result is
duplicated based on the fields being displayed. If the same package is found
in more than one repository, but you don't have the REPOSITORY field showing,
PPM will only list the package once.


=head1 settings -- View or Set PPM Settings

=head2 Synopsis

 set                 Displays current settings
 set <name>          Displays the current setting of the given <name>
 set <name> <value>  Sets <name> to <value>
 unset <name>        Sets <name> to a "false" value: '0' for boolean
                     settings, '' for others.

=head2 Description

The settings command is used to configure the default PPM environment.
Settings such as the number of lines displayed per page, case-sensitivity,
and the log file are configured using the settings command.

Setting names may be abbreviated to uniqueness. For example, instead
of typing 'case-sensitivity', you may type 'case'.

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

For information about migrating options used by previous
versions of PPM, see 'help ppm_migration'.

When you assign a value to a setting, PPM saves the configuration.
Therefore, setting values persist across sessions.

=head1 targets -- View Target Installer Backends

=head2 Synopsis

 target                      Displays a list of backend targets
 target <number>             Sets <number> as default backend target
 target [select] <name or num>
                             Sets <name or num> as default backend target
 target describe [name or num]
                             Describes the given (or default) target
 target set <key> <val>      Sets the target's <key> to <val> 
 target rename <name or num> <name>
                             Renames the given target to <name>

=head2 Description

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
asterisk). Using multiple targets, you can manage multiple 
installations of Perl from a single command-line.

=head1 tree -- Show Dependency Tree for Packages

=head2 Synopsis

 tree                Displays the dependency-tree of the current
                     or default package
 tree <number>       Displays the dependency-tree of the given <number>
 tree <range>        Displays a <range> of dependency-trees
 tree <package name> Displays the dependency-tree of the named package
 tree <url>          Displays the dependency-tree for the
                     package at <url>
 tree <glob pattern> Performs a new search using <glob pattern>

=head2 Description

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

=head1 remove, uninstall -- Uninstalls Installed Packages

=head2 Synopsis

 remove              Deletes default installed package
 remove <number>     Deletes installed package <number>
 remove <range>      Deletes a <range> of installed packages
 remove <name>       Deletes a packages by a specific name
 remove <url>        Deletes the package located at <url>

=head2 Description

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
to use a query to find a package before deleting it. Remember that removing
packages clears all previous query requests, since the numerical
sequence stored in any query will no longer be true once package(s) have
been removed.

Packages can also be removed in groups.  For example:

    remove 4-7

will delete packages 4, 5, 6, and 7 from your target. You can also skip
packages:

    remove 3-5, 7

this will delete packages 3, 4, 5 and 7, but will leave 6 intact.
Remember to run a new query whenever you remove a package from your
target.

If you specify the package as a URL, PPM determines the package name from
the URL and removes that.

Please note that wildcards like "*" or "?" cannot be used with the remove
command.

=head2 See Also

profile

=head1 upgrade -- List or install available upgrades

=head2 Synopsis

 upgrade [*]         Lists upgrades available for all installed packages
 upgrade <number>    Lists upgrades for installed package <number>
 upgrade <range>     Lists upgrades for a <range> of installed packages
 upgrade <package>   Lists upgrades for the named <package>

=head2 Description

The upgrade command lists package upgrades that are available on the
active repositories for packages installed on your system. To install
available upgrades, use the '--install' option.

If profile tracking is enabled, (see 'help profile'), your profile
will be updated to reflect changes to any packages which are upgraded.

There are several modifiers to the upgrade command:

=over 4

=item -install

Installs, rather than lists, available upgrades

=item -precious

Allows upgrading of "precious" packages

=item -force

See 'help install'

=item -follow

See 'help install'

=back

By default, 'upgrade' typed by itself only lists the available upgrades.
To actually install all available upgrades, enter

    upgrade -install

To enable upgrading "precious" packages, enter

    upgrade -install -precious

=head2 See Also

profile

=head1 version -- print the name and version of PPM.

Prints the name and version of PPM3.


=head1 EXTRA HELP TOPICS

The following sections describe extra help topics available in PPM. An extra
help topic is not the name of a command -- it is only available as a help
page. The following help is available via the online help feature by typing
'help' in PPM3.

=head1 ppm migration -- PPM Migration Guide

=head2 Description

Those familiar with PPM version 2 should appreciate the extended
functionality  of PPM version 3, including the command-line history,
autocomplete and profiles. Some PPM version 2 commands are different
in PPM version 3. Examples of command changes include:

=over 4

=item 1

Adding a repository

PPM2:

  set repository my_repository http://my/repository

PPM3:

  repository add my_repository http://my/repository

=item 2

Removing a repository

PPM2:

  set repository --remove my_repository

PPM3:

  repository del my_repository

=item 3

Setting the temporary directory

PPM2:

  set build DIRECTORY

PPM3

  set tempdir DIRECTORY

=item 4

Setting frequency of download updates

PPM2:

  set downloadstatus NUMBER

PPM3:

  set download-chunksize NUMBER

=item 5

Changing the installation root directory:

PPM2:

  set root DIRECTORY

PPM3:

  target set root DIRECTORY

=item 6

Listing all installed packages:

PPM2:

  query

PPM3:

  query *

=item 7

Listing all packages on server:

PPM2:

  search

PPM3:

  search *

=item 8

Enabling HTML documentation generation:

PPM2:

  set rebuildhtml 1

PPM3:

  set rebuild-html 1

=back

=head1 prompt -- information about the PPM3 prompt

=head2 Description

The PPM prompt can tell you six things:

=over 4

=item 1)

The current repository;

=item 2)

The current target;

=item 3)

The last search you made on the current repository;

=item 4)

The last query you made on the current target;

=item 5)

The last package you described from this repository; and,

=item 6)

The last package you described from this target.

=back

To enable the prompt to tell you this information, you must set
'prompt-context' to '1'. The following examples all assume this setting.

=head2 Examples

=over 4

=item 1

Repository and Target:

Set 'prompt-context' The prompt will resemble:

    ppm:1:1> 

In this case, the first '1' means that the first repository is selected.
The second '1' means the first target is selected. You can prove this by
adding another repository and switching to it:

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

If you delete all the repositories, the repository number changes to '?'.
The same goes for targets. If either item is indicated by a question mark,
you must configure a repository or target before proceeding.

=item 2

Search and Query:

PPM stores searches and search results from in the current session.
The prompt displays the search number:

    ppm:1:1> search Text
    [results displayed here]
    ppm:1:1:s1> 

The 's1' indicates that the last search you performed
can be viewed again by entering 'search 1'. Type 'search' with no
arguments to view the list of cached searches:

    ppm:1:1:s1> search
    Search Result Sets:
    * 1. Text

If you then enter 'search 1', you will see the same results as when you
typed 'search Text' earlier. If you search for something else ('search 
Parse') then the number will change to 's2':

    ppm:1:1:s1> search Parse
    [results displayed here]
    ppm:1:1:s2>

The same indicators apply to the query command. When you run a query,
a numerical indicator displays the current query:

    ppm:1:1:s1> query PPM
    [results displayed here]
    ppm:1:1:s1:q1> 

You can view the past queries with 'query', and view results by querying a
particular number.

=item 3

Describe and Properties:

When you use the describe command with the numerical switch (to view
package information based on the package number in the last search or
query), PPM sets that index to the current index. If you use the desribe
command with the name switch, and the name is found within the current
result, the index is set to the current one. If no package is found,
PPM creates a new search or query on-the-fly, and sets it as the current
search or query.

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

The last prompt has an extra 'sp1'. That stands for 'search
package 1', and it means that PPM considers 'Convert-Context' to be the
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

Notice that the correct package got selected, even though you specified it
by name.

=back

This behaviour also applies to the query and properties commands.

=head2 See Also

describe, properties, query, search

=head1 quickstart -- a beginners' guide to PPM3

=head2 Description

PPM (Programmer's Package Manager) is a utility for managing
software "packages". A package is a modular extension for a language
or a software program. Packages reside in  repositories. PPM can use 
three types of repositories:

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
PPM will recognize it.  For example, 'repository add foo' can be
entered as 'rep add foo'.

PPM features user profiles, which store information about installed
packages. Profiles are stored as part of your ASPN account; thus,
you can easily maintain package profiles for different languages, or
configure one machine with your favorite packages, and then copy that
installation to another machine by accessing your ASPN profile.  

For more information, type 'help profile' at the PPM prompt.

=head1 unicode -- Notes About Unicode Author Names

=head2 Description

CPAN author names are defined to be in Unicode. Unicode is an international
standard ISO 10646, defining the I<Universal Character Set (UCS)>. UCS
contains all characters of all other character set standards. For more
information about Unicode, see F<http://www.unicode.org/>.

The CPAN authors website is located at your local CPAN mirror under
/authors/00whois.html. For example, you can view it at
F<http://www.cpan.org/authors/00whois.html>. This page can be rendered by
Mozilla 0.9.8 and Internet Explorer 5.0, but you may have to install extra
language packs to view all the author names.

By default, PPM3 renders all characters as Latin1 when it prints them to your
console. Characters outside the Latin1 range (0-255) are not printed at all.

If your console can render UTF-8 characters, you can tell PPM3 not to recode
characters by using one of the following environment variables:

=over 4

=item

LC_ALL

=item

LC_CTYPE

=item

LANG

=item

PPM_LANG

=back

PPM3 requires one of these environment variables to contain the string
'UTF-8'. For example, the following setting make PPM3 print
beautifully-formatted authors in RedHat Linux 7.2 (assumes you're using a
Bourne shell):

  $ PPM_LANG='en_US.UTF-8' xterm -u8 -e ppm3

Linux and Solaris users should refer to L<xterm> for more information about
setting up xterm to display UTF-8 characters.

=head1 BUGS

If you find a bug in PPM, please report it at this URL:

  http://bugs.activestate.com/enter_bug.cgi?set_product=PPM

Bugs regarding the ActiveState Package Repository (particularly missing or
out-of-date packages) should be sent via email to this email address:

  mailto:ppm-request@ActiveState.com

=head1 SEE ALSO

For information about the older version of PPM, see L<ppm>.

=head1 AUTHOR

ActiveState Corporation (support@ActiveState.com)

=head1 COPYRIGHT

Copyright (C) 2001, 2002, ActiveState Corporation. All Rights Reserved.

=cut

__END__
:endofperl
