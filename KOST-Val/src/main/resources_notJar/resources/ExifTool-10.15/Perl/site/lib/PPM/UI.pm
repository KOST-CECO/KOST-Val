package PPM::UI;

use strict;
use Data::Dumper;
use PPM::Repository;
use PPM::Installer;
use PPM::Config;
use PPM::Trace qw(trace);
use PPM::Result qw(Ok Warning Error List);

$PPM::UI::VERSION = '3.00';

my $CONF;
my $TARG;
my $REPS;

#=============================================================================
# Name indexes into arrays:
#=============================================================================
use constant PROP_PPD_OBJ => 0;
use constant PROP_INSTDATE => 1;
use constant PROP_REPOS => 2;

use constant QUERY_NAME => 0;
use constant QUERY_VERSION => 1;
use constant QUERY_ABSTRACT => 2;
use constant QUERY_AUTHOR => 3;

use constant CONF_INFO_KEY => 0;
use constant CONF_INFO_VAL => 1;

use constant CONF_KEYS_KEY => 0;
use constant CONF_KEYS_RW => 1;

use constant REP_INFO_NAME => 0;
use constant REP_INFO_LOC => 1;
use constant REP_INFO_TYPE => 2;

#=============================================================================
# Configuration Options
#=============================================================================

my %config_keys;
BEGIN {
   %config_keys = (tempdir => 1,
		   downloadbytes => 1,
		   tracefile => 1,
		   tracelvl => 1,
		   profile  => 1,
		   profile_server => 0,
		   profile_enable => 1,
		  );
}

sub config_keys {
    trace(3, "PPM::UI::config_keys()\n");
    List(map { [$_ => $config_keys{$_}] } keys %config_keys);
}

sub config_info {
    trace(3, "PPM::UI::config_info()\n");
    List(map { [$_ => $CONF->{DATA}{$_}] } keys %config_keys);
}

sub config_set {
    my $key = shift;
    my $val = shift;
    trace(3, "PPM::UI::config_set($key, $val)\n");
    return Error("no such config key '$key'")
      unless exists $config_keys{$key};
    return Error("read-only configuration key '$key'")
      unless $config_keys{$key};
    $CONF->{DATA}{$key} = $val;
    $CONF->save;
    return Ok();
}

sub config_get {
    my $key = shift;
    trace(3, "PPM::UI::config_get($key)\n");
    return Error("no such config key '$key'.")
      unless exists $config_keys{$key};
    return Ok($CONF->{DATA}{$key});
}

#=============================================================================
# Repositories
#=============================================================================

sub repository_list {
    trace(3, "PPM::UI::repository_list()\n");
    return List(sort keys %{$REPS->{DATA}});
}

sub repository_add {
    my $name = shift;
    my $location = shift;
    my $username = shift;
    my $password = shift;
    trace(3, "PPM::UI::repository_add($name, $location, ", $username,
	  ", ", $password, ")\n");

    # Validate it:
    my ($r, $er) = PPM::Repository->new($location, $name, $username, $password);
    return Error($er)
      unless $r;

    $REPS->{DATA}{$name}{url} = $location;
    $REPS->{DATA}{$name}{username} = $username if defined $username;
    $REPS->{DATA}{$name}{password} = $password if defined $password;
    $REPS->save;
    return Ok();
}

sub repository_del {
    my $name = shift;
    trace(3, "PPM::UI::repository_del($name)\n");
    return Error("Can't delete nonexistent repository '$name'.")
      unless exists $REPS->{DATA}{$name};
    delete $REPS->{DATA}{$name};
    $REPS->save;
    del_rep($name);
    return Ok();
}

sub repository_rename {
    my $oldname = shift;
    my $newname = shift;
    trace(3, "PPM::UI::repository_rename($oldname, $newname)\n");
    $REPS->{DATA}{$newname} = $REPS->{DATA}{$oldname}
      if exists $REPS->{DATA}{$oldname};
    repository_del($oldname);
}

sub repository_info {
    my $name = shift;
    trace(3, "PPM::UI::repository_info($name)\n");
    return Error("Can't describe nonexistent repository '$name'.")
      unless exists $REPS->{DATA}{$name};
    my $rep = get_rep($name);
    if ($rep->ok) {
	$rep = $rep->result;
	return List(
	    $name,
	    $rep->location,
	    $rep->type_printable,
	    $rep->username,
	    $rep->password,
	);
    }
    else {
	return Warning($rep->msg_raw,
		       -1,
		       [$name,
			$REPS->{DATA}{$name}{url},
			'unsupported']);
    }
}

#=============================================================================
# Operations on non-installed packages
#=============================================================================

sub search {
    my ($rlist, $rbad) = get_reps(shift);
    unless (@$rlist) {
	my $msg = "No valid repositories:\n";
	$msg .= $_->msg for @$rbad;
	return Error($msg);
    }
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $query = shift;
    my $case = shift;
    my @results;
    for my $r (@$rlist) {
	my $l = $r->search($target, $query, $case);
	next unless $l->ok;
	push @results, map { get_pkg($_, $r) } $l->result_l;
    }
    return List(@results);
}

sub describe {
    my ($rlist, $rbad) = get_reps(shift);
    unless (@$rlist) {
	my $msg = "No valid repositories:\n";
	$msg .= $_->msg for @$rbad;
	return Error($msg);
    }
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $pkg = get_pkg(shift, $rlist);
    my $package = $pkg->name;
    my $desc = $pkg->describe($target);
    return $desc unless $desc->ok;
    return Ok(get_pkg($desc->result, $rlist));
}

sub install {
    my ($reps, $targ_name, $package, $opts, $status_cb) = @_;

    my ($rlist, $rbad) = get_reps(shift);
    my $pkg = get_pkg($package, $rlist);

    # Check the package type.  If it is not a file, check that we have valid
    # repositories.
    if ($pkg->{type} ne 'FILE' and !(@$rlist)) {
	my $msg = "No valid repositories:\n";
	$msg .= $_->msg for @$rbad;
	return Error($msg);
    }
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;

    # Find the correct target for this package. This means matching the
    # LANGUAGE tag in the PPD. Basically we find out what LANGUAGE the PPD
    # represents, and we search through the targets looking for a subset which
    # implement that language. If more than one target implements the language
    # and version, we pick the first. If none work, we fail. If the LANGUAGE
    # tag is missing, or the LANGUAGE matches the given target, we use the
    # given target.
    $target = $pkg->choose_target($target, target_list()->result_l);
    return $target unless $target->ok;
    $target = $target->result;
    install_or_upgrade($rlist, $target, $pkg, $opts, $status_cb, 'install');
}

#=============================================================================
# Targets
#=============================================================================

sub target_list {
    trace(3, "PPM::UI::target_list()\n");
    return List(sort keys %{$TARG->{DATA}});
}

sub target_info {
    my $target = shift;
    trace(3, "PPM::UI::target_info($target)\n");
    my $t = get_targ($target);
    return $t unless $t->ok;
    $t = $t->result;
    my @keys = map { "\u$_" } $t->ckeys;
    my @vals = $t->cvals;
    my %hash;
    @hash{@keys} = @vals;
    return Ok(\%hash);
}
sub target_raw_info {
    my $target = shift;
    return Ok($TARG->{DATA}{$target});
}

sub target_config_info {
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    trace(3, "PPM::UI::target_config_info(", $target->name, ")\n");
    return $target->config_info;
}

sub target_config_keys {
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    trace(3, "PPM::UI::target_config-keys(", $target->name, ")\n");
    return $target->config_keys;
}

sub target_config_get {
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $key = shift;
    trace(3, "PPM::UI::target_config_get(", $target->name, ", $key)\n");
    return $target->config_get($key);
}

sub target_config_set {
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $key = shift;
    my $value = shift;
    trace(3, "PPM::UI::target_config_get(", $target->name, ", $key, $value)\n");
    return $target->config_set($key, $value);
}

sub target_rename {
    my $oldname = shift;
    my $newname = shift;

    # Make sure the target even exists:
    my @targets = target_list()->result_l;
    return Error("Can't rename nonexistent target '$oldname'.")
      unless grep { $_ eq $oldname } @targets;

    # Load the targets file read/write:
    {
	my $t = PPM::Config::load_config_file('targets', 'rw');
	$t->{DATA}{$newname} = $t->{DATA}{$oldname};
	delete $t->{DATA}{$oldname};
    }
    $TARG = PPM::Config::load_config_file('targets', 'ro');

    # Return success if profile tracking is disabled.
    return Ok() unless config_get('profile_enable')->result;

    # We must rename the target in all profiles:
    my $res = profile_list();
    unless ($res->is_success) {
	return Error(
	    "failed to rename target in profiles: " . $res->msg_raw
	) unless $res->ok;
    }
    my @profiles = $res->result_l;

    my $repos = get_rep(config_get('profile_server')->result);
    return $repos unless $repos->ok;
    $repos = $repos->result;
    for my $profile (@profiles) {
	my $r = $repos->profile_target_rename($profile, $oldname, $newname);
	return Error(
	    "failed to rename target in profiles: " . $r->msg_raw
	) unless $r->ok;
    }

    Ok();
}

sub target_fix_paths {
    my $from = shift;
    my $to   = shift;
    my $i    = $^O eq 'MSWin32' ? '(?i)' : '';
    {
	my $t    = PPM::Config::load_config_file('targets', 'rw');
	for my $targ (target_list()->result_l) {
	    for my $key (keys %{$t->{DATA}{$targ}}) {
		$t->{DATA}{$targ}{$key} =~ s{$i\Q$from\E}{$to};
	    }
	}
    }
    $TARG = PPM::Config::load_config_file('targets', 'ro');
}

sub target_add {
    my $name = shift;
    my %opts = @_;

    # Handle loading a file:
    if (not defined $name and -f $opts{From}) {
	my $t = PPM::Config->new;
	$t->loadfile($opts{From});
	# There's only ever 1 target in that config file:
	($name) = keys %{$t->{DATA}};
	%opts = %{$t->{DATA}{$name}};
    }
    return Error("can't add existing target '$name'")
	if exists $TARG->{DATA}{$name};

    # Find an unused Port:
    $opts{port} = 14533;
    ++$opts{port} while (
	grep { $opts{port} eq $TARG->{DATA}{$_}{port} }
	keys %{$TARG->{DATA}}
    );

    # Save the file:
    {
	my $t = PPM::Config::load_config_file('targets', 'rw');
	$t->{DATA}{$name} = \%opts;
    }
    $TARG = PPM::Config::load_config_file('targets', 'ro');
    return Ok();
}

sub target_del {
    my $name = shift;
    return Error("can't delete nonexistent target '$name'")
	unless exists $TARG->{DATA}{$name};
    {
	my $t = PPM::Config::load_config_file('targets', 'rw');
	delete $t->{DATA}{$name};
    }
    $TARG = PPM::Config::load_config_file('targets', 'ro');
    return Ok();
}

#=============================================================================
# Operations on installed packages
#=============================================================================

sub query {
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $query = shift;
    my $case = shift;
    trace(3, "PPM::UI::query(", $target->name, ", '$query', $case)\n");
    my $res = $target->query($query, $case);
    return $res unless $res->ok;
    my @results = map { get_pkg($_) } $res->result_l;
    List(@results);
}

sub properties {
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $pkg = get_pkg(shift, undef); # don't care about repository.
    trace(3, "PPM::UI::properties(", $target->name, ", ", $pkg->name, ")\n");
    my $res = $target->properties($pkg->name);
    return $res unless $res->ok;
    my @res = $res->result_l;
    $res[0] = get_pkg($res[0]);
    return List(@res);
}

sub remove {
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $pkg = get_pkg(shift, undef); # don't care about repository.
    my $package = $pkg->name;
    my $force = shift;  # normally, if removing a package would break a
			# dependency of another installed package, we refuse.
			# But if the user really wants to...
    my $cb_remove = shift;
    my $verbose = shift;

    trace(3, "PPM::UI::remove(", $target->name, ", $package)\n");
    if (grep { $pkg->name eq $_ } $target->precious->result_l) {
	return Error("package '$package' is required by the target.");
    }
    my $prop = $target->properties($package);
    return $prop unless $prop->ok;

    my $ok = $target->dependents($package);
    return $ok unless $ok->ok;

    my @deps = $ok->result_l;
    if (@deps and not $force) {
	my $msg = "removing '$package' would break these dependencies:\n";
	$msg .= "\t$package is needed by $_.\n" for @deps;
	return Error($msg);
    }

    my $version = ($prop->result_l)[PROP_PPD_OBJ]->version;
    $cb_remove->($package, $version, $target->name, "PRE-REMOVE");
    my $ret = $target->remove($package, $verbose);
    return $ret unless $ret->ok;
    $cb_remove->($package, $version, $target->name, "COMPLETE");
    my $track = config_get('profile_enable')->result;
    if ($track and $ret->ok) {
	my $repos = get_rep(config_get('profile_server')->result);
	return $repos unless $repos->ok;
	$repos = $repos->result;
	my $rep = ($prop->result_l)[PROP_REPOS];
	my $ver = ($prop->result_l)[PROP_PPD_OBJ]->version_osd;
	my $entry = [$rep,
		     $target->config_get('TARGET_TYPE')->result,
		     $target->name,
		     $package,
		     $ver
		    ];
	my $profile = config_get('profile')->result;
	$repos->removed($profile, $entry);
    }
    $ret;
}

sub verify {
    my ($rlist, $rbad) = get_reps(shift);
    unless (@$rlist) {
	my $msg = "No valid repositories:\n";
	$msg .= $_->msg for @$rbad;
	return Error($msg);
    }
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $pkg = get_pkg(shift, $rlist);
    my $package = $pkg->name;
    trace(3, "PPM::UI::verify(", $target->name, ", $package)\n");

    # To do: 
    # 1. Check if the package is installed; return false otherwise.
    my $prop = $target->properties($package);
    return $prop unless $prop->ok;
    my @prop = $prop->result_l;

    my $bundled  = grep { $prop[PROP_PPD_OBJ]->name eq $_ }
    			$target->bundled->result_l;
    my $precious = grep { $prop[PROP_PPD_OBJ]->name eq $_ }
    			$target->precious->result_l;

    # 2. Get the installed version of the package.
    my $ver = $prop[PROP_PPD_OBJ]->version_osd;
    my $ver_p = $prop[PROP_PPD_OBJ]->version;

    # 3. Send the installed version to the Repository for checking.
    # I used to only upgrade from the place it came from. Now it will come
    # from the current repository.
    my $res = $pkg->uptodate($target);
    unless ($res->ok) {
	return Error("bundled package - no upgrade available")
	  if $bundled;
	return $res;
    }
    my ($uptodate, $newversion) = $res->result_l;

    # 4. Return uptodate(t/f), newversion, oldversion, bundled(t/f):
    return List($uptodate, $bundled, $precious, $newversion, $ver_p);
}

sub verify_pkgs {
    my ($rlist, $rbad) = get_reps(shift);
    unless (@$rlist) {
	my $msg = "No valid repositories:\n";
	$msg .= $_->msg for @$rbad;
	return Error($msg);
    }
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my @pkgs = @_;

    # Get the versions of the packages
    my (@ppds, @b, @p);
    my @version = map {
	my $pkg = $_;
	my $inst = properties($target, $pkg);
	return $inst unless $inst->ok;
	my @prop = $inst->result_l;
	my $ppd = $prop[PROP_PPD_OBJ]->getppd_obj($target)->result;
	push @ppds, $ppd;
	my $bundled  = grep { $prop[PROP_PPD_OBJ]->name eq $_ }
			    $target->bundled->result_l;
	push @b, $bundled;
	my $precious = grep { $prop[PROP_PPD_OBJ]->name eq $_ }
			    $target->precious->result_l;
	push @p, $precious;
	$ppd->version_osd;
    } @pkgs;

    # Query the package in each repository
    my @ans;
    for my $rep (@$rlist) {
	my @batch;
	for my $i (0 .. $#pkgs) {
	    my $pkg = get_pkg($pkgs[$i], [$rep]);
	    push @batch, ['uptodate2', $pkg->{id}, $version[$i]];
	}
	my $resp = $rep->batch($target, @batch);
	return $resp unless $resp->ok;
	my @ret = $resp->result_l;
	for my $i (0 .. $#pkgs) {
	    next unless $ret[$i]->is_success;
	    my $val = $ret[$i]->result;
	    push @$val, $ppds[$i], $b[$i], $p[$i];
	}
	push @ans, @ret;
	#push @ans, [$resp->result_l];
    }
    List(@ans);
}

sub upgrade {
    my ($rlist, $rbad) = get_reps(shift);
    unless (@$rlist) {
	my $msg = "No valid repositories:\n";
	$msg .= $_->msg for @$rbad;
	return Error($msg);
    }
    my $target = get_targ(shift);
    return $target unless $target->ok;
    $target = $target->result;
    my $pkg = get_pkg(shift, $rlist);
    my $package = $pkg->name;
    my $opts = shift;
    my $status_cb = shift;

    trace(3, "PPM::UI::upgrade(", $target->name,
	  ", $package, $opts->{force}, $opts->{follow}, $opts->{dryrun})\n");

    install_or_upgrade($rlist, $target, $pkg, $opts, $status_cb, 'upgrade');
}

#=============================================================================
# Operations which require you to have logged in
#=============================================================================

sub profile_set {
    my $profile = shift;
    trace(3, "PPM::UI::profile_set($profile)\n");
    config_set('profile', $profile);
    Ok();
}

sub profile_get {
    trace(3, "PPM::UI::profile_get()\n");
    config_get('profile');
}

sub profile_list {
    trace(3, "PPM::UI::profile_list()\n");
    my $p_rep = config_get('profile_server')->result;
    my $rep = get_rep($p_rep);
    return $rep unless $rep->ok;
    $rep = $rep->result;
    $rep->profile_list;
}

sub profile_add {
    my $profile = shift;
    trace(3, "PPM::UI::profile_add($profile)\n");
    my $p_rep = config_get('profile_server')->result;
    my $rep = get_rep($p_rep);
    return $rep unless $rep->ok;
    $rep = $rep->result;
    $rep->profile_add($profile);
}

sub profile_del {
    my $profile = shift;
    trace(3, "PPM::UI::profile_del($profile)\n");
    my $p_rep = config_get('profile_server')->result;
    my $rep = get_rep($p_rep);
    return $rep unless $rep->ok;
    $rep = $rep->result;
    $rep->profile_del($profile);
}

sub profile_restore {
    my $profile = shift;
    my $status_cb = shift;
    my $remove_cb = shift;
    my $force = shift;
    my $follow = shift;
    my $dry = shift;
    my $clean_pkgs = shift;

    trace(3, "PPM::UI::profile_restore($profile, CODEREF, ",
	  "$force, $follow, $dry, $clean_pkgs)\n");

    my $p_rep = config_get('profile_server')->result;
    my $rep = get_rep($p_rep);
    return $rep unless $rep->ok;
    $rep = $rep->result;

    # 1. Download the profile_info() from the repository
    my $res = $rep->profile_info($profile);
    return $res unless $res->ok;

    my %packages;

    # 2. For each package in profile_info(), upgrade (or install)
    for my $entry ($res->result_l) {
	my ($repos, $targ_type, $targ_name, $package, $version) = @$entry;
	my $rep = get_rep($repos);
	return $rep unless $rep->ok;
	$rep = $rep->result;
	my $targ = get_targ($targ_name)->result;
	next unless $targ;	# skip unknown targs

	$packages{$targ->name}{$package} = $version;

	my $prop = properties($targ_name, $package);
	if ($prop->ok) {
	    my $pkg = ($prop->result_l)[PROP_PPD_OBJ];
	    my $ppm_ppd = $pkg->getppd_obj;
	    next if $ppm_ppd->ok and $ppm_ppd->result->version_osd eq $version;
	}

	if ($dry) {
	    my $version = PPM::PPD::printify($version);
	    $status_cb->($package, $version, $targ->name,
			 'PRE-INSTALL', 0, 0, 0);
	}
	else {
	    remove($targ_name, $package, 1, $remove_cb)
	      if $prop->ok;
	    my $opts = {force => $force, follow => $follow, dryrun => $dry};
	    install([$rep], $targ_name, $package, $opts, $status_cb);
	}
    }

    return Ok() unless $clean_pkgs;

    # 3. Now query each target and make sure it only contains the packages we
    # just installed (if clean_pkgs is set):
    for my $target (keys %packages) {
	my @precious = get_targ($target)->result->precious->result_l;
	my $q = query($target, '*', 0);
	next unless $q->ok;
	for my $pkg ($q->result_l) {
	    next if exists $packages{$target}{$pkg->name};
	    next if grep { $pkg->name eq $_ } @precious;
	    if ($dry) {
		$remove_cb->($pkg->name, $pkg->version, $target);
	    }
	    else {
		remove($target, $pkg->name, 1, $remove_cb);
	    }
	}
    }

    return Ok();
}

sub profile_save {
    my $name = shift;
    trace(3, "PPM::UI::profile_save($name)\n");

    my $p_rep = config_get('profile_server')->result;
    my $rep = get_rep($p_rep);
    return $rep unless $rep->ok;
    $rep = $rep->result;

    # 1. Get the "query *" information from all current targets.
    my @entries;

    # First, get the targets:
    my @targets = map { get_targ($_)->result } target_list()->result_l;
    for my $targ (@targets) {

	# Now get information about that target:
	my $targ_t = $targ->config_get("TARGET_TYPE")->result;
	my $targ_name = $targ->name;

	# Now get the packages:
	my @pkgs = query($targ, '*', 0)->result_l;
	for my $pkg (@pkgs) {
	    my $obj = $pkg->getppd_obj;
	    next unless $obj->ok;
	    my $prop = properties($targ, $pkg->name);
	    my $repos = ($prop->result_l)[PROP_REPOS];
	    my $entry = [$repos,
			 $targ_t,
			 $targ_name,
			 $pkg->name,
			 $obj->result->version_osd,
			];
	    push @entries, $entry;
	}
    }

    # 2. Upload the information to the Repository.
    $rep->profile_save($name, @entries);
}

sub profile_info {
    my $name = shift;
    trace(3, "PPM::UI::profile_info($name)\n");
    my $p_rep = config_get('profile_server')->result;
    my $rep = get_rep($p_rep);
    return $rep unless $rep->ok;
    $rep = $rep->result;
    my $res = $rep->profile_info($name);
    return $res unless $res->ok;
    my @lst = $res->result_l;
    my @ret;
    for (@lst) {
	my $ent = [@$_[qw(3 4 2)]];
	$ent->[1] = PPM::PPD::printify($ent->[1]);
	push @ret, $ent;
    }
    List(@ret);
}

sub profile_rename {
    my $oldname = shift;
    my $newname = shift;

    # Make sure the profile actually exists:
    my @profiles = profile_list()->result_l;
    return Error("Can't rename nonexistent profile '$oldname'.")
	unless grep { $oldname eq $_ } @profiles;
    return Error("Old '$oldname' and new '$newname' profile names are identical.")
	if lc($oldname) eq lc($newname);

    # Tell the server to rename the profile:
    my $repos = get_rep(config_get('profile_server')->result);
    return $repos unless $repos->ok;
    $repos = $repos->result;
    $repos->profile_rename($oldname, $newname);
}

#=============================================================================
# Utilities
#=============================================================================
sub install_or_upgrade {
    my $rlist = shift;      # A list of repositories to search in order
    my $target = shift;
    my $package = shift;
    my $opts = shift;
    my %opts = %$opts;
    my $status_cb = shift;
    my $event_name = shift;

    my $do_install = sub {
	my $pkg = shift;

	# Download the PPD and package tarball:
	my $pkg_obj = $pkg->getppd_obj($target)->result;
	if ($opts{dryrun}) {
	    $status_cb->($pkg->name, $pkg_obj->version,
			 $target->name, 'PRE-INSTALL', 0, 0, 0);
	    return Ok(); # do nothing, successfully
	}
	my $location = $pkg->getppm($target,
				    config_get("tempdir")->result,
				    $status_cb,
				    config_get("downloadbytes")->result,
				   );

	# update ERR appropriately, and fail.
	return $location unless $location->ok;

	# Send the install (or update) event to the backend:
	my $err = $target->$event_name($pkg_obj->name,
				       $location->result,
				       $pkg_obj->ppd,
				       $pkg->rep->location,
				       $opts{verbose},
				      );
	return $err unless $err->ok;

	$status_cb->($pkg->name, $pkg_obj->version, $target->name, "COMPLETE");

	# Track the profile:
	my $track = config_get('profile_enable')->result;
	if ($track) {
	    my $p_rep = get_rep(config_get('profile_server')->result);
	    my $ok = $p_rep;
	    my $profile = config_get('profile')->result;
	    if ($p_rep->ok) {
		$p_rep = $p_rep->result;
		my $entry = [$pkg->rep->location,
			     $target->config_get('TARGET_TYPE')->result,
			     $target->name,
			     $pkg_obj->name,
			     $pkg_obj->version_osd,
			    ];
		if ($event_name eq 'install') {
		    $ok = $p_rep->installed($profile, $entry);
		}
		else {
		    $ok = $p_rep->upgraded($profile, $entry);
		}
	    }
	    unless ($ok->is_success) {
		my $warning = Warning("Profile '$profile' may not be in sync. "
			       . $ok->msg_raw);
		return $warning;
	    }
	}
	return Ok();
    };

    # We can shortcut the prerequisite check if we're ignoring that:
    return $do_install->($package)
      if ($opts{force} and not $opts{follow});

    my $warning = Ok();
    my @pkgs = ($package);
    my %done;

  PACKAGE:
    while (@pkgs) {
	my $pkg = shift @pkgs;

	# If the package spec came from a repository, assume the user knows
	# what they're doing, and it's ready to be updated.
	$pkg->make_complete($target);
	my $ppd_ref = $pkg->getppd_obj($target);
	return $ppd_ref unless $ppd_ref->ok;
	if ($ppd_ref->result->from ne 'repository') {
	    # If the package is up to date (and $force isn't set), return.
	    my $prop = properties($target, $pkg);
	    if ($prop->ok) {
		my $u2d = $pkg->uptodate($target);

		# If the server doesn't have that package available, we'll
		# _assume_ it's up to date, issuing a warning to that effect
		my $uptodate = 1;
		if ($u2d->ok) {
		    ($uptodate) = $u2d->result_l;
		}
		else {
		    #print "NOTE: package " . $pkg->name . " not on server...\n";
		    #print Dumper $u2d;
		    #print Dumper \@pkgs;
		    next PACKAGE;
		}
		next PACKAGE if ($uptodate and not $opts{force});
	    }
	}

	# Try to get a list of prerequisites for the package:
	my @missing;
	my $impl = $ppd_ref->result->find_impl($target);
	return $impl unless $impl->ok;
	
	# Get a list of prerequisites from the implementation:
	my @prereqs = grep { not $done{$_->name} } $impl->result->prereqs;

	# We can shortcut the cross-checking of prereqs if we're forcing the
	# install of any prereqs:
	if ($opts{force} and $opts{follow} and @prereqs) {
	    unshift @pkgs, (map { get_pkg($_->name, $rlist) } @prereqs), $pkg;
	    next PACKAGE;
	}

	# Check each prerequisite to see if it's installed.
	else {
	    for my $pre (@prereqs) {
		my $prop = $target->properties($pre->name);
		push @missing, $pre->name and next
		  unless $prop->ok;
		my $ver = ($prop->result_l)[PROP_PPD_OBJ]->version_osd;
		my $ok = $pkg->uptodate($target, $pre->name, $ver);
		push @missing, $pre->name
		  if ($ok->ok and not (($ok->result_l)[0]));
	    }
	    if (@missing and not $opts{force} and not $opts{follow}) {
		return Error(
		    "can't install package '", $pkg->name,
		    "': missing prereqs @missing."
		);
	    }
	    elsif (@missing) {
		unshift @pkgs, (map { get_pkg($_, $rlist) } @missing), $pkg;
		next PACKAGE;
	    }
	}

	# Install the durned package
	my $res = $do_install->($pkg);
	return $res unless $res->ok;
	$warning = $res unless $res->is_success;
	$done{$pkg->name}++;
    }
    return $warning;
}

#=============================================================================
# These utilities make it easier for clients of this class to find out
# information about packages. Any subroutine which takes the name of a package
# can now take either a URL or a filename. That means clients may want to know
# this!
#=============================================================================
sub get_pkg {
    my $pkg = shift;
    my $rep = shift;
    return $pkg if eval { $pkg->isa('PPM::Package') };
    PPM::Package->new($pkg, $rep);
}

sub pkg_type {
    my $pkg = get_pkg(shift, undef); # not going to use the repository
    return $pkg->type;
}

sub is_pkg {
    my $pkg = shift;
    return 1 if eval { $pkg->isa('PPM::Package') };
    my $p = PPM::Package->new($pkg);
    return 0 if $p->type eq 'UNKNOWN';
    1;
}

#=============================================================================
# Cache of "active" repositories and targets:
#=============================================================================
my %open_repositories;
sub get_rep {
    my $rep = shift;
    trace(3, "PPM::UI::get_rep($rep)\n");
    return Ok($rep) if eval { $rep->isa("PPM::Repository") };
    return Ok($open_repositories{$rep})
      if exists $open_repositories{$rep};
    my ($url,$name,$pass) = exists $REPS->{DATA}{$rep}
			  ? @{$REPS->{DATA}{$rep}}{qw(url username password)}
			  : ($rep, undef, undef);

    my @ok = PPM::Repository->new($url, $rep, $name, $pass);
    return Error($ok[1]) unless $ok[0];
    $open_repositories{$rep} = $ok[0];
    Ok($ok[0]);
}
sub del_rep {
    my $rep = shift;
    delete $open_repositories{$rep};
}
sub get_reps {
    my $reps = shift;
    my (@good, @bad);
    for my $_rep (@$reps) {
	my $rep = get_rep($_rep);
	if ($rep->ok) {
	    push @good, $rep->result;
	}
	else {
	    push @bad, $rep;
	}
    }
    (\@good, \@bad)
}

my %open_installers;
sub get_targ {
    my $targ = shift;
    $targ = '' unless defined $targ;
    trace(3, "PPM::UI::get_targ($targ)\n");
    return Ok($targ) if eval { $targ->isa("PPM::Installer") };
    return Ok($open_installers{$targ})
      if exists $open_installers{$targ};
    return Error("Target '$targ' not found")
      unless exists $TARG->{DATA}{$targ};
    my $t = $TARG->{DATA}{$targ};
    my @r = PPM::Installer->new($targ, $t);
    return Error($r[1]) unless $r[0];
    $open_installers{$targ} = $r[0];
    Ok($r[0]);
}

#=============================================================================
# Settings persistence
#=============================================================================
BEGIN {
    $CONF = PPM::Config::load_config_file('clientlib');
    $REPS = PPM::Config::load_config_file('repositories');
    $TARG = PPM::Config::load_config_file('targets', 'ro');

    my $tempdir = config_get('tempdir');
    config_set('tempdir', $ENV{TEMP}) if $tempdir->ok && ! -d $tempdir->result;

    # Start up the trace if it's needed:
    my $tracelvl = config_get('tracelvl');
    if ($tracelvl->result && $tracelvl->result > 0) {
	PPM::Trace::trace_init(config_get('tracefile')->result,
			       config_get('tracelvl')->result);
    }
}

package PPM::Package;
use strict;
use PPM::Result qw(Ok Warning Error List);
use URI;
use Data::Dumper;

sub new {
    my $class = shift;
    my $name  = shift;
    my $rep   = shift;
    my $o = bless {}, ref($class) || $class;
    $rep = [] if not defined $rep;

    # A PPM::PPD object
    if (eval { $name->isa('PPM::PPD') }) {
	$o->{type}    = 'PPM::PPD';
	$o->{name}    = $o->{rawname} = $name->name;
	$o->{id}      = $name->id;
	$o->{current_rep} =
	    defined $name->repository ? $name->repository :
	    ref $rep eq 'ARRAY'       ? $rep->[0]         : $rep;
	$o->{reps}    = [$o->{current_rep}];
	$o->{obj}     = Ok($name);
    }
    # A URL:
    elsif ($name =~ m{(^[^:]{2,}://.+)/([^/]+)\.ppd$}i) {
	$o->{type}    = 'WWW';
	$o->{rawname} = $o->{id} = $2;
	my $rep       = PPM::UI::get_rep($1);
	$o->{uri}     = URI->new($name);
	die "Can't parse PPD location $name: " . $rep->msg
	  unless $rep->is_success;
	$o->{reps}    = [$rep->result];
	my $ppd_obj   = $o->getppd_obj(undef); # undef'd target...
	$o->{name}    = $ppd_obj->ok ? $ppd_obj->result->name : $o->{rawname};
    }
    # A filename:
    elsif ($name =~ m{((?:^[A-Z]:[\\/]|[\\/]{2})?.*?)?([^\\/]+)\.ppd$}i) {
	$o->{type}    = 'FILE';
	$o->{rawname} = $o->{id} = $2;
	my $dir = $1;
	$dir =~ s{[\\/]+$}{} if $dir and $dir !~ m{^([A-Z]:)?[\\/]+$}i;
	my $rep = PPM::UI::get_rep($dir || '.');
	die "Can't parse PPD location $name: " . $rep->msg
	  unless $rep->is_success;
	$o->{reps}  = [$rep->result];
	my $ppd_obj = $o->getppd_obj(undef); # undef'd target...
	$o->{name}    = $ppd_obj->ok ? $ppd_obj->result->name : $o->{rawname};
    }
    # A plain package name:
    elsif ($name =~ m{^[-_A-Za-z0-9]+$}) {
	$o->{type} = 'PKG';
	$o->{name} = $o->{rawname} = $name;
	$o->{reps} = ref($rep) eq 'ARRAY' ? $rep : [$rep];
    }
    # Something else:
    else {
	#print STDERR "WARNING: could not parse package name '$name'.\n";
	$o->{type} = 'UNKNOWN';
	$o->{name} = $o->{rawname} = $name;
	$o->{reps} = ref($rep) eq 'ARRAY' ? $rep : [$rep];
    }
    $o->{id} = $o->{rawname} unless defined $o->{id};

    return $o;
}

sub name {
    my $o = shift;
    $o->{name} = shift if @_;
    $o->{name};
}

sub reps {
    my $o = shift;
    @{$o->{reps}};
}

sub rep {
    my $o = shift;
    $o->{current_rep};
}

sub type {
    my $o = shift;
    $o->{type};
}

sub uri {
    my $o = shift;
    $o->{uri};
}

# Forces a refresh of the {obj} or {desc} fields if they are not marked as
# complete by the PPM::Repository client.
sub make_complete {
    my $o = shift;
    my $targ = PPM::UI::get_targ(shift)->result;
    my $obj = $o->getppd_obj($targ);
    return if (
	$obj and
	$obj->ok and
	$obj->result->is_complete and
	$obj->result->ppd
    );
    delete @$o{qw(obj desc)};
    $o->{obj} = $o->{desc} = $o->getppd_obj($targ);
}

# Find the first repository containing the package, and report whether the
# package is up-to-date w.r.t that repository.
sub uptodate {
    my $o = shift;
    my $target = PPM::UI::get_targ(shift)->result;
    my $desc = $o->describe($target);
    if ($o->{current_rep} and $desc and $desc->ok) {
	my $u2d = $o->{current_rep}->uptodate(
	    $target,
	    $o->{id},
	    $desc->result->version_osd,
	);
	return $u2d;
    }
    Error("package $o->{rawname} not found in repositories");
}

sub describe {
    my $o = shift;
    my $target = PPM::UI::get_targ(shift)->result;
    return $o->{obj}  if $o->{type} eq 'PPM::PPD';
    return $o->{desc} if $o->{desc};
    unless ($o->{desc}) {
	for my $rep (@{$o->{reps}}) {
	    $o->{current_rep} = $rep;
	    $o->{desc} = $rep->describe($target, $o->{id});
	    last if $o->{desc}->ok;
	}
    }
    $o->{desc};
}

sub getppd_obj {
    my $o = shift;
    my $target = PPM::UI::get_targ(shift)->result;
    return $o->{obj} if defined $o->{obj};
    for my $rep (@{$o->{reps}}) {
	$o->{current_rep} = $rep;
	$o->{obj} = $rep->getppd_obj($target, $o->{id});
	last if $o->{obj}->ok;
    }
    $o->{obj}
}

sub getppd {
    my $o = shift;
    my $target = PPM::UI::get_targ(shift)->result;

    # If the current object already has a complete PPD, use it
    return Ok($o->{obj}->result->ppd) if (
	$o->{obj} and
	$o->{obj}->ok and
	$o->{obj}->result->is_complete and
	$o->{obj}->result->ppd
    );
    $o->make_complete($target);
    return Ok($o->{obj}->result->ppd);
}

sub getppm {
    my $o = shift;
    my $target = PPM::UI::get_targ(shift)->result;
    my $ppm;
    for my $rep (@{$o->{reps}}) {
	$o->{current_rep} = $rep;
	$ppm = $rep->getppm($target, $o->{id}, @_);
	last if $ppm->ok;
    }
    $ppm;
}

# Find the correct target for this package. This means matching the
# LANGUAGE tag in the PPD. Basically we find out what LANGUAGE the PPD
# represents, and we search through the targets looking for a subset which
# implement that language. If more than one target implements the language
# and version, we pick the first. If none work, we fail. If the LANGUAGE
# tag is missing, or the LANGUAGE matches the given target, we use the
# given target.
# NOTE: because LANGUAGE is a child-node of IMPLEMENTATION, we _first_ have to
# search for an implementation that matches the target, _then_ we have to
# verify that the target supports the LANGUAGE tag. If it does, we return it,
# otherwise we go on to the next target.
sub choose_target {
    my $o = shift;
    for (@_) {
	# Load the target:
	my $target = PPM::UI::get_targ($_);
	next unless $target->ok;
	$target = $target->result;

	# Load the PPD and find a suitable implementation for this target:
	$o->make_complete($target);
	my $ppd = $o->getppd_obj($target);
	return $ppd unless $ppd->ok;	# the package doesn't exist.
	my $impl = $ppd->result->find_impl($target);
	next unless $impl->ok;
	my $lang = $impl->result->language;

	# Older PPDs didn't have a LANGUAGE tag, so we must assume a Perl
	# implementation. For old-times' sake, we'll assume version 5.6.0 is
	# required.
	unless (defined $lang) {
	    $lang = PPM::PPD::Language->new({
		NAME	=> 'Perl',
		VERSION	=> '5.6.0',
	    });
	}

	# Check if this implementation's language is understood by the target:
	my $match = $lang->matches_target($target);
	return $match unless $match->ok;
	return Ok($target) if $match->result;
    }
    return Error(
	"no suitable installation target found for package $o->{name}."
    );
}

1;
