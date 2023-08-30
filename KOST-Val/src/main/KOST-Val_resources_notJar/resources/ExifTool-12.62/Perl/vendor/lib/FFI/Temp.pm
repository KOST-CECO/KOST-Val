package FFI::Temp;

use strict;
use warnings;
use 5.008004;
use Carp qw( croak );
use File::Spec;
use File::Temp qw( tempdir );

# ABSTRACT: Temp Dir support for FFI::Platypus
our $VERSION = '1.34'; # VERSION


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
  unless(-d $root)
  {
    mkdir $root or die "unable to create temp root $!";
  }

  # TODO: doesn't account for fork...
  my $lock = File::Spec->catfile($root, "l$$");
  unless(-f $lock)
  {
    open my $fh, '>', $lock;
    close $fh;
  }
  $root{$root} = 1;
  $root;
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

version 1.34

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
