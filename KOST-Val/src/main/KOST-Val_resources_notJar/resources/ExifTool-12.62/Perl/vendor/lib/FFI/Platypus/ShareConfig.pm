package FFI::Platypus::ShareConfig;

use strict;
use warnings;
use 5.008004;
use File::Spec;

our $VERSION = '1.34'; # VERSION

sub dist_dir ($)
{
  my($dist_name) = @_;

  my @pm = split /-/, $dist_name;
  $pm[-1] .= ".pm";

  foreach my $inc (@INC)
  {
    if(-f File::Spec->catfile($inc, @pm))
    {
      my $share = File::Spec->catdir($inc, qw( auto share dist ), $dist_name );
      if(-d $share)
      {
        return File::Spec->rel2abs($share);
      }
      last;
    }
  }
  Carp::croak("unable to find dist share directory for $dist_name");
}

sub get
{
  my(undef, $name) = @_;
  my $config;

  unless($config)
  {
    my $fn = File::Spec->catfile(dist_dir('FFI-Platypus'), 'config.pl');
    $fn = File::Spec->rel2abs($fn) unless File::Spec->file_name_is_absolute($fn);
    local $@;
    unless($config = do $fn)
    {
      die "couldn't parse configuration $fn $@" if $@;
      die "couldn't do $fn $!"                  if $!;
      die "bad or missing config file $fn";
    };
  }

  defined $name ? $config->{$name} : $config;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::ShareConfig

=head1 VERSION

version 1.34

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
