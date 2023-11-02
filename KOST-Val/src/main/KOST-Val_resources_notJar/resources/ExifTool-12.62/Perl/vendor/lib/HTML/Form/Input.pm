package HTML::Form::Input;

use strict;

our $VERSION = '6.11';

# ABSTRACT: A generic HTML form input element for use with HTML::Form

sub new {
    my $class = shift;
    my $self  = bless {@_}, $class;
    $self;
}

sub add_to_form {
    my ( $self, $form ) = @_;
    push( @{ $form->{'inputs'} }, $self );
    $self;
}

sub strict {
    my $self = shift;
    my $old  = $self->{strict};
    if (@_) {
        $self->{strict} = shift;
    }
    $old;
}

sub fixup { }

sub type {
    shift->{type};
}

sub name {
    my $self = shift;
    my $old  = $self->{name};
    $self->{name} = shift if @_;
    $old;
}

sub id {
    my $self = shift;
    my $old  = $self->{id};
    $self->{id} = shift if @_;
    $old;
}

sub class {
    my $self = shift;
    my $old  = $self->{class};
    $self->{class} = shift if @_;
    $old;
}

sub selected {
    my ( $self, $sel ) = @_;
    return undef unless defined $sel;
    my $attr
        = $sel =~ s/^\^// ? "name"
        : $sel =~ s/^#//  ? "id"
        : $sel =~ s/^\.// ? "class"
        :                   "name";
    return 0 unless defined $self->{$attr};
    return $self->{$attr} eq $sel;
}

sub value {
    my $self = shift;
    my $old  = $self->{value};
    $self->{value} = shift if @_;
    $old;
}

sub autocomplete {
    my $self = shift;
    my $old  = $self->{autocomplete};
    $self->{autocomplete} = shift if @_;
    $old;
}

sub possible_values {
    return;
}

sub other_possible_values {
    return;
}

sub value_names {
    return;
}

sub readonly {
    my $self = shift;
    my $old  = $self->{readonly};
    $self->{readonly} = shift if @_;
    $old;
}

sub disabled {
    my $self = shift;
    my $old  = $self->{disabled};
    $self->{disabled} = shift if @_;
    $old;
}

sub form_name_value {
    my $self = shift;
    my $name = $self->{'name'};
    return unless defined $name;
    return if $self->disabled;
    my $value = $self->value;
    return unless defined $value;
    return ( $name => $value );
}

sub dump {
    my $self = shift;
    my $name = $self->name;
    $name = "<NONAME>" unless defined $name;
    my $value = $self->value;
    $value = "<UNDEF>" unless defined $value;
    my $dump = "$name=$value";

    my $type = $self->type;

    $type .= " disabled" if $self->disabled;
    $type .= " readonly" if $self->readonly;
    return sprintf "%-30s %s", $dump, "($type)" unless $self->{menu};

    my @menu;
    my $i = 0;
    for ( @{ $self->{menu} } ) {
        my $opt = $_->{value};
        $opt = "<UNDEF>" unless defined $opt;
        $opt .= "/$_->{name}"
            if defined $_->{name} && length $_->{name} && $_->{name} ne $opt;
        substr( $opt, 0, 0 ) = "-" if $_->{disabled};
        if ( exists $self->{current} && $self->{current} == $i ) {
            substr( $opt, 0, 0 ) = "!" unless $_->{seen};
            substr( $opt, 0, 0 ) = "*";
        }
        else {
            substr( $opt, 0, 0 ) = ":" if $_->{seen};
        }
        push( @menu, $opt );
        $i++;
    }

    return sprintf "%-30s %-10s %s", $dump, "($type)",
        "[" . join( "|", @menu ) . "]";
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::Input - A generic HTML form input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
