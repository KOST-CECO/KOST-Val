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

use Mojo::Server::Hypnotoad;
use Mojo::Util qw(extract_usage getopt);

getopt
  'f|foreground' => \$ENV{HYPNOTOAD_FOREGROUND},
  'h|help'       => \my $help,
  's|stop'       => \$ENV{HYPNOTOAD_STOP},
  't|test'       => \$ENV{HYPNOTOAD_TEST};

die extract_usage if $help || !(my $app = shift || $ENV{HYPNOTOAD_APP});
Mojo::Server::Hypnotoad->new->run($app);

=encoding utf8

=head1 NAME

hypnotoad - Hypnotoad HTTP and WebSocket server

=head1 SYNOPSIS

  Usage: hypnotoad [OPTIONS] [APPLICATION]

    hypnotoad ./script/my_app
    hypnotoad ./myapp.pl
    hypnotoad -f ./myapp.pl

  Options:
    -f, --foreground   Keep manager process in foreground
    -h, --help         Show this message
    -s, --stop         Stop server gracefully
    -t, --test         Test application and exit

=head1 DESCRIPTION

Start L<Mojolicious> and L<Mojolicious::Lite> applications with the L<Hypnotoad|Mojo::Server::Hypnotoad> web server.

=head1 SEE ALSO

L<Mojolicious>, L<Mojolicious::Guides>, L<https://mojolicious.org>.

=cut
__END__
:endofperl
@set "ErrorLevel=" & @goto _undefined_label_ 2>NUL || @"%COMSPEC%" /d/c @exit %ErrorLevel%
