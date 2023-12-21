@rem = '--*-Perl-*--
@set "ErrorLevel="
@if "%OS%" == "Windows_NT" @goto WinNT
@perl -x -S "%0" %1 %2 %3 %4 %5 %6 %7 %8 %9
@set ErrorLevel=%ErrorLevel%
@goto endofperl
:WinNT
@perl -x -S %0 %*
@set ErrorLevel=%ErrorLevel%
@if NOT "%COMSPEC%" == "%SystemRoot%\system32\cmd.exe" @goto endofperl
@if %ErrorLevel% == 9009 @echo You do not have Perl in your PATH.
@goto endofperl
@rem ';
#!perl
#line 30
use Mojo::Base -strict;

use Mojo::Server::Morbo;
use Mojo::Util qw(extract_usage getopt);

getopt
  'b|backend=s' => \$ENV{MOJO_MORBO_BACKEND},
  'h|help'      => \my $help,
  'l|listen=s'  => \my @listen,
  'm|mode=s'    => \$ENV{MOJO_MODE},
  'v|verbose'   => \my $verbose,
  'w|watch=s'   => \my @watch;

die extract_usage if $help || !(my $app = shift);
my $morbo = Mojo::Server::Morbo->new(silent => !$verbose);
$morbo->daemon->listen(\@listen) if @listen;
$morbo->backend->watch(\@watch)  if @watch;
$morbo->run($app);

=encoding utf8

=head1 NAME

morbo - Morbo HTTP and WebSocket development server

=head1 SYNOPSIS

  Usage: morbo [OPTIONS] [APPLICATION]

    morbo ./script/my_app
    morbo ./myapp.pl
    morbo -m production -l https://*:443 -l http://[::]:3000 ./myapp.pl
    morbo -l 'https://*:443?cert=./server.crt&key=./server.key' ./myapp.pl
    morbo -w /usr/local/lib -w public -w myapp.conf ./myapp.pl

  Options:
    -b, --backend <name>           Morbo backend to use for reloading, defaults
                                   to "Poll"
    -h, --help                     Show this message
    -l, --listen <location>        One or more locations you want to listen on,
                                   defaults to the value of MOJO_LISTEN or
                                   "http://*:3000"
    -m, --mode <name>              Operating mode for your application,
                                   defaults to the value of
                                   MOJO_MODE/PLACK_ENV or "development"
    -v, --verbose                  Print details about what files changed to
                                   STDOUT
    -w, --watch <directory/file>   One or more directories and files to watch
                                   for changes, defaults to the application
                                   script as well as the "lib" and "templates"
                                   directories in the current working
                                   directory

=head1 DESCRIPTION

Start L<Mojolicious> and L<Mojolicious::Lite> applications with the L<Mojo::Server::Morbo> web server.

=head1 SEE ALSO

L<Mojolicious>, L<Mojolicious::Guides>, L<https://mojolicious.org>.

=cut
__END__
:endofperl
@set "ErrorLevel=" & @goto _undefined_label_ 2>NUL || @"%COMSPEC%" /d/c @exit %ErrorLevel%
