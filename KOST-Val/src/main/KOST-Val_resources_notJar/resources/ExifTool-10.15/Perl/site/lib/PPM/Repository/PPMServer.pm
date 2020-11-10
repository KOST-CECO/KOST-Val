package PPM::Repository::PPMServer;

use strict;
use PPM::PPD;
use PPM::Search;
use PPM::Result qw(Ok Error Warning List);
use base qw(PPM::Repository);
use vars qw($VERSION @ISA);

use Data::Dumper;

$VERSION = '3.06';

#=============================================================================
# Note: The server appears to expose this interface:
# search_ppds('archname', 'package', 'searchtag')
# fetch_ppd('package')
# fetch_summary()
# packages()
#=============================================================================

# The server-side search function only supports totally lame queries:
sub search {
    my $o = shift;
    my $target = shift;
    my $qstring = $o->mod_to_pkg(shift);
    my ($q_type, $query, $searchtag) = parse_query($qstring);
    my $casei = shift;

    my @ppds;
    if ($q_type eq 'TRADITIONAL') {
	substr($query, 0, 0) = "(?i)" if $casei;
	my $archname = $target->config_get("ARCHITECTURE")->result;
	my $data = eval {
	    $o->{client}->search_ppds($archname, $query, $searchtag)->result
	};
	if ($@) {
	    chomp $@;
	    return Error("server-side search failed: $@");
	}
	my $res = $o->parse_summary($data, $qstring);
	return $res unless $res->ok;
	@ppds = values %{$res->result};
    }
    else {
	unless ($o->{full_summary}) {
	    my $data = eval {
		$o->{client}->fetch_summary()->result
	    };
	    if ($@) {
		chomp $@;
		return Error("server-side summary fetch failed: $@");
	    }
	    $o->{full_summary} = $data;
	}
	my $res = $o->parse_summary($o->{full_summary}, 'full_summary');
	return $res unless $res->ok;
	my $compiled = PPM::PPD::Search->new($query, $casei);
	return Error($compiled->error) unless $compiled->valid;
        @ppds = $compiled->search(values %{$res->result});
    }
    $_->{is_complete} = 0 for @ppds;
    return List(@ppds);
}

sub describe {
    my $o = shift;
    my $target = shift;
    my $pkg = $o->mod_to_pkg(shift);
    my $ppd = $o->getppd($target, $pkg);
    return $ppd unless $ppd->ok;
    my $ppd_ref = PPM::PPD->new($ppd->result, $o, $pkg);
    $ppd_ref->{from} = 'repository';
    return Ok($ppd_ref);
}

sub getppd {
    my $o = shift;
    my $target = shift;
    my $pkg = $o->mod_to_pkg(shift);
    my $ppd = eval { $o->{client}->fetch_ppd($pkg)->result };
    if ($@) {
	chomp $@;
	return Error("server-side fetch-ppd failed: $@");
    }
    elsif (not $ppd) {
	return Error("Package '$pkg' not found on server. "
		     . "Please 'search' for it first.");
    }
    Ok($ppd);
}

sub load_pkg { }
sub type_printable { "PPMServer 2.0" }

#=============================================================================
# This query parser decides what type of query we're getting: a traditional
# query which searches by TITLE only, or an advanced query:
#=============================================================================
sub parse_query {
    my $query = shift;
    
    # If the query is '*', return everything:
    if ($query eq '*') {
	# Although we could do this with TRADITIONAL, it's actually faster to
	# request the whole summary, and just return it directly. It's also
	# more portable: the guy at theoryx5.uwinnipeg.ca decided not to
	# implement empty searches. He must have reverse-engineered the PPM
	# Server.
	return ('ADVANCED', '');
    }
    # If there are only alphanumeric characters in it:
    if ($query =~ /^[-_A-Za-z0-9]+$/) {
	return ('TRADITIONAL', $query);
    }
    # If there's only 1 field spec: i.e. NAME=foo, or TITLE=bar
    if ($query =~ /^([A-Za-z]+)=([-_A-Za-z0-9]+)$/ && is_traditional($1)) {
	my ($f, $q) = (uc($1), $2);
	$f = 'TITLE' if $f eq 'NAME'; # Required for the server
	return ('TRADITIONAL', $q, $f);
    }
    # If there are only alphanumeric characters, plus '*' and '.' in it,
    # convert it to the same regular expression PPM::Search would use, and let
    # the server do it:
    if ($query =~ /^[-_A-Za-z0-9\*\.\?]+$/) {
	my $re = PPM::Search::glob_to_regex($query, 0);
	return ('TRADITIONAL', "$re");
    }

    # Otherwise, get the whole summary and use PPM::Search
    return ('ADVANCED', $query);
}

sub is_traditional {
    my $field = uc(shift);
    return scalar grep { $field eq $_ } qw(ABSTRACT AUTHOR TITLE NAME);
}

#=============================================================================
# The batch method, plus supporting setup_ and cleanup_ methods.
#=============================================================================
sub batch {
    my $o = shift;

    # The batch() method was introduced to PPM2 in protocol version 201
    return $o->SUPER::batch(@_) unless $o->protocol >= 201;

    my $targ = shift; # ignored
    my @tasks = @_;

    # Because the PPMServer is quite lame, a lot of the PPMServer code is
    # executed client-side. That means a more complicated batch() method:
    # those methods which require client-side code are executed entirely
    # client side. Those which don't are dispatched to the server in one go.
    # The original task list is stitched back together after the call.
    my @batch;   # this will actually be dispatched to the server.
    my @results; # the dispatched slots are left undefined, so that

    # Decide what stuff to dispatch. Each setup method is free to munge
    # arguments as needed. If $to_dispatch is false, the method will not be
    # dispatched to the server, and $task is assumed to contain the result.
    for my $i (0 .. $#tasks) {
	my $task = $tasks[$i];
	my $meth = "setup_$task->[0]";
	if ($o->can($meth)) {
	    my $to_dispatch = $o->$meth($task);
	    if ($to_dispatch) {
		push @batch, $task;
		$results[$i] = undef;
	    }
	    else {
		$results[$i] = $task;
	    }
	}
    }

    # Dispatch the batch, then stitch and patch. Atchoo!
    my $response = eval {
	$o->{client}->batch(@batch)->result;
    };
    if ($@) {
	chomp $@;
	return Error("batch method failed: $@");
    }
    elsif (not defined $response) {
	return Error("batch method returned undefined results");
    }

    # Stitch the results back together, calling cleanup methods as we go:
    for my $i (0 .. $#results) {
	$results[$i] = shift @$response unless defined $results[$i];
	my $result = $results[$i];
	if ($result->{error}) {
	    $results[$i] = Error($result->{error});
	}
	else {
	    my $method = "cleanup_$tasks[$i][0]";
	    $results[$i] = $o->can($method)
		? $o->$method($result->{result})
		: Ok($result->{result});
	}
    }
    List(@results);
}

# Just tell it to dispatch to the server
sub setup_uptodate2 { 1 }

sub cleanup_uptodate2 {
    my ($o, $result) = @_;
    my ($uptodate, $ppdtext) = @$result{qw(uptodate ppd)};
    defined $uptodate and defined $ppdtext
	or return Error("uptodate2 method returned undefined results");
    my $ppd_ref = PPM::PPD->new($ppdtext, $o);
    $ppd_ref->{id}   = $ppd_ref->name;
    $ppd_ref->{from} = 'repository';
    List($uptodate, $ppd_ref);
}

1;
