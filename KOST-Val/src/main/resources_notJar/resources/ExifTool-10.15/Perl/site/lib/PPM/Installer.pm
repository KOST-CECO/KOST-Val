package PPM::Installer;

use strict;
use Socket;
use Data::Dumper;
use PPM::Result qw(Error Warning Ok List);

my %connections;
sub new {
    my ($class, $name, $targ) = @_;

    my @ret;
    my $pkg  = "PPM::Installer::$targ->{type}";

    # Let the type decide which subclass to use:
    eval "require $pkg";
    if (not $@) {
	no strict 'refs';
	$connections{$name} = &{"${pkg}::new"}($pkg, $targ, $name);
	$connections{$name}->init;
	@ret = $connections{$name};
    }
    else {
	@ret = (undef, "Unsupported installer type '$targ->{type}'");
    }
    @ret;
}

sub DESTROY {
    my $o = shift;
    $o->fini;
}

#=============================================================================
# API methods -- simple fetch routines.
#=============================================================================
sub name {
    my $inst = shift;
    $inst->{name};
}

sub port {
    my $inst = shift;
    $inst->{port};
}

sub type {
    my $inst = shift;
    $inst->{type};
}

sub ckeys { qw(port type) }
sub cvals {
    my $o = shift;
    map { $o->$_ } $o->ckeys;
}

#=============================================================================
# Methods which determine which version of the API the backend is running.
#=============================================================================
# We remember here what methods were supported under protocol version 1.
my %HAS = map { $_ => 1 } qw(
    query
    properties
    remove
    precious
    bundled
    dependents
    config_info
    config_keys
    config_get
    config_set
    error_str
    install
    upgrade
    pkginit
    pkgfini
    transmit
    stop
);

sub has {
    my $inst = shift;
    my $method = shift;
    return $inst->_has($method) if $inst->protocol >= 2;
    return $HAS{$method} ? 1 : 0;
}

sub _has {
    my $inst = shift;
    my $method = shift;
    my $send = $inst->encode_record("HAS", $method);
    $inst->sendmsg($send);
    my $msg = $inst->recvmsg;
    return 0 if $msg and $msg eq 'NOK';
    return 1;
}

#=============================================================================
# API methods which probably should be overridden in subclasses.
#=============================================================================
sub init { }
sub fini { }

# Transmit files to the backend installer agent. This is used for remote
# installers, which must copy the files to the remote machine in order to
# install them. Local installers can just use the local copy.
sub pkginit  { Ok() }	# creates any temporary directory needed
sub transmit { Ok() }	# transmits files (into the temp dir)
sub pkgfini  { Ok() }	# removes the temporary directory

#=============================================================================
# API methods which _may_ be overridden in subclasses.
#=============================================================================
sub connect_to {
    my $inst = shift;
    my $addr = shift;
    my $port = shift;
    my ($iaddr, $paddr, $proto);
    $iaddr = inet_aton($addr)
      or return Error("no host: $addr");
    $paddr = sockaddr_in($port, $iaddr);
    $proto = getprotobyname('tcp');
    socket($inst->{SOCK}, PF_INET, SOCK_STREAM, $proto)
      or return Error("socket: $!");
    connect($inst->{SOCK}, $paddr)
      or return Error("connect: $!");
    select((select($inst->{SOCK}), $| = 1)[0]);
    Ok();
}

sub query {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("QUERY", @_));
    my $msg = $inst->recvmsg;
    return Error($inst->error_str) if $msg eq 'NOK';
    my@l = map { PPM::PPD->new($_) } 
	   map { chomp; $inst->decode_record($_) }	# flattens fields
	   split(/^/, $msg);
    List(@l);
}

sub properties {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("PROPERTIES", @_));
    my $recv = $inst->recvmsg;
    if ($recv eq 'NOK') {
	return Error($inst->error_str);
    }
    my ($ppd, @other) = $inst->decode_record($recv);
    return List(PPM::PPD->new($ppd), @other);
}

sub precious {
    my $inst = shift;
    $inst->sendmsg("PRECIOUS");
    List($inst->decode_record($inst->recvmsg));
}

sub bundled {
    my $inst = shift;
    $inst->sendmsg("BUNDLED");
    List($inst->decode_record($inst->recvmsg));
}

sub dependents {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("DEPENDENTS", @_));
    my $recv = $inst->recvmsg;
    return Error($inst->error_str) if $recv eq 'NOK';
    return List() if not defined (($inst->decode_record($recv))[0]);
    return List($inst->decode_record($recv));
}

sub remove {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("REMOVE", @_));
    return Ok() if $inst->recvmsg eq 'OK';
    return Error($inst->error_str);
}

sub install {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("INSTALL", @_));
    return Ok() if $inst->recvmsg eq 'OK';
    return Error($inst->error_str);
}

sub upgrade {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("UPGRADE", @_));
    return Ok() if $inst->recvmsg eq 'OK';
    return Error($inst->error_str);
}

sub config_info {
    my $inst = shift;
    $inst->sendmsg("CONFIG_INFO");
    my %info = map { chomp; $inst->decode_record } split /^/, $inst->recvmsg;
    return List(map { [$_, $info{$_}] } sort keys %info);
}

sub config_keys {
    my $inst = shift;
    $inst->sendmsg("CONFIG_KEYS");
    my %keys = map { chomp; $inst->decode_record } split /^/, $inst->recvmsg;
    return List(map { [$_, $keys{$_}] } sort keys %keys);
}

sub config_get {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("CONFIG_GET", @_));
    my $value = $inst->recvmsg;
    return Error($inst->error_str)
      if $value eq 'NOK';
    return Ok($value);
}

sub config_set {
    my $inst = shift;
    $inst->sendmsg($inst->encode_record("CONFIG_SET", @_));
    return Ok() if $inst->recvmsg eq 'OK';
    return Error($inst->error_str);
}

sub protocol {
    my $inst = shift;
    my $v = $inst->config_get("PROTOCOL");
    return $v->result if $v->ok;
    return 1; # version 1 didn't expose the PROTOCOL variable.
}

# This method was not supported in the first version of the PPM3 backend. To
# compensate, we'll do a lousy emulation: if the languages are the same, and
# the required version is lexically less than the PERLCORE exposed by the
# backend, we'll allow the installation to proceed.
sub can_install {
    my $inst = shift;
    unless ($inst->has('can_install')) {
	my ($lang, $version, $compat) = @_;
	return Ok(1) if (
	    lc $lang eq lc $inst->config_get("TARGET_TYPE")->result and
	    lc $version lt lc $inst->config_get("PERLCORE")->result
	);
	return Ok(0);
    }
    $inst->sendmsg($inst->encode_record("CAN_INSTALL", @_));
    my $result = $inst->recvmsg;
    return Error($inst->error_str) if $result eq 'NOK';
    return Ok($result);
}

sub error_str {
    my $inst = shift;
    $inst->sendmsg("ERROR_STR");
    $inst->recvmsg;
}

#=============================================================================
# Non-API methods. Touch not, lest ye be smacked.
#=============================================================================
use constant FIELD_SEP => "\001";
use constant FIELD_UNDEF => "\002";
my $EOL = "\015\012";
sub sendmsg {
    my $inst = shift;
    my $msg = shift;
    my $fd = $inst->{SOCK};
    {
	local $\ = "$EOL.$EOL";
	print $fd $msg;
    }
}

sub recvmsg {
    my $inst = shift;
    local $/ = "$EOL.$EOL";
    my $fd = $inst->{SOCK};
    chomp(my $msg = <$fd>);
    return $msg;
}

sub qmeta {
    local $_ = shift || $_;
    s{([^A-Za-z0-9])}{sprintf('\x%.2X',ord($1))}eg;
    $_;
}

sub uqmeta {
    local $_ = shift || $_;
    $_ = eval qq{qq{$_}};
    warn $@ if $@;
    $_;
}

sub encode_record {
    my $o = shift;
    my @fields = map { my $a = defined $_ ? $_ : FIELD_UNDEF; qmeta($a) } @_;
    join FIELD_SEP, @fields;
}

sub decode_record {
    my $o = shift;
    my $t = shift || $_;
    return map { $_ = &uqmeta; $_ = undef if $_ eq FIELD_UNDEF; $_ }
	   split(FIELD_SEP, $t, -1);
}

1;
