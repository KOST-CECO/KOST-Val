package Module::CPANTS::Kwalitee::NeedsCompiler;
use warnings;
use strict;

our $VERSION = '1.01';
$VERSION =~ s/_//; ## no critic

sub order { 200 }

##################################################################
# Analyse
##################################################################

sub analyse {
    my $class = shift;
    my $me = shift;
    
    my $files = $me->d->{files_array};
    foreach my $f (@$files) {
        if ($f =~ /\.[hc]$/i or $f =~ /\.xs$/i) {
            $me->d->{needs_compiler} = 1;
            return;
        }
    }
    if (defined ref($me->d->{prereq}) and ref($me->d->{prereq}) eq 'ARRAY') {
        for my $m (@{ $me->d->{prereq} }) {
            if ($m->{requires} =~ /^Inline::/
               or $m->{requires} eq 'ExtUtils::CBuilder'
               or $m->{requires} eq 'ExtUtils::ParseXS') {
                $me->d->{needs_compiler} = 1;
                return;
            }
        }
    }
    return;
}

##################################################################
# Kwalitee Indicators
##################################################################

sub kwalitee_indicators{
    return [
    ];
}


q{Favourite compiler:
  gcc};

__END__

=encoding UTF-8

=head1 NAME

Module::CPANTS::Kwalitee::NeedsCompiler - Checks if the module needs a (probably C) compiler

=head1 SYNOPSIS

Checks if there is some indication in the module that it needs a C compiler to build and install

=head1 DESCRIPTION

=head2 Methods

=head3 order

Defines the order in which Kwalitee tests should be run.

Returns C<200>.

=head3 analyse

Checks for file with F<.c>, F<.h> or F<.xs> extensions.
Check is the module depends on any of the Inline:: modules or
on ExtUtils::CBuilder or ExtUtils::ParseXS.

=head3 TODO:

How to recognize cases such as https://metacpan.org/release/GAAS/Perl-API-0.01/
and https://metacpan.org/release/Term-Size-Perl
that generate the .c files during installation

In addition there are modules that can work without their XS part.
E.g. Scalar-List-Utils, Net-DNS, Template-Toolkit 
For our purposes these all should be marked as "need C compiler"
as they need it for their full functionality and speed.
 
=head3 kwalitee_indicators

No Kwalitee Indicator.

=head1 SEE ALSO

L<Module::CPANTS::Analyse>

=head1 AUTHOR

L<Gábor Szabó|https://metacpan.org/author/szabgab>

=head1 COPYRIGHT AND LICENSE

Copyright © 2006–2008 L<Gábor Szabó|https://metacpan.org/author/szabgab>

You may use and distribute this module according to the same terms
that Perl is distributed under.
