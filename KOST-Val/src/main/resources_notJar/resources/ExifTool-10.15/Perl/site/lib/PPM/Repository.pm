package PPM::Repository;

use strict;
use Data::Dumper;
require PPM::PPD;
require LWP::UserAgent;
use PPM::Archive;
use PPM::Result qw(Ok Warning Error List);
use File::Path qw(rmtree);
use vars qw($VERSION);

$VERSION = '3.06';

sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    my $self = bless {}, $class;
    $self->{location} = shift;
    $self->{name} = shift;
    $self->{username} = shift;
    $self->{password} = shift;

    # All Repositories need a UserAgent to download PPMs, so we might as well
    # initialize it now:
    $self->new_ua;

    my $error;
    ($self->{type}, $error) = $self->select_type($self->{location});
    if ($self->{type} eq 'LOCAL') {
	require PPM::Repository::Local;
	bless $self, 'PPM::Repository::Local';
    }
    elsif ($self->{type} eq 'WWW') {
	require PPM::Repository::WWW;
	bless $self, 'PPM::Repository::WWW';
    }
    elsif ($self->{type} eq 'PPMSERVER') {
	require PPM::Repository::PPMServer;
	bless $self, 'PPM::Repository::PPMServer';
    }
    elsif ($self->{type} eq 'PPM3SERVER') {
	require PPM::Repository::PPM3Server;
	bless $self, 'PPM::Repository::PPM3Server';
    }
    elsif ($self->{type} eq 'UNSUPPORTED') {
	my $err = PPM::Repository::Result->new($error, $self->{location});
	return (undef, $err->msg_raw);
    }
    $self->init(@_)
	or return (undef, $self->{errmsg});
    return $self;
}

sub select_type {
    my $o = shift;
    my $loc = shift;

    # Some kind of URI:
    if ($loc =~ m[^[^:]{2,}://]i) {

	# Set the base URL for resolving relative links. For SOAP repositories
	# this is incorrect, but unfortunately the servers do not expose a
	# url_base() method. This is used for WWW repositories, though.
	$loc = "$loc/" unless $loc =~ m#/$#;
	$o->{url_base} = URI->new($loc);

	# A SOAP url, by convention
	if ($loc =~ m[^(http://.*)\?(.*)]i) {
	    eval {
		require SOAP::Lite;
		SOAP::Lite->VERSION(0.51);
	    };
	    return ('UNSUPPORTED', 
		    "SOAP-Lite 0.51 is required to support SOAP servers")
	      if $@;
	    my ($proxy, $uri) = ($1, $2);
	    my $client = $o->{client} = SOAP::Lite->uri($uri)->proxy($proxy);

	    # Query the server about its supported version. If it
	    # fails, select the "old" ppmserver. If it succeeds, select the
	    # "new" ppmserver.

	    my ($type, $msg, $proto) = eval {
		my $soap_result = $client->ppm_protocol;
		my $r = $soap_result->result;
		if (defined $r) {
		    my $v = (split /\s+/, $r)[-1];
		    return "PPM3SERVER",$client,$v if defined $v and $v >= 300;
		    return "PPMSERVER" ,$client,$v if defined $v and $v >= 200;
		    return "UNSUPPORTED", "Unknown PPM Server protocol '$r'";
		}
		# There's just ONE guy who managed to create a PPM2 server out
		# there, and he doesn't support the ppm_protocol message.
		return "UNSUPPORTED", <<END;
This SOAP server does not expose a PPM3-compatible interface.  Specifically,
it does not implement the ppm_protocol() method.  Please inform the server's
administrator of the problem.
END
	    };
	    $o->{proto}  = $proto;

	    if ($@) {
		# Fix up "404 Not Found at c:/Perl806/site/lib/PPM/Repository.pm line 84"
		$@ =~ s/ at \S+\.pm line.*//;
		return "UNSUPPORTED", "$@";
	    }
	    return $type, $msg;
	}
	else {
	    $o->{url} = $loc;
	    return "WWW";
	}
    }

    # The default is to assume it's a local repository
    else {
	return "LOCAL";
    }
}

# Create and initialize an LWP::UserAgent object
sub new_ua {
    my $o = shift;
    return $o->{ua} if $o->{ua};
    $o->{ua} = LWP::UserAgent->new;
    $o->{ua}->agent("ppm/$VERSION");
    $o->init_ua;
    $o->{ua};
}

sub init_ua {
    my $o = shift;
    my $ua = shift || $o->{ua};
    $o->{ua}->env_proxy;
    # Special configuration should go here:
}

# Create a HTTP::Request object, and authenticate it
sub new_request {
    my $o = shift;
    my $req = HTTP::Request->new(@_);
    my $user = $o->username;
    my $pass = $o->password;
    if (defined $user and defined $pass) {
	$req->authorization_basic($user, $pass);
    }
    if (defined $ENV{HTTP_proxy_user} and defined $ENV{HTTP_proxy_pass}) {
	$req->proxy_authorization_basic($ENV{HTTP_proxy_user}, $ENV{HTTP_proxy_pass});
    }
    # Special headers (HTTP 1.1, keepalive) should go here:
    $req;
}

sub search {
    my $o = shift;
    my $target = shift;
    my $query = shift;
    die "Error: base method PPM::Repository::search() called";
}

sub describe {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    die "Error: base method PPM::Repository::describe() called";
}

sub getppd {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    die "Error: base method PPM::Repository::getppd() called";
}

# absolutize(): the codebase is potentially relative to the location of the
# PPD, which in turn lives at the base of the repository itself,
# $o->{location}.
sub absolutize {
    my $o = shift;
    my $codebase_rel = shift;
    return URI->new_abs($codebase_rel, $o->{url_base})->as_string;
}

# This function is provided so that the three unintelligent subclasses (Local,
# WWW, and PPMServer) know how to find packages when they're asked to find
# modules. This naive function assumes that the package containing a module is
# just the module name with '::' converted to '-'.
sub mod_to_pkg {
    my $o = shift;
    my $module = shift;
    $module =~ s/::/-/g;
    $module;
}

# This guarantees to return a "complete" PPM::PPD object based on the actual
# PPD text downloaded from the server.
sub getppd_obj {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    $o->describe($target, $pkg);
}

sub getppm {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    my $tmp = shift;
    my $status_cb = shift;
    my $cb_bytes = shift;

    # Calculate the target's name:
    my $tname = $target->name;

    # We can't rely on $o->describe() returning a fully-featured PPD object,
    # because the PPM3Server only returns exactly what we need to display for
    # searching and describing. The getppd_obj() method is guaranteed to
    # return a full PPD object, with a codebase.
    my $ppd = $o->getppd_obj($target, $pkg);
    return $ppd unless $ppd->ok;
    $ppd = $ppd->result;
    my $ver = $ppd->version;
    my $impl = $ppd->find_impl($target);
    return $impl unless $impl->ok;
    my $codebase = $impl->result->codebase;

    # Make sure the codebase is an absolute URL:
    $codebase = $o->absolutize($codebase, $pkg);

    # Create a temporary directory and chdir there:
    (my $filename = $codebase) =~ s|.*/||;
    (my $pkgname = $filename)  =~ s|\..*||;
    my $tmpdir = join '/', $tmp, "$pkgname-$$-".time;
    mkdir $tmpdir or
      return Error("can't create temporary directory '$tmpdir': $!");

    use Cwd qw(cwd);
    my $cwd = cwd();

    # Neat trick: create a result that, when it goes out of scope, deletes the
    # temporary directory and cleans up on the remote end. If there's an error
    # before we return it, we auto-clean everything up. If we do return it,
    # then it is destroyed after being used by the calling sub.
    my $success_retval = Ok($tmpdir);
    $success_retval->on_destruct(sub {
	chdir($cwd);
	rmtree($tmpdir) if $tmpdir;
	$target->pkgfini($pkg);
    });
    # Notify the backend that we're going to start processing the files now.
    {
	my $ok = $target->pkginit($pkg);
	return $ok unless $ok->ok;
    }

    chdir $tmpdir or
      return Error("can't chdir to $tmpdir: $!");

    # Download the tarball:
    my ($bytes, $total, $s_time);
    my $cb = sub {
	my ($data, $res, $prot) = @_;
	$total ||= $res->content_length;
	print FILE $data;
	$bytes += length($data);

	# Notify the user through the status callback:
	$status_cb->($pkg, $ver, $tname, 'DOWNLOAD', 
		     $bytes, $total, $s_time - time);
    };
    my $ua = $o->{ua};
    my $req = $o->new_request('GET', $codebase);
    open(FILE, '>', $filename) or
      return Error("can't write $filename: $!");
    binmode(FILE) or
      return Error("can't set $filename binary: $!");
    $status_cb->($pkg, $ver, $tname, 'PRE-INSTALL', 0, 0, 0);
    $s_time = time;
    my $res = $ua->request($req, $cb, $cb_bytes);
    unless ($res->is_success) {
	close(FILE);
	return Error("error downloading '$codebase': " . $res->status_line);
    }
    close(FILE) or
      return Error("can't close $filename: $!");

    $status_cb->($pkg, $ver, $tname, 'PRE-EXPAND', $total, $total, 0);
    my $ok = eval {
	my $archive = PPM::Archive->new($filename);
	my @files = $archive->list_files;
	my $files = scalar @files;
	my $n = 1;
	for (@files) {
	    $status_cb->($pkg, $ver, $tname, 'EXPAND', $n, $files, $_);
	    $n++;
	    $archive->extract($_);
	    next unless -f $_;
	    my $ok = $target->transmit($pkg, $_);
	    return $ok unless $ok->ok;
	}
	Ok();
    };
    if ($@) {
	return Error("$@"); # stringify it
    }
    return $ok unless $ok->ok;

    # Remove the tarball:
    unlink $filename;

    # transmit() the install and uninstall scripts to the $target. If it knows
    # how to use them, it will. Otherwise, it won't. YAY!
    for my $thing (qw(install uninstall)) {
	my $method = "${thing}_script";
	my $script = $impl->result->$method;
	next unless $script;

	if (my $href = $script->href) {
	    $href = $o->absolutize($href, $pkg);
	    my $req = $o->new_request('GET', $href);
	    my $res = $ua->request($req, $method);
	    $res->is_success or return Error(
		"error downloading $thing script '$href': " .
		$res->status_line
	    );
	}
	if (-f $method) {
	    $target->transmit($pkg, $method);
	}
    }

    chdir $cwd;
    return $success_retval;
}

sub init { 1 }

sub uptodate {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    my $version = shift;
    my $ppd = $o->describe($target, $pkg);
    return $ppd unless $ppd->ok;
    List($ppd->result->uptodate($version), $ppd->result->version);
}

# Run multiple requests in one call, and return the list of results. This may
# be overridden by sub-repositories if the server supports it. This default
# version just calls each method.
sub batch {
    my $o = shift;
    my $target = shift;
    my @batch;
    for my $task (@_) {
	my ($meth, @args) = @$task;
	push @batch, $o->$meth($target, @args);
    }
    List(@batch);
}

sub uptodate2 {
    my ($o, $targ, $pkg, $instver) = @_;

    # Check whether it's up to date:
    my $ppd = $o->describe($targ, $pkg);
    return $ppd unless $ppd->ok;
    List($ppd->result->uptodate($instver), $ppd->result);
}

sub location {
    my $o = shift;
    $o->{location};
}

sub name {
    my $o = shift;
    return $o->{name};
}

sub username {
    my $o = shift;
    return $o->{username};
}

sub password {
    my $o = shift;
    return $o->{password};
}

sub type {
    my $o = shift;
    $o->{type};
}

sub protocol {
    my $o = shift;
    $o->{proto};
}

sub parse_summary {
    my $o = shift;
    my $doc = shift;
    my $cache_key = shift;
    my $complete = shift;
    $complete = 1 unless defined $complete;
    return Ok($o->{cache}{$cache_key})
      if defined $cache_key and exists $o->{cache}{$cache_key};
    $doc =~ s/<\?xml[^>]+\?>//;
    return Error("could not parse package summary")
      unless $doc =~ /^\s*<REPOSITORYSUMMARY>/;
    $doc =~ s|</?REPOSITORYSUMMARY>||g;
    my %ppds =  map { $_->name, $_ }
		map { @$_{qw(is_complete id)} = ($complete, $_->name); $_ }
		map { $_ .= '</SOFTPKG>'; PPM::PPD->new($_, $o) }
		grep { /\S/ }
		split('</SOFTPKG>', $doc);
    $o->{cache}{$cache_key} = \%ppds
      if defined $cache_key;
    return Ok(\%ppds);
}

#=============================================================================
# Profile stuff must be overridden in the PPM3 Server
#=============================================================================
sub profile_list    {
    my $rep = shift;
    my $name = $rep->name;
    my $type = $rep->type_printable;
    Error("Profiles are not supported on the repository '$name'. It is of type '$type', and only 'PPMServer 3.0' or better support profiles.");
}
sub profile_add     { goto &profile_list }
sub profile_del     { goto &profile_list }
sub profile_save    { goto &profile_list }
sub profile_info    { goto &profile_list }
sub profile_target_rename { goto &profile_list }

# Profile tracking
sub installed { }
sub upgraded  { }
sub removed   { }

package PPM::Repository::Result;
use Data::Dumper;
use PPM::Config;

sub new {
    my $p = shift;
    my $msg = shift;
    my $loc = shift;
    my $code = shift || 1;;
    my $site = q{R:\inetpub\wwwroot\www2.ActiveState.com};

    # If there's an error about the server being down:
    if ($msg =~ m{\Q$site\E} or $msg =~ m{syntax error}) {
	$msg = "The server '$loc' is not accepting SOAP connections. Please try again later.";
    }
    elsif ($msg =~ /obtaining a license/i) {
	my $file = PPM::Config::get_license_file();
	my $found = -f $file ? "is present" : "not found";
	$msg = join ' ', $msg, "License file '$file' $found.";
    }
    return PPM::Result->new('', $code, $msg);
}

1;
