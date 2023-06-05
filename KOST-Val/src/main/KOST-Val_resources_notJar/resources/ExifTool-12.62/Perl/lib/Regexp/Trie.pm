#
# $Id: Trie.pm,v 0.2 2006/04/27 05:24:40 dankogai Exp dankogai $
#

package Regexp::Trie;
use 5.008001;
use strict;
use warnings;

our $VERSION = sprintf "%d.%02d", q$Revision: 0.2 $ =~ /(\d+)/g;

# use overload q("") => sub { shift->regexp };

sub new{ bless {} => shift }
sub add{
    my $self = shift;
    my $str  = shift;
    my $ref  = $self;
    for my $char (split //, $str){
        $ref->{$char} ||= {};
        $ref = $ref->{$char};
    }
    $ref->{''} = 1; # { '' => 1 } as terminator
    $self;
}
sub _regexp{
    my $self = shift;
    return if $self->{''} and scalar keys %$self == 1; # terminator
    my (@alt, @cc);
    my $q = 0;
    for my $char (sort keys %$self){
        my $qchar = quotemeta $char;
        if (ref $self->{$char}){
            if (defined (my $recurse = _regexp($self->{$char}))){
                push @alt, $qchar . $recurse;
            }else{
                push @cc, $qchar;
            }
        }else{
            $q = 1;
        }
    }
    my $cconly = !@alt;
    @cc and push @alt, @cc == 1 ? $cc[0] : '['. join('', @cc). ']';
    my $result = @alt == 1 ? $alt[0] : '(?:' . join('|', @alt) . ')';
    $q and $result = $cconly ? "$result?" : "(?:$result)?";
    return $result;
}
sub regexp{ my $str = shift->_regexp; qr/$str/ }

1;
__END__
# Below is stub documentation for your module. You'd better edit it!

=head1 NAME

Regexp::Trie - builds trie-ized regexp

=head1 SYNOPSIS

  use Regexp::Trie;
  my $rt = Regexp::Trie->new;
  for (qw/foobar fooxar foozap fooza/){
    $rt->add($_);
  }
  print $rt->regexp, "\n" # (?-xism:foo(?:bar|xar|zap?))

=head1 DESCRIPTION

This module is a faster but simpler version of L<Regexp::Assemble> or
L<Regexp::Optimizer>.  It builds a trie-ized regexp as above.

This module is faster than L<Regexp::Assemble> but you can only add
literals.  C<a+b> is treated as C<a\+b>, not "more than one a's
followed by b".

I wrote this module because I needed something faster than
L<Regexp::Assemble> and L<Regexp::Optimizer>.  If you need more minute
control, use those instead.

=head1 TIPS

See t/dict2rx.pl to find how to convert a big dictionary into a single
regexp that can be later loaded as:

  my $rx = do 'dict.rx';

=head2 EXPORT

None.

=head1 SEE ALSO

L<Regexp::Optimizer>,  L<Regexp::Assemble>, L<Regex::PreSuf>

=head1 AUTHOR

Dan Kogai, E<lt>dankogai@dan.co.jpE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2006 by Dan Kogai

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself, either Perl version 5.8.8 or,
at your option, any later version of Perl 5 you may have available.

=cut
