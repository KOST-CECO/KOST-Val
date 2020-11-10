package ActivePerl::DocTools::Tree::HTML;

use strict;
use warnings;
use Pod::Find;
use Pod::Html;
use File::Basename;
use File::Path;
use Config;

sub _rel_path {
    my $path = shift;
    my $pfx = shift;
    $path =~ s{\\}{/}g if $^O =~ /^MSWin/;
    $path =~ s{/\z}{} if $path !~ m{^([A-Za-z]:)?/\z};
    if (defined $pfx and length $pfx) {
	$pfx =~ s{\\}{/}g if $^O =~ /^MSWin/;
        $pfx =~ s{/\z}{} if $pfx !~ m{^([A-Za-z]:)?/\z};
	my @pathbits = split '/', $path;
	my @pfxbits = split '/', $pfx;
	return $path if @pathbits < @pfxbits;
	while (@pfxbits) {
	    my $pathbit = shift @pathbits;
	    my $pfxbit = shift @pfxbits;
	    unless ($pathbit eq $pfxbit
		    or ($^O =~ /^MSWin/ and lc($pathbit) eq lc($pfxbit)))
	    {
		return $path;
	    }
	}
	$path = join '/', @pathbits;
	$path = "." unless length $path;
    }
    $path;
}

sub Update {
    my %args = @_;

    chdir $Config{installprefix}
	|| do { warn "Can't cd to root of Perl installation: $!\n"; return; };

    my $wd = $Config{installprefix};
    my $hd = $Config{installhtmldir} || "$Config{installprefix}/html";

    # turn $hd into a relative path
    $hd = _rel_path($hd,$wd);

    print "Building HTML tree at $hd, cwd is $wd\n" if $args{verbose};
    
    my $css = "Active.css";

    my %pods = Pod::Find::pod_find({}, @Config{qw(privlib sitelib scriptdir)});
    my $podpath = join ":", map { _rel_path($_,$wd) }
				@Config{qw(privlib sitelib scriptdir)};
	             
    foreach my $key (sort(keys %pods)) {

	my $infile = $key;
	$infile = _rel_path($infile,$wd);

	my $outfile = "$hd/$infile";

        # replace trailing .suffix with .html
	$outfile =~ s{\.[^.]*\z}{};
	$outfile .= ".html";

	if (! -e $outfile or (stat $infile)[9] > (stat $outfile)[9]) {
	    print "Making $outfile from $infile => $pods{$key}\n"
		if $args{verbose};
	    unlink($outfile) if -e $outfile;
	    my $dir = dirname($outfile);
	    mkpath($dir);

	    my $depth = 0;
	    while ($dir =~ m!/!g) {
		$depth++;
	    }

	    (my $back = "../" x $depth) =~ s{/$}{};
	    $back = "." unless $back;

    
	    my @args = (
		    "--htmldir=$dir",
		    "--htmlroot=$back",
		    "--podroot=.",
		    "--podpath=$podpath",
		    "--libpods=perlfunc:perlguts:perlvar:perlrun:perlopt",
		    "--header", 
		    "--infile=$infile", 
		    "--outfile=$outfile", 
		    "--css=$back/$css", 
		    "--quiet",
		    );
	    #print "pod2html('", join("',", @args), "')\n" if $args{verbose};
	    pod2html(@args);
	}
	else {
	    print "Skipping $outfile\n" if $args{verbose};
	}
    }
}

1;

__END__

#=head1 NAME

ActivePerl::DocTools::Tree::HTML - module for generating Perl documentation

#=head1 SYNOPSIS

  use ActivePerl::DocTools;
  ActivePerl::DocTools::UpdateHTML();
  
#=head1 DESCRIPTION

Module for generating Perl html docs.

#=head2 EXPORTS

nothing

#=head1 AUTHOR

David Sparks, daves@ActiveState.com

#=head1 SEE ALSO

The amazing L<PPM>.

L<ActivePerl::DocTools>

#=cut
