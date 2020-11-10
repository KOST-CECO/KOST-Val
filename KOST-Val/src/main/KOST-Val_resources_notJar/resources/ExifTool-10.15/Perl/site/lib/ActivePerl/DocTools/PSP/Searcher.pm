#
# Performs search or if a document path has been specified
# serve it to the browser inserting anchors to skip straight
# to the correct line.
#
# Contributed by John Holdsworth (c) 2001.
# http://www.openpsp.org
#
# This code is distriubuted under the 
# "Artistic" license a copy of which 
# is distributed with perl.
#

package ActivePerl::DocTools::PSP::Searcher;
use base qw(ActivePerl::DocTools::PSP::Socket);
use strict;
use CGI;

my $findmax = 500; # max results
my $chunk = 200; # rows per send

# recover path of this document...
( my $perltoc = $main::window->document->{url} )
	=~ s@^(file:[\\/]*)@@;

# prefix of file urls
my $furl = $1 || "file://";

# and get root directory of html docs
( my $base = $perltoc ) =~ s@([\\/][^\\/]+){2}$@@;
my $css = "$furl$base\\html\\Active.css";
my $prev = "_none_";


my (@basic, @source);
my @files; # list of files to search
my @out; # buffer of lines of output

my $argh;
my $pathinfo;
my $fh;

sub main {
    my $cgi = CGI->new();
    $argh = {pattern=>$cgi->param('pattern')};
    $pathinfo = "$base$ENV{PATH_INFO}";

#    $main::window->document->search->pattern->{style}{cursor} = "wait";
#    $main::window->{document}{body}{style}{cursor} = "wait";
#    $main::window->setTimeout( "cb()", 1 );
#    $fh = $_[0];
#}
#
#sub cb {
    $argh->{pattern} =~ s/(\ \.\.\.)+$//;
    my $pattern = $argh->{pattern};

    # pre-compile regexp into a closure for speed
    (my $p = $pattern) =~ s@/@\\/@g;
    my $matcher = eval "sub { \$_[0] =~ /\\b$p\\b/i && \$_[0] !~ /href=['\"]+$p/i }";      
    # if there is no path, this is the initial search
    if ( $ENV{PATH_INFO} eq "/" ) {

	print <<HTML;
<html><head><title>Perl documentation search</title>
<link rel='STYLESHEET' href='$css' type='text/css'>
</head><body>
HTML
	if ( !@basic ) {
	    # obtain list of files in a useful order
	    # from pertoc.html (this file)
	    extractLinks( $perltoc, \@basic );
	    unshift @basic, "lib/Pod/perlfunc.html";
	    @basic = map "html/$_", @basic;

	    if (0) {
	    # traverse "site" directory for
	    # any additional documentation
	    findFiles( $base, "html/site", \@basic );
	    findFiles( $base, "lib", \@source );

#	    @files = grep $_ !~ m@lib/Pod@, @files;
	}
	}

	@files = $pattern =~ /^sub \w+/ ? @source : $pattern eq $prev ?
		(@basic, @source) : @basic;
        $prev = $pattern;
	
	warn "Searching for pattern '$pattern' in @{[scalar @files]} files";

	if( !$matcher ) {
	    print "<b>Invalid regular expression: $pattern</b>";
	    return;
	}

	# grep quickly for any file name matches.
	# with no pattern this lists all files.
	my $matched = join ", ", map "<a href='$furl$base/$_'>$_</a>",
		grep $_ =~ /\b$pattern\b/i && $_ !~ /\.$pattern/i,
	       		(@basic, @source);

	print <<HTML if $matched;
<h4>Files with names matching "$pattern" (<a href='#pmatches'>.. content matches</a>)</h4>
$matched<br><br>
HTML
        flush STDOUT;

	# only search if there is a pattern!
	if ( $pattern ) {	
	    # pattern will become part of a link and needs to be escaped
	    $pattern =~ s/([^a-zA-Z0-9_.-])/uc sprintf("%%%02x",ord($1))/eg;

	    # scan all files for pattern
	    foreach my $file (@files) {
		scanFile( $pattern, $matcher, $base, $file, \@out );
		if ( @out > $findmax ) {
		    push @out, "<tr><td colspan=2>And perhaps more...</td></tr>\r\n";
		    last;
		}
	    }

	    # frame a table if there are any matches	    
	    if ( @out ) {
		unshift @out, <<HTML;
<a name='pmatches'>
<table BORDER='1' CELLSPACING='0' BGCOLOR='FFFFFF' BORDERCOLOR='000000'
BORDERCOLORLIGHT='000000' BORDERCOLORDARK='000000' width='100%'><thead>
<th bgcolor="C86040" align='left'>&nbsp;File containing pattern</th>
<th bgcolor="D0D080" align='left'>&nbsp;Contents</th></thead>
HTML
		push @out, "</table>\n";
	    }
	    else { # otherwise....
		push @out, "<h4>No matches found for '$argh->{pattern}'\n";
	    }
	}

	$main::window->document->search->pattern->{value} = $argh->{pattern};
	$main::window->document->search->pattern->select();
    }
    elsif ( open FILE, my $file = "$base/html$ENV{PATH_INFO}" ) {
        # This is where a link has been clicked on
	# and the server needs to insert anchors
	# into the document so the links go to the
	# tcorrect place where the match was found.

	# links inside the served document point
	# back to the versions on the hard drive.
	print "<html><head><base href='$furl$file'></head>\n";

	my $pre = $file !~ /.html$/;
	print "<h4>$file:</h4><pre>\n" if $pre;

	my $occ; # occurance number
    	while ( defined (my $line = <FILE>) ) {
	    if ( $pre ) {
		$line =~ s/>/&gt;/g;
		$line =~ s/</&lt;/g;
	    }
	    else {
	        $line =~ s@(../)*Active.css@$css@;
	    }
	    if ( &$matcher( $line ) ) {
	        ++$occ;
	        $line = "<a name='occ_$occ'></a>$line";
	    }
	    push @out, $line;
	}

	warn "$occ occurances of $pattern anchored in file '$file'";
   }
   else {
	print "<html><body>Invalid link path $base -- $ENV{PATH_INFO}";
	warn "could not locate '$base -- $ENV{PATH_INFO}'";
   }

   if ( @out ) {
	# This is a little tricky. Attempts to print
	# large amounts of HTML to the browser from
	# inside the browser result in a deadlock
	# so a callback is used to give other
	# threads a chance to render output
	open BATCH, ">&".fileno(STDOUT);
	sendResponse();
    }
}

#
# Called to output to the browser in small chunks to avoid deadlock
#
sub sendResponse {
    print BATCH my $out = join '', splice @out, 0, $chunk, ();
    warn length($out)." bytes sent at ".localtime();
    if ( @out ) {
      $main::window->setTimeout( "sendResponse()", 0 )
    }
    else {
#      $main::window->{document}{body}{style}{cursor} = "default";
#      close STDOUT;
#      close STDIN;
      close BATCH;
      undef $fh;
   }
}

#
# Extract the paths of any links in a file
#
sub extractLinks {
    my ( $htmlfile, $links ) = @_;
    open FILE, $htmlfile or warn "Could not open $htmlfile ($!)";

    while ( defined (my $line = <FILE>) ) {
	push @$links, $1 if $line =~ / href=\"([^\"\#]+)/ && $1 !~ /^http:/;
    }
}

#
# Find the paths of all files in a directory
#
sub findFiles {
    my ( $base, $dir, $files ) = @_;
    my %files = map {$_, 1} @$files;

    opendir DIR, "$base/$dir" or
    	warn "Could not open dir: $base/$dir";
    my @files = readdir DIR;

    # warn @files." being scanned in $base/$dir";

    foreach my $file (@files) {
    	if ( -f "$base/$dir/$file" && !$files{"$dir/$file"} ) {
	   push @$files, "$dir/$file";
	}
	elsif ( -d _ && $file !~ /^\.\.?$/ ) {
	   findFiles( $base, "$dir/$file", $files );
	}
    }
}

#
# Scan a file line by line for a pattern. The match
# is passed in as a function reference to avoid
# recompiling the regular expression repeatedly.
#
sub scanFile {
    my ( $pattern, $matcher, $base, $file, $out ) = @_;

    open FILE, "$base/$file" or
	return warn "Could not open: $base/$file";

    # whip off the extension
    ( my $f = $file ) =~ s@\.html?$@@;
    $f =~ s@^html/@@;

    read FILE, my $buffer, 10_000_000;
    if ( &$matcher( $buffer ) ) {
	open FILE, "$base/$file";

	my ($occ, $lno); # occurance number for the anchor..
	while ( defined (my $line = <FILE>) ) {
	    ++$lno;

	    if ( &$matcher( $line ) ) {
		$occ++;
		$line =~ s@\s+$@@;
		$line =~ s@^<\w+>|<\w+>$@@;
		$line =~ s@</?(hr|h\d|p|td|tr|br|title|table)[^>]*>@@gi;
		(my $dest = "$f.html?pattern=$pattern#occ_$occ")=~ s/ /+/g;
		push @$out, "<tr><td><a href='$dest'>$f</a>".
		    "</td><td>$line</td></tr>\n\n";
	    }
	}
    }

    close FILE;
}

1;
