package Email::MIME::Kit::ManifestReader::YAML 3.000007;
# ABSTRACT: read manifest.yaml files

use v5.20.0;
use Moose;

with 'Email::MIME::Kit::Role::ManifestReader',
     'Email::MIME::Kit::Role::ManifestDesugarer';

use YAML::XS ();

sub read_manifest {
  my ($self) = @_;

  my $yaml_ref = $self->kit->kit_reader->get_kit_entry('manifest.yaml');

  # YAML::XS is documented as expecting UTF-8 bytes, which we give it.
  my ($content) = YAML::XS::Load($$yaml_ref);

  return $content;
}

no Moose;
__PACKAGE__->meta->make_immutable;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::MIME::Kit::ManifestReader::YAML - read manifest.yaml files

=head1 VERSION

version 3.000007

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 AUTHOR

Ricardo Signes <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2023 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
