package V;
use strict;

use vars qw( $VERSION $NO_EXIT );
$VERSION  = '0.16';

$NO_EXIT ||= 0; # prevent import() from exit()ing and fall of the edge

=head1 NAME

V - Print version of the specified module(s).

=head1 SYNOPSIS

    $ perl -MV=V

or if you want more than one

    $ perl -MV=CPAN,V

Can now also be used as a light-weight module for getting versions of
modules without loading them:

    require V;
    printf "%s has version '%s'\n", "V", V::get_version( "V" );

If you want all available files/versions from C<@INC>:

    require V;
    my @all_V = V::Module::Info->all_installed("V");
    printf "%s:\n", $all_V[0]->name;
    printf "\t%-50s - %s\n", $_->file, $_->version
        for @all_V;

Each element in that array isa C<V::Module::Info> object with 3 attributes and a method:

=over

=item I<attribute> B<name>

The package name.

=item I<attribute> B<file>

Full filename with directory.

=item I<attribute> B<dir>

The base directory (from C<@INC>) where the package-file was found.

=item I<method> B<version>

This method will look through the file to see if it can find a version
assignment in the file and uses that determine the version. As of version
0.13_01, all versions found are passed through the L<version> module.

=back

=head1 DESCRIPTION

This module uses stolen code from L<Module::Info> to find the location
and version of the specified module(s). It prints them and exit()s.

It defines C<import()> and is based on an idea from Michael Schwern
on the perl5-porters list. See the discussion:

  https://www.nntp.perl.org/group/perl.perl5.porters/2002/01/msg51007.html

=head2 V::get_version($pkg)

Returns the version of the first available file for this package as found by
following C<@INC>.

=head3 Arguments

=over

=item 1. $pkg

The name of the package for which one wants to know the version.

=back

=head3 Response

This C<V::get_version()> returns the version of the file that was first found
for this package by following C<@INC> or C<undef> if no file was found.

=begin implementation

=head2 report_pkg

This sub prints the results for a package.

=head3 Arguments

=over

=item 1. $pkg

The name of the package that was probed for versions

=item 2. @versions

An array of Module-objects with full path and version.

=back

=end implementation

=head1 AUTHOR

Abe Timmerman C<< <abeltje@cpan.org> >>.

=head1 COPYRIGHT & LICENSE

Copyright 2002-2006 Abe Timmerman, All Rights Reserved.

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

=cut

sub report_pkg($@) {
    my $pkg = shift;

    print "$pkg\n";
    @_ or print "\tNot found\n";
    for my $module ( @_ ) {
        printf "\t%s: %s\n", $module->file, $module->version || '?';
    }
}

sub import {
    shift;
    @_ or push @_, 'V';

   for my $pkg ( @_ ) {
        my @modules = V::Module::Info->all_installed( $pkg );
        report_pkg $pkg, @modules;
    }
    exit() unless $NO_EXIT;
}

sub get_version {
    my( $pkg ) = @_;
    my( $first ) = V::Module::Info->all_installed( $pkg );
    return $first ? $first->version : undef;
}

caller or V->import( @ARGV );

1;

# Okay I did the AUTOLOAD() bit, but this is a Copy 'n Paste job.
# Thank you Michael Schwern for Module::Info! This one is mostly that!

package V::Module::Info;

require File::Spec;

sub new_from_file {
    my($proto, $file) = @_;
    my($class) = ref $proto || $proto;

    return unless -r $file;

    my $self = {};
    $self->{file} = File::Spec->rel2abs($file);
    $self->{dir}  = '';
    $self->{name} = '';

    return bless $self, $class;
}

sub all_installed {
    my($proto, $name, @inc) = @_;
    my($class) = ref $proto || $proto;

    @inc = @INC unless @inc;
    my $file = File::Spec->catfile(split m/::/, $name) . '.pm';

    my @modules = ();
    foreach my $dir (@inc) {
        # Skip the new code ref in @INC feature.
        next if ref $dir;

        my $filename = File::Spec->catfile($dir, $file);
        if( -r $filename ) {
            my $module = $class->new_from_file($filename);
            $module->{dir} = File::Spec->rel2abs($dir);
            $module->{name} = $name;
            push @modules, $module;
        }
    }

    return @modules;
}

# Thieved from ExtUtils::MM_Unix 1.12603
sub version {
    my($self) = shift;

    my $parsefile = $self->file;

    open(my $mod, '<', $parsefile) or die "open($parsefile): $!";

    my $inpod = 0;
    my $result;
    local $_;
    while (<$mod>) {
        $inpod = /^=(?!cut)/ ? 1 : /^=cut/ ? 0 : $inpod;
        next if $inpod || /^\s*#/;

        chomp;
        my $eval;
        if (m/([\$*])(([\w\:\']*)\bVERSION)\b.*\=/) {
            { local($1, $2); ($_ = $_) = m/(.*)/; } # untaint
            $eval = qq{
                package V::Module::Info::_version;
                no strict;

                local $1$2;
                \$$2=undef; do {
                    $_
                }; \$$2
            };
        }
        # perl 5.12.0+
        elsif (m/^\s* package \s+ [^\s]+ \s+ ([^;\{]+) [;\{]/x) {
            $eval = qq{
                package V::Module::Info::_version $1;
                V::Module::Info::_version->VERSION;;
            };
        }
        if (defined($eval)) {
            local $^W = 0;
            $result = eval($eval);
            warn "Could not eval '$eval' in $parsefile: $@" if $@;
            $result = "undef" unless defined $result;

            # use the version modulue to deal with v-strings
            require version;
            $result = version->parse($result);
            last;
        }
    }
    close($mod);
    return $result;
}

sub accessor {
    my $self = shift;
    my $field = shift;

    $self->{ $field } = $_[0] if @_;
    return $self->{ $field };
}

sub AUTOLOAD {
    my( $self ) = @_;

    use vars qw( $AUTOLOAD );
    my( $method ) = $AUTOLOAD =~ m|.+::(.+)$|;

    if ( exists $self->{ $method } ) {
        splice @_, 1, 0, $method;
        goto &accessor;
    }
}
