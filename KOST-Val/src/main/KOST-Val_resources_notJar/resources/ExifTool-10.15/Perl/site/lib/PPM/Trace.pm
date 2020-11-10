package PPM::Trace;

use strict;
use Exporter;

our $VERSION = '3.00';
our @EXPORT_OK = qw(trace);
our @ISA = qw(Exporter);

my $trace_fh;		# the filehandle
my $trace_level = 0;

sub trace_init {
    my $file = shift;
    $trace_level = shift;
    open ($trace_fh, '>>', $file) or die "Can't append $file: $!";
    select((select($trace_fh), $| = 1)[0]);
    my $t = localtime;
    trace($trace_level, <<END);
$0: trace started $t.
END
}

sub trace_fini {
    close ($trace_fh) or die "Can't close trace file: $!"
      if $trace_fh;
}

sub trace_level {
    $trace_level = $_[0] if defined $_[0];
    $trace_level;
}

sub trace {
    my $lvl = shift;
    if ($trace_level and $trace_fh and $trace_level >= $lvl) {
	print $trace_fh @_;
    }
    1;
}

END {
    trace_fini();
}

1;
