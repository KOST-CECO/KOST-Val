package strictures::extra;
use strict;
use warnings FATAL => 'all';

sub import {
  $ENV{PERL_STRICTURES_EXTRA} = 1;
}

sub unimport {
  $ENV{PERL_STRICTURES_EXTRA} = 0;
}

1;

__END__
=head1 NAME

strictures::extra - enable or disable strictures additional checks

=head1 SYNOPSIS

  no strictures::extra;
  # will not enable indirect, multidimensional, or bareword filehandle checks
  use strictures;

=head1 DESCRIPTION

Enable or disable strictures additional checks, preventing checks for C<.git>
or other VCS directories.

Equivalent to setting the C<PERL_STRICTURES_EXTRA> environment variable.

=head1 AUTHORS

See L<strictures> for authors.

=head1 COPYRIGHT AND LICENSE

See L<strictures> for the copyright and license.

=cut
