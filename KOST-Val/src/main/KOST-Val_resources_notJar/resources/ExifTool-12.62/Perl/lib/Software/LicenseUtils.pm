use strict;
use warnings;
use Carp;

package Software::LicenseUtils;
# ABSTRACT: little useful bits of code for licensey things
$Software::LicenseUtils::VERSION = '0.103014';
use File::Spec;
use IO::Dir;
use Module::Load;

#pod =method guess_license_from_pod
#pod
#pod   my @guesses = Software::LicenseUtils->guess_license_from_pod($pm_text);
#pod
#pod Given text containing POD, like a .pm file, this method will attempt to guess
#pod at the license under which the code is available.  This method will either
#pod a list of Software::License classes (or instances) or false.
#pod
#pod Calling this method in scalar context is a fatal error.
#pod
#pod =cut

my $_v = qr/(?:v(?:er(?:sion|\.))?(?: |\.)?)/i;
my @phrases = (
  "under the same (?:terms|license) as perl $_v?6" => [],
  'under the same (?:terms|license) as (?:the )?perl'    => 'Perl_5',
  'affero g'                                    => 'AGPL_3',
  "GNU (?:general )?public license,? $_v?([123])" => sub { "GPL_$_[0]" },
  'GNU (?:general )?public license'             => [ map {"GPL_$_"} (1..3) ],
  "GNU (?:lesser|library) (?:general )?public license,? $_v?([23])\\D"  => sub {
    $_[0] == 2 ? 'LGPL_2_1' : $_[0] == 3 ? 'LGPL_3_0' : ()
  },
  'GNU (?:lesser|library) (?:general )?public license'  => [ qw(LGPL_2_1 LGPL_3_0) ],
  '(?:the )?2[-\s]clause (?:Free)?BSD' => 'FreeBSD',
  'BSD license'                => 'BSD',
  'FreeBSD license'            => 'FreeBSD',
  "Artistic license $_v?(\\d)" => sub { "Artistic_$_[0]_0" },
  'Artistic license'           => [ map { "Artistic_$_\_0" } (1..2) ],
  "LGPL,? $_v?(\\d)"             => sub {
    $_[0] == 2 ? 'LGPL_2_1' : $_[0] == 3 ? 'LGPL_3_0' : ()
  },
  'LGPL'                       => [ qw(LGPL_2_1 LGPL_3_0) ],
  "GPL,? $_v?(\\d)"              => sub { "GPL_$_[0]" },
  'GPL'                        => [ map { "GPL_$_" } (1..3) ],
  'FreeBSD'                    => 'FreeBSD',
  'BSD'                        => 'BSD',
  'Artistic'                   => [ map { "Artistic_$_\_0" } (1..2) ],
  'MIT'                        => 'MIT',
  'has dedicated the work to the Commons' => 'CC0_1_0',
  'waiving all of his or her rights to the work worldwide under copyright law' => 'CC0_1_0',
);

my %meta_keys  = ();
my %meta1_keys = ();
my %meta2_keys = ();
my %spdx_expression  = ();

# find all known Software::License::* modules and get identification data
#
# XXX: Grepping over @INC is dangerous, as it means that someone can change the
# behavior of your code by installing a new library that you don't load.  rjbs
# is not a fan.  On the other hand, it will solve a real problem.  One better
# solution is to check "core" licenses first, then fall back, and to skip (but
# warn about) bogus libraries.  Another is, at least when testing S-L itself,
# to only scan lib/ blib. -- rjbs, 2013-10-20
for my $lib (map { "$_/Software/License" } @INC) {
  next unless -d $lib;
  for my $file (IO::Dir->new($lib)->read) {
    next unless $file =~ m{\.pm$};

    # if it fails, ignore it
    eval {
      (my $mod = $file) =~ s{\.pm$}{};
      my $class = "Software::License::$mod";
      load $class;
      $meta_keys{  $class->meta_name  }{$mod} = undef;
      $meta1_keys{ $class->meta_name  }{$mod} = undef;
      $meta_keys{  $class->meta2_name }{$mod} = undef;
      $meta2_keys{ $class->meta2_name }{$mod} = undef;
      if (defined $class->spdx_expression) {
        $spdx_expression{  $class->spdx_expression   }{$class} = undef;
      }
      my $name = $class->name;
      unshift @phrases, qr/\Q$name\E/, [$mod];
      if ((my $name_without_space = $name) =~ s/\s+\(.+?\)//) {
        unshift @phrases, qr/\Q$name_without_space\E/, [$mod];
      }
    };
  }
}

sub guess_license_from_pod {
  my ($class, $pm_text) = @_;
  die "can't call guess_license_* in scalar context" unless wantarray;
  return unless $pm_text =~ /
    (
      =head \d \s+
      (?:licen[cs]e|licensing|copyright|legal)\b
    )
  /ixmsg;

  my $header = $1;

  if (
    $pm_text =~ m/
      \G
      (
        .*?
      )
      (=head\\d.*|=cut.*|)
      \z
    /ixms
  ) {
    my $license_text = "$header$1";

    for (my $i = 0; $i < @phrases; $i += 2) {
      my ($pattern, $license) = @phrases[ $i .. $i+1 ];
      $pattern =~ s{\s+}{\\s+}g
        unless ref $pattern eq 'Regexp';
      if ( $license_text =~ /\b$pattern\b/i ) {
        my $match = $1;
        # if ( $osi and $license_text =~ /All rights reserved/i ) {
        #   warn "LEGAL WARNING: 'All rights reserved' may invalidate Open Source licenses. Consider removing it.";
        # }
        my @result = (ref $license||'') eq 'CODE'  ? $license->($match)
                   : (ref $license||'') eq 'ARRAY' ? @$license
                   :                                 $license;

        return unless @result;
        return map { "Software::License::$_" } sort @result;
      }
    }
  }

  return;
}

#pod =method guess_license_from_meta
#pod
#pod   my @guesses = Software::LicenseUtils->guess_license_from_meta($meta_str);
#pod
#pod Given the content of the META.(yml|json) file found in a CPAN distribution, this
#pod method makes a guess as to which licenses may apply to the distribution.  It
#pod will return a list of zero or more Software::License instances or classes.
#pod
#pod =cut

sub guess_license_from_meta {
  my ($class, $meta_text) = @_;
  die "can't call guess_license_* in scalar context" unless wantarray;

  my ($license_text) = $meta_text =~ m{\b["']?license["']?\s*:\s*["']?([a-z_0-9]+)["']?}gm;

  return unless $license_text and my $license = $meta_keys{ $license_text };

  return map { "Software::License::$_" } sort keys %$license;
}

{
  no warnings 'once';
  *guess_license_from_meta_yml = \&guess_license_from_meta;
}

#pod =method guess_license_from_meta_key
#pod
#pod   my @guesses = Software::LicenseUtils->guess_license_from_meta_key($key, $v);
#pod
#pod This method returns zero or more Software::License classes known to use C<$key>
#pod as their META key.  If C<$v> is supplied, it specifies whether to treat C<$key>
#pod as a v1 or v2 meta entry.  Any value other than 1 or 2 will raise an exception.
#pod
#pod =cut

sub guess_license_from_meta_key {
  my ($self, $key, $v) = @_;

  my $src = (! defined $v) ? \%meta_keys
          : $v eq '1'      ? \%meta1_keys
          : $v eq '2'      ? \%meta2_keys
          : Carp::croak("illegal META version: $v");

  return unless $src->{$key};
  return map { "Software::License::$_" } sort keys %{ $src->{$key} };
}

my %short_name = (
  'GPL-1'      =>  'Software::License::GPL_1',
  'GPL-2'      =>  'Software::License::GPL_2',
  'GPL-3'      =>  'Software::License::GPL_3',
  'LGPL-2'     =>  'Software::License::LGPL_2',
  'LGPL-2.1'   =>  'Software::License::LGPL_2_1',
  'LGPL-3'     =>  'Software::License::LGPL_3_0',
  'LGPL-3.0'   =>  'Software::License::LGPL_3_0',
  'Artistic'   =>  'Software::License::Artistic_1_0',
  'Artistic-1' =>  'Software::License::Artistic_1_0',
  'Artistic-2' =>  'Software::License::Artistic_2_0',
);

#pod =method new_from_short_name
#pod
#pod   my $license_object = Software::LicenseUtils->new_from_short_name( {
#pod      short_name => 'GPL-1',
#pod      holder => 'X. Ample'
#pod   }) ;
#pod
#pod Create a new L<Software::License> object from the license specified
#pod with C<short_name>. Known short license names are C<GPL-*>, C<LGPL-*> ,
#pod C<Artistic> and C<Artistic-*>
#pod
#pod =cut

sub new_from_short_name {
  my ( $class, $arg ) = @_;

  Carp::croak "no license short name specified"
    unless defined $arg->{short_name};
  my $short = delete $arg->{short_name};
  Carp::croak "Unknow license with short name $short"
    unless $short_name{$short};

  my $lic_file = my $lic_class = $short_name{$short} ;
  $lic_file =~ s!::!/!g;
  require "$lic_file.pm";
  return $lic_class->new( $arg );
}

#pod =method new_from_spdx_expression
#pod
#pod   my $license_object = Software::LicenseUtils->new_from_spdx_expression( {
#pod      spdx_expression => 'MPL-2.0',
#pod      holder => 'X. Ample'
#pod   }) ;
#pod
#pod Create a new L<Software::License> object from the license specified
#pod with C<spdx_expression>. Some licenses doesn't have an spdx
#pod identifier (for example L<Software::License::Perl_5>), so you can pass
#pod spdx identifier but also expressions.
#pod Known spdx license identifiers are C<BSD>, C<MPL-1.0>.
#pod
#pod =cut

sub new_from_spdx_expression {
  my ( $class, $arg ) = @_;

  Carp::croak "no license spdx name specified"
    unless defined $arg->{spdx_expression};
  my $spdx = delete $arg->{spdx_expression};
  Carp::croak "Unknow license with spdx name $spdx"
    unless $spdx_expression{$spdx};

  my ($lic_file) = my ($lic_class) = keys %{$spdx_expression{$spdx}} ;
  $lic_file =~ s!::!/!g;
  require "$lic_file.pm";
  return $lic_class->new( $arg );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Software::LicenseUtils - little useful bits of code for licensey things

=head1 VERSION

version 0.103014

=head1 METHODS

=head2 guess_license_from_pod

  my @guesses = Software::LicenseUtils->guess_license_from_pod($pm_text);

Given text containing POD, like a .pm file, this method will attempt to guess
at the license under which the code is available.  This method will either
a list of Software::License classes (or instances) or false.

Calling this method in scalar context is a fatal error.

=head2 guess_license_from_meta

  my @guesses = Software::LicenseUtils->guess_license_from_meta($meta_str);

Given the content of the META.(yml|json) file found in a CPAN distribution, this
method makes a guess as to which licenses may apply to the distribution.  It
will return a list of zero or more Software::License instances or classes.

=head2 guess_license_from_meta_key

  my @guesses = Software::LicenseUtils->guess_license_from_meta_key($key, $v);

This method returns zero or more Software::License classes known to use C<$key>
as their META key.  If C<$v> is supplied, it specifies whether to treat C<$key>
as a v1 or v2 meta entry.  Any value other than 1 or 2 will raise an exception.

=head2 new_from_short_name

  my $license_object = Software::LicenseUtils->new_from_short_name( {
     short_name => 'GPL-1',
     holder => 'X. Ample'
  }) ;

Create a new L<Software::License> object from the license specified
with C<short_name>. Known short license names are C<GPL-*>, C<LGPL-*> ,
C<Artistic> and C<Artistic-*>

=head2 new_from_spdx_expression

  my $license_object = Software::LicenseUtils->new_from_spdx_expression( {
     spdx_expression => 'MPL-2.0',
     holder => 'X. Ample'
  }) ;

Create a new L<Software::License> object from the license specified
with C<spdx_expression>. Some licenses doesn't have an spdx
identifier (for example L<Software::License::Perl_5>), so you can pass
spdx identifier but also expressions.
Known spdx license identifiers are C<BSD>, C<MPL-1.0>.

=head1 AUTHOR

Ricardo Signes <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2018 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
