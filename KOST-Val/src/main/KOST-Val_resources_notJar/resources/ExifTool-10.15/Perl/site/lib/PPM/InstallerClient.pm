package PPM::InstallerClient;

use strict;
use Socket;
use Cwd qw(cwd);
use File::Basename qw(dirname basename);
use File::Path qw(mkpath rmtree);

use Data::Dumper;
use vars qw($VERSION %handlers);

use constant PROTOCOL => 2;
$VERSION = '3.0';

#=============================================================================
# API:
#=============================================================================
sub new {
    my ($pkg, $port) = @_;
    my $o = bless {}, ref($pkg) || $pkg;
    $o->init($port);
}

sub init {
    my ($inst, $ppm_port) = @_;
    my ($paddr, $proto, $msg);

    # Set up a temporary socket server and waits for the frontend to connect
    # to it.
    # TODO: put this in a big while(1) loop, and keep a list of connected
    # frontends. That way, we can service multiple front-ends at once, which
    # prevents multiple instances of the same target from clobbering each
    # other's changes.
    $proto = getprotobyname('tcp');
    socket(SERVER, PF_INET, SOCK_STREAM, $proto)	|| die "socket: $!";
    setsockopt(SERVER, SOL_SOCKET, SO_REUSEADDR,
	       pack('l', 1))				|| die "setsockopt: $!";
    bind(SERVER, sockaddr_in($ppm_port, INADDR_ANY))	|| die "bind: $!";
    listen(SERVER, SOMAXCONN);
    $paddr = accept(CLIENT, SERVER);
    my ($port, $iaddr) = sockaddr_in($paddr);
    my $name = gethostbyaddr($iaddr, AF_INET);
    select((select(CLIENT), $| = 1)[0]);
    $inst->{fd} = \*CLIENT;
    close(SERVER);

    $inst->{cwd} = cwd;

    # Read commands from the socket:
    while ($msg = $inst->recvmsg) {
	my ($cmd, @args) = decode_record($msg);
	my $h = $handlers{lc($cmd)};
	if (defined $h) {
	    last unless $inst->$h($cmd, @args);
	}
	else {
	    die "Unrecognized command: $cmd";
	}
    }
}

%handlers = (
    has		=> sub {
	my ($inst, undef, @args) = @_;
	my $cmd = $args[0];
	if (exists $handlers{lc($cmd)}) {
	    $inst->sendmsg("OK");
	}
	else {
	    $inst->sendmsg("NOK");
	}
    },
    query	=> sub {
	my ($inst, $cmd, @args) = @_;
	my @ppds = $inst->query(@args);
	if (not $ppds[0]) {
	    $inst->sendmsg("NOK");
	}
	else {
	    my @records = map { encode_record($_) } @ppds[1..$#ppds];
	    local $" = "\n";
	    $inst->sendmsg("@records");
	}
	1;
    },
    properties	=> sub {
	my ($inst, $cmd, @args) = @_;
	my @fields = $inst->properties(@args);
	if (@fields) {
	    $inst->sendmsg(encode_record(@fields));
	}
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    remove	=> sub {
	my ($inst, $cmd, @args) = @_;
	my $ret = $inst->remove(@args);
	if ($ret) {
	    $inst->sendmsg("OK");
	}
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    precious	=> sub {
	my ($inst, $cmd, @args) = @_;
	my @ret = $inst->precious();
	$inst->sendmsg(encode_record(@ret));
	1;
    },
    bundled	=> sub {
	my ($inst, $cmd, @args) = @_;
	my @ret = $inst->bundled();
	$inst->sendmsg(encode_record(@ret));
    },
    dependents	=> sub {
	my ($inst, $cmd, @args) = @_;
	my @ret = $inst->dependents(@args);
	if (@ret == 1 and not defined $ret[0]) {
	    $inst->sendmsg('NOK');
	}
	elsif (@ret == 0) {
	    $inst->sendmsg(encode_record(undef));
	}
	else {
	    $inst->sendmsg(encode_record(@ret));
	}
	1;
    },
    config_info	=> sub {
	my ($inst, $cmd, @args) = @_;
	my @ret = $inst->config_info;
	if (@ret) {
	    my @records = map { encode_record(@$_) } @ret;
	    local $" = "\n";
	    $inst->sendmsg("@records");
	}
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    config_keys	=> sub {
	my ($inst, $cmd, @args) = @_;
	my @ret = $inst->config_keys;
	if (@ret) {
	    my @records = map { encode_record(@$_) } @ret;
	    local $" = "\n";
	    $inst->sendmsg("@records");
	}
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    config_get	=> sub {
	my ($inst, $cmd, @args) = @_;
	# Because I don't want the actual installer reporting this key, I
	# will override it here, in the client library. The C version will do
	# this.
	if ($args[0] eq 'PROTOCOL') {
	    $inst->sendmsg(PROTOCOL);
	    return 1;
	}
	my $ret = $inst->config_get(@args);
	if ($ret) {
	    $inst->sendmsg($ret);
	}
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    config_set	=> sub {
	my ($inst, $cmd, @args) = @_;
	if ($inst->config_set(@args)) {
	    $inst->sendmsg("OK");
	} 
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    error_str	=> sub {
	my ($inst, $cmd, @args) = @_;
	$inst->sendmsg($inst->error_str);
	1;
    },
    can_install	=> sub {
	my ($inst, $cmd, @args) = @_;
	# The following line is for reference:
	# my ($lang, $version, $compat_type) = @args;
	my $ret = $inst->can_install(@args);
	if (defined $ret) {
	    $inst->sendmsg($ret);
	}
	else {
	    sendmsg('NOK');
	}
	1;
    },
    install	=> sub {
	my ($inst, $cmd, @args) = @_;
	# The following line is for reference:
	# my ($pkg, $ppmpath, $ppd, $repos, $ppmpath) = @args;
	$args[1] = $inst->{tmpdirs}{$args[0]}
	  if exists $inst->{tmpdirs}{$args[0]};

	my $ret = $inst->install(@args);
	if ($ret) {
	    $inst->sendmsg("OK");
	}
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    upgrade	=> sub {
	my ($inst, $cmd, @args) = @_;
	# The following line is for reference:
	# my ($pkg, $ppmpath, $ppd, $repos, $ppmpath) = @args;
	$args[1] = $inst->{tmpdirs}{$args[0]}
	  if exists $inst->{tmpdirs}{$args[0]};

	my $ret = $inst->upgrade(@args);
	if ($ret) {
	    $inst->sendmsg("OK");
	}
	else {
	    $inst->sendmsg("NOK");
	}
	1;
    },
    pkginit	=> sub {
	my ($inst, $cmd, @args) = @_;
	my $pkg = shift @args;
	my $tmpdir = $inst->config_get("tempdir");
	unless ($tmpdir and -w $tmpdir) {
	    $inst->sendmsg(encode_record(
		'NOK', "Backend tempdir '$tmpdir' not writeable"
	    ));
	    return 1;
	}
	$tmpdir .= "/$pkg-$$";
	mkpath($tmpdir);
	$inst->{tmpdirs}{$pkg} = $tmpdir;
	$inst->sendmsg('OK');
	1;
    },
    pkgfini	=> sub {
	my ($inst, $cmd, @args) = @_;
	my $pkg = shift @args;
	my $path = $inst->{tmpdirs}{$pkg} or do {
	    $inst->sendmsg(
		encode_record('NOK', 'pkgfini() without pkginit()')
	    );
	    return 1;
	};
	rmtree($path);
	delete $inst->{tmpdirs}{$pkg};
	$inst->sendmsg('OK');
	1;
    },
    transmit	=> sub {
	my ($inst, $cmd, @args) = @_;
	my $pkg  = shift @args;
	my $tmpdir = $inst->{tmpdirs}{$pkg};
	my $file = shift @args;
	my $dir  = dirname($file);

	chdir($tmpdir);
	mkpath($dir);

	eval {
	    open(FILE, "> $file")	|| die "can't write $file: $!";
	    binmode(FILE)		|| die "can't binmode $file: $!";
	};
	if ($@) {
	    $inst->sendmsg(encode_record('NOK', "$@"));
	    return 1;
	}
	$inst->sendmsg('OK');
	my $msg;
	while ($msg = $inst->recvmsg) {
	    my ($flag, $data) = decode_record($msg);
	    last if $flag eq 'EOT';
	    print FILE $data;
	}
	eval {
	    close(FILE)			|| die "can't close $file: $!";
	};
	if ($@) {
	    $inst->sendmsg(encode_record('NOK', "$@"));
	    return 1;
	}
	$inst->sendmsg('OK');
	chdir($inst->{cwd});
	1;
    },
    stop	=> sub {
	my ($inst, $cmd, @args) = @_;
	close(CLIENT);
	0;
    },
);

#=============================================================================
# Private functions!
#=============================================================================

use constant FIELD_SEP => "\001";
use constant FIELD_UNDEF => "\002";
my $EOL = "\015\012";

sub sendmsg {
    my $o = shift;
    my $fd = $o->{fd};
    my $msg = shift;
    local $\ = "$EOL.$EOL";
    print $fd $msg;
}

sub recvmsg {
    my $o = shift;
    my $fd = $o->{fd};
    local $/ = "$EOL.$EOL";
    my $msg = <$fd>;
    chomp $msg if $msg;
    return $msg;
}

sub qmeta {
    local $_ = shift || $_;
    s{([^A-Za-z0-9])}{sprintf('\x%.2X',ord($1))}eg;
    $_;
}

sub uqmeta {
    local $_ = shift || $_;
    eval qq{qq{$_}};
}

sub encode_record {
    my @fields = map { my $a = defined $_ ? $_ : FIELD_UNDEF; qmeta($a) } @_;
    join FIELD_SEP, @fields;
}

sub decode_record {
    my $t = shift || $_;
    return map { $_ = &uqmeta; $_ = undef if $_ eq FIELD_UNDEF; $_ }
	   split(FIELD_SEP, $t, -1);
}

