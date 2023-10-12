package HTML::Form::FileInput;

use strict;
use parent 'HTML::Form::TextInput';

our $VERSION = '6.11';

# ABSTRACT: An HTML form file input element for use with HTML::Form

sub file {
    my $self = shift;
    $self->value(@_);
}

sub filename {
    my $self = shift;
    my $old  = $self->{filename};
    $self->{filename} = shift if @_;
    $old = $self->file unless defined $old;
    $old;
}

sub content {
    my $self = shift;
    my $old  = $self->{content};
    $self->{content} = shift if @_;
    $old;
}

sub headers {
    my $self = shift;
    my $old  = $self->{headers} || [];
    $self->{headers} = [@_] if @_;
    @$old;
}

sub form_name_value {
    my ( $self, $form ) = @_;
    return $self->SUPER::form_name_value($form)
        if $form->method ne "POST"
        || $form->enctype ne "multipart/form-data";

    my $name = $self->name;
    return unless defined $name;
    return if $self->{disabled};

    my $file     = $self->file;
    my $filename = $self->filename;
    my @headers  = $self->headers;
    my $content  = $self->content;
    my %headers  = @headers;
    if ( defined $content || grep m/^Content$/i, keys %headers ) {
        $filename = $file unless defined $filename;
        $file     = undef;
        unshift( @headers, "Content" => $content );
    }
    elsif ( !defined($file) || length($file) == 0 ) {
        return;
    }

    # legacy (this used to be the way to do it)
    if ( ref($file) eq "ARRAY" ) {
        my $f  = shift @$file;
        my $fn = shift @$file;
        push( @headers, @$file );
        $file     = $f;
        $filename = $fn;
    }

    return ( $name => [ $file, $filename, @headers ] );
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::FileInput - An HTML form file input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
