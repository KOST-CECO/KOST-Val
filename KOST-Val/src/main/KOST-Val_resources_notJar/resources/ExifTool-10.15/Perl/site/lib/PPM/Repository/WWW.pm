package PPM::Repository::WWW;

use strict;

use File::Listing;	# part of LWP - used to parse FTP-style dir listings
use PPM::PPD;
use PPM::Search;
use PPM::Result qw(Ok Error Warning List);

use Data::Dumper;

use base qw(PPM::Repository);
use vars qw($VERSION);
$VERSION = '3.05';

sub search {
    my $o = shift;
    my $target = shift;
    my $query = shift;
    my $case = shift;
    my $res;
    $res = $o->load_pkgs($target);
    return $res unless $res->ok;
    $query = $o->mod_to_pkg($query);
    my $compiled = PPM::PPD::Search->new($query, $case);
    return Error($compiled->error) unless $compiled->valid;
    my @pkgs = $compiled->search($o->pkgs);
    List(@pkgs);
}

sub getppd {
    my $o = shift;
    my $target = shift;
    my $pkg = $o->mod_to_pkg(shift);
    my $res = $o->describe($target, $pkg);
    return $res unless $res->ok;
    return Ok($res->result->ppd);
}

# Load the PPD at the given URL. The unique key will be the URL.
sub describe {
    my $o = shift;
    my $target = shift;
    my $rel = shift;
    my $url = $o->absolutize($rel);
    $url .= ".ppd" unless $url =~ /\.ppd$/i;
    return Ok($o->{ppds}{$url})
	if exists $o->{ppds}{$url} and $o->{ppds}{$url}->is_complete;

    my $req = $o->new_request('GET', $url);
    my $res = $o->{ua}->request($req);
    return Error("Failed to download URL $url: " . $res->status_line)
	unless $res->is_success;
    $o->{ppds}{$url} = PPM::PPD->new($res->content, $o, $url);
    $o->{ppds}{$url}{from} = 'repository';
    Ok($o->{ppds}{$url});
}

sub load_pkgs {
    my $o = shift;
    my $target = shift;
    return Ok() if $o->{pkgs_loaded};

    # A list of files to try downloading. These are listed in preferred order:
    # the summary is the smallest, followed by the search summary (why?), and
    # finally the package.lst is just the concatenation of all the packages.
    # The values represent whether the generated PPDs are "complete", or
    # whether we'll need to re-fetch them.
    my @indices = (
	# INDEX			COMPLETE	PARSE METHOD
	'summary.ppm'		=> 0		=> summary =>
	'searchsummary.ppm'	=> 0		=> summary =>
	'package.lst'		=> 1		=> summary =>
	''			=> 0		=> listing =>
    );

    # This closure calls the appropriate parse method with the right
    # arguments, depending on how the thing needs to be parsed.
    my $parser = sub {
	my $o = shift;
	my ($doc, $complete, $method) = @_;
	return $o->parse_summary($doc, undef, $complete)
	    if $method eq 'summary';
	return $o->parse_listing($doc, $target)
	    if $method eq 'listing';
	die "internal error: PPM::Repository::WWW::load_pkgs corrupted.";
    };

    # NOTE: it may pay to provide a callback to update the UI for very long
    # downloads of package.lst files:
    delete $o->{ppds};
    my $error;
    while (@indices) {
	my ($index, $complete, $parse_method) = splice @indices, 0, 3;
	my $url = $o->absolutize($index);
	my $req = $o->new_request('GET', $url);
	my $res = $o->{ua}->request($req);
	$error = $res->status_line, next unless $res->is_success;
	my $smry = $o->$parser($res->content, $complete, $parse_method);
	return $smry unless $smry->ok;
	$o->{ppds} = $smry->result;
	last;
    }
    return Error("couldn't download package list from $o->{url}: $error")
	unless $o->{ppds} and %{$o->{ppds} || {}};

    $o->{pkgs_loaded} = 1;
    Ok();
}

sub parse_listing {
    my $o = shift;
    my $doc = shift;
    my $target = shift;
    my @urls;

    # FTP-style directory listing
    if ($doc =~ /^total\s+(\d+)\s+[-rwx]{9,}/) {
	@urls = grep { m/\.ppd$/i }
		map  { $$_[0] }
		grep { $$_[1] eq 'f' }
		parse_dir($doc);
    }
#    # IIS format directory listing
#    elsif ($doc =~ /^<head><title>/) {
#	@ppds = map  { s/\.ppd<.*$//is; s/.*>//is; $_ }
#		grep { m/\.ppd/i }
#		split('<br>', $doc);
#    }
#    # output of default.prk
#    elsif ($doc =~ /<BODY BGCOLOR=FFFFFF>\n\n<form name=VPMform/s) {
#	@ppds = map  { /^<!--Key:(.*)-->$/; $1 }
#		grep { /^<!--Key:.*-->$/ }
#		split('\n', $doc);
#    }
    # pick up all plain "*.ppd" links
    else {
	require HTML::Parser;
	my $p = HTML::Parser->new(api_version => 3,
				  report_tags => ['a', 'base'],
				  start_h => [sub {
				      my $tag = shift;
				      my $href = shift->{href} || return;
				      if ($tag eq 'base') {
					  $o->{url_base} = URI->new_abs(
					      $href,
					      $o->{url_base}
					  );
				      }
				      else {
					  return unless $href =~ /\.ppd$/i;
					  push(@urls, $href);
				      }
				  }, "tagname,attr"],
				 );
	$p->parse($doc)->eof;
    }
    $o->describe($target, $_) for @urls;
    unless (keys %{$o->{ppds}}) {
	return Error("may not be a PPM repository.");
    }
    return Ok($o->{ppds});
}

sub pkgs {
    my $o = shift;
    return values %{$o->{ppds}};
}

sub type_printable { "Webpage" }
