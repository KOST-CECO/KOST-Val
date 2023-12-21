package Email::MIME::Kit::KitReader::Dir 3.000007;
# ABSTRACT: read kit entries out of a directory

use v5.20.0;
use Moose;
with 'Email::MIME::Kit::Role::KitReader';

use File::Spec;

# cache sometimes
sub get_kit_entry {
  my ($self, $path) = @_;

  my $fullpath = File::Spec->catfile($self->kit->source, $path);

  open my $fh, '<:raw', $fullpath
    or die "can't open $fullpath for reading: $!";
  my $content = do { local $/; <$fh> };

  return \$content;
}

no Moose;
__PACKAGE__->meta->make_immutable;
1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::MIME::Kit::KitReader::Dir - read kit entries out of a directory

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
