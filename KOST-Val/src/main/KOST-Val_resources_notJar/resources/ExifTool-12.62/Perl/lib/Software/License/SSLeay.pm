use strict;
use warnings;
package Software::License::SSLeay;
$Software::License::SSLeay::VERSION = '0.103014';
use parent 'Software::License';
# ABSTRACT: The Original SSLeay License

sub name { 'Original SSLeay License' }
sub url  { 'http://h71000.www7.hp.com/doc/83final/BA554_90007/apcs02.html' }
sub meta_name  { 'unrestricted' }
sub meta2_name { 'ssleay' }
sub spdx_expression  { 'SSLeay' }

1;

=pod

=encoding UTF-8

=head1 NAME

Software::License::SSLeay - The Original SSLeay License

=head1 VERSION

version 0.103014

=head1 AUTHOR

Ricardo Signes <rjbs@cpan.org>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2018 by Ricardo Signes.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut

__DATA__
__LICENSE__
  Copyright (c) 1995-1998 Eric Young (eay@cryptsoft.com) All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

  This library is free for commercial and non-commercial use as long as
  the following conditions are aheared to.  The following conditions
  apply to all code found in this distribution, be it the RC4, RSA,
  lhash, DES, etc., code; not just the SSL code.  The SSL documentation
  included with this distribution is covered by the same copyright terms
  except that the holder is Tim Hudson (tjh@cryptsoft.com).

  Copyright remains Eric Young’s, and as such any Copyright notices in the code
  are not to be removed. If this package is used in a product, Eric Young
  should be given attribution as the author of the parts of the library used.
  This can be in the form of a textual message at program startup or in
  documentation (online or textual) provided with the package.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the copyright
       notice, this list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
    3. All advertising materials mentioning features or use of this software
       must display the following acknowledgement:
       “This product includes cryptographic software written by
        Eric Young (eay@cryptsoft.com)”
       The word ‘cryptographic’ can be left out if the rouines from the library
       being used are not cryptographic related :-).
    4. If you include any Windows specific code (or a derivative thereof) from
       the apps directory (application code) you must include an
       acknowledgement: “This product includes software written by Tim Hudson
       (tjh@cryptsoft.com)”

  THIS SOFTWARE IS PROVIDED BY ERIC YOUNG ‘‘AS IS’’ AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.

  The licence and distribution terms for any publically available version or
  derivative of this code cannot be changed.  i.e. this code cannot simply be
  copied and put under another distribution licence
  [including the GNU Public Licence.]
