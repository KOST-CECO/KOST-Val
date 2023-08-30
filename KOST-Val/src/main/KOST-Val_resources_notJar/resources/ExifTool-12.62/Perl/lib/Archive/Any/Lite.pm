package Archive::Any::Lite;

use strict;
use warnings;
use File::Spec;

our $VERSION = '0.11';
our $IGNORE_SYMLINK;

sub new {
  my ($class, $file, $opts) = @_;

  $file = File::Spec->rel2abs($file);
  unless (-f $file) {
    warn "$file not found\n";
    return;
  }

  # just for undocumented backward compat
  my $type = !ref $opts ? $opts : '';

  # XXX: trust file extensions until I manage to make File::MMagic
  #      more reliable while fork()ing or I happen to find a decent
  #      and portable alternative to File::MMagic.

  my $handler =
    ($type && lc $type eq 'tar') || $file =~ /\.(?:tar|tar\.(?:gz|bz2)|gtar|tgz)$/ ? 'Archive::Any::Lite::Tar' :
    ($type && lc $type eq 'zip') || $file =~ /\.(?:zip)$/ ? 'Archive::Any::Lite::Zip' : undef;
  unless ($handler) {
    warn "No handler available for $file\n";
    return;
  }

  bless {
    file    => $file,
    handler => $handler,
    opts    => ref $opts ? $opts : undef,
  }, $class;
}

sub extract {
  my ($self, $dir, $opts) = @_;

  $self->{handler}->extract($self->{file}, $dir, $opts || $self->{opts});
}

sub files {
  my $self = shift;
  $self->{handler}->files($self->{file});
}

sub is_impolite {
  my $self = shift;

  my @files       = $self->files;
  my $first_file  = $files[0];
  my ($first_dir) = File::Spec->splitdir($first_file);

  return grep( !/^\Q$first_dir\E/, @files ) ? 1 : 0;
}

sub is_naughty {
  my ($self) = shift;
  return ( grep { m{^(?:/|(?:\./)*\.\./)} } $self->files ) ? 1 : 0;
}

sub type {
  my $self = shift;
  my ($type) = lc $self->{handler} =~ /::(\w+)$/;
  return $type;
}

package Archive::Any::Lite::Tar;
use Archive::Tar;

sub files {
  my ($self, $file) = @_;
  Archive::Tar->list_archive($file);
}

sub extract {
  my ($self, $file, $dir, $opts) = @_;
  $dir = '.' unless defined $dir;
  $dir = File::Spec->rel2abs($dir);
  my $tar = Archive::Tar->new;
  my $fh;
  if ($file =~ /\.(tgz|tar\.gz)$/) {
    require IO::Zlib;
    $fh = IO::Zlib->new($file, "rb") or do { warn "$file: $!"; return };
  }
  elsif ($file =~ /\.tar.bz2$/) {
    require IO::Uncompress::Bunzip2;
    $fh = IO::Uncompress::Bunzip2->new($file) or do { warn "$file: $!"; return };
  }
  else {
    open $fh, '<', $file or do { warn "$file: $!"; return };
    binmode $fh;
  }

  # Archive::Tar is too noisy when an archive has minor glitches.
  # Note also that $file can't hold the last error.
  local $Archive::Tar::WARN;
  my %errors;
  my $has_extracted;
  my %read_opts = (limit => 1);
  if ($opts) {
    for (qw/limit md5 filter filter_cb extract/) {
      if (exists $opts->{"tar_$_"}) {
        $read_opts{$_} = $opts->{"tar_$_"};
      }
      elsif (exists $opts->{$_}) {
        $read_opts{$_} = $opts->{$_};
      }
    }
  }
  until (eof $fh) {
    my @files = $tar->read($fh, undef, \%read_opts);
    if (my $error = $tar->error) {
      warn $error unless $errors{$error}++;
    }
    if (!@files && !$has_extracted) {
      warn "No data could be read from $file";
      return;
    }
    for my $file (@files) {
      next if $IGNORE_SYMLINK && ($file->is_symlink or $file->is_hardlink);
      my $path = File::Spec->catfile($dir, $file->prefix, $file->name);
      $tar->extract_file($file, File::Spec->canonpath($path)) or do {
        if (my $error = $tar->error) {
          warn $error unless $errors{$error}++;
        }
      };
    }
    $has_extracted += @files;
  }
  return if %errors;
  return 1;
}

sub type { 'tar' }

package Archive::Any::Lite::Zip;
use Archive::Zip qw/:ERROR_CODES/;

sub files {
  my ($self, $file) = @_;
  my $zip = Archive::Zip->new($file) or return;
  $zip->memberNames;
}

sub extract {
  my ($self, $file, $dir, $opts) = @_;
  my $zip = Archive::Zip->new($file) or return;
  $dir = '.' unless defined $dir;
  my $error = 0;
  for my $member ($zip->members) {
    next if $IGNORE_SYMLINK && $member->isSymbolicLink;
    my $path = File::Spec->catfile($dir, $member->fileName);
    my $ret = $member->extractToFileNamed(File::Spec->canonpath($path));
    $error++ if $ret != AZ_OK;
  }
  return if $error;
  return 1;
}

sub type { 'zip' }

1;

__END__

=head1 NAME

Archive::Any::Lite - simple CPAN package extractor

=head1 SYNOPSIS

    use strict;
    use warnings;
    use Archive::Any::Lite;

    local $Archive::Any::Lite::IGNORE_SYMLINK = 1; # for safety

    my $tarball = 'foo.tar.gz';
    my $archive = Archive::Any::Lite->new($tarball);
    $archive->extract('into/some/directory/');

=head1 DESCRIPTION

This is a fork of L<Archive::Any> by Michael Schwern and Clint Moore. The main difference is this works properly even when you fork(), and may require less memory to extract a tarball. On the other hand, this isn't pluggable (this only supports file formats used in the CPAN toolchains), and this doesn't check mime types (at least as of this writing).

=head1 METHODS

=head2 new

  my $archive = Archive::Any::Lite->new($archive_file);
  my $archive = Archive::Any::Lite->new($archive_file, {tar_filter => qr/foo/});

Creates an object.
You can pass an optional hash reference for finer control.

=head2 extract

  $archive->extract;
  $archive->extract($directory);
  $archive->extract($directory, {tar_filter => qr/foo/});

Extracts the files in the archive to the given $directory. If no $directory is given, it will go into the current working directory.

You can pass an optional hash reference for finer control. If passed, options passed in C<new> will be ignored.

=head2 files

  my @file = $archive->files;

A list of files in the archive.

=head2 is_impolite

  my $is_impolite = $archive->is_impolite;

Checks to see if this archive is going to unpack into the current directory rather than create its own.

=head2 is_naughty

  my $is_naughty = $archive->is_naughty;

Checks to see if this archive is going to unpack outside the current directory.

=head2 type

Deprecated. For backward compatibility only.

=head1 GLOBAL VARIABLE

=head2 $IGNORE_SYMLINK

If set to true, symlinks (and hardlinks for tarball) will be ignored.

=head1 SEE ALSO

L<Archive::Any>, L<Archive::Tar::Streamed>

=head1 AUTHOR

L<Archive::Any> is written by Michael G Schwern and Clint Moore.

Kenichi Ishigaki, E<lt>ishigaki@cpan.orgE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2012 by Kenichi Ishigaki.

This program is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=cut
