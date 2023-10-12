package Alien::Build::Plugin::Download::GitLab;

use strict;
use warnings;
use 5.008004;
use Carp qw( croak );
use URI;
use JSON::PP qw( decode_json );
use URI::Escape qw( uri_escape );
use Alien::Build::Plugin;
use File::Basename qw( basename );
use Path::Tiny qw( path );

# ABSTRACT: Alien::Build plugin to download from GitLab
our $VERSION = '0.01'; # VERSION


has gitlab_host    => 'https://gitlab.com';
has gitlab_user    => undef;
has gitlab_project => undef;


has type => 'source';  # source or link


has format => 'tar.gz';


has version_from    => 'tag_name'; # tag_name or name
has convert_version => undef;
has link_name       => undef;

sub init
{
  my($self, $meta) = @_;

  croak("No gitlab_user provided") unless defined $self->gitlab_user;
  croak("No gitlab_project provided") unless defined $self->gitlab_project;
  croak("Don't set set a start_url with the Download::GitLab plugin") if defined $meta->prop->{start_url};

  $meta->add_requires('configure' => 'Alien::Build::Plugin::Download::GitLab' => 0 );

  my $url = URI->new($self->gitlab_host);
  $url->path("/api/v4/projects/@{[ uri_escape(join '/', $self->gitlab_user, $self->gitlab_project) ]}/releases");
  $meta->prop->{start_url} ||= "$url";

  $meta->apply_plugin('Download');
  $meta->apply_plugin('Extract', format => $self->format );

  # we assume that GitLab returns the releases in reverse
  # chronological order.
  $meta->register_hook(
    prefer => sub {
      my($build, $res) = @_;
      return $res;
    },
  );

  croak "type must be one of source or link" if $self->type !~ /^(source|link)$/;
  croak "version_from must be one of tag_name or name" if $self->version_from !~ /^(tag_name|name)$/;

  ## TODO insert tokens as header if possible
  ## This may help with rate limiting (or if not then don't bother)
  # curl --header "PRIVATE-TOKEN: <your_access_token>" "https://gitlab.example.com/api/v4/projects/24/releases"

  $meta->around_hook(
    fetch => sub {
      my $orig = shift;
      my($build, $url, @the_rest) = @_;

      # only modify the response if we are using the GitLab API
      # to get the release list
      return $orig->($build, $url, @the_rest)
        if defined $url && $url ne $meta->prop->{start_url};

      my $res = $orig->($build, $url, @the_rest);

      my $res2 = {
        type => 'list',
        list => [],
      };

      $res2->{protocol} = $res->{protocol} if exists $res->{protocol};

      my $rel;
      if($res->{content})
      {
        $rel = decode_json $res->{content};
      }
      elsif($res->{path})
      {
        $rel = decode_json path($res->{path})->slurp_raw;
      }
      else
      {
        croak("malformed response object: no content or path");
      }

      foreach my $release (@$rel)
      {
        my $version = $self->version_from eq 'name' ? $release->{name} : $release->{tag_name};
        $version = $self->convert_version->($version) if $self->convert_version;

        if($self->type eq 'source')
        {
          foreach my $source (@{ $release->{assets}->{sources} })
          {
            next unless $source->{format} eq $self->format;
            my $url = URI->new($source->{url});
            my $filename = basename $url->path;
            push @{ $res2->{list} }, {
              filename => $filename,
              url      => $source->{url},
              version  => $version,
            };
          }
        }
        else # link
        {
          foreach my $link (@{ $release->{assets}->{links} })
          {
            my $url = URI->new($link->{url});
            my $filename => basename $url->path;
            if($self->link_name)
            {
              next unless $filename =~ $self->link_name;
            }
            push @{ $res2->{list} }, {
              filename => $filename,
              url      => $link->{url},
              version  => $version,
            };
          }
        }
      }

      return $res2;

    },
  );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Alien::Build::Plugin::Download::GitLab - Alien::Build plugin to download from GitLab

=head1 VERSION

version 0.01

=head1 SYNOPSIS

 use alienfile;
 
 plugin 'Download::GitLab' => (
   gitlab_user    => 'plicease',
   gitlab_project => 'dontpanic',
 );

=head1 DESCRIPTION

This plugin is designed for downloading assets from a GitLab instance.

=head1 PROPERTIES

=head2 gitlab_host

The host to fetch from L<https://gitlab.com> by default.

=head2 gitlab_user

The user to fetch from.

=head2 gitlab_project

The project to fetch from.

=head2 type

The asset type to fetch.  This must be one of C<source> or C<link>.

=head2 format

The expected format of the asset.  This should be one that
L<Alien::Build::Plugin::Extract::Negotiate> understands.  The
default is C<tar.gz>.

=head2 version_from

Where to compute the version from.  This should be one of
C<tag_name> or C<name>.  The default is C<tag_name>.

=head2 convert_version

This is an optional code reference, which can be used to modify
the version.  For example, if tags have a C<v> prefix you could
remove it like so:

 plugin 'Download::GitLab' => (
   gitlab_user     => 'plicease',
   gitlab_project  => 'dontpanic',
   convert_version => sub {
     my $version = shift;
     $version =~ s/^v//;
     return $version;
   },
 );

=head2 link_name

For C<link> types, this is a regular expression that filters the
asset filenames.  For example, if there are multiple archive
formats provided, you can get just the gzip'd tarball by setting
this to C<qr/\.tar\.gz$/>.

=head1 SEE ALSO

=over 4

=item L<Alien>

=item L<Alien::Build::Plugin::Download::GitHub>

=item L<alienfile>

=item L<Alien::Build>

=back

=head1 AUTHOR

Graham Ollis <plicease@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2022 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
