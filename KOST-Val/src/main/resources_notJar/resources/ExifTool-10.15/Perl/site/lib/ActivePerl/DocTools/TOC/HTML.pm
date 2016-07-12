package ActivePerl::DocTools::TOC::HTML;
our $VERSION = '0.10';

use strict;
use warnings;
use base ('ActivePerl::DocTools::TOC');


my $indent = '&nbsp;' x 4;

# constructs the simpler methods
sub text {
	my ($text) =  join '', map { "$_\n" } @_;
	return sub { $text };
}


# extra info is tedious to collect -- is done in a subclass or something.
sub extra { '' };


*header = text ("<hr><p>","<h4>Perl Core Documentation</h4>",);

sub before_pods { '' }

*pod_separator = text('<br>');

sub pod {
	my ($self, $file) = @_;
	return _page($self->{'podz'}->{"Pod::$file"}, $file, $self->extra($file));
}

sub after_pods { '' }

*before_scripts = text("<hr><p>","<h4>Programs</h4>",);

sub script {
	my ($self, $file) = @_;
	return _page($self->{'scriptz'}->{$file}, $file, $self->extra($file));
}

sub after_scripts { '' }


*before_pragmas = text("<hr><p>","<h4>Pragmas</h4>",);

sub pragma {
	my ($self, $file) = @_;
	return _page($self->{'pragmaz'}->{$file}, $file, $self->extra($file));
}

sub after_pragmas { '' }


*before_libraries = text("<hr><p>","<h4>Modules</h4>",);

*library_indent_open = sub {''};  # text('<ul compact>');
*library_indent_close = sub {''}; # text('</ul>');
*library_indent_same = sub {''};

sub library {
	my ($self, $file, $showfile, $depth) = @_;
	return (($indent x $depth) . _page($self->{'filez'}->{$file}, $showfile, $self->extra($file)));
}

sub library_container {
	my ($self, $file, $showfile, $depth) = @_;
	return (($indent x $depth) . _folder($showfile));
}

sub after_libraries { '' }

*footer = text("</div></body></html>");


sub _folder {
	my ($text) = @_;
	defined $text or die "no argument to _folder!";
	return qq'<img src="images/greysmallbullet.gif" width="5" height="5" alt="*"> $text<br>\n';
}

sub _page {
	my ($href, $text, $extra) = @_;
	die "bad arguments to _page: ($href, $text, $extra)" unless (defined $href and defined $text);
	defined $extra or $extra = '';
	if ($extra ne '') {
		$extra = " $extra";  # just to make it EXACTLY identical to the old way. 
	}
	return qq'<img src="images/greysmallbullet.gif" width="5" height="5" alt="*"> <a class="doc" href="$href">$text</a>$extra<br>\n';
}


sub boilerplate {
	# warn "boilerplate";
	return boiler_header() . boiler_links();
}
	
sub boiler_header {
	# warn "boiler_header";
	return (<<'HERE');
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>

<head>
<title>ActivePerl User Guide - Table of Contents</title>
<base target="PerlDoc">
<link rel="STYLESHEET" href="Active.css" type="text/css">
</head>

<body>

<h1>Table of Contents</h1>
<!-- This is disabled due to a bug somewhere, current suspect is PerlScript
<script language='PerlScript'>

#
# An embedded Web server in an HTML page using PerlScript
# This allows HTML documents stored on a hard drive to
# be able to search themselves (on Internet explorer).
#
# Contributed by John Holdsworth (c) 2001.
# patched by http://www.openpsp.org
#
# This code is distriubuted under the 
# "Artistic" license a copy of which 
# is distributed with perl.
#

$ID = q$Id: perltoc.pl,v 3.7 2002/02/14 21:50:54 johnh Exp $;

use ActivePerl::DocTools::PSP::Searcher;
use vars qw($window);
use IO::File;
use strict;

# required for open to work
close STDIN;
close STDOUT;
close STDERR;

# log errors to temporary file
my $tmp = (grep -d $_, ($ENV{TEMP}, $ENV{TMP}, "/tmp", "c:\\temp", "c:\\"))[0];
open STDERR, "> $tmp\\perltoc.log" or alert( "Could not open log file in $tmp: $!" );
select STDERR; $| = 1;

# server available on this port
my $sock = ActivePerl::DocTools::PSP::Searcher->new( Listen=>5,
	Reuse=>1, LocalPort=>7329, Timeout=>0.000001 )
	    or warn "Could not open Socket";
# a zero second timeout is not possible
# using IO::Socket

my $port = $sock->sockport();

    $window->{document}->write( <<HTML );
<form name='search' method='get' action='http://127.0.0.1:$port/' onsubmit='onsub();'>
<b >Search:</b> <input name='pattern' type='textfield' size='20'
title='Enter a regular expression to search the documentation for.'>
</form>
HTML

    $window->{document}{search}{pattern}->focus();

# warn "Initialised, \$base: '$base' \$furl: '$furl'";

#
# This function is called periodically 
# to poll for incomming connections.
#
sub poll {
    # This will not block as a timeout
    # has been set on the socket.
    $sock->poll(); $@ = '';

    # poll again in 100ms using timeout
    $window->setTimeout( "poll()", 100 );
}

# start polling for connections
# poll itself calls a timeout so
# a trial accept() is performed
# repeatedly. 

poll();

sub sendResponse {
    ActivePerl::DocTools::PSP::Searcher::sendResponse();
}

sub cb {
    ActivePerl::DocTools::PSP::Searcher::cb();
}

</script>
-->
HERE

}


sub boiler_links {
	# warn "boiler_links";
	return (<<HERE);
<div nowrap>

<p><strong>Getting Started</strong><br>

&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="perlmain.html">Welcome To ActivePerl</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="RELEASE.html">Release Notes</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="install.html">Installation Guide</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="readme.html">Getting Started</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="CHANGES58.html">ActivePerl 5.8 Change Log</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="CHANGES56.html">ActivePerl 5.6 Change Log</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="resources.html">More Resources</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Copyright.html">License and Copyright</a><br>


<strong>ActivePerl Components</strong><br>

&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Components/Descriptions.html">Overview</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/ActivePerl-faq2.html">PPM</a><br>

&nbsp;&nbsp;&nbsp;&nbsp;<strong>Windows Specific</strong><br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="OLE-Browser/Browser.html" target="_blank">OLE Browser</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Components/Windows/PerlScript.html">PerlScript</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="../eg/IEExamples/index.htm">PerlScript Examples</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Components/Windows/PerlISAPI.html">Perl for ISAPI</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Components/Windows/PerlEz.html">PerlEz</a><br>

<strong>ActivePerl FAQ</strong><br>

&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/ActivePerl-faq.html">Introduction</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/ActivePerl-faq1.html">Availability &amp; Install</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/ActivePerl-faq2.html">Using PPM</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/ActivePerl-faq3.html">Docs &amp; Support</a><br>


&nbsp;&nbsp;&nbsp;&nbsp;<strong>Windows Specific</strong><br>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq2.html">Perl for ISAPI</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq4.html">Windows 9X/NT/2000</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq5.html">Windows Quirks</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq6.html">Web Server Config</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq7.html">Web Programming</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq8.html">Windows Programming</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq9.html">Modules &amp; Samples</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq10.html">Embedding &amp; Extending</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="faq/Windows/ActivePerl-Winfaq12.html">Using OLE with Perl</a><br>

<strong>Windows Scripting</strong><br>

&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Windows/ActiveServerPages.html">Active Server Pages</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Windows/WindowsScriptHost.html">Windows Script Host</a><br>
&nbsp;&nbsp;&nbsp;&nbsp;<img src="images/greysmallbullet.gif" width="5" height="5" alt="*">
<a class="doc" href="Windows/WindowsScriptComponents.html">Windows Script Components</a></p>


HERE
}


