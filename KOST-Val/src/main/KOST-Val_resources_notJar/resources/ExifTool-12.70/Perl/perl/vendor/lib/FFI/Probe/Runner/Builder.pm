package FFI::Probe::Runner::Builder;

use strict;
use warnings;
use 5.008004;
use Config;
use Capture::Tiny qw( capture_merged );
use Text::ParseWords ();
use FFI::Build::Platform;

# ABSTRACT: Probe runner builder for FFI
our $VERSION = '2.08'; # VERSION


sub new
{
  my($class, %args) = @_;

  $args{dir} ||= 'blib/lib/auto/share/dist/FFI-Platypus/probe';

  my $platform = FFI::Build::Platform->new;

  my $self = bless {
    dir      => $args{dir},
    platform => $platform,
    # we don't use the platform ccflags, etc because they are geared
    # for building dynamic libs not exes
    cc       => [$platform->shellwords($Config{cc})],
    ld       => [$platform->shellwords($Config{ld})],
    ccflags  => [$platform->shellwords($Config{ccflags})],
    optimize => [$platform->shellwords($Config{optimize})],
    ldflags  => [$platform->shellwords($Config{ldflags})],
    libs     =>
      $^O eq 'MSWin32'
        ? [[]]
        : [['-ldl'], [], map { [$_] } grep !/^-ldl/, $platform->shellwords($Config{perllibs})],
  }, $class;

  $self;
}


sub dir
{
  my($self, @subdirs) = @_;
  my $dir = $self->{dir};

  if(@subdirs)
  {
    require File::Spec;
    $dir = File::Spec->catdir($dir, @subdirs);
  }

  unless(-d $dir)
  {
    require File::Path;
    File::Path::mkpath($dir, 0, oct(755));
  }
  $dir;
}


sub cc       { shift->{cc}       }
sub ccflags  { shift->{ccflags}  }
sub optimize { shift->{optimize} }
sub ld       { shift->{ld}       }
sub ldflags  { shift->{ldflags}  }
sub libs     { shift->{libs}     }


sub file
{
  my($self, @sub) = @_;
  @sub >= 1 or die 'usage: $builder->file([@subdirs], $filename)';
  my $filename  = pop @sub;
  require File::Spec;
  File::Spec->catfile($self->dir(@sub), $filename);
}

my $source;


sub exe
{
  my($self) =  @_;
  my $xfn = $self->file('bin', "dlrun$Config{exe_ext}");
}


sub source
{
  unless($source)
  {
    local $/;
    $source = <DATA>;
  }

  $source;
}


our $VERBOSE = !!$ENV{V};

sub extract
{
  my($self) = @_;

  # the source src/dlrun.c
  {
    print "XX src/dlrun.c\n" unless $VERBOSE;
    my $fh;
    my $fn = $self->file('src', 'dlrun.c');
    my $source = $self->source;
    open $fh, '>', $fn or die "unable to write $fn $!";
    print $fh $source;
    close $fh;
  }

  # the bin directory bin
  {
    print "XX bin/\n" unless $VERBOSE;
    $self->dir('bin');
  }

}


sub run
{
  my($self, $type, @cmd) = @_;
  @cmd = map { ref $_ ? @$_ : $_ } @cmd;
  my($out, $ret) = capture_merged {
    $self->{platform}->run(@cmd);
  };
  if($ret)
  {
    print STDERR $out;
    die "$type failed";
  }
  print $out if $VERBOSE;
  $out;
}


sub run_list
{
  my($self, $type, @commands) = @_;

  my $log = '';

  foreach my $cmd (@commands)
  {
    my($out, $ret) = capture_merged {
      $self->{platform}->run(@$cmd);
    };
    if($VERBOSE)
    {
      print $out;
    }
    else
    {
      $log .= $out;
    }
    return if !$ret;
  }

  print $log;
  die "$type failed";
}


sub build
{
  my($self) = @_;
  $self->extract;

  # this should really be done in `new` but the build
  # scripts for FFI-Platypus edit the ldfalgs from there
  # so.  Also this may actually belong in FFI::Build::Platform
  # which would resolve the problem.
  if($^O eq 'MSWin32' && $Config{ccname} eq 'cl')
  {
    $self->{ldflags} = [
      grep !/^-nodefaultlib$/i,
      @{ $self->{ldflags} }
    ];
  }

  my $cfn = $self->file('src', 'dlrun.c');
  my $ofn = $self->file('src', "dlrun$Config{obj_ext}");
  my $xfn = $self->exe;

  # compile
  print "CC src/dlrun.c\n" unless $VERBOSE;
  $self->run(
    compile =>
      $self->cc,
      $self->ccflags,
      $self->optimize,
      '-c',
      $self->{platform}->flag_object_output($ofn),
      $cfn,
  );

  # link
  print "LD src/dlrun$Config{obj_ext}\n" unless $VERBOSE;
  $self->run_list(link =>
    map { [
      $self->ld,
      $self->ldflags,
      $self->{platform}->flag_exe_output($xfn),
      $ofn,
      @$_
    ] } @{ $self->libs },
  );

  ## FIXME
  if($^O eq 'MSWin32' && $Config{ccname} eq 'cl')
  {
    if(-f 'dlrun.exe' && ! -f $xfn)
    {
      rename 'dlrun.exe', $xfn;
    }
  }

  # verify
  print "VV bin/dlrun$Config{exe_ext}\n" unless $VERBOSE;
  my $out = $self->run(verify => $xfn, 'verify', 'self');
  if($out !~ /dlrun verify self ok/)
  {
    print $out;
    die "verify failed string match";
  }

  # remove object
  print "UN src/dlrun$Config{obj_ext}\n" unless $VERBOSE;
  unlink $ofn;

  $xfn;
}

1;

=pod

=encoding UTF-8

=head1 NAME

FFI::Probe::Runner::Builder - Probe runner builder for FFI

=head1 VERSION

version 2.08

=head1 SYNOPSIS

 use FFI::Probe::Runner::Builder;
 my $builder = FFI::Probe::Runner::Builder->new
   dir => "/foo/bar",
 );
 my $exe = $builder->build;

=head1 DESCRIPTION

This is a builder class for the FFI probe runner.  It is mostly only of
interest if you are hacking on L<FFI::Platypus> itself.

The interface may and will change over time without notice.  Use in
external dependencies at your own peril.

=head1 CONSTRUCTORS

=head2 new

 my $builder = FFI::Probe::Runner::Builder->new(%args);

Create a new instance.

=over 4

=item dir

The root directory for where to place the probe runner files.
Will be created if it doesn't already exist.  The default
makes sense for when L<FFI::Platypus> is being built.

=back

=head1 METHODS

=head2 dir

 my $dir = $builder->dir(@subdirs);

Returns a subdirectory from the builder root.  Directory
will be created if it doesn't already exist.

=head2 cc

 my @cc = @{ $builder->cc };

The C compiler to use.  Returned as an array reference so that it may be modified.

=head2 ccflags

 my @ccflags = @{ $builder->ccflags };

The C compiler flags to use.  Returned as an array reference so that it may be modified.

=head2 optimize

The C optimize flags to use.  Returned as an array reference so that it may be modified.

=head2 ld

 my @ld = @{ $builder->ld };

The linker to use.  Returned as an array reference so that it may be modified.

=head2 ldflags

 my @ldflags = @{ $builder->ldflags };

The linker flags to use.  Returned as an array reference so that it may be modified.

=head2 libs

 my @libs = @{ $builder->libs };

The library flags to use.  Returned as an array reference so that it may be modified.

=head2 file

 my $file = $builder->file(@subdirs, $filename);

Returns a file in a subdirectory from the builder root.
Directory will be created if it doesn't already exist.
File will not be created.

=head2 exe

 my $exe = $builder->exe;

The name of the executable, once it is built.

=head2 source

 my $source = $builder->source;

The C source for the probe runner.

=head2 extract

 $builder->extract;

Extract the source for the probe runner.

=head2 run

 $builder->run($type, @command);

Runs the given command.  Dies if the command fails.

=head2 run_list

 $builder->run($type, \@command, \@command, ...);

Runs the given commands in order until one succeeds.
Dies if they all fail.

=head2 build

 my $exe = $builder->build;

Builds the probe runner.  Returns the path to the executable.

=head1 AUTHOR

Author: Graham Ollis E<lt>plicease@cpan.orgE<gt>

Contributors:

Bakkiaraj Murugesan (bakkiaraj)

Dylan Cali (calid)

pipcet

Zaki Mughal (zmughal)

Fitz Elliott (felliott)

Vickenty Fesunov (vyf)

Gregor Herrmann (gregoa)

Shlomi Fish (shlomif)

Damyan Ivanov

Ilya Pavlov (Ilya33)

Petr Písař (ppisar)

Mohammad S Anwar (MANWAR)

Håkon Hægland (hakonhagland, HAKONH)

Meredith (merrilymeredith, MHOWARD)

Diab Jerius (DJERIUS)

Eric Brine (IKEGAMI)

szTheory

José Joaquín Atria (JJATRIA)

Pete Houston (openstrike, HOUSTON)

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015-2022 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut

__DATA__

#if defined __CYGWIN__
#include <dlfcn.h>
#elif defined _WIN32
#include <windows.h>
#else
#include <dlfcn.h>
#endif
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#if defined __CYGWIN__
typedef void * dlib;
#elif defined _WIN32

#define RTLD_LAZY 0
typedef HMODULE dlib;

dlib
dlopen(const char *filename, int flags)
{
  (void)flags;
  return LoadLibrary(filename);
}

void *
dlsym(dlib handle, const char *symbol_name)
{
  return GetProcAddress(handle, symbol_name);
}

int
dlclose(dlib handle)
{
  FreeLibrary(handle);
  return 0;
}

const char *
dlerror()
{
  return "an error";
}

#else
typedef void * dlib;
#endif

int
main(int argc, char **argv)
{
  char *filename;
  int flags;
  int (*dlmain)(int, char **);
  char **dlargv;
  dlib handle;
  int n;
  int ret;

  if(argc < 3)
  {
    fprintf(stderr, "usage: %s dlfilename dlflags [ ... ]\n", argv[0]);
    return 1;
  }

  if(!strcmp(argv[1], "verify") && !strcmp(argv[2], "self"))
  {
    printf("dlrun verify self ok\n");
    return 0;
  }

#if defined WIN32
  SetErrorMode(SetErrorMode(0) | SEM_NOGPFAULTERRORBOX);
#endif

  dlargv = malloc(sizeof(char*)*(argc-2));
  dlargv[0] = argv[0];
  filename = argv[1];
  flags = !strcmp(argv[2], "-") ? RTLD_LAZY : atoi(argv[2]);
  for(n=3; n<argc; n++)
    dlargv[n-2] = argv[n];

  handle = dlopen(filename, flags);

  if(handle == NULL)
  {
    fprintf(stderr, "error loading %s (%d|%s): %s", filename, flags, argv[2], dlerror());
    return 1;
  }

  dlmain = dlsym(handle, "dlmain");

  if(dlmain == NULL)
  {
    fprintf(stderr, "no dlmain symbol");
    return 1;
  }

  ret = dlmain(argc-2, dlargv);

  dlclose(handle);

  return ret;
}
