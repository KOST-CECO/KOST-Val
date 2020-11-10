package PPM::Repository::PPM3Server;

use strict;
use SOAP::Lite 0.51;
use Data::Dumper;
use File::Basename qw(basename);
use Digest::MD5 ();

use PPM::Config;
use PPM::Sysinfo;
use PPM::Result qw(Ok Error Warning List);
use PPM::PPD;

use base qw(PPM::Repository);
our $VERSION = '3.06';
our $PROTOCOL_VERSION = 3;

#==============================================================================
# Note: the server exports this interface:
#   describe('language', 'package name');
#   getppd('language', 'package name');
#   search('language', 'query', 'case-insensitive');
#   uptodate('language', 'package name', 'osd-version');
#   ppm_protocol();
#
# Note: definition of package_entity:
#   package_entity {
#	repository_url
#       target_type
#       target_name
#       package_name
#       package_version
#   }
#
# This part of the interface is for profile management:
#   profile_create('name');
#   profile_delete('name');
#   profile_save('name', package_entity(s));
#   profile_info('name');
#   profile_target_rename('name', 'oldtarget', 'newtarget');
#   profile_rename('oldname', 'newname'); # XXX: emulated on client-side!
#
# This part is for profile tracking:
#   installed(package_entity);
#   removed(package_entity);
#   upgraded(package_entity);
#==============================================================================

sub init {
    my $o = shift;
    my $file = PPM::Config::get_license_file();

    $o->{licfile} = $file;
    $o->{mtime} = 0;
    $o->{license} = undef;

    1;
}

sub check_license {
    my $o = $_[0];
    my $file = $o->{licfile};

    # Reset state unless the license file exists
    goto &init unless -f $file;

    # Used the cached license unless the license file has changed on disk
    my $f_mtime = ((stat($file))[9]);
    my $l_mtime = $o->{mtime};
    return unless ($f_mtime > $l_mtime or $file ne $o->{liclast});

    # Update the cache from the disk
    if (open (my $LICENSE, $file)) {
	$o->{mtime}   = $f_mtime;
	$o->{license} = do { local $/; <$LICENSE> };
	$o->{liclast} = $file;
	close ($LICENSE) or die "can't close license file $file: $!";
    }
}

sub search {
    my $o = shift;
    my $target = shift;
    my $query = shift;
    my $casei = shift;

    # Get all my arguments together:
    my $target_t = $target->config_get("TARGET_TYPE")->result;
    my @headers = $o->mkheaders(target => $target);
    my $response = eval {
	$o->{client}->search($target_t, $query, $casei, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;

    my @results;
    for my $res (@{$response->{'results'}}) {
	my $h = {
            name => $res->[0], 
            version => $res->[1],
            abstract => $res->[2],
        };
        my $ppd = PPM::Repository::PPM3Server::PPD->new($h, 0);
	@$ppd{qw(rep id from)} = ($o, $ppd->name, 'repository');
	push @results, $ppd;
    }
    List(@results);
}

sub describe {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;

    my $target_t = $target->config_get("TARGET_TYPE")->result;
    my @headers = $o->mkheaders(target => $target);
    my $response = eval {
	$o->{client}->describe($target_t, $pkg, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;

    my $ppd = PPM::Repository::PPM3Server::PPD->new($response->{'results'}, 1);
    @$ppd{qw(rep id from)} = ($o, $ppd->name, 'repository');
    Ok($ppd);
}

sub getppd_obj {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    my $ppd_txt = $o->getppd($target, $pkg);
    return $ppd_txt unless $ppd_txt->ok;
    my $obj = PPM::PPD::->new($ppd_txt->result, $o, $pkg);
    $obj->{from} = 'repository';
    Ok($obj);
}

sub getppd {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;

    my $target_t = $target->config_get("TARGET_TYPE")->result;
    my @headers = $o->mkheaders(target => $target);
    my $response = eval {
	$o->{client}->getppd($target_t, $pkg, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;
    Ok($response->{'results'});
}

sub uptodate {
    my $o = shift;
    my $target = shift;
    my $pkg = shift;
    my $version = shift;

    my $target_t = $target->config_get("TARGET_TYPE")->result;
    my @headers = $o->mkheaders(target => $target);
    my $response = eval {
	# call the more efficient uptodate2 method.
	$o->{client}->uptodate2($target_t, $pkg, $version, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;
    List($response->{results}[0], $response->{results}[1]{version});
#    # If the status is false (it's out of date) return the version on the
#    # server as the new version.
#    my $newversion = '';
#    unless ($response->{'results'}) {
#	my $ppd = $o->describe($target, $pkg)->result;
#	$newversion = $ppd->version;
#    }
#    List($response->{'results'}, $newversion);
}

sub batch {
    my $o = shift;
    # The batch() method was introduced in PPM protocol version 303.
    return $o->SUPER::batch(@_) unless $o->protocol >= 303;
    my $targ = shift;
    my @tasks = @_;
    my $targ_t = $targ->config_get("TARGET_TYPE")->result;

    # Every task needs the target as the first argument, so add it.
    for my $task (@tasks) {
	splice @$task, 1, 0, $targ_t;
    }
    my @headers = $o->mkheaders($targ => $targ);
    my $response = eval {
	$o->{client}->batch($targ_t, @tasks, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;
    my @responses = @{$response->{results}};
    for my $i (0 .. $#tasks) {
	my $resp = $responses[$i];
	my $meth = $tasks[$i][0];
	my $err = $o->errors($resp);
	if ($err->ok) {
	    my $clean_method = "cleanup_$meth";
	    $responses[$i] = $o->can($clean_method)
		? $o->$clean_method($resp->{results})
		: Ok($resp->{results});
	}
	else {
	    $responses[$i] = $err;
	}
    }
    List(@responses);
}

#=============================================================================
# Cleanup functions for the batch() method.
#=============================================================================
sub cleanup_uptodate2 {
    my ($o, $result) = @_;
    my $ppd = PPM::Repository::PPM3Server::PPD->new($result->[1], 0);
    @$ppd{qw(rep id from)} = ($o, $ppd->name, 'repository');
    $result->[1] = $ppd;
    Ok($result);
}

#=============================================================================
# Profiles
#=============================================================================
sub profile_list {
    my $o = shift;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_list(@headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;

    my @profiles = @{$response->{'results'}};
    List(@profiles);
}

sub profile_add {
    my $o = shift;
    my $name = shift;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_create($name, @headers)->result;
    };
    my $err = $o->errors($response,"profile_create");
    return $err unless $err->ok;
    Ok();
}

sub profile_del {
    my $o = shift;
    my $name = shift;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_delete($name, @headers)->result;
    };
    my $err = $o->errors($response,"profile_delete");
    return $err unless $err->ok;
    Ok();
}

sub profile_save {
    my $o = shift;
    my $name = shift;
    my @entries = @_;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_save($name, \@entries, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;
    Ok();
}

sub profile_info {
    my $o = shift;
    my $name = shift;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_info($name, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;
    my $entries = $response->{'results'};
    List(@$entries);
}

sub profile_target_rename {
    my $o = shift;
    my $profile = shift;
    my $oldname = shift;
    my $newname = shift;
    my @headers = $o->mkheaders;
    my $response = eval {
	my @args = ($profile, $oldname, $newname);
	$o->{client}->profile_target_rename(@args, @headers)->result;
    };
    my $err = $o->errors($response);
    return $err unless $err->ok;
    Ok();
}

# This is a bit of a temporary hack: the server doesn't actually expose a
# profile_rename() method, so I emulate it by retrieving the doomed profile,
# saving it as the new profile, and deleting the other. I suspect this will be
# moved over to the server in future, because it can be lightening-fast if
# done directly in the database.
sub profile_rename {
    my $o = shift;
    my $oldprof = shift;
    my $newprof = shift;

    # Retrieve the old profile:
    my $info = $o->profile_info($oldprof);
    return $info unless $info->ok;

    # Delete the new one, but don't croak if it returns an error. This allows
    # us to rename over old profiles.
    my $purge = $o->profile_del($newprof);

    # Create the new one:
    my $new = $o->profile_add($newprof);
    return $new unless $new->ok;

    # Save the new one:
    my $save = $o->profile_save($newprof, $info->result_l);
    return $save unless $save->ok;

    # Delete the old one:
    my $del = $o->profile_del($oldprof);
    return $del unless $del->ok;

    Ok();
}

#=============================================================================
# Profile Tracking:
#=============================================================================
sub installed {
    my $o = shift;
    my $profile = shift;
    my @l = @_;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_pkgs_installed($profile, \@l, @headers)->result;
    };
    $o->errors($response);
}

sub upgraded {
    my $o = shift;
    my $profile = shift;
    my @l = @_;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_pkgs_upgraded($profile, \@l, @headers)->result;
    };
    $o->errors($response);
}

sub removed {
    my $o = shift;
    my $profile = shift;
    my @l = @_;
    my @headers = $o->mkheaders;
    my $response = eval {
	$o->{client}->profile_pkgs_removed($profile, \@l, @headers)->result;
    };
    $o->errors($response);
}

# Calculate a hash of the current user, plus the host and the install time.
# This is useful for tracking how many "users" are using each installation.
my $userhash = PPM::Sysinfo::generate_user_key();
my $insthash = PPM::Sysinfo::inst_key();

# This little helper builds SOAP Headers we can use to send along with the
# SOAP request. The license and other information is sent along with it.
sub mkheaders {
    my $o = shift;
    my %args = @_;
    my @headers;

    # By checking the license each time, we can auto-detect new licenses
    # without a re-start of PPM:
    $o->check_license;
    push @headers, SOAP::Header->name('license', $o->{license});

    # Push on the ID of this installation of PPM:
    push @headers, SOAP::Header->name('ppm_install_id', $insthash);

    # Push on a hash of the current user plus hostname & install time. Note
    # that we specifically don't want to use all the same elements that went
    # into the install_id of this host (hostname, insttime, os, ip_addr). We
    # want to have each user get a unique string, but to munge enough extra
    # uniqueness that the usernames can't be guessed just by a simple
    # dictionary attack against someone sniffing the MD5 keys.
    push @headers, SOAP::Header->name('user_hash', $userhash);

    # Push on the client's protocol version and "real" version:
    push @headers, SOAP::Header->name('client_version', $VERSION);
    push @headers, SOAP::Header->name('ppm_protocol',
				      $PROTOCOL_VERSION);

    # This information has to be grabbed from the piece of software actually
    # interacting with the user. Currently, there's no way to do that cleanly.
    push @headers, SOAP::Header->name('useragent', 'PPM');
    push @headers, SOAP::Header->name('useragent_vers', '3.0');

    # Push on target-specific stuff:
    for my $k (keys %args) {
	if ($k eq 'target') {
	    my $t = $args{$k};
	    push(@headers,
	         SOAP::Header->name('archname',
				    $t->config_get("ARCHITECTURE")->result),
		 SOAP::Header->name('os', $t->config_get("OSVALUE")->result),
		 SOAP::Header->name('osvers',
		 		    $t->config_get("OSVERSION")->result),
		);
	}
    }
    @headers;
}

sub type_printable { "PPMServer 3.0" }

sub errors {
    my $o = shift;
    my $response = shift;

    # assuming that method name here and method name on server are usually
    # equivalent.  if not, use an optional second argument to supply method
    # name.
    my $method = shift || (split '::', (caller(1))[3])[-1];

    if ($@) {
	chomp $@;
	return PPM::Repository::Result::->new("$method exception: $@.",
					      $o->location,
				             );
    }
    elsif (not defined $response) {
	return
	  PPM::Repository::Result::->new(
	      "$method returned undefined results.",
	      $o->location,
	  );
    }
    elsif ($response->{'status'} != 0) {
	return PPM::Repository::Result::->new($response->{'message'},
					      $o->location,
					      $response->{'status'}
					     );
    }
    Ok();
}

package PPM::Repository::PPM3Server::PPD;
our @ISA = qw(PPM::PPD);

sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    my $self = bless {}, $class;
    my $server_ppd = shift;
    my $complete = shift;

    $self->{is_complete} = $complete;

    # Author:  "authorname (authoremail)"
    if (defined $server_ppd->{'authorname'}) {
        $self->{parsed}{AUTHOR} = $server_ppd->{'authorname'};
        if (defined $server_ppd->{'authoremail'}) {
            $self->{parsed}{AUTHOR} .= " ($server_ppd->{'authoremail'})";
        }
    }
    
    # Name, title, version, abstract:
    for my $field (qw(title abstract version name)) {
	$self->{parsed}{"\U$field"} = $server_ppd->{$field};
    }

    # Implementations:
    for my $impl (@{$server_ppd->{implementation}}) {
	my $i = bless { ARCHITECTURE => $impl },
		      'PPM::Repository::PPM3Server::PPD::Implementation';
	push @{$self->{parsed}{IMPLEMENTATION}}, $i;

	# Dependencies:
	for my $dep (@{$server_ppd->{dependency}}) {
	    my $dep = bless { NAME => $dep->{name},
			      VERSION => $dep->{version} },
			    'PPM::Repository::PPM3Server::PPD::Dependency';
	    push @{$i->{DEPENDENCY}}, $dep;
	}
    }
    return $self;
}

sub version {
    my $o = shift;
    $o->version_osd;
}

package PPM::Repository::PPM3Server::PPD::Implementation;
our @ISA = qw(PPM::PPD::Implementation);

package PPM::Repository::PPM3Server::PPD::Dependency;
our @ISA = qw(PPM::PPD::Dependency);

sub version {
    my $o = shift;
    $o->version_osd;
}
