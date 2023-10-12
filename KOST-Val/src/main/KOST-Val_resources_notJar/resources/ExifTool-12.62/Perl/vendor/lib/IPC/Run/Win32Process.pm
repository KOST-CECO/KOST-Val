package IPC::Run::Win32Process;

=pod

=head1 NAME

IPC::Run::Win32Process -- deliver nonstandard command lines via IPC::Run.

=head1 SYNOPSIS

   use File::Spec ();
   use IPC::Run qw(run);
   use IPC::Run::Win32Process ();
   use Win32 ();

   $find_exe = File::Spec->catfile(Win32::GetFolderPath(Win32::CSIDL_SYSTEM),
                                   'find.exe');
   run(IPC::Run::Win32Process->new($ENV{COMSPEC}, q{cmd.exe /c echo ""}),
       '|', IPC::Run::Win32Process->new($find_exe, q{find_exe """"""}),
       '>', \$out);

=head1 DESCRIPTION

This class facilitates executing Windows programs that don't use L<standard
command line parsing
rules|https://docs.microsoft.com/en-us/cpp/cpp/main-function-command-line-args#parsing-c-command-line-arguments>.
Notable programs having nonstandard rules include F<cmd.exe>, F<cscript.exe>,
and Cygwin programs called from non-Cygwin programs.  IPC::Run will use the two
strings, verbatim, as the lpApplicationName and lpCommandLine arguments of
CreateProcessA().  This furnishes unfiltered control over the child process
command line.

=head1 FUNCTIONS & METHODS

=over

=cut

use strict;
use warnings;
use Carp;

use overload '""' => sub {
    my ($self) = @_;
    return join(
        '',
        'IPC::Run::Win32Process(',
        $self->{lpApplicationName},
        ', ',
        $self->{lpCommandLine},
        ')'
    );
};

use vars qw{$VERSION};

BEGIN {
    $VERSION = '20220807.0';
}

=item new

   IPC::Run::Win32Process->new( $lpApplicationName, $lpCommandLine );
   IPC::Run::Win32Process->new( $ENV{COMSPEC}, q{cmd.exe /c echo ""} );

Constructor.

=back

=cut

sub new {
    my ( $class, $lpApplicationName, $lpCommandLine ) = @_;
    $class = ref $class || $class;

    croak "missing lpApplicationName" if !defined $lpApplicationName;
    croak "missing lpCommandLine"     if !defined $lpCommandLine;

    my IPC::Run::Win32Process $self = bless {}, $class;
    $self->{lpApplicationName} = $lpApplicationName;
    $self->{lpCommandLine}     = $lpCommandLine;

    return $self;
}

1;
