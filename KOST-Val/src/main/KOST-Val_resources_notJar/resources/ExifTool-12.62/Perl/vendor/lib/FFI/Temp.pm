package FFI::Temp;

use strict;
use warnings;
use 5.008004;
use Carp qw( croak );
use File::Spec;
use File::Temp qw( tempdir );

# ABSTRACT: Temp Dir support for FFI::Platypus
our $VERSION = '2.08'; # VERSION


# problem with vanilla File::Temp is that is often uses
# as /tmp that has noexec turned on.  Workaround is to
# create a temp directory in the build directory, but
# we have to be careful about cleanup.  This puts all that
# (attempted) carefulness in one place so that when we
# later discover it isn't so careful we can fix it in
# one place rather thabn alllll the places that we need
# temp directories.

my %root;

sub _root
{
  my $root = File::Spec->rel2abs(File::Spec->catdir(".tmp"));
  my $lock = File::Spec->catfile($root, "l$$");

  foreach my $try (0..9)
  {
    sleep $try if $try != 0;
    mkdir $root or die "unable to create temp root $!" unless -d $root;

    # There is a race condition here if the FFI::Temp is
    # used in parallel.  To work around we run this 10
    # times until it works.  There is still a race condition
    # if it fails 10 times, but hopefully that is unlikely.

    # ??: doesn't account for fork, but probably doesn't need to.
    open my $fh, '>', $lock or next;
    close $fh or next;

    $root{$root} = 1;
    return $root;
  }
}

END {
  foreach my $root (keys %root)
  {
    my $lock = File::Spec->catfile($root, "l$$");
    unlink $lock;
    # try to delete if possible.
    # if not possible then punt
    rmdir $root if -d $root;
  }
}

sub newdir
{
  my $class = shift;
  croak "uneven" if @_ % 2;
  File::Temp->newdir(DIR => _root, @_);
}

sub new
{
  my $class = shift;
  croak "uneven" if @_ % 2;
  File::Temp->new(DIR => _root, @_);
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Temp - Temp Dir support for FFI::Platypus

=head1 VERSION

version 2.08

=head1 DESCRIPTION

This class is private to L<FFI::Platypus>.

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
