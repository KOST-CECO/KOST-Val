package XML::LibXSLT::Quick;

use strict;
use warnings;
use 5.010;
use autodie;

use Carp ();

use XML::LibXML  ();
use XML::LibXSLT ();

use vars qw( $VERSION );

$VERSION = '2.002001';

sub stylesheet
{
    my $self = shift;

    if (@_)
    {
        $self->{stylesheet} = shift;
    }

    return $self->{stylesheet};
}

sub xml_parser
{
    my $self = shift;

    if (@_)
    {
        $self->{xml_parser} = shift;
    }

    return $self->{xml_parser};
}

sub new
{
    my $class = shift;
    my $args  = shift;

    my $xslt       = ( $args->{xslt_parser} // XML::LibXSLT->new() );
    my $xml_parser = ( $args->{xml_parser}  // XML::LibXML->new() );

    my $style_doc = $xml_parser->load_xml(
        location => $args->{location},
        no_cdata => ( $args->{no_cdata} // 0 ),
    );
    my $stylesheet = $xslt->parse_stylesheet($style_doc);
    my $obj        = bless( +{}, $class );
    $obj->{xml_parser} = $xml_parser;
    $obj->{stylesheet} = $stylesheet;
    return $obj;
}

sub _write_utf8_file
{
    my ( $out_path, $contents ) = @_;

    open my $out_fh, '>:encoding(utf8)', $out_path;

    print {$out_fh} $contents;

    close($out_fh);

    return;
}

sub _write_raw_file
{
    my ( $out_path, $contents ) = @_;

    open my $out_fh, '>:raw', $out_path;

    print {$out_fh} $contents;

    close($out_fh);

    return;
}

sub generic_transform
{
    my $self = shift;

    my ( $dest, $source, ) = @_;
    my $xml_parser = $self->xml_parser();
    my $stylesheet = $self->stylesheet();

    my $ret;
    my $params = {};
    if ( ref($source) eq '' )
    {
        $source = $xml_parser->parse_string($source);
    }
    elsif ( ref($source) eq 'HASH' )
    {
        if ( my $p = $source->{'params'} )
        {
            $params = $p;
        }
        my $type = $source->{type};
        if (0)
        {
        }
        elsif ( $type eq "file" )
        {
            $source = $xml_parser->parse_file( $source->{path} );
        }
        elsif ( $type eq "string" )
        {
            $source = $xml_parser->parse_string( $source->{string} );
        }
        else
        {
            Carp::confess("unknown source type");
        }
    }
    my $dom_results = $stylesheet->transform( $source, %$params, );
    my $calc_ret    = sub {
        return ( $ret //= $stylesheet->output_as_chars( $dom_results, ) );
    };
    my $destref = ref($dest);
    if ( $destref eq "SCALAR" )
    {
        if ( ref($$dest) eq "" )
        {
            $$dest .= scalar( $calc_ret->() );
        }
        else
        {
            Carp::confess(
                "\$dest as a reference to a non-string scalar is not supported!"
            );
        }
    }
    elsif ( $destref eq "HASH" )
    {
        my $type = $dest->{type};
        if (0)
        {
        }
        elsif ( $type eq "dom" )
        {
            return $dom_results;
        }
        elsif ( $type eq "file" )
        {
            my $path = $dest->{path};
            _write_utf8_file( $path, scalar( $calc_ret->() ) );
        }
        elsif ( $type eq "return" )
        {
            return scalar( $calc_ret->() );
        }
        else
        {
            Carp::confess("unknown dest type");
        }
    }
    else
    {
        $dest->print( scalar( $calc_ret->() ) );
    }
    return scalar( $calc_ret->() );
}

sub output_as_chars
{
    my $self = shift;

    return $self->stylesheet()->output_as_chars(@_);
}

sub transform
{
    my $self = shift;

    return $self->stylesheet()->transform(@_);
}

sub transform_into_chars
{
    my $self = shift;

    return $self->stylesheet()->transform_into_chars(@_);
}

1;

__END__

=encoding utf8

=head1 NAME

XML::LibXSLT::Quick - an easier to use (= "quicker") interface to XML::LibXSLT

=head1 SYNOPSIS

    use XML::LibXSLT::Quick ();

    my $stylesheet =
        XML::LibXSLT::Quick->new( { location => 'example/1.xsl', } );
    my $xml1_text = _utf8_slurp('example/1.xml');
    my $out_fn = 'foo.xml';
    $stylesheet->generic_transform(
        +{
            type => 'file',
            path => $out_fn,
        },
        $xml1_text,
    );

=head1 DESCRIPTION

This is a module that wraps L<XML::LibXSLT> with an easier to use interface.

It can be used to process XML documents using
L<XSLT (Extensible Stylesheet Language Transformations)|https://en.wikipedia.org/wiki/XSLT>
stylesheets.

=head1 METHODS

=head2 XML::LibXSLT::Quick->new({ location=>"./xslt/my.xslt", });

TBD.

=head2 $obj->stylesheet()

The result of parse_stylesheet().

=head2 $obj->xml_parser()

The L<XML::LibXML> instance.

=head2 $obj->generic_transform($dest, $source)

To be discussed.

See C<t/using-quick-wrapper.t> .

$dest can be:

C<\$my_string_var>

C<<< {type => 'dom', } >>> - the DOM will be returned.

C<<< {type => 'path', path => $filepath, } >>> - the output string will be written to $filepath .

C<<< {type => 'return', } >>> - the output string will be returned.

$source can be:

A string.

C<<< {type => 'file', path => $filepath, params => +{}, } >>> - the file will be parsed.

C<<< {type => 'string', string => $my_xml_string, params => +{}, } >>> - the string will be parsed.

=head2 $obj->output_as_chars($dom)

=head2 $obj->transform(...)

=head2 $obj->transform_into_chars(...)

Delegating from $obj->stylesheet() . See L<XML::LibXSLT> .

=head1 SEE ALSO

L<XML::LibXSLT::Easy> by Yuval Kogman - requires some MooseX modules.

L<XML::LibXSLT> - used as the backend of this module.

=head1 COPYRIGHT & LICENSE

B<NOTE!!! :> this licence applies to this file alone. The blanket licence
for the distribution is "same as Perl 5".

(I am not a lawyer (= "IANAL") / etc. )

For more information, consult:

=over 4

=item * L<https://www.shlomifish.org/philosophy/computers/open-source/foss-licences-wars/rev2/#which-licence-same-as-perl>

=item * L<https://github.com/shlomif/perl-XML-LibXSLT/issues/5>

=item * L<https://en.wikiquote.org/w/index.php?title=Rick_Cook&oldid=3060266>

“Programming today is a race between software engineers striving to build bigger and better idiot-proof programs, and the Universe trying to produce bigger and better idiots. So far, the Universe is winning.”

=back

Copyright 2022 by Shlomi Fish

This program is distributed under the MIT / Expat License:
L<http://www.opensource.org/licenses/mit-license.php>

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

=cut
