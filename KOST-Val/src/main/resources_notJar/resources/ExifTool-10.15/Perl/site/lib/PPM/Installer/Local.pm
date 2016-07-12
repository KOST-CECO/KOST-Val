package PPM::Installer::Local;
@PPM::Installer::Local::ISA = qw(PPM::Installer);

use strict;
use Data::Dumper;
use PPM::PPD;
use PPM::Result qw(Ok Warning Error List);

sub new {
    my $class = shift;
    my $targ = shift;
    my $name = shift;
    my $inst = bless {
	name => $name,
	path => $targ->{path},
	type => $targ->{type},
	port => $targ->{port},
    }, $class;
    return $inst;
}

sub init {
    my $inst = shift;

    # First, check if the process is already running. If so, use that.
    my $ok = $inst->connect_to('localhost', $inst->{port});

    # If it isn't, try to launch it ourselves, _then_ connect. Because
    # spawning a process is fairly slow, we have to try to connect for a
    # little while, to give the child time to set up the server.
    do {
	$inst->spawn;
	for (1 .. 10) {
	    $ok = $inst->connect_to('localhost', $inst->{port});
	    last if $ok->ok;
	    # let the CPU swap out to the child process.
	    defined &Win32::Sleep ? Win32::Sleep(250) : sleep 1;
	}
	die $ok->msg unless $ok->ok;
    } unless $ok->ok;
    $ok;
}

sub fini {
    my $o = shift;
    $o->sendmsg('STOP');
}

sub spawn {
    my $inst = shift;

    $ENV{PPM_PORT} = $inst->port;

    # Note: $inst->{path} must be an executable, but Windows
    # does not support an executable permission bit, checking
    # for .bat or .exe files instead. This isn't suitable
    # for scripts, so we only make this check on non-Windows boxes
    die "$0: error: '$inst->{path}' is not executable"
      unless ((-x $inst->{path}) or ($^O eq 'MSWin32'));
    if ($^O eq 'MSWin32') {
	$inst->{path} =~ s,/,\\,g;

	# We don't want the child to inherit our STDOUT because that
	# would break things like `ppm query * > modlist`.
	# XXX Unfortunately this also silences all output from
	# XXX post-install scripts. :(  Disabled for now...
	#open(MYSTDOUT, ">&STDOUT") or die $!;
	#close(STDOUT);
	#open(STDOUT, ">nul") or die $!;

	$inst->{pid} = system(1, $inst->{path});

	#open(STDOUT, ">&MYSTDOUT") or die $!;
	#close(MYSTDOUT);
    }
    else {
	$inst->{pid} = fork();
	if (defined $inst->{pid} and $inst->{pid} == 0) {
	    exec $inst->{path};
	    exit 1;
	}
    }
}

sub ckeys {
    my $o = shift;
    ($o->SUPER::ckeys,
     qw(path),
    );
}

sub path {
    my $o = shift;
    $o->{path};
}

1;
