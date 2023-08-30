use strict;
use warnings;
package Software::License::OpenSSL;
$Software::License::OpenSSL::VERSION = '0.103014';
use parent 'Software::License';
# ABSTRACT: The OpenSSL License

require Software::License::SSLeay;

sub name { 'OpenSSL License' }
sub url  { 'http://www.openssl.org/source/license.html' }
sub meta_name  { 'open_source' }
sub meta2_name { 'openssl' }
sub spdx_expression  { 'OpenSSL' }

sub _ssleay {
  my ($self) = @_;
  return $self->{_ssleay} ||= Software::License::SSLeay->new({
    year   => $self->year,
    holder => $self->holder,
  });
}

1;

=pod

=encoding UTF-8

=head1 NAME

Software::License::OpenSSL - The OpenSSL License

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
  LICENSE ISSUES
  ==============

  The OpenSSL toolkit stays under a dual license, i.e. both the conditions of
  the OpenSSL License and the original SSLeay license apply to the toolkit.
  See below for the actual license texts. Actually both licenses are BSD-style
  Open Source licenses. In case of any license issues related to OpenSSL
  please contact openssl-core@openssl.org.

  OpenSSL License
  ---------------

  ====================================================================
  Copyright (c) 1998-2008 The OpenSSL Project.  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

  1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the
     distribution.

  3. All advertising materials mentioning features or use of this
     software must display the following acknowledgment:
     "This product includes software developed by the OpenSSL Project
     for use in the OpenSSL Toolkit. (http://www.openssl.org/)"

  4. The names "OpenSSL Toolkit" and "OpenSSL Project" must not be used to
     endorse or promote products derived from this software without
     prior written permission. For written permission, please contact
     openssl-core@openssl.org.

  5. Products derived from this software may not be called "OpenSSL"
     nor may "OpenSSL" appear in their names without prior written
     permission of the OpenSSL Project.

  6. Redistributions of any form whatsoever must retain the following
     acknowledgment:
     "This product includes software developed by the OpenSSL Project
     for use in the OpenSSL Toolkit (http://www.openssl.org/)"

  THIS SOFTWARE IS PROVIDED BY THE OpenSSL PROJECT ``AS IS'' AND ANY
  EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE OpenSSL PROJECT OR
  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  OF THE POSSIBILITY OF SUCH DAMAGE.
  ====================================================================

  This product includes cryptographic software written by Eric Young
  (eay@cryptsoft.com).  This product includes software written by Tim
  Hudson (tjh@cryptsoft.com).

  {{$self->_ssleay->name}}
  -----------------------

{{$self->_ssleay->license}}
