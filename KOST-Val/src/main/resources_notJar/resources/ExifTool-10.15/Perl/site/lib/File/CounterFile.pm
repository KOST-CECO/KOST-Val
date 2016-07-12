package File::CounterFile;

# $Id: CounterFile.pm,v 0.23 2004/01/23 08:37:18 gisle Exp $

require 5.004;

use strict;

use Carp   qw(croak);
use Symbol qw(gensym);
use Fcntl qw(LOCK_EX O_RDWR O_CREAT);

BEGIN {
    # older version of Fcntl did not know about SEEK_SET
    if ($] < 5.006) {
	*SEEK_SET = sub () { 0 };
    }
    else {
	Fcntl->import("SEEK_SET");
    }
}

use vars qw($VERSION $MAGIC $DEFAULT_INITIAL $DEFAULT_DIR);

sub Version { $VERSION; }
$VERSION = "1.04";

$MAGIC = "#COUNTER-1.0\n";             # first line in counter files
$DEFAULT_INITIAL = 0;                  # default initial counter value

 # default location for counter files
$DEFAULT_DIR = $ENV{TMPDIR} || "/usr/tmp";

# Experimental overloading.
use overload ('++'     => \&inc,
	      '--'     => \&dec,
	      '""'     => \&value,
	      fallback => 1,
             );


sub new
{
    my($class, $file, $initial) = @_;
    croak("No file specified\n") unless defined $file;

    $file = "$DEFAULT_DIR/$file" unless $file =~ /^[\.\/]/;
    $initial = $DEFAULT_INITIAL unless defined $initial;

    my $value;
    local($/, $\) = ("\n", undef);
    local *F;
    sysopen(F, $file, O_RDWR|O_CREAT) or croak("Can't open $file: $!");
    flock(F, LOCK_EX) or croak("Can't flock: $!");
    my $first_line = <F>;
    if (defined $first_line) {
	croak "Bad counter magic '$first_line' in $file" unless $first_line eq $MAGIC;
	$value = <F>;
	chomp($value);
    }
    else {
	seek(F, 0, SEEK_SET);
	print F $MAGIC;
	print F "$initial\n";
	$value = $initial;
    }
    close(F) || croak("Can't close $file: $!");

    bless { file    => $file,  # the filename for the counter
	   'value'  => $value, # the current value
	    updated => 0,      # flag indicating if value has changed
	    # handle => XXX,   # file handle symbol. Only present when locked
	  };
}


sub locked
{
    exists shift->{handle};
}


sub lock
{
    my($self) = @_;
    $self->unlock if $self->locked;

    my $fh = gensym();
    my $file = $self->{file};

    open($fh, "+<$file") or croak "Can't open $file: $!";
    flock($fh, LOCK_EX) or croak "Can't flock: $!";  # 2 = exlusive lock

    local($/) = "\n";
    my $magic = <$fh>;
    if ($magic ne $MAGIC) {
	$self->unlock;
	croak("Bad counter magic '$magic' in $file");
    }
    chomp($self->{'value'} = <$fh>);

    $self->{handle}  = $fh;
    $self->{updated} = 0;
    $self;
}


sub unlock
{
    my($self) = @_;
    return unless $self->locked;

    my $fh = $self->{handle};

    if ($self->{updated}) {
	# write back new value
	local($\) = undef;
	seek($fh, 0, SEEK_SET) or croak "Can't seek to beginning: $!";
	print $fh $MAGIC;
	print $fh "$self->{'value'}\n";
    }

    close($fh) or warn "Can't close: $!";
    delete $self->{handle};
    $self;
}


sub inc
{
    my($self) = @_;

    if ($self->locked) {
	$self->{'value'}++;
	$self->{updated} = 1;
    } else {
	$self->lock;
	$self->{'value'}++;
	$self->{updated} = 1;
	$self->unlock;
    }
    $self->{'value'}; # return value
}


sub dec
{
    my($self) = @_;

    if ($self->locked) {
	unless ($self->{'value'} =~ /^\d+$/) {
	    $self->unlock;
	    croak "Autodecrement is not magical in perl";
	}
	$self->{'value'}--;
	$self->{updated} = 1;
    }
    else {
	$self->lock;
	unless ($self->{'value'} =~ /^\d+$/) {
	    $self->unlock;
	    croak "Autodecrement is not magical in perl";
	}
	$self->{'value'}--;
	$self->{updated} = 1;
	$self->unlock;
    }
    $self->{'value'}; # return value
}


sub value
{
    my($self) = @_;
    my $value;
    if ($self->locked) {
	$value = $self->{'value'};
    }
    else {
	$self->lock;
	$value = $self->{'value'};
	$self->unlock;
    }
    $value;
}


sub DESTROY
{
    my $self = shift;
    $self->unlock;
}

1;

__END__

=head1 NAME

File::CounterFile - Persistent counter class

=head1 SYNOPSIS

 use File::CounterFile;
 $c = File::CounterFile->new("COUNTER", "aa00");

 $id = $c->inc;
 open(F, ">F$id");

=head1 DESCRIPTION

This module implements a persistent counter class.  Each counter is
represented by a separate file in the file system.  File locking is
applied, so multiple processes can attempt to access a counter
simultaneously without risk of counter destruction.

You give the file name as the first parameter to the object
constructor (C<new>).  The file is created if it does not exist.

If the file name does not start with "/" or ".", then it is
interpreted as a file relative to C<$File::CounterFile::DEFAULT_DIR>.
The default value for this variable is initialized from the
environment variable C<TMPDIR>, or F</usr/tmp> if no environment
variable is defined.  You may want to assign a different value to this
variable before creating counters.

If you pass a second parameter to the constructor, it sets the
initial value for a new counter.  This parameter only takes effect
when the file is created (i.e. it does not exist before the call).

When you call the C<inc()> method, you increment the counter value by
one. When you call C<dec()>, the counter value is decremented.  In both
cases the new value is returned.  The C<dec()> method only works for
numerical counters (digits only).

You can peek at the value of the counter (without incrementing it) by
using the C<value()> method.

The counter can be locked and unlocked with the C<lock()> and
C<unlock()> methods.  Incrementing and value retrieval are faster when
the counter is locked, because we do not have to update the counter
file all the time.  You can query whether the counter is locked with
the C<locked()> method.

There is also an operator overloading interface to the
File::CounterFile object.  This means that you can use the C<++>
operator for incrementing and the C<--> operator for decrementing the counter,
and you can interpolate counters directly into strings.

=head1 COPYRIGHT

Copyright (c) 1995-1998,2002,2003 Gisle Aas. All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=head1 AUTHOR

Gisle Aas <gisle@aas.no>

=cut
