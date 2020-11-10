package PPM::Installer::Remote;
@PPM::Installer::Remote::ISA = qw(PPM::Installer);

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
	host => $targ->{host},
	type => $targ->{type},
	port => $targ->{port},
    }, $class;
    return $inst;
}

sub init {
    my $inst = shift;
    my $ok = $inst->connect_to($inst->{host}, $inst->{port});
    die $ok->msg unless $ok->ok;
    $ok;
}

sub ckeys {
    my $o = shift;
    ($o->SUPER::ckeys,
     qw(host),
    );
}

sub host {
    my $o = shift;
    $o->{host};
}

sub pkginit {
    my $o = shift;
    my $package = shift;
    $o->sendmsg($o->encode_record('PKGINIT', $package));
    my @msg = $o->decode_record($o->recvmsg);
    return Error($msg[1]) unless $msg[0] eq 'OK';
    Ok();
}

sub pkgfini {
    my $o = shift;
    my $package = shift;
    $o->sendmsg($o->encode_record('PKGFINI', $package));
    my @msg = $o->decode_record($o->recvmsg);
    return Error($msg[1]) unless $msg[0] eq 'OK';
    Ok();
}

sub transmit {
    my $o = shift;
    my $package = shift;
    my $file = shift;

    open (my $FILE, $file) or return Error("can't open $file: $!");
    binmode($FILE);

    # Let the other side know we're coming:
    $o->sendmsg($o->encode_record('TRANSMIT', $package, $file));
    my @msg = $o->decode_record($o->recvmsg);
    return Error($msg[1]) 
      unless $msg[0] eq 'OK';

    # The size of individual packets is up to negotiation, but it makes some
    # sense to pick a power of 2. I'm using 16384 bytes, or 16KB.
    my $n;
    my $dat;
    while ($n = read($FILE, $dat, 16384)) {
	$dat = $o->encode_record('DATA', $dat);
	$o->sendmsg($dat);
    }

    # Tell them we're done:
    $o->sendmsg('EOT');
    
    @msg = $o->decode_record($o->recvmsg);
    return Error($msg[1]) unless $msg[0] eq 'OK';
    Ok();
}

1;
