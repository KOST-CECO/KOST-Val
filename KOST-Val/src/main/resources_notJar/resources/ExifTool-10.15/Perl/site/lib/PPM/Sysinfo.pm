package PPM::Sysinfo;
$VERSION = '3.00';

use strict;
use Data::Dumper;
use PPM::Config;
use Digest::MD5 qw(md5_hex);

# The hostname of the currently running machine
sub hostname {
    my $hostname;
    if ($^O eq 'MSWin32') {
	$hostname = (gethostbyname('localhost'))[0];
    }
    elsif (eval { require Net::Domain }) {
	$hostname = Net::Domain::hostfqdn();
    }
    elsif (eval { require Sys::Hostname }) {
	$hostname = Sys::Hostname::hostname();
    }
    else {
	$hostname = "localhost";
    }
    $hostname;
}

# The IP address of the currently-running machine
sub ip_addr {
    my @matches;
    my $ip_addr = '0.0.0.0';
    my $ip_raw  = $^O eq 'MSWin32' ? `ipconfig` : `/sbin/ifconfig -a`;
    if ($^O eq 'MSWin32') {
	@matches = ($ip_raw =~ /((?:\d+\.){3}\d+)/g);
    }
    elsif ($^O eq 'solaris') {
	@matches = ($ip_raw =~ /inet ((?:\d+\.){3}\d+)/ig);
    }
    else {
	@matches = ($ip_raw =~ /inet addr:((?:\d+\.){3}\d+)/ig);
    }
    for (@matches) {
	next if $_ eq '0.0.0.0';
	$ip_addr = $_;
	last;
    }
    $ip_addr;
}

# The login id of the current user
sub username {
    return getlogin;
}

# The install key (generates one if it doesn't exist)
sub inst_key {
    my $f = PPM::Config::load_config_file('instkey', 'ro');
    unless (defined $f->{DATA}{inst_id}) {
	$f = PPM::Config::load_config_file('instkey', 'rw');
	$f->{DATA} = generate_inst_key();
	goto &inst_key;
    }
    $f->{DATA}{inst_id};
}

sub generate_inst_key {
    my $DATA = {
	hostname => hostname(),
	os       => $^O,
	date     => scalar localtime,
	randkey	 => rand,
    };
    $DATA->{inst_id} = md5_hex(
	join':', @$DATA{qw(randkey hostname os date)}
    );
    $DATA;
}

sub generate_user_key {
    my $f = PPM::Config::load_config_file('instkey', 'ro');
    my $user = username();
    my $host = hostname();
    my $time = $f->{DATA}{date};
    Digest::MD5::md5_hex("$user:$host:$time");
}

1;

__DATA__
