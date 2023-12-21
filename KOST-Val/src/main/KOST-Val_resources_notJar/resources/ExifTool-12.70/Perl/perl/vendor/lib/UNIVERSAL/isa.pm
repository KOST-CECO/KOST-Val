use strict;
use warnings;
package UNIVERSAL::isa; # git description: 1.20150614-11-gdfa589a
# ABSTRACT: Attempt to recover from people calling UNIVERSAL::isa as a function

our $VERSION = '1.20171012';

use 5.006002;

use Scalar::Util ();
use warnings::register; # creates a warnings category for this module

my ( $orig, $verbose_warning );

BEGIN { $orig = \&UNIVERSAL::isa }

sub original_isa { goto $orig }

sub import
{
    my $class = shift;
    no strict 'refs';

    for my $arg (@_)
    {
        *{ caller() . '::isa' } = \&UNIVERSAL::isa if $arg eq 'isa';
        $verbose_warning = 1 if $arg eq 'verbose';
    }
}

our $_recursing;

no warnings 'redefine';
sub UNIVERSAL::isa
{
    goto &original_isa if $_recursing;
    my $type = _invocant_type(@_);
    $type->(@_);
}
use warnings;

sub _invocant_type
{
    my $invocant = shift;
    return \&_nonsense unless defined($invocant);
    return \&_object_or_class if Scalar::Util::blessed($invocant);
    return \&_reference       if ref($invocant);
    return \&_nonsense unless $invocant;
    return \&_object_or_class;
}

sub _nonsense
{
    _report_warning('on invalid invocant') if $verbose_warning;
    return;
}

sub _object_or_class
{
    local $@;
    local $_recursing = 1;

    if ( my $override = eval { $_[0]->can('isa') } )
    {
        unless ( $override == \&UNIVERSAL::isa )
        {
            _report_warning();
            my $obj = shift;
            return $obj->$override(@_);
        }
    }

    _report_warning() if $verbose_warning;
    goto &original_isa;
}

sub _reference
{
    _report_warning('Did you mean to use Scalar::Util::reftype() instead?')
        if $verbose_warning;
    goto &original_isa;
}

sub _report_warning
{
    my $extra = shift;
    $extra = $extra ? " ($extra)" : '';

    if ( warnings::enabled() )
    {
        # check calling sub
        return if (( caller(3) )[3] || '') =~ /::isa$/;
        # check calling package - exempt Test::Builder??
        return if (( caller(3) )[0] || '') =~ /^Test::Builder/;
        return if (( caller(2) )[0] || '') =~ /^Test::Stream/;

        warnings::warn(
            "Called UNIVERSAL::isa() as a function, not a method$extra" );
    }
}

__PACKAGE__;

__END__

=pod

=encoding UTF-8

=head1 NAME

UNIVERSAL::isa - Attempt to recover from people calling UNIVERSAL::isa as a function

=head1 VERSION

version 1.20171012

=head1 SYNOPSIS

    # from the shell
    echo 'export PERL5OPT=-MUNIVERSAL::isa' >> /etc/profile

    # within your program
    use UNIVERSAL::isa;

    # enable warnings for all dodgy uses of UNIVERSAL::isa
    use UNIVERSAL::isa 'verbose';

=head1 DESCRIPTION

Whenever you use L<UNIVERSAL/isa> as a function, a kitten using
L<Test::MockObject> dies. Normally, the kittens would be helpless, but if they
use L<UNIVERSAL::isa> (the module whose docs you are reading), the kittens can
live long and prosper.

This module replaces C<UNIVERSAL::isa> with a version that makes sure that,
when called as a function on objects which override C<isa>, C<isa> will call
the appropriate method on those objects

In all other cases, the real C<UNIVERSAL::isa> gets called directly.

B<NOTE:> You should use this module only for debugging purposes. It does not
belong as a dependency in running code.

=head1 FUNCTIONS

=head2 original_isa

This sub contains the definition of the I<original> C<UNIVERSAL::isa>
definition, in case you need it.

=head1 WARNINGS

If the lexical warnings pragma is available, this module will emit a warning
for each naughty invocation of C<UNIVERSAL::isa>. Silence these warnings by
saying:

    no warnings 'UNIVERSAL::isa';

in the lexical scope of the naughty code.

After version 1.00, warnings only appear when naughty code calls
UNIVERSAL::isa() as a function on an invocant for which there is an overridden
isa().  These are really truly I<active> bugs, and you should fix them rather
than relying on this module to find them.

To get warnings for all potentially dangerous uses of UNIVERSAL::isa() as a
function, not a method (that is, for I<all> uses of the method as a function,
which are latent bugs, if not bugs that will break your code as it exists now),
pass the C<verbose> flag when using the module.  This can generate many extra
warnings, but they're more specific as to the actual wrong practice and they
usually suggest proper fixes.

=head1 SEE ALSO

L<Perl::Critic::Policy::BuiltinFunctions::ProhibitUniversalIsa>

L<UNIVERSAL::can> for another discussion of the problem at hand.

L<Test::MockObject> for one example of a module that really needs to override
C<isa()>.

Any decent explanation of OO to understand why calling methods as functions is
a staggeringly bad idea.

=head1 SUPPORT

Bugs may be submitted through L<the RT bug tracker|https://rt.cpan.org/Public/Dist/Display.html?Name=UNIVERSAL-isa>
(or L<bug-UNIVERSAL-isa@rt.cpan.org|mailto:bug-UNIVERSAL-isa@rt.cpan.org>).

=head1 AUTHORS

=over 4

=item *

Audrey Tang <cpan@audreyt.org>

=item *

chromatic <chromatic@wgz.org>

=item *

יובל קוג'מן (Yuval Kogman) <nothingmuch@woobling.org>

=back

=head1 CONTRIBUTORS

=for stopwords Karen Etheridge Graham Knop Ricardo Signes

=over 4

=item *

Karen Etheridge <ether@cpan.org>

=item *

Graham Knop <haarg@haarg.org>

=item *

Ricardo Signes <rjbs@cpan.org>

=back

=head1 COPYRIGHT AND LICENCE

This software is copyright (c) 2011 by chromatic@wgz.org.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
