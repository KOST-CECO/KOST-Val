package FFI::Probe;

use strict;
use warnings;
use 5.008004;
use File::Basename qw( dirname );
use Data::Dumper ();
use FFI::Probe::Runner;
use FFI::Build;
use FFI::Build::File::C;
use Capture::Tiny qw( capture_merged capture );
use FFI::Temp;

# ABSTRACT: System detection and probing for FFI extensions.
our $VERSION = '1.34'; # VERSION


sub new
{
  my($class, %args) = @_;

  $args{log}           ||= "ffi-probe.log";
  $args{data_filename} ||= "ffi-probe.pl";

  unless(ref $args{log})
  {
    my $fn = $args{log};
    my $fh;
    open $fh, '>>', $fn;
    $args{log} = $fh;
  }

  my $data;

  if(-r $args{data_filename})
  {
    my $fn = $args{data_filename};
    unless($data = do $fn)
    {
      die "couldn't parse configuration $fn $@" if $@;
      die "couldn't do $fn $!"                  if $!;
      die "bad or missing config file $fn";
    }
  }

  $data ||= {};

  my $self = bless {
    headers => [],
    log           => $args{log},
    data_filename => $args{data_filename},
    data          => $data,
    dir           => FFI::Temp->newdir( TEMPLATE => 'ffi-probe-XXXXXX' ),
    counter       => 0,
    runner        => $args{runner},
    alien         => $args{alien} || [],
    cflags        => $args{cflags},
    libs          => $args{libs},
  }, $class;

  $self;
}

sub _runner
{
  my($self) = @_;
  $self->{runner} ||= FFI::Probe::Runner->new;
}


sub check_header
{
  my($self, $header) = @_;

  return if defined $self->{data}->{header}->{$header};

  my $code = '';
  $code .= "#include <$_>\n" for @{ $self->{headers} }, $header;

  my $build = FFI::Build->new("hcheck@{[ ++$self->{counter} ]}",
    verbose => 2,
    dir     => $self->{dir},
    alien   => $self->{alien},
    cflags  => $self->{cflags},
    libs    => $self->{libs},
  );
  my $file = FFI::Build::File::C->new(
    \$code,
    dir => $self->{dir},
    build => $build,
  );
  my($out, $o) = capture_merged {
    eval { $file->build_item };
  };
  $self->log_code($code);
  $self->log($out);
  if($o)
  {
    $self->set('header', $header => 1);
    push @{ $self->{headers} }, $header;
    return 1;
  }
  else
  {
    $self->set('header', $header => 0);
    return;
  }
}


sub check_cpp
{
  my($self, $code) = @_;

  my $build = FFI::Build->new("hcheck@{[ ++$self->{counter} ]}",
    verbose => 2,
    dir     => $self->{dir},
    alien   => $self->{alien},
    cflags  => $self->{cflags},
    libs    => $self->{libs},
  );
  my $file = FFI::Build::File::C->new(
    \$code,
    dir => $self->{dir},
    build => $build,
  );
  my($out, $i) = capture_merged {
    eval { $file->build_item_cpp };
  };
  $self->log_code($code);
  $self->log($out);

  if($i && -f $i->path)
  {
    return $i->slurp;
  }
  else
  {
    return;
  }
}


sub check_eval
{
  my($self, %args) = @_;

  my $code = $args{_template} || $self->template;

  my $headers = join "", map { "#include <$_>\n" } (@{ $self->{headers} }, @{ $args{headers} || [] });
  my @decl    = @{ $args{decl} || [] };
  my @stmt    = @{ $args{stmt} || [] };
  my %eval    = %{ $args{eval} || {} };

  $code =~ s/##HEADERS##/$headers/;
  $code =~ s/##DECL##/join "\n", @decl/e;
  $code =~ s/##STMT##/join "\n", @stmt/e;

  my $eval = '';
  my $i=0;
  my %map;
  foreach my $key (sort keys %eval)
  {
    $i++;
    $map{$key} = "eval$i";
    my($format,$expression) = @{ $eval{$key} };
    $eval .= "  printf(\"eval$i=<<<$format>>>\\n\", $expression);\n";
  }

  $code =~ s/##EVAL##/$eval/;

  my $build = FFI::Build->new("eval@{[ ++$self->{counter} ]}",
    verbose => 2,
    dir     => $self->{dir},
    alien   => $self->{alien},
    cflags  => $self->{cflags},
    libs    => $self->{libs},
    export  => ['dlmain'],
  );
  $build->source(
    FFI::Build::File::C->new(
      \$code,
      dir => $self->{dir},
      build => $build,
    ),
  );

  my $lib = do {
    my($out, $lib, $error) = capture_merged {
      my $lib = eval {
        $build->build;
      };
      ($lib, $@);
    };

    $self->log_code($code);
    $self->log("[build]");
    $self->log($out);
    if($error)
    {
      $self->log("exception: $error");
      return;
    }
    elsif(!$lib)
    {
      $self->log("failed");
      return;
    }
    $lib;
  };

  my $result = $self->_runner->run($lib->path);

  $self->log("[stdout]");
  $self->log($result->stdout);
  $self->log("[stderr]");
  $self->log($result->stderr);
  $self->log("rv  = @{[ $result->rv ]}");
  $self->log("sig = @{[ $result->signal ]}") if $result->signal;

  if($result->pass)
  {
    foreach my $key (sort keys %eval)
    {
      my $eval = $map{$key};
      if($result->stdout =~ /$eval=<<<(.*?)>>>/)
      {
        my $value = $1;
        my @key = split /\./, $key;
        $self->set(@key, $value);
      }
    }
    return 1;
  }
  else
  {
    return;
  }
}


sub check
{
  my($self, $name, $code) = @_;
  if($self->check_eval(_template => $code))
  {
    $self->set('probe', $name, 1);
    return 1;
  }
  else
  {
    $self->set('probe', $name, 0);
    return;
  }
}


sub check_type_int
{
  my($self, $type) = @_;

  $self->check_header('stddef.h');

  my $ret = $self->check_eval(
    decl => [
      '#define signed(type)  (((type)-1) < 0) ? "signed" : "unsigned"',
      "struct align { char a; $type b; };",
    ],
    eval => {
      "type.$type.size"  => [ '%d' => "(int)sizeof($type)"             ],
      "type.$type.sign"  => [ '%s' => "signed($type)"                  ],
      "type.$type.align" => [ '%d' => '(int)offsetof(struct align, b)' ],
    },
  );

  return unless $ret;

  my $size = $self->data->{type}->{$type}->{size};
  my $sign = $self->data->{type}->{$type}->{sign};

  sprintf("%sint%d", $sign eq 'signed' ? 's' : 'u', $size*8);
}


sub check_type_enum
{
  my($self) = @_;

  $self->check_header('stddef.h');

  my $ret = $self->check_eval(
    decl => [
      '#define signed(type)  (((type)-1) < 0) ? "signed" : "unsigned"',
      "typedef enum { ONE, TWO } myenum;",
      "struct align { char a; myenum b; };",
    ],
    eval => {
      "type.enum.size"  => [ '%d' => '(int)sizeof(myenum)'            ],
      "type.enum.sign"  => [ '%s' => 'signed(myenum)'                 ],
      "type.enum.align" => [ '%d' => '(int)offsetof(struct align, b)' ],
    },
  );

  return unless $ret;

  my $size = $self->data->{type}->{enum}->{size};
  my $sign = $self->data->{type}->{enum}->{sign};

  sprintf("%sint%d", $sign eq 'signed' ? 's' : 'u', $size*8);
}


sub check_type_signed_enum
{
  my($self) = @_;

  $self->check_header('stddef.h');

  my $ret = $self->check_eval(
    decl => [
      '#define signed(type)  (((type)-1) < 0) ? "signed" : "unsigned"',
      "typedef enum { NEG = -1, ONE = 1, TWO = 2 } myenum;",
      "struct align { char a; myenum b; };",
    ],
    eval => {
      "type.senum.size"  => [ '%d' => '(int)sizeof(myenum)'            ],
      "type.senum.sign"  => [ '%s' => 'signed(myenum)'                 ],
      "type.senum.align" => [ '%d' => '(int)offsetof(struct align, b)' ],
    },
  );

  return unless $ret;

  my $size = $self->data->{type}->{senum}->{size};
  my $sign = $self->data->{type}->{senum}->{sign};

  sprintf("%sint%d", $sign eq 'signed' ? 's' : 'u', $size*8);
}


sub check_type_float
{
  my($self, $type) = @_;

  $self->check_header('stddef.h');

  my $ret = $self->check_eval(
    decl => [
      "struct align { char a; $type b; };",
    ],
    eval => {
      "type.$type.size"  => [ '%d' => "(int)sizeof($type)"             ],
      "type.$type.align" => [ '%d' => '(int)offsetof(struct align, b)' ],
    },
  );

  return unless $ret;

  my $size    = $self->data->{type}->{$type}->{size};
  my $complex = !!$type =~ /complex/;

  if($complex) {
    $size /= 2;
  }

  my $t;
  if($size == 4)
  { $t = 'float' }
  elsif($size == 8)
  { $t = 'double' }
  elsif($size > 9)
  { $t = 'longdouble' }

  $t = "complex_$t" if $complex;

  $t;
}


sub check_type_pointer
{
  my($self) = @_;

  $self->check_header('stddef.h');

  my $ret = $self->check_eval(
    decl => [
      "struct align { char a; void* b; };",
    ],
    eval => {
      "type.pointer.size"  => [ '%d' => '(int)sizeof(void *)'            ],
      "type.pointer.align" => [ '%d' => '(int)offsetof(struct align, b)' ],
    },
  );

  return unless $ret;
  'pointer';
}

sub _set
{
  my($data, $value, @key) = @_;
  my $key = shift @key;
  if(@key > 0)
  {
    _set($data->{$key} ||= {}, $value, @key);
  }
  else
  {
    $data->{$key} = $value;
  }
}


sub set
{
  my $self = shift;
  my $value = pop;
  my @key = @_;

  my $print_value = $value;
  if(ref $print_value)
  {
    my $d = Data::Dumper->new([$value], [qw($value)]);
    $d->Indent(0);
    $d->Terse(1);
    $print_value = $d->Dump;
  }

  my $key = join ".", map { /\./ ? "\"$_\"" : $_ } @key;
  print "PR $key=$print_value\n";
  $self->log("$key=$print_value");
  _set($self->{data}, $value, @key);
}


sub save
{
  my($self) = @_;

  my $dir = dirname($self->{data_filename});

  my $dd = Data::Dumper->new([$self->{data}],['x'])
    ->Indent(1)
    ->Terse(0)
    ->Purity(1)
    ->Sortkeys(1)
    ->Dump;

  mkpath( $dir, 0, oct(755) ) unless -d $dir;

  my $fh;
  open($fh, '>', $self->{data_filename}) || die "error writing @{[ $self->{data_filename} ]}";
  print $fh 'do { my ';
  print $fh $dd;
  print $fh '$x;}';
  close $fh;
}


sub data { shift->{data} }


sub log
{
  my($self, $string) = @_;
  my $fh = $self->{log};
  chomp $string;
  print $fh $string, "\n";
}


sub log_code
{
  my($self, $code) = @_;
  my @code = split /\n/, $code;
  chomp for @code;
  $self->log("code: $_") for @code;
}

sub DESTROY
{
  my($self) = @_;
  $self->save;
  my $fh = $self->{log};
  return unless defined $fh;
  close $fh;
}

my $template;


sub template
{
  unless(defined $template)
  {
    local $/;
    $template = <DATA>;
  }

  $template;
}

1;

=pod

=encoding UTF-8

=head1 NAME

FFI::Probe - System detection and probing for FFI extensions.

=head1 VERSION

version 1.34

=head1 SYNOPSIS

 use FFI::Probe;
 
 my $probe = FFI::Probe->new;
 $probe->check_header('foo.h');
 ...

=head1 DESCRIPTION

This class provides an interface for probing for system
capabilities.  It is used internally as part of the
L<FFI::Platypus> build process, but it may also be useful
for extensions that use Platypus as well.

=head1 CONSTRUCTOR

=head2 new

 my $probe = FFI::Probe->new(%args);

Creates a new instance.

=over 4

=item log

Path to a log or file handle to write to.

=item data_filename

Path to a file which will be used to store/cache results.

=back

=head1 METHODS

=head2 check_header

 my $bool = $probe->check_header($header);

Checks that the given C header file is available.
Stores the result, and returns a true/false value.

=head2 check_cpp

=head2 check_eval

 my $bool = $probe>check_eval(%args);

=over 4

=item headers

Any additional headers.

=item decl

Any C declarations that need to be made before the C<dlmain> function.

=item stmt

Any C statements that should be made before the evaluation.

=item eval

Any evaluations that should be returned.

=back

=head2 check

=head2 check_type_int

 my $type = $probe->check_type_int($type);

=head2 check_type_enum

 my $type = $probe->check_type_enum;

=head2 check_type_enum

 my $type = $probe->check_type_enum;

=head2 check_type_float

 my $type = $probe->check_type_float($type);

=head2 check_type_pointer

 my $type = $probe->check_type_pointer;

=head2 set

 $probe->set(@key, $value);

Used internally to store a value.

=head2 save

 $probe->save;

Saves the values already detected.

=head2 data

 my $data = $probe->data;

Returns a hashref of the data already detected.

=head2 log

 $probe->log($string);

Sends the given string to the log.

=head2 log_code

 $prbe->log_code($string);

Sends the given multi-line code block to the log.

=head2 template

 my $template = $probe->template;

Returns the C code template used for C<check_eval> and other
C<check_> methods.

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

Petr Pisar (ppisar)

Mohammad S Anwar (MANWAR)

Håkon Hægland (hakonhagland, HAKONH)

Meredith (merrilymeredith, MHOWARD)

Diab Jerius (DJERIUS)

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015,2016,2017,2018,2019,2020 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut

__DATA__
#include <stdio.h>
##HEADERS##
##DECL##


int
dlmain(int argc, char *argv[])
{
##STMT##

##EVAL##
  return 0;
}
