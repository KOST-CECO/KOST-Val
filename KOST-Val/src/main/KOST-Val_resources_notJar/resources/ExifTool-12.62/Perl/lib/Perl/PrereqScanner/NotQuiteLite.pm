package Perl::PrereqScanner::NotQuiteLite;

use strict;
use warnings;
use Carp;
use Perl::PrereqScanner::NotQuiteLite::Context;
use Perl::PrereqScanner::NotQuiteLite::Util;

our $VERSION = '0.9913';

our @BUNDLED_PARSERS = qw/
  Aliased AnyMoose Autouse Catalyst ClassAccessor
  ClassAutouse ClassLoad Core Inline KeywordDeclare Later
  Mixin ModuleRuntime MojoBase Moose MooseXDeclare Only
  PackageVariant Plack POE Prefork Superclass Syntax SyntaxCollector
  TestClassMost TestMore TestRequires UniversalVersion Unless
/;
our @DEFAULT_PARSERS = qw/Core Moose/;

### Helpers For Debugging

use constant DEBUG => !!$ENV{PERL_PSNQL_DEBUG} || 0;
use constant DEBUG_RE => DEBUG > 3 ? 1 : 0;

sub _debug {}
sub _error {}
sub _dump_stack {}

if (DEBUG) {
  require Data::Dump; Data::Dump->import(qw/dump/);
  no warnings 'redefine';
  *_debug = sub { print @_, "\n" };
  *_error = sub { print @_, "*" x 50, "\n" };
  *_dump_stack = sub {
    my ($c, $char) = @_;
    my $stacked = join '', map {($_->[2] ? "($_->[2])" : '').$_->[0]} @{$c->{stack}};
    _debug("$char \t\t\t\t stacked: $stacked");
  };
}

sub _match_error {
  my $rstr = shift;
  $@ = shift() . substr($$rstr, pos($$rstr), 100);
  return;
}

### Global Variables To Be Sorted Out Later

my %unsupported_packages = map {$_ => 1} qw(
);

my %sub_keywords = (
  'Function::Parameters' => [qw/fun method/],
  'TryCatch' => [qw/try catch/],
);

my %filter_modules = (
  tt => sub { ${$_[0]} =~ s|\G.+?no\s*tt\s*;||s; 0; },
  'Text::RewriteRules' => sub { ${$_[0]} =~ s|RULES.+?ENDRULES\n||gs; 1 },
);

my %is_conditional = map {$_ => 1} qw(
  if elsif unless else given when
  for foreach while until
);

my %ends_expr = map {$_ => 1} qw(
  and or xor
  if else elsif unless when default
  for foreach while until
  && || !~ =~ = += -= *= /= **= //= %= ^= |=
  > < >= <= <> <=> cmp ge gt le lt eq ne ? :
);

my %has_sideff = map {$_ => 1} qw(
  and or xor && || //
  if unless when
);

# keywords that allow /regexp/ to follow directly
my %regexp_may_follow = map {$_ => 1} qw(
  and or cmp if elsif unless eq ne
  gt lt ge le for while until grep map not split when
  return
);

my $re_namespace = qr/(?:::|')?(?:[a-zA-Z0-9_]+(?:(?:::|')[a-zA-Z0-9_]+)*)/;
my $re_nonblock_chars = qr/[^\\\(\)\{\}\[\]\<\>\/"'`#q~,\s]*/;
my $re_variable = qr/
  (?:$re_namespace)
  | (?:\^[A-Z\]])
  | (?:\{\^[A-Z0-9_]+\})
  | (?:[_"\(\)<\\\&`'\+\-,.\/\%#:=~\|?!\@\*\[\]\^])
/x;
my $re_pod = qr/(
  =[a-zA-Z]\w*\b
  .*?
  (?:(?:\n)
  =cut\b.*?(?:\n|\z)|\z)
)/sx;
my $re_comment = qr/(?:\s*#[^\n]*?\n)*(?:\s*#[^\n]*?)(?:\n|$)/s;

my $g_re_scalar_variable = qr{\G(\$(?:$re_variable))};
my $g_re_hash_shortcut = qr{\G(\{\s*(?:[\+\-]?\w+|(['"])[\w\s]+\2|(?:$re_nonblock_chars))\s*(?<!\$)\})};
my $g_re_prototype = qr{\G(\([^\)]*?\))};

my %ReStrInDelims;
sub _gen_re_str_in_delims {
  my $delim = shift;
  $ReStrInDelims{$delim} ||= do {
    if ($delim eq '\\') {
      qr/(?:[^\\]*(?:(?:\\\\)[^\\]*)*)/s;
    } else {
      $delim = quotemeta $delim;
      qr/(?:[^\\$delim]*(?:\\.[^\\$delim]*)*)/s;
    }
  };
}

my $re_str_in_single_quotes = _gen_re_str_in_delims(q{'});
my $re_str_in_double_quotes = _gen_re_str_in_delims(q{"});
my $re_str_in_backticks     = _gen_re_str_in_delims(q{`});

my %ReStrInDelimsWithEndDelim;
sub _gen_re_str_in_delims_with_end_delim {
  my $delim = shift;
  $ReStrInDelimsWithEndDelim{$delim} ||= do {
    my $re = _gen_re_str_in_delims($delim);
    qr{$re\Q$delim\E};
  };
}

my %RdelSkip;
sub _gen_rdel_and_re_skip {
  my $ldel = shift;
  @{$RdelSkip{$ldel} ||= do {
    (my $rdel = $ldel) =~ tr/[({</])}>/;
    my $re_skip = qr{[^\Q$ldel$rdel\E\\]+};
    [$rdel, $re_skip];
  }};
}

my %RegexpShortcut;
sub _gen_re_regexp_shortcut {
  my ($ldel, $rdel) = @_;
  $RegexpShortcut{$ldel} ||= do {
    $ldel = quotemeta $ldel;
    $rdel = $rdel ? quotemeta $rdel : $ldel;
    qr{(?:[^\\\(\)\{\}\[\]<>$ldel$rdel]*(?:\\.[^\\\(\)\[\]\{\}<>$ldel$rdel]*)*)$rdel};
  };
}

############################

my %LOADED;

sub new {
  my ($class, %args) = @_;

  my %mapping;
  my @parsers = $class->_get_parsers($args{parsers});
  for my $parser (@parsers) {
    if (!exists $LOADED{$parser}) {
      eval "require $parser; 1";
      if (my $error = $@) {
        $parser->can('register') or die "Parser Error: $error";
      }
      $LOADED{$parser} = $parser->can('register') ? $parser->register(%args) : undef;
    }
    my $parser_mapping = $LOADED{$parser} or next;
    for my $type (qw/use no keyword method/) {
      next unless exists $parser_mapping->{$type};
      for my $name (keys %{$parser_mapping->{$type}}) {
        $mapping{$type}{$name} = [
          $parser,
          $parser_mapping->{$type}{$name},
          (($type eq 'use' or $type eq 'no') ? ($name) : ()),
        ];
      }
    }
    if ($parser->can('register_fqfn')) {
      my $fqfn_mapping = $parser->register_fqfn;
      for my $name (keys %$fqfn_mapping) {
        my ($module) = $name =~ /^(.+)::/;
        $mapping{keyword}{$name} = [
          $parser,
          $fqfn_mapping->{$name},
          $module,
        ];
      }
    }
  }
  $args{_} = \%mapping;

  bless \%args, $class;
}

sub _get_parsers {
  my ($class, $list) = @_;
  my @parsers;
  my %should_ignore;
  for my $parser (@{$list || [qw/:default/]}) {
    if ($parser eq ':installed') {
      require Module::Find;
      push @parsers, Module::Find::findsubmod("$class\::Parser");
    } elsif ($parser eq ':bundled') {
      push @parsers, map {"$class\::Parser::$_"} @BUNDLED_PARSERS;
    } elsif ($parser eq ':default') {
      push @parsers, map {"$class\::Parser::$_"} @DEFAULT_PARSERS;
    } elsif ($parser =~ s/^\+//) {
      push @parsers, $parser;
    } elsif ($parser =~ s/^\-//) {
      $should_ignore{"$class\::Parser\::$parser"} = 1;
    } elsif ($parser =~ /^$class\::Parser::/) {
      push @parsers, $parser;
    } else {
      push @parsers, "$class\::Parser\::$parser";
    }
  }
  grep {!$should_ignore{$_}} @parsers;
}

sub scan_file {
  my ($self, $file) = @_;
  _debug("START SCANNING $file") if DEBUG;
  print STDERR " Scanning $file\n" if $self->{verbose};
  open my $fh, '<', $file or croak "Can't open $file: $!";
  my $code = do { local $/; <$fh> };
  $self->{file} = $file;
  $self->scan_string($code);
}

sub scan_string {
  my ($self, $string) = @_;

  $string = '' unless defined $string;

  my $c = Perl::PrereqScanner::NotQuiteLite::Context->new(%$self);

  if ($self->{quick}) {
    $c->{file_size} = length $string;
    $self->_skim_string($c, \$string) if $c->{file_size} > 30_000;
  }

  # UTF8 BOM
  if ($string =~ s/\A(\xef\xbb\xbf)//s) {
    utf8::decode($string);
    $c->{decoded} = 1;
  }
  # Other BOMs (TODO: also decode?)
  $string =~ s/\A(\x00\x00\xfe\xff|\xff\xfe\x00\x00|\xfe\xff|\xff\xfe)//s;

  # normalize
  if ("\n" eq "\015") {
    $string =~ s/(?:\015?\012)/\n/gs;
  } elsif ("\n" eq "\012") {
    $string =~ s/(?:\015\012?)/\n/gs;
  } elsif ("\n" eq "\015\012") {
    $string =~ s/(?:\015(?!\012)|(?<!\015)\012)/\n/gs;
  } else {
    $string =~ s/(?:\015\012|\015|\012)/\n/gs;
  }
  $string =~ s/[ \t]+/ /g;
  $string =~ s/(?: *\n)+/\n/gs;

  # FIXME
  $c->{stack} = [];
  $c->{errors} = [];
  $c->{callback} = {
    use     => \&_use,
    require => \&_require,
    no      => \&_no,
  };
  $c->{wants_doc} = 0;

  pos($string) = 0;

  {
    local $@;
    eval { $self->_scan($c, \$string, 0) };
    push @{$c->{errors}}, "Scan Error: $@" if $@;
    if ($c->{redo}) {
      delete $c->{redo};
      delete $c->{ended};
      @{$c->{stack}} = ();
      redo;
    }
  }

  if (@{$c->{stack}} and !$c->{quick}) {
    require Data::Dump;
    push @{$c->{errors}}, Data::Dump::dump($c->{stack});
  }

  $c->remove_inner_packages_from_requirements;
  $c->merge_perl;

  $c;
}

sub _skim_string {
  my ($self, $c, $rstr) = @_;
  my $pos = pos($$rstr) || 0;
  my $last_found = 0;
  my $saw_moose;
  my $re = qr/\G.*?\b((?:use|require|no)\s+(?:[A-Za-z][A-Za-z0-9_]*::)*[A-Za-z][A-Za-z0-9_]*)/;
  while(my ($match) = $$rstr =~ /$re/gc) {
    $last_found = pos($$rstr) + length $match;
    if (!$saw_moose and $match =~ /^use\s+(?:Mo(?:o|(?:[ou]se))?X?|MooseX::Declare)\b/) {
      $re = qr/\G.*?\b((?:(?:use|require|no)\s+(?:[A-Za-z][A-Za-z0-9_]*::)*[A-Za-z][A-Za-z0-9_]*)|(?:(?:extends|with)\s+(?:["']|q[a-z]*[^a-zA-Z0-9_])(?:[A-Za-z][A-Za-z0-9_]*::)*[A-Za-z][A-Za-z0-9_]*))/;
      $saw_moose = 1;
    }
  }
  $c->{last_found_by_skimming} = $last_found;
  pos($$rstr) = $pos;
}

sub _scan {
  my ($self, $c, $rstr, $parent_scope) = @_;

  if (@{$c->{stack}} > 90) {
    _error("deep recursion found");
    $c->{ended} = 1;
  }

  _dump_stack($c, "BEGIN SCOPE") if DEBUG;

  # found __DATA|END__ somewhere?
  return $c if $c->{ended};

  my $wants_doc = $c->{wants_doc};
  my $line_top = 1;
  my $waiting_for_a_block;

  my $current_scope = 0;
  my ($token, $token_desc, $token_type) = ('', '', '');
  my ($prev_token, $prev_token_type) = ('', '');
  my ($stack, $unstack);
  my (@keywords, @tokens, @scope_tokens);
  my $caller_package;
  my $prepend;
  my ($pos, $c1);
  my $prev_pos = 0;
  while(defined($pos = pos($$rstr))) {
    $token = undef;

    # cache first letter for better performance
    $c1 = substr($$rstr, $pos, 1);

    if ($line_top) {
      if ($c1 eq '=') {
        if ($$rstr =~ m/\G($re_pod)/gcsx) {
          ($token, $token_desc, $token_type) = ($1, 'POD', '') if $wants_doc;
          next;
        }
      }
    }
    if ($c1 eq "\n") {
      pos($$rstr)++;
      $line_top = 1;
      next;
    }

    $line_top = 0;
    # ignore whitespaces
    if ($c1 eq ' ') {
      pos($$rstr)++;
      next;
    } elsif ($c1 eq '_') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '_' and $$rstr =~ m/\G(__(?:DATA|END)__\b)(?!\s*=>)/gc) {
        if ($wants_doc) {
          ($token, $token_desc, $token_type) = ($1, 'END_OF_CODE', '');
          next;
        } else {
          $c->{ended} = 1;
          last;
        }
      }
    } elsif ($c1 eq '#') {
      if ($$rstr =~ m{\G($re_comment)}gcs) {
        ($token, $token_desc, $token_type) = ($1, 'COMMENT', '') if $wants_doc;
        $line_top = 1;
        next;
      }
    } elsif ($c1 eq ';') {
      pos($$rstr) = $pos + 1;
      ($token, $token_desc, $token_type) = ($c1, ';', ';');
      $current_scope |= F_STATEMENT_END|F_EXPR_END;
      next;
    } elsif ($c1 eq '$') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '#') {
        if (substr($$rstr, $pos + 2, 1) eq '{') {
          if ($$rstr =~ m{\G(\$\#\{[\w\s]+\})}gc) {
            ($token, $token_desc, $token_type) = ($1, '$#{NAME}', 'EXPR');
            next;
          } else {
            pos($$rstr) = $pos + 3;
            ($token, $token_desc, $token_type) = ('$#{', '$#{', 'EXPR');
            $stack = [$token, $pos, 'VARIABLE'];
            next;
          }
        } elsif ($$rstr =~ m{\G(\$\#(?:$re_namespace))}gc) {
          ($token, $token_desc, $token_type) = ($1, '$#NAME', 'EXPR');
          next;
        } elsif ($prev_token_type eq 'ARROW') {
          my $c3 = substr($$rstr, $pos + 2, 1);
          if ($c3 eq '*') {
            pos($$rstr) = $pos + 3;
            ($token, $token_desc, $token_type) = ('$#*', 'VARIABLE', 'VARIABLE');
            $c->add_perl('5.020', '->$#*');
            next;
          }
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('$#', 'SPECIAL_VARIABLE', 'EXPR');
          next;
        }
      } elsif ($c2 eq '$') {
        if ($$rstr =~ m{\G(\$(?:\$)+(?:$re_namespace))}gc) {
          ($token, $token_desc, $token_type) = ($1, '$$NAME', 'VARIABLE');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('$$', 'SPECIAL_VARIABLE', 'EXPR');
          next;
        }
      } elsif ($c2 eq '{') {
        if ($$rstr =~ m{\G(\$\{[\w\s]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '${NAME}', 'VARIABLE');
          if ($prev_token_type eq 'KEYWORD' and $c->token_expects_fh_or_block_list($prev_token)) {
            $token_type = '';
            next;
          }
        } elsif ($$rstr =~ m{\G(\$\{\^[A-Z_]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '${^NAME}', 'VARIABLE');
          if ($token eq '${^CAPTURE}' or $token eq '${^CAPTURE_ALL}') {
            $c->add_perl('5.026', '${^CAPTURE}');
          }
          if ($token eq '${^SAFE_LOCALES}') {
            $c->add_perl('5.028', '${^SAFE_LOCALES}');
          }
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('${', '${', 'VARIABLE');
          $stack = [$token, $pos, 'VARIABLE'];
        }
        if ($parent_scope & F_EXPECTS_BRACKET) {
          $current_scope |= F_SCOPE_END;
        }
        next;
      } elsif ($c2 eq '*' and $prev_token_type eq 'ARROW') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('$*', '$*', 'VARIABLE');
        $c->add_perl('5.020', '->$*');
        next;
      } elsif ($c2 eq '+' or $c2 eq '-') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('$'.$c2, 'SPECIAL_VARIABLE', 'VARIABLE');
        $c->add_perl('5.010', '$'.$c2);
        next;
      } elsif ($$rstr =~ m{$g_re_scalar_variable}gc) {
        ($token, $token_desc, $token_type) = ($1, '$NAME', 'VARIABLE');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'VARIABLE');
        next;
      }
    } elsif ($c1 eq '@') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '_' and $$rstr =~ m{\G\@_\b}gc) {
        ($token, $token_desc, $token_type) = ('@_', 'SPECIAL_VARIABLE', 'VARIABLE');
        next;
      } elsif ($c2 eq '{') {
        if ($$rstr =~ m{\G(\@\{[\w\s]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '@{NAME}', 'VARIABLE');
          if ($token eq '@{^CAPTURE}' or $token eq '@{^CAPTURE_ALL}') {
            $c->add_perl('5.026', '@{^CAPTURE}');
          }
        } elsif ($$rstr =~ m{\G(\@\{\^[A-Z_]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '@{^NAME}', 'VARIABLE');
          if ($token eq '@{^CAPTURE}' or $token eq '@{^CAPTURE_ALL}') {
            $c->add_perl('5.026', '@{^CAPTURE}');
          }
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('@{', '@{', 'VARIABLE');
          $stack = [$token, $pos, 'VARIABLE'];
        }
        if ($prev_token_type eq 'ARROW') {
          $c->add_perl('5.020', '->@{}');
        }
        if ($parent_scope & F_EXPECTS_BRACKET) {
          $current_scope |= F_SCOPE_END;
        }
        next;
      } elsif ($c2 eq '$') {
        if ($$rstr =~ m{\G(\@\$(?:$re_namespace))}gc) {
          ($token, $token_desc, $token_type) = ($1, '@$NAME', 'VARIABLE');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('@$', '@$', 'VARIABLE');
          next;
        }
      } elsif ($prev_token_type eq 'ARROW') {
        # postderef
        if ($c2 eq '*') {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('@*', '@*', 'VARIABLE');
          $c->add_perl('5.020', '->@*');
          next;
        } else {
          pos($$rstr) = $pos + 1;
          ($token, $token_desc, $token_type) = ('@', '@', 'VARIABLE');
          $c->add_perl('5.020', '->@');
          next;
        }
      } elsif ($c2 eq '[') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('@[', 'SPECIAL_VARIABLE', 'VARIABLE');
        next;
      } elsif ($c2 eq '+' or $c2 eq '-') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('@'.$c2, 'SPECIAL_VARIABLE', 'VARIABLE');
        $c->add_perl('5.010', '@'.$c2);
        next;
      } elsif ($$rstr =~ m{\G(\@(?:$re_namespace))}gc) {
        ($token, $token_desc, $token_type) = ($1, '@NAME', 'VARIABLE');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'VARIABLE');
        next;
      }
    } elsif ($c1 eq '%') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '{') {
        if ($$rstr =~ m{\G(\%\{[\w\s]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '%{NAME}', 'VARIABLE');
        } elsif ($$rstr =~ m{\G(\%\{\^[A-Z_]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '%{^NAME}', 'VARIABLE');
          if ($token eq '%{^CAPTURE}' or $token eq '%{^CAPTURE_ALL}') {
            $c->add_perl('5.026', '%{^CAPTURE}');
          }
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('%{', '%{', 'VARIABLE');
          $stack = [$token, $pos, 'VARIABLE'];
        }
        if ($prev_token_type eq 'ARROW') {
          $c->add_perl('5.020', '->%{');
        }
        if ($parent_scope & F_EXPECTS_BRACKET) {
          $current_scope |= F_SCOPE_END;
        }
        next;
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('%=', '%=', 'OP');
        next;
      } elsif ($$rstr =~ m{\G(\%\$(?:$re_namespace))}gc) {
        ($token, $token_desc, $token_type) = ($1, '%$NAME', 'VARIABLE');
        next;
      } elsif ($$rstr =~ m{\G(\%(?:$re_namespace))}gc) {
        ($token, $token_desc, $token_type) = ($1, '%NAME', 'VARIABLE');
        next;
      } elsif ($prev_token_type eq 'VARIABLE' or $prev_token_type eq 'EXPR') {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      } elsif ($prev_token_type eq 'ARROW') {
        if ($c2 eq '*') {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('%*', '%*', 'VARIABLE');
          $c->add_perl('5.020', '->%*');
          next;
        } else {
          pos($$rstr) = $pos + 1;
          ($token, $token_desc, $token_type) = ('%', '%', 'VARIABLE');
          $c->add_perl('5.020', '->%');
          next;
        }
      } elsif ($c2 eq '+' or $c2 eq '-') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('%'.$c2, 'SPECIAL_VARIABLE', 'VARIABLE');
        $c->add_perl('5.010', '%'.$c2);
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'VARIABLE');
        next;
      }
    } elsif ($c1 eq '*') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '{') {
        if ($prev_token_type eq 'ARROW') {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('*{', '*{', 'VARIABLE');
          $c->add_perl('5.020', '->*{}');
          next;
        } elsif ($$rstr =~ m{\G(\*\{[\w\s]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '*{NAME}', 'VARIABLE');
          if ($prev_token eq 'KEYWORD' and $c->token_expects_fh_or_block_list($prev_token)) {
            $token_type = '';
            next;
          }
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('*{', '*{', 'VARIABLE');
          $stack = [$token, $pos, 'VARIABLE'];
        }
        if ($parent_scope & F_EXPECTS_BRACKET) {
          $current_scope |= F_SCOPE_END;
        }
        next;
      } elsif ($c2 eq '*') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('**=', '**=', 'OP');
          next;
        } elsif ($prev_token_type eq 'ARROW') {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('**', '**', 'VARIABLE');
          $c->add_perl('5.020', '->**');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('**', '**', 'OP');
          next;
        }
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('*=', '*=', 'OP');
        next;
      } elsif ($$rstr =~ m{\G(\*(?:$re_namespace))}gc) {
        ($token, $token_desc, $token_type) = ($1, '*NAME', 'VARIABLE');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '&') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '&') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('&&', '&&', 'OP');
        next;
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('&=', '&=', 'OP');
        next;
      } elsif ($c2 eq '{') {
        if ($$rstr =~ m{\G(\&\{[\w\s]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '&{NAME}', 'EXPR');
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('&{', '&{', 'EXPR');
          $stack = [$token, $pos, 'FUNC'];
        }
        if ($parent_scope & F_EXPECTS_BRACKET) {
          $current_scope |= F_SCOPE_END;
        }
        next;
      } elsif ($c2 eq '.') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('&.=', '&.=', 'OP');
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('&.', '&.', 'OP');
        }
        $c->add_perl('5.022', '&.');
        next;
      } elsif ($$rstr =~ m{\G(\&(?:$re_namespace))}gc) {
        ($token, $token_desc, $token_type) = ($1, '&NAME', 'EXPR');
        next;
      } elsif ($$rstr =~ m{\G(\&\$(?:$re_namespace))}gc) {
        ($token, $token_desc, $token_type) = ($1, '&$NAME', 'EXPR');
        next;
      } elsif ($prev_token_type eq 'ARROW') {
        if ($c2 eq '*') {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('&*', '&*', 'VARIABLE');
          $c->add_perl('5.020', '->&*');
          next;
        }
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '\\') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '{') {
        if ($$rstr =~ m{\G(\\\{[\w\s]+\})}gc) {
          ($token, $token_desc, $token_type) = ($1, '\\{NAME}', 'VARIABLE');
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('\\{', '\\{', 'VARIABLE');
          $stack = [$token, $pos, 'VARIABLE'];
        }
        if ($parent_scope & F_EXPECTS_BRACKET) {
          $current_scope |= F_SCOPE_END;
        }
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, '');
        next;
      }
    } elsif ($c1 eq '-') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '>') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('->', 'ARROW', 'ARROW');
        if ($prev_token_type eq 'WORD' or $prev_token_type eq 'KEYWORD') {
          $caller_package = $prev_token;
          $current_scope |= F_KEEP_TOKENS;
        }
        next;
      } elsif ($c2 eq '-') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('--', '--', $prev_token_type);
        next;
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('-=', '-=', 'OP');
        next;
      } elsif ($$rstr =~ m{\G(\-[ABCMORSTWXbcdefgkloprstuwxz]\b)}gc) {
        ($token, $token_desc, $token_type) = ($1, 'FILE_TEST', 'EXPR');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq q{"}) {
      if ($$rstr =~ m{\G(?:\"($re_str_in_double_quotes)\")}gcs) {
        ($token, $token_desc, $token_type) = ([$1, q{"}], 'STRING', 'STRING');
        next;
      }
    } elsif ($c1 eq q{'}) {
      if ($$rstr =~ m{\G(?:\'($re_str_in_single_quotes)\')}gcs) {
        ($token, $token_desc, $token_type) = ([$1, q{'}], 'STRING', 'STRING');
        next;
      }
    } elsif ($c1 eq '`') {
      if ($$rstr =~ m{\G(?:\`($re_str_in_backticks)\`)}gcs) {
        ($token, $token_desc, $token_type) = ([$1, q{`}], 'BACKTICK', 'EXPR');
        next;
      }
    } elsif ($c1 eq '/') {
      if ($prev_token_type eq '' or $prev_token_type eq 'OP' or ($prev_token_type eq 'KEYWORD' and $regexp_may_follow{$prev_token})) { # undoubtedly regexp
        if (my $regexp = $self->_match_regexp0($c, $rstr, $pos, 'm')) {
          ($token, $token_desc, $token_type) = ($regexp, 'REGEXP', 'EXPR');
          next;
        } else {
          # the above may fail
          _debug("REGEXP ERROR: $@") if DEBUG;
          pos($$rstr) = $pos;
        }
      }
      if (($prev_token_type eq '' or (!($current_scope & F_EXPR) and $prev_token_type eq 'WORD')) or ($prev_token_type eq 'KEYWORD' and @keywords and $prev_token eq $keywords[-1] and $regexp_may_follow{$prev_token})) {

        if (my $regexp = $self->_match_regexp0($c, $rstr, $pos)) {
          ($token, $token_desc, $token_type) = ($regexp, 'REGEXP', 'EXPR');
          next;
        } else { 
          # the above may fail
          _debug("REGEXP ERROR: $@") if DEBUG;
          pos($$rstr) = $pos;
        }
      }
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '/') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('//=', '//=', 'OP');
          $c->add_perl('5.010', '//=');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('//', '//', 'OP');
          $c->add_perl('5.010', '//');
          next;
        }
      }
      if ($c2 eq '=') { # this may be a part of /=.../
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('/=', '/=', 'OP');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '{') {
      if ($$rstr =~ m{$g_re_hash_shortcut}gc) {
        ($token, $token_desc) = ($1, '{EXPR}');
        if ($current_scope & F_EVAL) {
          $current_scope &= MASK_EVAL;
          $c->{eval} = ($current_scope | $parent_scope) & F_EVAL ? 1 : 0;
        }
        if ($parent_scope & F_EXPECTS_BRACKET) {
          $current_scope |= F_SCOPE_END;
          next;
        }
        if ($prev_token_type eq 'ARROW' or $prev_token_type eq 'VARIABLE') {
          $token_type = 'VARIABLE';
          next;
        } elsif ($waiting_for_a_block) {
          $waiting_for_a_block = 0;
          if (@keywords and $c->token_expects_block($keywords[0])) {
            my $first_token = $keywords[0];
            $current_scope |= F_EXPR_END;
            if ($c->token_defines_sub($first_token) and $c->has_callback_for(sub => $first_token)) {
              $c->run_callback_for(sub => $first_token, \@tokens);
              $current_scope &= MASK_KEEP_TOKENS;
              @tokens = ();
            }
          }
          next;
        } elsif ($prev_token_type eq 'KEYWORD' and $c->token_expects_fh_or_block_list($prev_token)) {
          $token_type = '';
          next;
        } else {
          $token_type = 'EXPR';
          next;
        }
      }
      pos($$rstr) = $pos + 1;
      ($token, $token_desc) = ($c1, $c1);
      my $stack_owner;
      if (@keywords) {
        for(my $i = @keywords; $i > 0; $i--) {
          my $keyword = $keywords[$i - 1];
          if ($c->token_expects_block($keyword)) {
            $stack_owner = $keyword;
            if (@tokens and $c->token_defines_sub($keyword) and $c->has_callback_for(sub => $keyword)) {
              $c->run_callback_for(sub => $keyword, \@tokens);
              $current_scope &= MASK_KEEP_TOKENS;
              @tokens = ();
            }
            last;
          }
        }
      }
      $stack = [$token, $pos, $stack_owner || ''];
      if ($parent_scope & F_EXPECTS_BRACKET) {
        $current_scope |= F_SCOPE_END|F_STATEMENT_END|F_EXPR_END;
        next;
      }
      if ($prev_token_type eq 'ARROW' or $prev_token_type eq 'VARIABLE') {
        $token_type = 'VARIABLE';
      } elsif ($waiting_for_a_block) {
        $waiting_for_a_block = 0;
      } else {
        $token_type = (($current_scope | $parent_scope) & F_KEEP_TOKENS) ? 'EXPR' : '';
      }
      next;
    } elsif ($c1 eq '[') {
      if ($$rstr =~ m{\G(\[(?:$re_nonblock_chars)\])}gc) {
        ($token, $token_desc, $token_type) = ($1, '[EXPR]', 'VARIABLE');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'VARIABLE');
        $stack = [$token, $pos, 'VARIABLE'];
        next;
      }
    } elsif ($c1 eq '(') {
      my $prototype_re = $c->prototype_re;
      if ($waiting_for_a_block and @keywords and $c->token_defines_sub($keywords[-1]) and $$rstr =~ m{$prototype_re}gc) {
        my $proto = $1;
        if ($proto =~ /^\([\\\$\@\%\&\[\]\*;\+]*\)$/) {
          ($token, $token_desc, $token_type) = ($proto, '(PROTOTYPE)', '');
        } else {
          ($token, $token_desc, $token_type) = ($proto, '(SIGNATURES)', '');
          $c->add_perl('5.020', 'signatures');
        }
        next;
      } elsif ($$rstr =~ m{\G\(((?:$re_nonblock_chars)(?<!\$))\)}gc) {
        ($token, $token_desc, $token_type) = ([[[$1, 'EXPR']]], '()', 'EXPR');
        if ($prev_token_type eq 'KEYWORD' and @keywords and $keywords[-1] eq $prev_token and !$c->token_expects_expr_block($prev_token)) {
          if ($prev_token eq 'eval') {
            $current_scope &= MASK_EVAL;
            $c->{eval} = ($current_scope | $parent_scope) & F_EVAL ? 1 : 0;
          }
          pop @keywords;
        }
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'EXPR');
        my $stack_owner;
        if (@keywords) {
          for (my $i = @keywords; $i > 0; $i--) {
            my $keyword = $keywords[$i - 1];
            if ($c->token_expects_block($keyword)) {
              $stack_owner = $keyword;
              last;
            }
          }
        }
        $stack = [$token, $pos, $stack_owner || ''];
        next;
      }
    } elsif ($c1 eq '}') {
      pos($$rstr) = $pos + 1;
      ($token, $token_desc, $token_type) = ($c1, $c1, '');
      $unstack = $token;
      $current_scope |= F_STATEMENT_END|F_EXPR_END;
      next;
    } elsif ($c1 eq ']') {
      pos($$rstr) = $pos + 1;
      ($token, $token_desc, $token_type) = ($c1, $c1, '');
      $unstack = $token;
      next;
    } elsif ($c1 eq ')') {
      pos($$rstr) = $pos + 1;
      ($token, $token_desc, $token_type) = ($c1, $c1, '');
      $unstack = $token;
      next;
    } elsif ($c1 eq '<') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '<'){
        if ($$rstr =~ m{\G(<<(?:
          \\. |
          \w+ |
          [./-] |
          \[[^\]]*\] |
          \{[^\}]*\} |
          \* |
          \? |
          \~ |
          \$ |
        )*(?<!\-)>>)}gcx) {
          ($token, $token_desc, $token_type) = ($1, '<<NAME>>', 'EXPR');
          $c->add_perl('5.022', '<<NAME>>');
          next;
        } elsif ($$rstr =~ m{\G<<~?\s*(?:
          \\?[A-Za-z_][\w]* |
          "(?:[^\\"]*(?:\\.[^\\"]*)*)" |
          '(?:[^\\']*(?:\\.[^\\']*)*)' |
          `(?:[^\\`]*(?:\\.[^\\`]*)*)`
        )}sx) {
          if (my $heredoc = $self->_match_heredoc($c, $rstr)) {
            ($token, $token_desc, $token_type) = ($heredoc, 'HEREDOC', 'EXPR');
            next;
          } else {
            # the above may fail
            pos($$rstr) = $pos;
          }
        }
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('<<=', '<<=', 'OP');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('<<', '<<', 'OP');
          next;
        }
      } elsif ($c2 eq '=') {
        if (substr($$rstr, $pos + 2, 1) eq '>') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('<=>', '<=>', 'OP');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('<=', '<=', 'OP');
          next;
        }
      } elsif ($c2 eq '>') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('<>', '<>', 'OP');
        next;
      } elsif ($$rstr =~ m{\G(<(?:
        \\. |
        \w+ |
        [./-] |
        \[[^\]]*\] |
        \{[^\}]*\} |
        \* |
        \? |
        \~ |
        \$ |
      )*(?<!\-)>)}gcx) {
        ($token, $token_desc, $token_type) = ($1, '<NAME>', 'EXPR');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq ':') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq ':') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('::', '::', '');
        next;
      }
      if ($waiting_for_a_block and @keywords and $c->token_defines_sub($keywords[-1])) {
        while($$rstr =~ m{\G\s*(:?\s*[\w]+)}gcs) {
          my $startpos = pos($$rstr);
          if (substr($$rstr, $startpos, 1) eq '(') {
            my @nest = '(';
            pos($$rstr) = $startpos + 1;
            my ($p, $c1);
            while(defined($p = pos($$rstr))) {
              $c1 = substr($$rstr, $p, 1);
              if ($c1 eq '\\') {
                pos($$rstr) = $p + 2;
                next;
              }
              if ($c1 eq ')') {
                pop @nest;
                pos($$rstr) = $p + 1;
                last unless @nest;
              }
              if ($c1 eq '(') {
                push @nest, $c1;
                pos($$rstr) = $p + 1;
                next;
              }
              $$rstr =~ m{\G([^\\()]+)}gc and next;
            }
          }
        }
        $token = substr($$rstr, $pos, pos($$rstr) - $pos);
        ($token_desc, $token_type) = ('ATTRIBUTE', '');
        if ($token =~ /^:prototype\(/) {
          $c->add_perl('5.020', ':prototype');
        }
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '=') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '>') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('=>', 'COMMA', 'OP');
        if (@keywords and $prev_token_type eq 'KEYWORD' and $keywords[-1] eq $prev_token) {
          pop @keywords;
          if (!@keywords and ($current_scope & F_KEEP_TOKENS)) {
            $current_scope &= MASK_KEEP_TOKENS;
            @tokens = ();
          }
        }
        next;
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('==', '==', 'OP');
        next;
      } elsif ($c2 eq '~') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('=~', '=~', 'OP');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '>') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '>') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('>>=', '>>=', 'OP');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('>>', '>>', 'OP');
          next;
        }
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('>=', '>=', 'OP');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '+') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '+') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('++=', '++=', 'OP');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('++', '++', $prev_token_type);
          next;
        }
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('+=', '+=', 'OP');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '|') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '|') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('||=', '||=', 'OP');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('||', '||', 'OP');
          next;
        }
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('|=', '|=', 'OP');
        next;
      } elsif ($c2 eq '.') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('|.=', '|.=', 'OP');
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('|.', '|.', 'OP');
        }
        $c->add_perl('5.022', '|.');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '^') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('^=', '^=', 'OP');
        next;
      } elsif ($c2 eq '.') {
        if (substr($$rstr, $pos + 2, 1) eq '=') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('^.=', '^.=', 'OP');
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('^.', '^.', 'OP');
        }
        $c->add_perl('5.022', '^.');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '!') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '~') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('!~', '!~', 'OP');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '~') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '~') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('~~', '~~', 'OP');
        $c->add_perl('5.010', '~~');
        next;
      } elsif ($c2 eq '.') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('~.', '~.', 'OP');
        $c->add_perl('5.022', '~.');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq ',') {
      pos($$rstr) = $pos + 1;
      ($token, $token_desc, $token_type) = ($c1, 'COMMA', 'OP');
      next;
    } elsif ($c1 eq '?') {
      pos($$rstr) = $pos + 1;
      ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
      next;
    } elsif ($c1 eq '.') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq '.') {
        if (substr($$rstr, $pos + 2, 1) eq '.') {
          pos($$rstr) = $pos + 3;
          ($token, $token_desc, $token_type) = ('...', '...', 'OP');
          $c->add_perl('5.012', '...');
          next;
        } else {
          pos($$rstr) = $pos + 2;
          ($token, $token_desc, $token_type) = ('..', '..', 'OP');
          next;
        }
      } elsif ($c2 eq '=') {
        pos($$rstr) = $pos + 2;
        ($token, $token_desc, $token_type) = ('.=', '.=', 'OP');
        next;
      } else {
        pos($$rstr) = $pos + 1;
        ($token, $token_desc, $token_type) = ($c1, $c1, 'OP');
        next;
      }
    } elsif ($c1 eq '0') {
      my $c2 = substr($$rstr, $pos + 1, 1);
      if ($c2 eq 'x') {
        if ($$rstr =~ m{\G(0x[0-9A-Fa-f_]+)}gc) {
          ($token, $token_desc, $token_type) = ($1, 'HEX NUMBER', 'EXPR');
          next;
        }
      } elsif ($c2 eq 'b') {
        if ($$rstr =~ m{\G(0b[01_]+)}gc) {
          ($token, $token_desc, $token_type) = ($1, 'BINARY NUMBER', 'EXPR');
          next;
        }
      }
    }

    if ($$rstr =~ m{\G((?:0|[1-9][0-9_]*)(?:\.[0-9][0-9_]*)?)}gc) {
      my $number = $1;
      my $p = pos($$rstr);
      my $n1 = substr($$rstr, $p, 1);
      if ($n1 eq '.') {
        if ($$rstr =~ m{\G((?:\.[0-9_])+)}gc) {
          $number .= $1;
          ($token, $token_desc, $token_type) = ($number, 'VERSION_STRING', 'EXPR');
          next;
        } elsif (substr($$rstr, $p, 2) ne '..') {
          $number .= '.';
          pos($$rstr) = $p + 1;
        }
      } elsif ($n1 eq 'E' or $n1 eq 'e') {
        if ($$rstr =~ m{\G([Ee][+-]?[0-9]+)}gc) {
          $number .= $1;
        }
      }
      ($token, $token_desc, $token_type) = ($number, 'NUMBER', 'EXPR');
      if ($prepend) {
        $token = "$prepend$token";
        pop @tokens if @tokens and $tokens[-1][0] eq $prepend;
        pop @scope_tokens if @scope_tokens and $scope_tokens[-1][0] eq $prepend;
      }
      next;
    }

    if ($prev_token_type ne 'ARROW' and ($prev_token_type ne 'KEYWORD' or !$c->token_expects_word($prev_token))) {
      if ($prev_token_type eq 'EXPR' or $prev_token_type eq 'VARIABLE') {
        if ($c1 eq 'x') {
          if ($$rstr =~ m{\G(x\b(?!\s*=>))}gc){
            ($token, $token_desc, $token_type) = ($1, $1, '');
            next;
          }
        }
      }

      if ($c1 eq 'q') {
        my $quotelike_re = $c->quotelike_re;
        if ($$rstr =~ m{\G((?:$quotelike_re)\b(?!\s*=>))}gc) {
          if (my $quotelike = $self->_match_quotelike($c, $rstr, $1)) {
            ($token, $token_desc, $token_type) = ($quotelike, 'STRING', 'STRING');
            next;
          } else {
            _debug("QUOTELIKE ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        } elsif ($$rstr =~ m{\G((?:qw)\b(?!\s*=>))}gc) {
          if (my $quotelike = $self->_match_quotelike($c, $rstr, $1)) {
            ($token, $token_desc, $token_type) = ($quotelike, 'QUOTED_WORD_LIST', 'EXPR');
            next;
          } else {
            _debug("QUOTELIKE ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        } elsif ($$rstr =~ m{\G((?:qx)\b(?!\s*=>))}gc) {
          if (my $quotelike = $self->_match_quotelike($c, $rstr, $1)) {
            ($token, $token_desc, $token_type) = ($quotelike, 'BACKTICK', 'EXPR');
            next;
          } else {
            _debug("QUOTELIKE ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        } elsif ($$rstr =~ m{\G(qr\b(?!\s*=>))}gc) {
          if (my $regexp = $self->_match_regexp($c, $rstr)) {
            ($token, $token_desc, $token_type) = ($regexp, 'qr', 'EXPR');
            next;
          } else {
            _debug("QUOTELIKE ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        }
      } elsif ($c1 eq 'm') {
        if ($$rstr =~ m{\G(m\b(?!\s*=>))}gc) {
          if (my $regexp = $self->_match_regexp($c, $rstr)) {
            ($token, $token_desc, $token_type) = ($regexp, 'm', 'EXPR');
            next;
          } else {
            _debug("REGEXP ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        }
      } elsif ($c1 eq 's') {
        if ($$rstr =~ m{\G(s\b(?!\s*=>))}gc) {
          if (my $regexp = $self->_match_substitute($c, $rstr)) {
            ($token, $token_desc, $token_type) = ($regexp, 's', 'EXPR');
            next;
          } else {
            _debug("SUBSTITUTE ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        }
      } elsif ($c1 eq 't') {
        if ($$rstr =~ m{\G(tr\b(?!\s*=>))}gc) {
          if (my $trans = $self->_match_transliterate($c, $rstr)) {
            ($token, $token_desc, $token_type) = ($trans, 'tr', 'EXPR');
            next;
          } else {
            _debug("TRANSLITERATE ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        }
      } elsif ($c1 eq 'y') {
        if ($$rstr =~ m{\G(y\b(?!\s*=>))}gc) {
          if (my $trans = $self->_match_transliterate($c, $rstr)) {
            ($token, $token_desc, $token_type) = ($trans, 'y', 'EXPR');
            next;
          } else {
            _debug("TRANSLITERATE ERROR: $@") if DEBUG;
            pos($$rstr) = $pos;
          }
        }
      }
    }

    if ($$rstr =~ m{\G(\w+)}gc) {
      $token = $1;
      if ($prev_token_type eq 'ARROW') {
        $$rstr =~ m{\G((?:(?:::|')\w+)+)\b}gc and $token .= $1;
        ($token_desc, $token_type) = ('METHOD', 'METHOD');
      } elsif ($token eq 'CORE') {
        ($token_desc, $token_type) = ('NAMESPACE', 'WORD');
      } elsif ($token eq 'format') {
        if ($$rstr =~ m{\G([^=]*?=[ \t]*\n.*?\n\.\n)}gcs) {
          $token .= $1;
          ($token_desc, $token_type) = ('FORMAT', '');
          $current_scope |= F_STATEMENT_END|F_EXPR_END;
          next;
        }
      } elsif ($c->token_is_keyword($token) and ($prev_token_type ne 'KEYWORD' or !$c->token_expects_word($prev_token) or ($prev_token eq 'sub' and $token eq 'BEGIN'))) {
        if ($c->token_is_op_keyword($token)) {
          ($token_desc, $token_type) = ($token, 'OP');
        } else {
          ($token_desc, $token_type) = ('KEYWORD', 'KEYWORD');
          $c->check_new_keyword($token);
          push @keywords, $token unless $token eq 'undef';
        }
      } else {
        if ($c1 eq 'v' and $token =~ /^v(?:0|[1-9][0-9]*)$/) {
          if ($$rstr =~ m{\G((?:\.[0-9][0-9_]*)+)}gc) {
            $token .= $1;
            ($token_desc, $token_type) = ('VERSION_STRING', 'EXPR');
            next;
          }
        }
        $$rstr =~ m{\G((?:(?:::|')\w+)+)\b}gc and $token .= $1;
        ($token_desc, $token_type) = ('WORD', 'WORD');
        if ($prepend) {
          $token = "$prepend$token";
          pop @tokens if @tokens and $tokens[-1][0] eq $prepend;
          pop @scope_tokens if @scope_tokens and $scope_tokens[-1][0] eq $prepend;
        }
      }
      next;
    }

    # ignore control characters
    if ($$rstr =~ m{\G([[:cntrl:]]+)}gc) {
      next;
    }

    if ($$rstr =~ m{\G([[:ascii:]]+)}gc) {
      last if $parent_scope & F_STRING_EVAL;
      _error("UNKNOWN: $1");
      push @{$c->{errors}}, qq{"$1"};
      $token = $1;
      next;
    }
    if ($$rstr =~ m{\G([[:^ascii:]](?:[[:^ascii:]]|\w)*)}gc) {
      if (!$c->{utf8}) {
        last if $parent_scope & F_STRING_EVAL;
        _error("UNICODE?: $1");
        push @{$c->{errors}}, qq{"$1"};
      } else {
        _debug("UTF8: $1") if DEBUG;
      }
      $token = $1;
      next;
    }
    if ($$rstr =~ m{\G(\S+)}gc) {
      last if $parent_scope & F_STRING_EVAL;
      _error("UNEXPECTED: $1");
      push @{$c->{errors}}, qq{"$1"};
      $token = $1;
    }

    last;
  } continue {
    die "Aborted at $prev_pos" if $prev_pos == pos($$rstr);
    $prev_pos = pos($$rstr);

    if (defined $token) {
      if (!($current_scope & F_EXPR)) {
        _debug('BEGIN EXPR') if DEBUG;
        $current_scope |= F_EXPR;
      } elsif (($current_scope & F_EXPR) and (($current_scope & F_EXPR_END) or ($ends_expr{$token} and $token_type eq 'KEYWORD' and $prev_token ne ',' and $prev_token ne '=>'))) {
        @keywords = ();
        _debug('END EXPR') if DEBUG;
        $current_scope &= MASK_EXPR_END;
      }
      $prepend = undef;

      if (DEBUG) {
        my $token_str = ref $token ? Data::Dump::dump($token) : $token;
        _debug("GOT: $token_str ($pos) TYPE: $token_desc ($token_type)".($prev_token_type ? " PREV: $prev_token_type" : '').(@keywords ? " KEYWORD: @keywords" : '').(($current_scope | $parent_scope) & F_EVAL ? ' EVAL' : '').(($current_scope | $parent_scope) & F_KEEP_TOKENS ? ' KEEP' : ''));
      }

      if ($parent_scope & F_KEEP_TOKENS) {
        push @scope_tokens, [$token, $token_desc];
        if ($token eq '-' or $token eq '+') {
          $prepend = $token;
        }
      }
      if (!($current_scope & F_KEEP_TOKENS) and (exists $c->{callback}{$token} or exists $c->{keyword}{$token} or exists $c->{sub}{$token}) and $token_type ne 'METHOD' and !$c->token_expects_word($prev_token)) {
        $current_scope |= F_KEEP_TOKENS;
      }
      if ($c->token_expects_block($token)) {
        $waiting_for_a_block = 1;
      }
      if ($current_scope & F_EVAL or ($parent_scope & F_EVAL and (!@{$c->{stack}} or $c->{stack}[-1][0] ne '{'))) {
        if ($token_type eq 'STRING') {
          if ($token->[0] =~ /\b(?:(?:use|no)\s+[A-Za-z]|require\s+(?:q[qw]?.|['"])?[A-Za-z])/) {
            my $eval_string = $token->[0];
            if (defined $eval_string and $eval_string ne '') {
              $eval_string =~ s/\\(.)/$1/g;
              pos($eval_string) = 0;
              $c->{eval} = 1;
              my $saved_stack = $c->{stack};
              $c->{stack} = [];
              eval { $self->_scan($c, \$eval_string, (
                ($current_scope | $parent_scope | F_STRING_EVAL) &
                F_RESCAN
              ))};
              $c->{stack} = $saved_stack;
            }
          }
          $current_scope &= MASK_EVAL;
        } elsif ($token_desc eq 'HEREDOC') {
          if ($token->[0] =~ /\b(?:use|require|no)\s+[A-Za-z]/) {
            my $eval_string = $token->[0];
            if (defined $eval_string and $eval_string ne '') {
              $eval_string =~ s/\\(.)/$1/g;
              pos($eval_string) = 0;
              $c->{eval} = 1;
              my $saved_stack = $c->{stack};
              $c->{stack} = [];
              eval { $self->_scan($c, \$eval_string, (
                ($current_scope | $parent_scope | F_STRING_EVAL) &
                F_RESCAN
              ))};
              $c->{stack} = $saved_stack;
            }
          }
          $current_scope &= MASK_EVAL;
        } elsif ($token_type eq 'VARIABLE') {
          $current_scope &= MASK_EVAL;
        }
        $c->{eval} = ($current_scope | $parent_scope) & F_EVAL ? 1 : 0;
      }
      if ($token eq 'eval') {
        $current_scope |= F_EVAL;
        $c->{eval} = 1;
      }

      if ($current_scope & F_KEEP_TOKENS) {
        push @tokens, [$token, $token_desc];
        if ($token eq '-' or $token eq '+') {
          $prepend = $token;
        }
        if ($token_type eq 'KEYWORD' and $has_sideff{$token}) {
          $current_scope |= F_SIDEFF;
        }
      }
      if ($stack) {
        push @{$c->{stack}}, $stack;
        _dump_stack($c, $stack->[0]) if DEBUG;
        my $child_scope = $current_scope | $parent_scope;
        if ($token eq '{' and $is_conditional{$stack->[2]}) {
          $child_scope |= F_CONDITIONAL
        }
        my $scanned_tokens = $self->_scan($c, $rstr, (
          $child_scope & F_RESCAN
        ));
        if ($token eq '{' and $current_scope & F_EVAL) {
          $current_scope &= MASK_EVAL;
          $c->{eval} = ($current_scope | $parent_scope) & F_EVAL ? 1 : 0;
        }
        if ($current_scope & F_KEEP_TOKENS) {
          my $start = pop @tokens || '';
          my $end = pop @$scanned_tokens || '';
          push @tokens, [$scanned_tokens, "$start->[0]$end->[0]"];
        } elsif ($parent_scope & F_KEEP_TOKENS) {
          my $start = pop @scope_tokens || '';
          my $end = pop @$scanned_tokens || '';
          push @scope_tokens, [$scanned_tokens, "$start->[0]$end->[0]"];
        }

        if ($stack->[0] eq '(' and $prev_token_type eq 'KEYWORD' and @keywords and $keywords[-1] eq $prev_token and !$c->token_expects_expr_block($prev_token)) {
          pop @keywords;
        }

        if ($stack->[0] eq '{' and @keywords and $c->token_expects_block($keywords[0]) and !$c->token_expects_block_list($keywords[-1])) {
          $current_scope |= F_STATEMENT_END unless @tokens and ($c->token_defines_sub($keywords[-1]) or $keywords[-1] eq 'eval');
        }
        $stack = undef;
      }
      if ($current_scope & F_STATEMENT_END) {
        if (($current_scope & F_KEEP_TOKENS) and @tokens) {
          my $first_token = $tokens[0][0];
          if ($first_token eq '->') {
            $first_token = $tokens[1][0];
            # ignore ->use and ->no
            # ->require may be from UNIVERSAL::require
            if ($first_token eq 'use' or $first_token eq 'no') {
              $first_token = '';
            }
          }
          my $cond = (($current_scope | $parent_scope) & (F_CONDITIONAL|F_SIDEFF)) ? 1 : 0;
          if (exists $c->{callback}{$first_token}) {
            $c->{current_scope} = \$current_scope;
            $c->{cond} = $cond;
            $c->{callback}{$first_token}->($c, $rstr, \@tokens);

            if ($c->{found_unsupported_package} and !$c->{quick}) {
              my $unsupported = $c->{found_unsupported_package};
              $c->{quick} = 1;
              $self->_skim_string($c, $rstr);
              warn "Unsupported package '$unsupported' is found. Result may be incorrect.\n";
            }
          }
          if (exists $c->{keyword}{$first_token}) {
            $c->{current_scope} = \$current_scope;
            $c->{cond} = $cond;
            $tokens[0][1] = 'KEYWORD';
            $c->run_callback_for(keyword => $first_token, \@tokens);
          }
          if (exists $c->{method}{$first_token} and $caller_package) {
            unshift @tokens, [$caller_package, 'WORD'];
            $c->{current_scope} = \$current_scope;
            $c->{cond} = $cond;
            $c->run_callback_for(method => $first_token, \@tokens);
          }
          if ($current_scope & F_SIDEFF) {
            $current_scope &= MASK_SIDEFF;
            while(my $token = shift @tokens) {
              last if $has_sideff{$token->[0]};
            }
            $current_scope &= F_SIDEFF if grep {$has_sideff{$_->[0]}} @tokens;
            if (@tokens) {
              $first_token = $tokens[0][0];
              $cond = (($current_scope | $parent_scope) & (F_CONDITIONAL|F_SIDEFF)) ? 1 : 0;
              if (exists $c->{callback}{$first_token}) {
                $c->{current_scope} = \$current_scope;
                $c->{cond} = $cond;
                $c->{callback}{$first_token}->($c, $rstr, \@tokens);
              }
              if (exists $c->{keyword}{$first_token}) {
                $c->{current_scope} = \$current_scope;
                $c->{cond} = $cond;
                $tokens[0][1] = 'KEYWORD';
                $c->run_callback_for(keyword => $first_token, \@tokens);
              }
              if (exists $c->{method}{$first_token} and $caller_package) {
                unshift @tokens, [$caller_package, 'WORD'];
                $c->{current_scope} = \$current_scope;
                $c->{cond} = $cond;
                $c->run_callback_for(method => $first_token, \@tokens);
              }
            }
          }
        }
        @tokens = ();
        @keywords = ();
        $current_scope &= MASK_STATEMENT_END;
        $caller_package = undef;
        $token = $token_type = '';
        _debug('END SENTENSE') if DEBUG;
      }
      if ($unstack and @{$c->{stack}}) {
        my $stacked = pop @{$c->{stack}};
        my $stacked_type = substr($stacked->[0], -1);
        if (
          ($unstack eq '}' and $stacked_type ne '{') or
          ($unstack eq ']' and $stacked_type ne '[') or
          ($unstack eq ')' and $stacked_type ne '(')
        ) {
          my $prev_pos = $stacked->[1] || 0;
          die "mismatch $stacked_type $unstack\n" .
              substr($$rstr, $prev_pos, pos($$rstr) - $prev_pos);
        }
        _dump_stack($c, $unstack) if DEBUG;
        $current_scope |= F_SCOPE_END;
        $unstack = undef;
      }

      last if $current_scope & F_SCOPE_END;
      last if $c->{ended};
      last if $c->{last_found_by_skimming} and $c->{last_found_by_skimming} < pos($$rstr);

      ($prev_token, $prev_token_type) = ($token, $token_type);
    }

    if (@{$c->{errors}} and !($parent_scope & F_STRING_EVAL)) {
      my $rest = substr($$rstr, pos($$rstr));
      _error("REST:\n\n".$rest) if $rest;
      last;
    }
  }

  if (@tokens) {
    if (my $first_token = $tokens[0][0]) {
      if (exists $c->{callback}{$first_token}) {
        $c->{callback}{$first_token}->($c, $rstr, \@tokens);
      }
      if (exists $c->{keyword}{$first_token}) {
        $tokens[0][1] = 'KEYWORD';
        $c->run_callback_for(keyword => $first_token, \@tokens);
      }
    }
  }

  _dump_stack($c, "END SCOPE") if DEBUG;

  \@scope_tokens;
}

sub _match_quotelike {
  my ($self, $c, $rstr, $op) = @_;

  # '#' only works when it comes just after the op,
  # without prepending spaces
  $$rstr =~ m/\G(?:\s(?:$re_comment))?\s*/gcs;

  unless ($$rstr =~ m/\G(\S)/gc) {
    return _match_error($rstr, "No block delimiter found after $op");
  }
  my $ldel = $1;
  my $startpos = pos($$rstr);

  if ($ldel =~ /[[(<{]/) {
    my ($rdel, $re_skip) = _gen_rdel_and_re_skip($ldel);
    my @nest = ($ldel);
    my ($p, $c1);
    while(defined($p = pos($$rstr))) {
      $c1 = substr($$rstr, $p, 1);
      if ($c1 eq '\\') {
        pos($$rstr) = $p + 2;
        next;
      }
      if ($c1 eq $ldel) {
        pos($$rstr) = $p + 1;
        push @nest, $ldel;
        next;
      }
      if ($c1 eq $rdel) {
        pos($$rstr) = $p + 1;
        pop @nest;
        last unless @nest;
        next;
      }
      $$rstr =~ m{\G$re_skip}gc and next;
      last;
    }
    return if @nest;
  } else {
    my $re = _gen_re_str_in_delims_with_end_delim($ldel);
    $$rstr =~ /\G$re/gcs or return;
  }

  my $endpos = pos($$rstr);

  return [substr($$rstr, $startpos, $endpos - $startpos - 1), $op];
}

sub _match_regexp0 { # //
  my ($self, $c, $rstr, $startpos, $token_type) = @_;
  pos($$rstr) = $startpos + 1;

  my $re_shortcut = _gen_re_regexp_shortcut('/');
  $$rstr =~ m{\G$re_shortcut}gcs or  # shortcut
  defined($self->_scan_re($c, $rstr, '/', '/', $token_type ? 'm' : '')) or return _match_error($rstr, "Closing delimiter was not found: $@");

  $$rstr =~ m/\G([msixpodualgc]*)/gc;
  my $mod = $1;

  my $endpos = pos($$rstr);

  my $re = substr($$rstr, $startpos, $endpos - $startpos);
  if ($re =~ /\n/s and $mod !~ /x/) {
    return _match_error($rstr, "multiline without x");
  }
  return $re;
}

sub _match_regexp {
  my ($self, $c, $rstr) = @_;
  my $startpos = pos($$rstr) || 0;

  # '#' only works when it comes just after the op,
  # without prepending spaces
  $$rstr =~ m/\G(?:\s(?:$re_comment))?\s*/gcs;

  unless ($$rstr =~ m/\G(\S)/gc) {
    return _match_error($rstr, "No block delimiter found");
  }
  my ($ldel, $rdel) = ($1, $1);

  if ($ldel =~ /[[(<{]/) {
    $rdel =~ tr/[({</])}>/;
  }

  my $re_shortcut = _gen_re_regexp_shortcut($ldel, $rdel);
  $$rstr =~ m{\G$re_shortcut}gcs or  # shortcut
  defined($self->_scan_re($c, $rstr, $ldel, $rdel, 'm/qr')) or return _match_error($rstr, "Closing delimiter was not found: $@");

  # strictly speaking, qr// doesn't support gc.
  $$rstr =~ m/\G[msixpodualgc]*/gc;
  my $endpos = pos($$rstr);

  return substr($$rstr, $startpos, $endpos - $startpos);
}

sub _match_substitute {
  my ($self, $c, $rstr) = @_;
  my $startpos = pos($$rstr) || 0;

  # '#' only works when it comes just after the op,
  # without prepending spaces
  $$rstr =~ m/\G(?:\s(?:$re_comment))?\s*/gcs;

  unless ($$rstr =~ m/\G(\S)/gc) {
    return _match_error($rstr, "No block delimiter found");
  }
  my ($ldel1, $rdel1) = ($1, $1);

  if ($ldel1 =~ /[[(<{]/) {
    $rdel1 =~ tr/[({</])}>/;
  }

  my $re_shortcut = _gen_re_regexp_shortcut($ldel1, $rdel1);
  ($ldel1 ne '\\' and $$rstr =~ m{\G$re_shortcut}gcs) or  # shortcut
  defined($self->_scan_re($c, $rstr, $ldel1, $rdel1, 's')) or return _match_error($rstr, "Closing delimiter was not found: $@");
  defined($self->_scan_re2($c, $rstr, $ldel1, 's')) or return;
  $$rstr =~ m/\G[msixpodualgcer]*/gc;
  my $endpos = pos($$rstr);

  return substr($$rstr, $startpos, $endpos - $startpos);
}

sub _match_transliterate {
  my ($self, $c, $rstr) = @_;
  my $startpos = pos($$rstr) || 0;

  # '#' only works when it comes just after the op,
  # without prepending spaces
  $$rstr =~ m/\G(?:\s(?:$re_comment))?\s*/gcs;

  unless ($$rstr =~ m/\G(\S)/gc) {
    return _match_error($rstr, "No block delimiter found");
  }
  my $ldel1 = $1;
  my $ldel2;

  if ($ldel1 =~ /[[(<{]/) {
    (my $rdel1 = $ldel1) =~ tr/[({</])}>/;
    my $re = _gen_re_str_in_delims_with_end_delim($rdel1);
    $$rstr =~ /\G$re/gcs or return;
    $$rstr =~ /\G(?:$re_comment)/gcs;
    unless ($$rstr =~ /\G\s*(\S)/gc) {
      return _match_error($rstr, "Missing second block");
    }
    $ldel2 = $1;
  } else {
    my $re = _gen_re_str_in_delims_with_end_delim($ldel1);
    $$rstr =~ /\G$re/gcs or return;
    $ldel2 = $ldel1;
  }

  if ($ldel2 =~ /[[(<{]/) {
    (my $rdel2 = $ldel2) =~ tr/[({</])}>/;
    my $re = _gen_re_str_in_delims_with_end_delim($rdel2);
    $$rstr =~ /\G$re/gcs or return;
  } else {
    my $re = _gen_re_str_in_delims_with_end_delim($ldel2);
    $$rstr =~ /\G$re/gcs or return;
  }

  $$rstr =~ m/\G[cdsr]*/gc;
  my $endpos = pos($$rstr);

  return substr($$rstr, $startpos, $endpos - $startpos);
}

sub _match_heredoc {
  my ($self, $c, $rstr) = @_;

  my $startpos = pos($$rstr) || 0;

  $$rstr =~ m{\G(?:<<(~)?\s*)}gc;
  my $indent = $1 ? "\\s*" : "";

  my $label;
  if ($$rstr =~ m{\G\\?([A-Za-z_]\w*)}gc) {
    $label = $1;
  } elsif ($$rstr =~ m{
      \G ' ($re_str_in_single_quotes) '
    | \G " ($re_str_in_double_quotes) "
    | \G ` ($re_str_in_backticks) `
  }gcsx) {
    $label = $+;
  } else {
    return;
  }
  $label =~ s/\\(.)/$1/g;
  my $extrapos = pos($$rstr);
  $$rstr =~ m{\G.*\n}gc;
  my $str1pos = pos($$rstr)--;
  unless ($$rstr =~ m{\G.*?\n$indent(?=\Q$label\E\n)}gcs) {
    return _match_error($rstr, qq{Missing here doc terminator ('$label')});
  }
  my $ldpos = pos($$rstr);
  $$rstr =~ m{\G\Q$label\E\n}gc;
  my $ld2pos = pos($$rstr);

  my $heredoc = [
    substr($$rstr, $str1pos, $ldpos-$str1pos),
    substr($$rstr, $startpos, $extrapos-$startpos),
    substr($$rstr, $ldpos, $ld2pos-$ldpos),
  ];
  substr($$rstr, $str1pos, $ld2pos - $str1pos) = '';
  pos($$rstr) = $extrapos;
  if ($indent) {
    $c->add_perl('5.026', '<<~');
  }
  return $heredoc;
}

sub _scan_re {
  my ($self, $c, $rstr, $ldel, $rdel, $op) = @_;
  my $startpos = pos($$rstr) || 0;

  _debug(" L $ldel R $rdel") if DEBUG_RE;

  my ($outer_opening_delimiter, $outer_closing_delimiter);
  if (@{$c->{stack}}) {
    ($outer_closing_delimiter = $outer_opening_delimiter = $c->{stack}[-1][0]) =~ tr/[({</])}>/;
  }

  my @nesting = ($ldel);
  my $multiline = 0;
  my $saw_sharp = 0;
  my $prev;
  my ($p, $c1);
  while (defined($p = pos($$rstr))) {
    $c1 = substr($$rstr, $p, 1);
    if ($c1 eq "\n") {
      $$rstr =~ m{\G\n\s*}gcs;
      $multiline = 1;
      $saw_sharp = 0;
      # _debug("CRLF") if DEBUG_RE;
      next;
    }
    if ($c1 eq ' ' or $c1 eq "\t") {
      $$rstr =~ m{\G\s*}gc;
      # _debug("WHITESPACE") if DEBUG_RE;
      next;
    }
    if ($c1 eq '#' and $rdel ne '#') {
      if ($multiline and $$rstr =~ m{\G(#[^\Q$rdel\E]*?)\n}gcs) {
        _debug(" comment $1") if DEBUG_RE
      } else {
        pos($$rstr) = $p + 1;
        $saw_sharp = 1;
        _debug(" saw #") if DEBUG_RE;
      }
      next;
    }

    if ($c1 eq '\\' and $rdel ne '\\') {
      if ($$rstr =~ m/\G(\\.)/gcs) {
        _debug(" escaped $1") if DEBUG_RE;
        next;
      }
    }

    _debug(" looking @nesting: $c1") if DEBUG_RE;

    if ($c1 eq '[') {
      # character class may have other (ignorable) delimiters
      if ($$rstr =~ m/\G(\[\[:\w+?:\]\])/gcs) {
        _debug(" character class $1") if DEBUG_RE;
        next;
      }
      if ($$rstr =~ m/\G(\[[^\\\]]]*?(\\.[^\\\]]]*)*\])/gcs) {
        _debug(" character class: $1") if DEBUG_RE;
        next;
      }
    }

    if ($c1 eq $rdel) {
      pos($$rstr) = $p + 1;
      if ($saw_sharp) {
        my $tmp_pos = $p + 1;
        if ($op eq 's') {
          _debug(" looking for latter part") if DEBUG_RE;
          my $latter = $self->_scan_re2($c, $rstr, $ldel, $op);
          if (!defined $latter) {
            pos($$rstr) = $tmp_pos;
            next;
          }
          _debug(" latter: $latter") if DEBUG_RE;
        }
        if ($$rstr =~ m/\G[a-wyz]*x/) {
          # looks like an end of block
          _debug(" end of block $rdel (after #)") if DEBUG_RE;
          @nesting = ();
          pos($$rstr) = $tmp_pos;
          last;
        }
        pos($$rstr) = $tmp_pos;
        if ($multiline) {
          next; # part of a comment
        }
      }
      _debug(" end of block $rdel") if DEBUG_RE;
      my $expected = $rdel;
      if ($ldel ne $rdel) {
        $expected =~ tr/)}]>/({[</;
      }
      while(my $nested = pop @nesting) {
        last if $nested eq $expected;
      }
      last unless @nesting;
      next;
    } elsif ($c1 eq $ldel) {
      pos($$rstr) = $p + 1;
      if ($multiline and $saw_sharp) {
      } else {
        _debug(" block $ldel") if DEBUG_RE;
        push @nesting, $ldel;
        next;
      }
    }

    if ($c1 eq '{') {
      # quantifier shouldn't be nested
      if ($$rstr =~ m/\G(\{[0-9]+(?:,(?:[0-9]+)?)?})/gcs) {
        _debug(" quantifier $1") if DEBUG_RE;
        next;
      }
    }

    if ($c1 eq '(') {
      my $c2 = substr($$rstr, $p + 1, 1);
      if ($c2 eq '?' and !($multiline and $saw_sharp)) {
        # code
        if ($$rstr =~ m/\G((\()\?+?)(?=\{)/gc) {
          _debug(" code $1") if DEBUG_RE;
          push @nesting, $2;
          unless (eval { $self->_scan($c, $rstr, F_EXPECTS_BRACKET); 1 }) {
            _debug("scan failed") if DEBUG_RE;
            return;
          }
          next;
        }
        # comment
        if ($$rstr =~ m{\G(\(\?\#[^\\\)]*(?:\\.[^\\\)]*)*\))}gcs) {
          _debug(" comment $1") if DEBUG_RE;
          next;
        }
      }

      # grouping may have (ignorable) <>
      if ($$rstr =~ m/\G((\()(?:<[!=]|<\w+?>|>)?)/gc) {
        _debug(" group $1") if DEBUG_RE;
        push @nesting, $2;
        next;
      }
    }

    # maybe variables (maybe not)
    if ($c1 eq '$' and substr($$rstr, $p + 1, 1) eq '{') {
      my @tmp_stack = @{$c->{stack}};
      next if eval { $self->_scan($c, $rstr, F_EXPECTS_BRACKET); 1 };
      pos($$rstr) = $p;
      $c->{stack} = \@tmp_stack;
    }

    if ($c1 eq ')') {
      if (@nesting and $nesting[-1] eq '(') {
        _debug(" end of group $c1") if DEBUG_RE;
        pop @nesting;
        pos($$rstr) = $p + 1;
        next;
      } else {
        # die "unnested @nesting" unless $saw_sharp;
      }
    }

    # for //, see if an outer closing delimiter is found first (ie. see if it was actually a /)
    if (!$op) {
      if ($outer_opening_delimiter and $c1 eq $outer_opening_delimiter) {
        push @nesting, $c1;
        pos($$rstr) = $p + 1;
        next;
      }

      if ($outer_closing_delimiter and $c1 eq $outer_closing_delimiter) {
        if (@nesting and $nesting[-1] eq $outer_opening_delimiter) {
          pop @nesting;
          pos($$rstr) = $p + 1;
          next;
        }

        return _match_error($rstr, "Outer closing delimiter: $outer_closing_delimiter is found");
      }
    }

    if ($$rstr =~ m/\G(\w+|.)/gcs) {
      _debug(" rest $1") if DEBUG_RE;
      next;
    }
    last;
  }
  if ($#nesting>=0) {
    return _match_error($rstr, "Unmatched opening bracket(s): ". join("..",@nesting)."..");
  }

  my $endpos = pos($$rstr);

  return substr($$rstr, $startpos, $endpos - $startpos);
}


sub _scan_re2 {
  my ($self, $c, $rstr, $ldel, $op) = @_;
  my $startpos = pos($$rstr);

  if ($ldel =~ /[[(<{]/) {
    $$rstr =~ /\G(?:$re_comment)/gcs;

    unless ($$rstr =~ /\G\s*(\S)/gc) {
      return _match_error($rstr, "Missing second block for quotelike $op");
    }
    $ldel = $1;
  }

  if ($ldel =~ /[[(<{]/) {
    my ($rdel, $re_skip) = _gen_rdel_and_re_skip($ldel);
    my @nest = $ldel;
    my ($p, $c1);
    while(defined($p = pos($$rstr))) {
      $c1 = substr($$rstr, $p, 1);
      if ($c1 eq '\\') {
        pos($$rstr) = $p + 2;
        next;
      }
      if ($c1 eq $ldel) {
        pos($$rstr) = $p + 1;
        push @nest, $ldel;
        next;
      }
      if ($c1 eq $rdel) {
        pos($$rstr) = $p + 1;
        pop @nest;
        last unless @nest;
        next;
      }
      $$rstr =~ m{\G$re_skip}gc and next;
      last;
    }
    return _match_error($rstr, "nesting mismatch: @nest") if @nest;
  } else {
    my $re = _gen_re_str_in_delims_with_end_delim($ldel);
    $$rstr =~ /\G$re/gcs or return;
  }

  my $endpos = pos($$rstr);

  return substr($$rstr, $startpos, $endpos - $startpos);
}

sub _use {
  my ($c, $rstr, $tokens) = @_;
_debug("USE TOKENS: ".(Data::Dump::dump($tokens))) if DEBUG;
  shift @$tokens; # discard 'use' itself

  # TODO: see if the token is WORD or not?
  my $name_token = shift @$tokens or return;
  my $name = $name_token->[0];
  return if !defined $name or ref $name or $name eq '';

  my $c1 = substr($name, 0, 1);
  if ($c1 eq '5') {
    $c->add(perl => $name);
    return;
  }
  if ($c1 eq 'v') {
    my $c2 = substr($name, 1, 1);
    if ($c2 eq '5') {
      $c->add(perl => $name);
      return;
    }
    if ($c2 eq '6') {
      $c->{perl6} = 1;
      $c->{ended} = 1;
      return;
    }
  }
  if ($c->enables_utf8($name)) {
    $c->add($name => 0);
    $c->{utf8} = 1;
    if (!$c->{decoded}) {
      $c->{decoded} = 1;
      _debug("UTF8 IS ON") if DEBUG;
      utf8::decode($$rstr);
      pos($$rstr) = 0;
      $c->{ended} = $c->{redo} = 1;
    }
  }

  if (is_module_name($name)) {
    my $maybe_version_token = $tokens->[0];
    my $maybe_version_token_desc = $maybe_version_token->[1];
    if ($maybe_version_token_desc and ($maybe_version_token_desc eq 'NUMBER' or $maybe_version_token_desc eq 'VERSION_STRING')) {
      $c->add($name => $maybe_version_token->[0]);
      shift @$tokens;
    } else {
      $c->add($name => 0);
    }

    if (exists $sub_keywords{$name}) {
      $c->register_sub_keywords(@{$sub_keywords{$name}});
      $c->prototype_re(qr{\G(\((?:[^\\\(\)]*(?:\\.[^\\\(\)]*)*)\))});
    }
    if (exists $filter_modules{$name}) {
      my $tmp = pos($$rstr);
      my $redo = $filter_modules{$name}->($rstr);
      pos($$rstr) = $tmp;
      $c->{ended} = $c->{redo} = 1 if $redo;
    }
  }

  if ($c->has_callback_for(use => $name)) {
    eval { $c->run_callback_for(use => $name, $tokens) };
    warn "Callback Error: $@" if $@;
  } elsif ($name =~ /\b(?:Mo[ou]se?X?|MooX?|Elk|Antlers|Role)\b/) {
    my $module = $name =~ /Role/ ? 'Moose::Role' : 'Moose';
    if ($c->has_callback_for(use => $module)) {
      eval { $c->run_callback_for(use => $module, $tokens) };
      warn "Callback Error: $@" if $@;
    }
  }

  if (exists $unsupported_packages{$name}) {
    $c->{found_unsupported_package} = $name;
  }
}

sub _require {
  my ($c, $rstr, $tokens) = @_;
_debug("REQUIRE TOKENS: ".(Data::Dump::dump($tokens))) if DEBUG;
  shift @$tokens; # discard 'require' itself

  # TODO: see if the token is WORD or not?
  my $name_token = shift @$tokens or return;
  my $name = $name_token->[0];
  if (ref $name) {
    $name = $name->[0];
    return if $name =~ /\.pl$/i;

    $name =~ s|/|::|g;
    $name =~ s|\.pm$||i;
  }
  return if !defined $name or $name eq '';

  my $c1 = substr($name, 0, 1);
  if ($c1 eq '5') {
    $c->add_conditional(perl => $name);
    return;
  }
  if ($c1 eq 'v') {
    my $c2 = substr($name, 1, 1);
    if ($c2 eq '5') {
      $c->add_conditional(perl => $name);
      return;
    }
    if ($c2 eq '6') {
      $c->{perl6} = 1;
      $c->{ended} = 1;
      return;
    }
  }
  if (is_module_name($name)) {
    $c->add_conditional($name => 0);
    return;
  }
}

sub _no {
  my ($c, $rstr, $tokens) = @_;
_debug("NO TOKENS: ".(Data::Dump::dump($tokens))) if DEBUG;
  shift @$tokens; # discard 'no' itself

  # TODO: see if the token is WORD or not?
  my $name_token = shift @$tokens or return;
  my $name = $name_token->[0];
  return if !defined $name or ref $name or $name eq '';

  my $c1 = substr($name, 0, 1);
  if ($c1 eq '5') {
    $c->add_no(perl => $name);
    return;
  }
  if ($c1 eq 'v') {
    my $c2 = substr($name, 1, 1);
    if ($c2 eq '5') {
      $c->add_no(perl => $name);
      return;
    }
    if ($c2 eq '6') {
      $c->{perl6} = 1;
      $c->{ended} = 1;
      return;
    }
  }
  if ($name eq 'utf8') {
    $c->{utf8} = 0;
  }

  if (is_module_name($name)) {
    my $maybe_version_token = $tokens->[0];
    my $maybe_version_token_desc = $maybe_version_token->[1];
    if ($maybe_version_token_desc and ($maybe_version_token_desc eq 'NUMBER' or $maybe_version_token_desc eq 'VERSION_STRING')) {
      $c->add_no($name => $maybe_version_token->[0]);
      shift @$tokens;
    } else {
      $c->add_no($name => 0);
    }
  }

  if ($c->has_callback_for(no => $name)) {
    eval { $c->run_callback_for(no => $name, $tokens) };
    warn "Callback Error: $@" if $@;
    return;
  }
}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite - a tool to scan your Perl code for its prerequisites

=head1 SYNOPSIS

  use Perl::PrereqScanner::NotQuiteLite;
  my $scanner = Perl::PrereqScanner::NotQuiteLite->new(
    parsers => [qw/:installed -UniversalVersion/],
    suggests => 1,
    perl_minimum_version => 1,
  );
  my $context = $scanner->scan_file('path/to/file');
  my $requirements = $context->requires;
  my $recommends = $context->recommends;
  my $suggestions  = $context->suggests; # requirements in evals
  my $noes = $context->noes;

=head1 DESCRIPTION

Perl::PrereqScanner::NotQuiteLite is yet another prerequisites
scanner. It passes almost all the scanning tests for
L<Perl::PrereqScanner> and L<Module::ExtractUse> (ie. except for
a few dubious ones), and runs slightly faster than PPI-based
Perl::PrereqScanner. However, it doesn't run as fast as
L<Perl::PrereqScanner::Lite> (which uses an XS lexer).

Perl::PrereqScanner::NotQuiteLite also recognizes C<eval>.
Prerequisites in C<eval> are not considered as requirements, but you
can collect them as suggestions.

Conditional requirements or requirements loaded in a block are
treated as recommends. Noed modules are stored separately (since 0.94).
You may or may not need to merge them into requires.

Perl::PrereqScanner::NotQuiteLite can also recognize some of
the new language features such as C<say>, subroutine signatures,
and postfix dereferences, to improve the minimum perl requirement
(since 0.9905).

=head1 METHODS

=head2 new

creates a scanner object. Options are:

=over 4

=item parsers

By default, Perl::PrereqScanner::NotQuiteLite only recognizes
modules loaded directly by C<use>, C<require>, C<no> statements,
plus modules loaded by a few common modules such as C<base>,
C<parent>, C<if> (that are in the Perl core), and by two keywords
exported by L<Moose> family (C<extends> and C<with>).

If you need more, you can pass extra parser names to the scanner,
or C<:bundled>, which loads and registers all the parsers bundled
with this distribution. If you have your own parsers, you can
specify C<:installed> to load and register all the installed parsers.

You can also pass a project-specific parser (that lies outside the 
C<Perl::PrereqScanner::NotQuiteLite::Parser> namespace) by
prepending C<+> to the name.

  use Perl::PrereqScanner::NotQuiteLite;
  my $scanner = Perl::PrereqScanner::NotQuiteLite->new(
    parsers => [qw/+PrereqParser::For::MyProject/],
  );

If you don't want to load a specific parser for some reason,
prepend C<-> to the parser name.

=item suggests

Perl::PrereqScanner::NotQuiteLite ignores C<use>-like statements in
C<eval> by default. If you set this option to true,
Perl::PrereqScanner::NotQuiteLite also parses statements in C<eval>,
and records requirements as suggestions.

=item recommends

Perl::PrereqScanner::NotQuiteLite usually ignores C<require>-like
statements in a block by default. If you set this option to true,
Perl::PrereqScanner::NotQuiteLite also records requirements in
a block as recommendations.

=item perl_minimum_version

If you set this option to true, Perl::PrereqScanner::NotQuiteLite
adds a specific version of perl as a requirement when it finds
some of the new perl language features.

=back

=head2 scan_file

takes a path to a file and returns a ::Context object.

=head2 scan_string

takes a string, scans and returns a ::Context object.

=head1 SEE ALSO

L<Perl::PrereqScanner>, L<Perl::PrereqScanner::Lite>, L<Module::ExtractUse>

L<Perl::PrereqScanner::NotQuiteLite::App> to scan a whole distribution.

L<scan-perl-prereqs-nqlite> is a command line interface of the above.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
