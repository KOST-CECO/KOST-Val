package Perl::PrereqScanner::NotQuiteLite::Context;

use strict;
use warnings;
use CPAN::Meta::Requirements;
use Regexp::Trie;
use Perl::PrereqScanner::NotQuiteLite::Util;

my %defined_keywords = _keywords();

my %default_op_keywords = map {$_ => 1} qw(
  x eq ne and or xor cmp ge gt le lt not
);

my %default_conditional_keywords = map {$_ => 1} qw(
  if elsif unless else
);

my %default_expects_expr_block = map {$_ => 1} qw(
  if elsif unless given when
  for foreach while until
);

my %default_expects_block_list = map {$_ => 1} qw(
  map grep sort
);

my %default_expects_fh_list = map {$_ => 1} qw(
  print printf say
);

my %default_expects_fh_or_block_list = (
  %default_expects_block_list,
  %default_expects_fh_list,
);

my %default_expects_block = map {$_ => 1} qw(
  else default
  eval sub do while until continue
  BEGIN END INIT CHECK
  if elsif unless given when
  for foreach while until
  map grep sort
);

my %default_expects_word = map {$_ => 1} qw(
  use require no sub
);

my %enables_utf8 = map {$_ => 1} qw(
  utf8
  Mojo::Base
  Mojo::Base::Che
);

my %new_keyword_since = (
  say => '5.010',
  state => '5.010',
  given => '5.010',
  when => '5.010',
  default => '5.010',
);

my $default_g_re_prototype = qr{\G(\([^\)]*?\))};

sub new {
  my ($class, %args) = @_;

  my %context = (
    requires => CPAN::Meta::Requirements->new,
    noes => CPAN::Meta::Requirements->new,
    file => $args{file},
    verbose => $args{verbose},
    stash => {},
  );

  if ($args{suggests} or $args{recommends}) {
    $context{recommends} = CPAN::Meta::Requirements->new;
  }
  if ($args{suggests}) {
    $context{suggests} = CPAN::Meta::Requirements->new;
  }
  if ($args{perl_minimum_version}) {
    $context{perl} = CPAN::Meta::Requirements->new;
  }
  for my $type (qw/use no method keyword sub/) {
    if (exists $args{_}{$type}) {
      for my $key (keys %{$args{_}{$type}}) {
        $context{$type}{$key} = [@{$args{_}{$type}{$key}}];
      }
    }
  }

  bless \%context, $class;
}

sub stash { shift->{stash} }

sub register_keyword_parser {
  my ($self, $keyword, $parser_info) = @_;
  $self->{keyword}{$keyword} = $parser_info;
  $self->{defined_keywords}{$keyword} = 0;
}

sub remove_keyword_parser {
  my ($self, $keyword) = @_;
  delete $self->{keyword}{$keyword};
  delete $self->{keyword} if !%{$self->{keyword}};
  delete $self->{defined_keywords}{$keyword};
}

sub register_method_parser {
  my ($self, $method, $parser_info) = @_;
  $self->{method}{$method} = $parser_info;
}

*register_keyword = \&register_keyword_parser;
*remove_keyword = \&remove_keyword_parser;
*register_method = \&register_method_parser;

sub register_sub_parser {
  my ($self, $keyword, $parser_info) = @_;
  $self->{sub}{$keyword} = $parser_info;
  $self->{defined_keywords}{$keyword} = 0;
}

sub requires { shift->{requires} }
sub recommends { shift->_optional('recommends') }
sub suggests { shift->_optional('suggests') }
sub noes { shift->{noes} }

sub _optional {
  my ($self, $key) = @_;
  my $optional = $self->{$key} or return;

  # no need to recommend/suggest what are listed as requires
  if (my $requires = $self->{requires}) {
    my $hash = $optional->as_string_hash;
    for my $module (keys %$hash) {
      if (defined $requires->requirements_for_module($module) and
          $requires->accepts_module($module, $hash->{$module})
      ) {
        $optional->clear_requirement($module);
      }
    }
  }
  $optional;
}

sub add {
  shift->_add('requires', @_);
}

sub add_recommendation {
  shift->_add('recommends', @_);
}

sub add_suggestion {
  shift->_add('suggests', @_);
}

sub add_conditional {
  shift->_add('conditional', @_);
}

sub add_no {
  shift->_add('noes', @_);
}

sub add_perl {
  my ($self, $perl, $reason) = @_;
  return unless $self->{perl};
  $self->_add('perl', 'perl', $perl);
  $self->{perl_minimum_version}{$reason} = $perl;
}

sub _add {
  my ($self, $type, $module, $version) = @_;
  return unless is_module_name($module);

  my $CMR = $self->_object($type) or return;
  $version = 0 unless defined $version;
  if ($self->{verbose}) {
    if (!defined $CMR->requirements_for_module($module)) {
      print STDERR "  found $module $version ($type)\n";
    }
  }
  $CMR->add_minimum($module, "$version");
}

sub has_added {
  shift->_has_added('requires', @_);
}

sub has_added_recommendation {
  shift->_has_added('recommends', @_);
}

sub has_added_suggestion {
  shift->_has_added('suggests', @_);
}

sub has_added_conditional {
  shift->_has_added('conditional', @_);
}

sub has_added_no {
  shift->_has_added('no', @_);
}

sub _has_added {
  my ($self, $type, $module) = @_;
  return unless is_module_name($module);

  my $CMR = $self->_object($type) or return;
  defined $CMR->requirements_for_module($module) ? 1 : 0;
}

sub _object {
  my ($self, $key) = @_;
  if ($self->{eval}) {
    $key = 'suggests';
  } elsif ($self->{force_cond}) {
    $key = 'recommends';
  } elsif ($key && $key eq 'conditional') {
    if ($self->{cond}) {
      $key = 'recommends';
    } elsif (grep {$_->[0] eq '{' and $_->[2] ne 'BEGIN'} @{$self->{stack} || []}) {
      $key = 'recommends';
    } else {
      $key = 'requires';
    }
  } elsif (!$key) {
    $key = 'requires';
  }
  $self->{$key} or return;
}

sub has_callbacks {
  my ($self, $type) = @_;
  exists $self->{$type};
}

sub has_callback_for {
  my ($self, $type, $name) = @_;
  exists $self->{$type}{$name};
}

sub run_callback_for {
  my ($self, $type, $name, @args) = @_;
  return unless $self->_object;
  my ($parser, $method, @cb_args) = @{$self->{$type}{$name}};
  $parser->$method($self, @cb_args, @args);
}

sub prototype_re {
  my $self = shift;
  if (@_) {
    $self->{prototype_re} = shift;
  }
  return $default_g_re_prototype unless exists $self->{prototype_re};
  $self->{prototype_re};
}

sub quotelike_re {
  my $self = shift;
  return qr/qq?/ unless exists $self->{quotelike_re};
  $self->{quotelike_re};
}

sub register_quotelike_keywords {
  my ($self, @keywords) = @_;
  push @{$self->{quotelike}}, @keywords;
  $self->{defined_keywords}{$_} = 0 for @keywords;

  my $trie = Regexp::Trie->new;
  $trie->add($_) for 'q', 'qq', @{$self->{quotelike} || []};
  $self->{quotelike_re} = $trie->regexp;
}

sub token_expects_block_list {
  my ($self, $token) = @_;
  return 1 if exists $default_expects_block_list{$token};
  return 0 if !exists $self->{expects_block_list};
  return 1 if exists $self->{expects_block_list}{$token};
  return 0;
}

sub token_expects_fh_list {
  my ($self, $token) = @_;
  return 1 if exists $default_expects_fh_list{$token};
  return 0 if !exists $self->{expects_fh_list};
  return 1 if exists $self->{expects_fh_list}{$token};
  return 0;
}

sub token_expects_fh_or_block_list {
  my ($self, $token) = @_;
  return 1 if exists $default_expects_fh_or_block_list{$token};
  return 0 if !exists $self->{expects_fh_or_block_list};
  return 1 if exists $self->{expects_fh_or_block_list}{$token};
  return 0;
}

sub token_expects_expr_block {
  my ($self, $token) = @_;
  return 1 if exists $default_expects_expr_block{$token};
  return 0 if !exists $self->{expects_expr_block};
  return 1 if exists $self->{expects_expr_block}{$token};
  return 0;
}

sub token_expects_block {
  my ($self, $token) = @_;
  return 1 if exists $default_expects_block{$token};
  return 0 if !exists $self->{expects_block};
  return 1 if exists $self->{expects_block}{$token};
  return 0;
}

sub token_expects_word {
  my ($self, $token) = @_;
  return 1 if exists $default_expects_word{$token};
  return 0 if !exists $self->{expects_word};
  return 1 if exists $self->{expects_word}{$token};
  return 0;
}

sub token_is_conditional {
  my ($self, $token) = @_;
  return 1 if exists $default_conditional_keywords{$token};
  return 0 if !exists $self->{is_conditional_keyword};
  return 1 if exists $self->{is_conditional_keyword}{$token};
  return 0;
}

sub token_is_keyword {
  my ($self, $token) = @_;
  return 1 if exists $defined_keywords{$token};
  return 0 if !exists $self->{defined_keywords};
  return 1 if exists $self->{defined_keywords}{$token};
  return 0;
}

sub token_is_op_keyword {
  my ($self, $token) = @_;
  return 1 if exists $default_op_keywords{$token};
  return 0 if !exists $self->{defined_op_keywords};
  return 1 if exists $self->{defined_op_keywords}{$token};
  return 0;
}

sub check_new_keyword {
  my ($self, $token) = @_;
  if (exists $new_keyword_since{$token}) {
    $self->add_perl($new_keyword_since{$token}, $token);
  }
}

sub register_keywords {
  my ($self, @keywords) = @_;
  for my $keyword (@keywords) {
    $self->{defined_keywords}{$keyword} = 0;
  }
}

sub register_op_keywords {
  my ($self, @keywords) = @_;
  for my $keyword (@keywords) {
    $self->{defined_op_keywords}{$keyword} = 0;
  }
}

sub remove_keywords {
  my ($self, @keywords) = @_;
  for my $keyword (@keywords) {
    delete $self->{defined_keywords}{$keyword} if exists $self->{defined_keywords}{$keyword} and !$self->{defined_keywords}{$keyword};
  }
}

sub register_sub_keywords {
  my ($self, @keywords) = @_;
  for my $keyword (@keywords) {
    $self->{defines_sub}{$keyword} = 1;
    $self->{expects_block}{$keyword} = 1;
    $self->{expects_word}{$keyword} = 1;
    $self->{defined_keywords}{$keyword} = 0;
  }
}

sub token_defines_sub {
  my ($self, $token) = @_;
  return 1 if $token eq 'sub';
  return 0 if !exists $self->{defines_sub};
  return 1 if exists $self->{defines_sub}{$token};
  return 0;
}

sub enables_utf8 {
  my ($self, $module) = @_;
  exists $enables_utf8{$module} ? 1 : 0;
}

sub add_package {
  my ($self, $package) = @_;
  $self->{packages}{$package} = 1;
}

sub packages {
  my $self = shift;
  keys %{$self->{packages} || {}};
}

sub remove_inner_packages_from_requirements {
  my $self = shift;
  for my $package ($self->packages) {
    for my $rel (qw/requires recommends suggests noes/) {
      next unless $self->{$rel};
      $self->{$rel}->clear_requirement($package);
    }
  }
}

sub merge_perl {
  my $self = shift;
  return unless $self->{perl};

  my $perl = $self->{requires}->requirements_for_module('perl');
  if ($self->{perl}->accepts_module('perl', $perl)) {
    delete $self->{perl_minimum_version};
  } else {
    $self->add(perl => $self->{perl}->requirements_for_module('perl'));
  }
}

sub _keywords {(
    '__FILE__' => 1,
    '__LINE__' => 2,
    '__PACKAGE__' => 3,
    '__DATA__' => 4,
    '__END__' => 5,
    '__SUB__' => 6,
    AUTOLOAD => 7,
    BEGIN => 8,
    UNITCHECK => 9,
    DESTROY => 10,
    END => 11,
    INIT => 12,
    CHECK => 13,
    abs => 14,
    accept => 15,
    alarm => 16,
    and => 17,
    atan2 => 18,
    bind => 19,
    binmode => 20,
    bless => 21,
    break => 22,
    caller => 23,
    chdir => 24,
    chmod => 25,
    chomp => 26,
    chop => 27,
    chown => 28,
    chr => 29,
    chroot => 30,
    close => 31,
    closedir => 32,
    cmp => 33,
    connect => 34,
    continue => 35,
    cos => 36,
    crypt => 37,
    dbmclose => 38,
    dbmopen => 39,
    default => 40,
    defined => 41,
    delete => 42,
    die => 43,
    do => 44,
    dump => 45,
    each => 46,
    else => 47,
    elsif => 48,
    endgrent => 49,
    endhostent => 50,
    endnetent => 51,
    endprotoent => 52,
    endpwent => 53,
    endservent => 54,
    eof => 55,
    eq => 56,
    eval => 57,
    evalbytes => 58,
    exec => 59,
    exists => 60,
    exit => 61,
    exp => 62,
    fc => 63,
    fcntl => 64,
    fileno => 65,
    flock => 66,
    for => 67,
    foreach => 68,
    fork => 69,
    format => 70,
    formline => 71,
    ge => 72,
    getc => 73,
    getgrent => 74,
    getgrgid => 75,
    getgrnam => 76,
    gethostbyaddr => 77,
    gethostbyname => 78,
    gethostent => 79,
    getlogin => 80,
    getnetbyaddr => 81,
    getnetbyname => 82,
    getnetent => 83,
    getpeername => 84,
    getpgrp => 85,
    getppid => 86,
    getpriority => 87,
    getprotobyname => 88,
    getprotobynumber => 89,
    getprotoent => 90,
    getpwent => 91,
    getpwnam => 92,
    getpwuid => 93,
    getservbyname => 94,
    getservbyport => 95,
    getservent => 96,
    getsockname => 97,
    getsockopt => 98,
    given => 99,
    glob => 100,
    gmtime => 101,
    goto => 102,
    grep => 103,
    gt => 104,
    hex => 105,
    if => 106,
    index => 107,
    int => 108,
    ioctl => 109,
    join => 110,
    keys => 111,
    kill => 112,
    last => 113,
    lc => 114,
    lcfirst => 115,
    le => 116,
    length => 117,
    link => 118,
    listen => 119,
    local => 120,
    localtime => 121,
    lock => 122,
    log => 123,
    lstat => 124,
    lt => 125,
    m => 126,
    map => 127,
    mkdir => 128,
    msgctl => 129,
    msgget => 130,
    msgrcv => 131,
    msgsnd => 132,
    my => 133,
    ne => 134,
    next => 135,
    no => 136,
    not => 137,
    oct => 138,
    open => 139,
    opendir => 140,
    or => 141,
    ord => 142,
    our => 143,
    pack => 144,
    package => 145,
    pipe => 146,
    pop => 147,
    pos => 148,
    print => 149,
    printf => 150,
    prototype => 151,
    push => 152,
    q => 153,
    qq => 154,
    qr => 155,
    quotemeta => 156,
    qw => 157,
    qx => 158,
    rand => 159,
    read => 160,
    readdir => 161,
    readline => 162,
    readlink => 163,
    readpipe => 164,
    recv => 165,
    redo => 166,
    ref => 167,
    rename => 168,
    require => 169,
    reset => 170,
    return => 171,
    reverse => 172,
    rewinddir => 173,
    rindex => 174,
    rmdir => 175,
    s => 176,
    say => 177,
    scalar => 178,
    seek => 179,
    seekdir => 180,
    select => 181,
    semctl => 182,
    semget => 183,
    semop => 184,
    send => 185,
    setgrent => 186,
    sethostent => 187,
    setnetent => 188,
    setpgrp => 189,
    setpriority => 190,
    setprotoent => 191,
    setpwent => 192,
    setservent => 193,
    setsockopt => 194,
    shift => 195,
    shmctl => 196,
    shmget => 197,
    shmread => 198,
    shmwrite => 199,
    shutdown => 200,
    sin => 201,
    sleep => 202,
    socket => 203,
    socketpair => 204,
    sort => 205,
    splice => 206,
    split => 207,
    sprintf => 208,
    sqrt => 209,
    srand => 210,
    stat => 211,
    state => 212,
    study => 213,
    sub => 214,
    substr => 215,
    symlink => 216,
    syscall => 217,
    sysopen => 218,
    sysread => 219,
    sysseek => 220,
    system => 221,
    syswrite => 222,
    tell => 223,
    telldir => 224,
    tie => 225,
    tied => 226,
    time => 227,
    times => 228,
    tr => 229,
    truncate => 230,
    uc => 231,
    ucfirst => 232,
    umask => 233,
    undef => 234,
    unless => 235,
    unlink => 236,
    unpack => 237,
    unshift => 238,
    untie => 239,
    until => 240,
    use => 241,
    utime => 242,
    values => 243,
    vec => 244,
    wait => 245,
    waitpid => 246,
    wantarray => 247,
    warn => 248,
    when => 249,
    while => 250,
    write => 251,
    x => 252,
    xor => 253,
    y => 254 || 255,
)}

1;

__END__

=encoding utf-8

=head1 NAME

Perl::PrereqScanner::NotQuiteLite::Context

=head1 DESCRIPTION

This is typically used to keep callbacks, an eval state, and
found prerequisites for a processing file.

=head1 METHODS

=head2 add

  $c->add($module);
  $c->add($module => $minimum_version);

adds a module with/without a minimum version as a requirement
or a suggestion, depending on the eval state. You can add a module
with different versions as many times as you wish. The actual
minimum version for the module is calculated inside
(by L<CPAN::Meta::Requirements>).

=head2 register_keyword_parser, remove_keyword_parser, register_method_parser, register_sub_parser

  $c->register_keyword_parser(
    'func_name',
    [$parser_class, 'parser_for_the_func', $used_module],
  );
  $c->remove_keyword_parser('func_name');

  $c->register_method_parser(
    'method_name',
    [$parser_class, 'parser_for_the_method', $used_module],
  );

If you find a module that can export a loader function is actually
C<use>d (such as L<Moose> that can export an C<extends> function
that will load a module internally), you might also register the
loader function as a custom keyword dynamically so that the scanner
can also run a callback for the function to parse its argument
tokens.

You can also remove the keyword when you find the module is C<no>ed
(and when the module supports C<unimport>).

You can also register a method callback on the fly (but you can't
remove it).

If you always want to check some functions/methods when you load a
plugin, just register them using a C<register> method in the plugin.

=head2 requires

returns a CPAN::Meta::Requirements object for requirements.

=head2 suggests

returns a CPAN::Meta::Requirements object for suggestions
(requirements in C<eval>s), or undef when it is not expected to
parse tokens in C<eval>.

=head1 METHODS MOSTLY FOR INTERNAL USE

=head2 new

creates an instance. You usually don't need to call this because
it's automatically created in the scanner.

=head2 has_callbacks, has_callback_for, run_callback_for

  next unless $c->has_callbacks('use');
  next unless $c->has_callbacks_for('use', 'base');
  $c->run_callbacks_for('use', 'base', $tokens);

C<has_callbacks> returns true if a callback for C<use>, C<no>,
C<keyword>, or C<method> is registered. C<has_callbacks_for>
returns true if a callback for the module/keyword/method is
registered. C<run_callbacks_for> is to run the callback.

=head2 has_added

returns true if a module has already been added as a requirement
or a suggestion. Only useful for the ::UniversalVersion plugin.

=head1 AUTHOR

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015 by Kenichi Ishigaki.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
