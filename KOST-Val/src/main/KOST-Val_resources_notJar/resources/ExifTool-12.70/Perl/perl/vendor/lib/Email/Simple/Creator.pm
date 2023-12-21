use v5.12.0;
use warnings;
package Email::Simple::Creator 2.218;
# ABSTRACT: private helper for building Email::Simple objects

use Carp ();

sub _crlf {
  "\x0d\x0a";
}

sub _date_header {
  require Email::Date::Format;
  Email::Date::Format::email_date();
}

our @CARP_NOT = qw(Email::Simple Email::MIME);

sub _add_to_header {
  my ($class, $header, $key, $value) = @_;
  $value //= '';

  Carp::carp "Header name '$key' with wide characters" if $key =~ /[^\x00-\xFF]/;
  Carp::carp "Value '$value' for '$key' header with wide characters" if $value =~ /[^\x00-\xFF]/;

  if ($value =~ s/[\x0a\x0b\x0c\x0d\x85\x{2028}\x{2029}]+/ /g) {
    Carp::carp("replaced vertical whitespace in $key header with space; this will become fatal in a future version");
  }

  $$header .= Email::Simple::Header->__fold_objless("$key: $value", 78, q{ }, $class->_crlf);
}

sub _finalize_header {
  my ($class, $header) = @_;
  $$header .= $class->_crlf;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

Email::Simple::Creator - private helper for building Email::Simple objects

=head1 VERSION

version 2.218

=head1 PERL VERSION

This library should run on perls released even a long time ago.  It should work
on any version of perl released in the last five years.

Although it may work on older versions of perl, no guarantee is made that the
minimum required version will not be increased.  The version may be increased
for any reason, and there is no promise that patches will be accepted to lower
the minimum required perl.

=head1 AUTHORS

=over 4

=item *

Simon Cozens

=item *

Casey West

=item *

Ricardo SIGNES <cpan@semiotic.systems>

=back

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2003 by Simon Cozens.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
