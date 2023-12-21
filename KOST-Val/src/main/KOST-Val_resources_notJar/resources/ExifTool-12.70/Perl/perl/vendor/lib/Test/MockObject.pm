package Test::MockObject;
$Test::MockObject::VERSION = '1.20200122';
use strict;
use warnings;

use Scalar::Util qw( blessed refaddr reftype weaken );

sub import
{
    my $self = shift;
    return unless grep /^-debug/, @_;
    eval {
    require UNIVERSAL::isa;
    UNIVERSAL::isa::->import('verbose');
    };
    eval {
    require UNIVERSAL::can;
    UNIVERSAL::can::->import('-always_warn');
    };
}

use Test::Builder;

my $Test = Test::Builder->new();
my (%calls, %subs);

sub new
{
    my ($class, $type) = @_;
    $type ||= {};
    bless $type, $class;
}

sub mock
{
    my ($self, @names_and_subs) = @_;

    while ( my ($name, $sub) = splice @names_and_subs, 0, 2 ) {
        $sub ||= sub {};

        # leading dash means unlog, otherwise do log
        _set_log( $self, $name, ( $name =~ s/^-// ? 0 : 1 ) );
        _subs( $self )->{$name} = $sub;
    }

    $self;
}

sub set_isa
{
    my ($self, @supers) = @_;
    my $supers          = _isas( $self );
    $supers->{$_}       = 1 for @supers;
}

sub set_always
{
    my ($self, $name, $value) = @_;
    $self->mock( $name, sub { $value } );
}

sub set_true
{
    my $self = shift;

    for my $name ( @_ )
    {
        $self->mock( $name, sub { 1 } );
    }

    return $self;
}

sub set_false
{
    my $self = shift;

    for my $name ( @_ )
    {
        $self->mock( $name, sub {} );
    }

    return $self;
}

sub set_list
{
    my ($self, $name, @list) = @_;
    $self->mock( $name, sub { @{[ @list ]} } );
}

sub set_series
{
    my ($self, $name, @list) = @_;
    $self->mock( $name, sub { return unless @list; shift @list } );
}

sub set_bound
{
    my ($self, $name, $ref) = @_;

    my %bindings =
    (
        SCALAR => sub { $$ref },
        ARRAY  => sub { @$ref },
        HASH   => sub { %$ref },
    );

    return unless exists $bindings{reftype( $ref )};
    $self->mock( $name,  $bindings{reftype( $ref )} );
}

# hack around debugging mode being too smart for my sub names
my $old_p;
BEGIN
{
    $old_p  = $^P;
    $^P    &= ~0x200;
}

BEGIN
{
    for my $universal
    ( { sub => \&_subs, name => 'can' }, { sub => \&_isas, name => 'isa' } )
    {
        my $sub = sub
        {
            my ($self, $sub) = @_;
            local *__ANON__  = $universal->{name};

            # mockmethods are special cases, class methods are handled directly
            my $lookup = $universal->{sub}->( $self );
            return $lookup->{$sub} if blessed $self and exists $lookup->{$sub};
            my $parent = 'SUPER::' . $universal->{name};
            return $self->$parent( $sub );
        };

        no strict 'refs';
        *{ $universal->{name} } = $sub;
    }

    $^P = $old_p;
}

sub remove
{
    my ($self, $sub) = @_;
    delete _subs( $self )->{$sub};
    $self;
}

sub called
{
    my ($self, $sub) = @_;

    for my $called (reverse @{ _calls( $self ) })
    {
        return 1 if $called->[0] eq $sub;
    }

    return 0;
}

sub clear
{
    my $self             = shift;
    @{ _calls( $self ) } = ();
    $self;
}

sub call_pos
{
    $_[0]->_call($_[1], 0);
}

sub call_args
{
    return @{ $_[0]->_call($_[1], 1) };
}

sub _call
{
    my ($self, $pos, $type) = @_;
    my $calls               = _calls( $self );
    return if abs($pos) > @$calls;
    $pos-- if $pos > 0;
    return $calls->[$pos][$type];
}

sub call_args_string
{
    my $args = $_[0]->_call( $_[1], 1 ) or return;
    return join($_[2] || '', @$args);
}

sub call_args_pos
{
    my ($self, $subpos, $argpos) = @_;
    my $args = $self->_call( $subpos, 1 ) or return;
    $argpos-- if $argpos > 0;
    return $args->[$argpos];
}

sub next_call
{
    my ($self, $num)  = @_;
    $num            ||= 1;

    my $calls = _calls( $self );
    return unless @$calls >= $num;

    my ($call) = (splice(@$calls, 0, $num))[-1];
    return wantarray() ? @$call : $call->[0];
}

sub AUTOLOAD
{
    our $AUTOLOAD;

    my $self = shift;
    my $sub;
    {
        local $1;
        ($sub) = $AUTOLOAD =~ /::(\w+)\z/;
    }
    return if $sub eq 'DESTROY';

    $self->dispatch_mocked_method( $sub, @_ );
}

sub dispatch_mocked_method
{
    my $self = $_[0];
    my $sub  = splice( @_, 1, 1 );

    my $subs = _subs( $self );
    if (exists $subs->{$sub})
    {
        $self->log_call( $sub, @_ );
        goto &{ $subs->{$sub} };
    }
    else
    {
        require Carp;
        Carp::carp("Un-mocked method '$sub()' called");
    }

    return;
}

sub log_call
{
    my ($self, $sub, @call_args) = @_;
    return unless _logs( $self, $sub );

    # prevent circular references with weaken
    for my $arg ( @call_args )
    {
        next unless ref $arg;
        weaken( $arg ) if refaddr( $arg ) eq refaddr( $self );
    }

    push @{ _calls( $self ) }, [ $sub, \@call_args ];
}

sub called_ok
{
    my ($self, $sub, $name) = @_;
    $name ||= "object called '$sub'";
    $Test->ok( $self->called($sub), $name );
}

sub called_pos_ok
{
    my ($self, $pos, $sub, $name) = @_;
    $name ||= "object called '$sub' at position $pos";
    my $called = $self->call_pos($pos, $sub);
    unless ($Test->ok( (defined $called and $called eq $sub), $name ))
    {
        $called = 'undef' unless defined $called;
        $Test->diag("Got:\n\t'$called'\nExpected:\n\t'$sub'\n");
    }
}

sub called_args_string_is
{
    my ($self, $pos, $sep, $expected, $name) = @_;
    $name ||= "object sent expected args to sub at position $pos";
    $Test->is_eq( $self->call_args_string( $pos, $sep ), $expected, $name );
}

sub called_args_pos_is
{
    my ($self, $pos, $argpos, $arg, $name) = @_;
    $name ||= "object sent expected arg '$arg' to sub at position $pos";
    $Test->is_eq( $self->call_args_pos( $pos, $argpos ), $arg, $name );
}

sub fake_module
{
    my ($class, $modname, %subs) = @_;

    if ($class->check_class_loaded( $modname ) and ! keys %subs)
    {
        require Carp;
        Carp::croak( "No mocked subs for loaded module '$modname'" );
    }

    $modname =~ s!::!/!g;
    $INC{ $modname . '.pm' } = 1;

    no warnings 'redefine';
    {
        no strict 'refs';
        ${ $modname . '::' }{VERSION} ||= -1;
    }

    for my $sub (keys %subs)
    {
        my $type = reftype( $subs{ $sub } ) || '';
        unless ( $type eq 'CODE' )
        {
            require Carp;
            Carp::carp("'$sub' is not a code reference" );
            next;
        }
        no strict 'refs';
        *{ $_[1] . '::' . $sub } = $subs{ $sub };
    }
}

sub check_class_loaded
{
    my ($self, $class, $load_flag) = @_;

    (my $path    = $class) =~ s{::}{/}g;
    return 1 if exists $INC{ $path . '.pm' };

    my $symtable = \%main::;
    my $found    = 1;

    for my $symbol ( split( '::', $class ))
    {
        unless (exists $symtable->{ $symbol . '::' })
        {
            $found = 0;
            last;
        }

        $symtable = $symtable->{ $symbol . '::' };
    }

    return $found;
}

sub fake_new
{
    my ($self, $class) = @_;
    $self->fake_module( $class, new => sub { $self } );
}

sub DESTROY
{
    my $self = shift;
    $self->_clear_calls();
    $self->_clear_subs();
    $self->_clear_logs();
    $self->_clear_isas();
}

sub _get_key
{
    my $invocant = shift;
    return blessed( $invocant ) ? refaddr( $invocant ) : $invocant;
}

{
    my %calls;

    sub _calls
    {
        $calls{ _get_key( shift ) } ||= [];
    }

    sub _clear_calls
    {
        delete $calls{ _get_key( shift ) };
    }
}

{
    my %subs;

    sub _subs
    {
        $subs{ _get_key( shift ) } ||= {};
    }

    sub _clear_subs
    {
        delete $subs{ _get_key( shift ) };
    }
}

{
    my %logs;

    sub _set_log
    {
        my $key          = _get_key( shift );
        my ($name, $log) = @_;

        $logs{$key} ||= {};

        if ($log)
        {
            $logs{$key}{$name} = 1;
        }
        else
        {
            delete $logs{$key}{$name};
        }
    }

    sub _logs
    {
        my $key    = _get_key( shift );
        my ($name) = @_;
        return exists $logs{$key}{$name};
    }

    sub _clear_logs
    {
        delete $logs{ _get_key( shift ) };
    }
}

{
    my %isas;

    sub _isas
    {
        $isas{ _get_key( shift ) } ||= {};
    }

    sub _clear_isas
    {
        delete $isas{ _get_key( shift ) };
    }
}

1;

__END__

=head1 NAME

Test::MockObject - Perl extension for emulating troublesome interfaces

=head1 SYNOPSIS

  use Test::MockObject;
  my $mock = Test::MockObject->new();
  $mock->set_true( 'somemethod' );
  ok( $mock->somemethod() );

  $mock->set_true( 'veritas')
       ->set_false( 'ficta' )
       ->set_series( 'amicae', 'Sunny', 'Kylie', 'Bella' );

=head1 DESCRIPTION

It's a simple program that doesn't use any other modules, and those are easy to
test.  More often, testing a program completely means faking up input to
another module, trying to coax the right output from something you're not
supposed to be testing anyway.

Testing is a lot easier when you can control the entire environment.  With
Test::MockObject, you can get a lot closer.

Test::MockObject allows you to create objects that conform to particular
interfaces with very little code.  You don't have to reimplement the behavior,
just the input and the output.

=head2 IMPORTANT CAVEATS

Before you go wild with your testing powers, consider three caveats:

=over 4

=item * It is possible to write highly detailed unit tests that pass even when
your integration tests may fail.  Testing the pieces individually does not
excuse you from testing the whole thing together.

=item * In cases where you only need to mock one or two pieces of an existing
module, consider L<Test::MockObject::Extends> instead.

=item * If the code under testing produces strange errors about type checks,
pass the C<-debug> flag when using C<Test::MockObject> or
C<Test::MockObject::Extends>. This will load both L<UNIVERSAL::isa> and
L<UNIVERSAL::can> to perform additional debugging on the incorrect use of both
methods from the L<UNIVERSAL> package. (This behavior used to be active by
default, but that was, however correct, probably a burden to onerous for the
CPAN.)

=back

=head2 EXPORT

None.

=head2 METHODS

The most important thing a Mock Object can do is to conform sufficiently to an
interface.  For example, if you're testing something that relies on CGI.pm, you
may find it easier to create a mock object that returns controllable results
at given times than to fake query string input.

=head3 The Basics

=over 4

=item * C<new>

Creates a new mock object.  By default, this is a blessed hash.  Pass a
reference to bless that reference.

    my $mock_array  = Test::MockObject->new( [] );
    my $mock_scalar = Test::MockObject->new( \( my $scalar ) );
    my $mock_code   = Test::MockObject->new( sub {} );
    my $mock_glob   = Test::MockObject->new( \*GLOB );

=back

=head3 Mocking

Your mock object is nearly useless if you don't tell it what it's mocking.
This is done by installing methods.  You control the output of these mocked
methods.  In addition, any mocked method is tracked.  You can tell not only
what was called, but which arguments were passed.  Please note that you cannot
track non-mocked method calls.  They will still be allowed, though
Test::MockObject will carp() about them.  This is considered a feature, though
it may be possible to disable this in the future.

As implied in the example above, it's possible to chain these calls together.
Thanks to a suggestion from the fabulous Piers Cawley (CPAN RT #1249), this
feature came about in version 0.09.  Shorter testing code is nice!

=over 4

=item * C<mock(I<name>, I<coderef> [, I<name2>, I<coderef2>, ...])>

Adds one or more coderefs to the object.  This allows code to call the named
methods on the object.  For example, this code:

    my $mock = Test::MockObject->new();
    $mock->mock( 'fluorinate',
        sub { 'impurifying precious bodily fluids' } );
    print $mock->fluorinate;

will print a helpful warning message.  Please note that methods are only added
to a single object at a time and not the class.  (There is no small similarity
to the Self programming language or the L<Class::Prototyped> module.)

This method forms the basis for most of Test::MockObject's testing goodness.

B<Please Note:> this method used to be C<add()>.  Due to its ambiguity, it now
has a different spelling.  For backwards compatibility purposes, add() is
available, though version 0.07 deprecated it.  It goes to some contortions to
try to do what you mean, but I make few guarantees.

=item * C<fake_module(I<module name>), [ I<subname> => I<coderef>, ... ]

B<Note:> See L<Test::MockModule> for an alternate (and better) approach.

Lies to Perl that it has already loaded a named module.  This is handy when
providing a mockup of a real module if you'd like to prevent the actual module
from interfering with the nice fakery.  If you're mocking L<Regexp::English>,
say:

    $mock->fake_module( 'Regexp::English' );

This is both a class and as an object method.  Beware that this must take place
before the actual module has a chance to load.  Either wrap it in a BEGIN block
before a use or require or place it before a C<use_ok()> or C<require_ok()>
call.

You can optionally add functions to the mocked module by passing them as name
=> coderef pairs to C<fake_module()>.  This is handy if you want to test an
C<import()>:

    my $import;
    $mock->fake_module(
        'Regexp::English',
        import => sub { $import = caller }
    );
    use_ok( 'Regexp::Esperanto' );
    is( $import, 'Regexp::Esperanto',
        'Regexp::Esperanto should use() Regexp::English' );

If you use C<fake_module()> to mock a module that already exists in memory --
one you've loaded elsewhere perhaps, but do not pass any subroutines to mock,
this method will throw an exception.  This is because if you call the
constructor later on, you probably won't get a mock object back and you'll be
confused.

=item * C<fake_new(I<module name>)>

B<Note:> see L<Test::MockObject::Extends> for a better alternative to this
method.

Provides a fake constructor for the given module that returns the invoking mock
object.  Used in conjunction with C<fake_module()>, you can force the tested
unit to work with the mock object instead.

    $mock->fake_module( 'CGI' );
    $mock->fake_new( 'CGI' );

    use_ok( 'Some::Module' );
    my $s = Some::Module->new();
    is( $s->{_cgi}, $mock,
        'new() should create and store a new CGI object' );

=item * C<set_always(I<name>, I<value>)>

Adds a method of the specified name that always returns the specified value.

=item * C<set_true(I<name_1>, I<name_2>, ... I<name_n>)>

Adds a method of the specified name that always returns a true value.  This can
take a list of names.

=item * C<set_false(I<name_1>, I<name_2>, ... I<name_n>)>

Adds a method of the specified name that always returns a false value.  (Since
it installs an empty subroutine, the value should be false in both scalar and
list contexts.)  This can take a list of names.

=item * C<set_list(I<name>, [ I<item1>, I<item2>, ... ]>

Adds a method that always returns a given list of values.  It takes some care
to provide a list and not an array, if that's important to you.

=item * C<set_series(I<name>, [ I<item1>, I<item2>, ... ]>

Adds a method that will return the next item in a series on each call.  This
can help to test error handling, by forcing a failure on the first method call
and then subsequent successes.  Note that the series does not repeat; it will
eventually run out.

=item * C<set_bound(I<name>, I<reference>)>

Adds a method bound to a variable.  Pass in a reference to a variable in your
test.  When you change the variable, the return value of the new method will
change as well.  This is often handier than replacing mock methods.

=item * C<set_isa( I<name1>, I<name2>, ... I<namen> )>

Adds an apparent parent to the module, so that calling C<isa()> on the mock
will return true appropriately.  Sometimes you really need this.

=item * C<remove(I<name>)>

Removes a named method.

=back

=head3 Checking Your Mocks

=over 4

=item * C<can( $method_name )>

Returns a subroutine reference if this particular mocked object can handle the
named method, false otherwise.

=item * C<isa( $class_name )>

Returns true if the invocant object mocks a particular class.  You must have
used C<set_isa()> first.

=item * C<called(I<name>)>

Checks to see if something has called a named method on the object.  This
returns a boolean value.  The current implementation does not scale especially
well, so use this sparingly if you need to search through hundreds of calls.

=item * C<clear()>

Clears the internal record of all method calls on the object.  It's handy to do
this every now and then.  Note that this does not affect the mocked methods,
only all of the methods called on the object to this point.

It's handy to C<clear()> methods in between series of tests.  That makes it
much easier to call C<next_method()> without having to skip over the calls from
the last set of tests.

=item * C<next_call([ I<position> ])>

Returns the name and argument list of the next mocked method called on an
object, in list context.  In scalar context, returns only the method name.
There are two important things to know about this method.  First, it starts at
the beginning of the call list.  If your code runs like this:

    $mock->set_true( 'foo' );
    $mock->set_true( 'bar' );
    $mock->set_true( 'baz' );

    $mock->foo();
    $mock->bar( 3, 4 );
    $mock->foo( 1, 2 );

Then you might see output of:

    my ($name, $args) = $mock->next_call();
    print "$name (@$args)";

    # prints 'foo'

    $name = $mock->next_call();
    print $name;

    # prints 'bar'

    ($name, $args) = $mock->next_call();
    print "$name (@$args)";

    # prints 'foo 1 2'

If you provide an optional number as the I<position> argument, the method will
skip that many calls, returning the data for the last one skipped.

    $mock->foo();
    $mock->bar();
    $mock->baz();

    $name = $mock->next_call();
    print $name;

    # prints 'foo'

    $name = $mock->next_call( 2 );
    print $name

    # prints 'baz'

When it reaches the end of the list, it returns undef.  This is probably the
most convenient method in the whole module, but for the sake of completeness
and backwards compatibility (it takes me a while to reach the truest state of
laziness!), there are several other methods.

=item * C<call_pos(I<position>)>

Returns the name of the method called on the object at a specified position.
This is handy if you need to test a certain order of calls.  For example:

    Some::Function( $mock );
    is( $mock->call_pos(1),  'setup',
        'Function() should first call setup()' );
    is( $mock->call_pos(-1), 'end',
        '... and last call end()' );

Positions can be positive or negative.  Please note that the first position is,
in fact, 1.  (This may change in the future.  I like it, but am willing to
reconsider.)

=item * C<call_args(I<position>)>

Returns a list of the arguments provided to the method called at the appropriate
position.  Following the test above, one might say:

    is( ($mock->call_args(1))[0], $mock,
        '... passing the object to setup()' );
    is( scalar $mock->call_args(-1), 0,
        '... and no args to end()' );

=item * C<call_args_pos(I<call position>, I<argument position>)>

Returns the argument at the specified position for the method call at the
specified position.  One might rewrite the first test of the last example as:

    is( $mock->call_args_pos(1, 1), $mock,
        '... passing the object to setup()');

=item * C<call_args_string(I<position>, [ I<separator> ])>

Returns a stringified version of the arguments at the specified position.  If
no separator is given, they will not be separated.  This can be used as:

    is( $mock->call_args_string(1), "$mock initialize",
        '... passing object, initialize as arguments' );

=item * C<called_ok(I<method name>, [ I<test name> ])>

Tests to see whether a method of the specified name has been called on the
object.  This and the following methods use Test::Builder, so they integrate
nicely with a test suite built around Test::Simple, Test::More, or anything
else compatible:

    $mock->foo();
    $mock->called_ok( 'foo' );

A generic default test name is provided.

=item * C<called_pos_ok(I<position>, I<method name>, [ I<test name> ])>

Tests to see whether the named method was called at the specified position.  A
default test name is provided.

=item * C<called_args_pos_is(I<method position>, I<argument position>, I<expected>, [ I<test name> ])>

Tests to see whether the argument at the appropriate position of the method in
the specified position equals a specified value.  A default, rather
non-descript test name is provided.

=item * C<called_args_string_is(I<method position>, I<separator>, I<expected>, [ I<test name> ])>

Joins together all of the arguments to a method at the appropriate position and
matches against a specified string.  A generically bland test name is provided
by default.  You can probably do much better.

=item * C<check_class_loaded( $class_name )>

Attempts to determine whether you have a class of the given name loaded and
compiled.  Returns true or false.

=back

=head3 Logging

Test::MockObject logs all mocked methods by default.  Sometimes you don't want
to do this.  To prevent logging all calls to a given method, prepend the name
of the method with C<-> when mocking it.

That is:

    $mock->set_true( '-foo', 'bar' );

will set mock both C<foo()> and C<bar()>, causing both to return true.
However, the object will log only calls to C<bar()>, not C<foo()>.  To log
C<foo()> again, merely mock it again without the leading C<->:

    $mock->set_true( 'foo' );

C<$mock> will log all subsequent calls to C<foo()> again.

=head3 Subclassing

There are two methods provided for subclassing:

=over 4

=item * C<dispatch_mocked_method( $method_name, @_ )>

This method determines how to call a method (named as C<$method_name>) not
available in this class.  It also controls logging.  You may or may not find it
useful, but I certainly take advantage of it for Test::MockObject::Extends.

=item * C<log_call( $method_name, @_ )>

This method tracks the call of the named method and its arguments.

=back

=head1 TODO

=over 4

=item * Add a factory method to avoid namespace collisions (soon)

=item * Add more useful methods (catch C<import()>?)

=back

=head1 AUTHOR

chromatic, E<lt>chromatic at wgz dot orgE<gt>

Thanks go to Curtis 'Ovid' Poe, as well as ONSITE! Technology, Inc., for
finding several bugs and providing several constructive suggestions.

Jay Bonci also found a false positive in C<called_ok()>.  Thanks!

Chris Winters was the first to report I'd accidentally scheduled 0.12 for
deletion without uploading a newer version.  He also gave useful feedback on
Test::MockObject::Extends.

Stevan Little provided the impetus and code for C<set_isa()>.

Nicholas Clark found a documentation error.

Mutant suggested a potential problem with fake_module().

=head1 SEE ALSO

L<perl>, L<Test::Tutorial>, L<Test::More>,
L<http:E<sol>E<sol>www.perl.comE<sol>pubE<sol>aE<sol>2001E<sol>12E<sol>04E<sol>testing.html>,
and
L<http:E<sol>E<sol>www.perl.comE<sol>pubE<sol>aE<sol>2002E<sol>07E<sol>10E<sol>tmo.html>.

=head1 COPYRIGHT

Copyright (c) 2002 - 2016 by chromatic E<lt>chromatic at wgz dot orgE<gt>.

This program is free software; you can use, modify, and redistribute it under
the same terms as Perl 5.24 itself.

See http://www.perl.com/perl/misc/Artistic.html

=cut
