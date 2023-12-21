package HTML::Form::ListInput;

use strict;
use parent 'HTML::Form::Input';

use Carp 'croak';

our $VERSION = '6.11';

# ABSTRACT: An HTML form list input element for use with HTML::Form

#select/option   (val1, val2, ....)
#input/radio     (undef, val1, val2,...)
#input/checkbox  (undef, value)
#select-multiple/option (undef, value)

sub new {
    my $class = shift;
    my $self  = $class->SUPER::new(@_);

    my $value      = delete $self->{value};
    my $value_name = delete $self->{value_name};
    my $type       = $self->{type};

    if ( $type eq "checkbox" ) {
        $value = "on" unless defined $value;
        $self->{menu} = [
            { value => undef,  name => "off", },
            { value => $value, name => $value_name, },
        ];
        $self->{current} = ( delete $self->{checked} ) ? 1 : 0;

    }
    else {
        $self->{option_disabled}++
            if $type eq "radio" && delete $self->{disabled};
        $self->{menu} = [
            { value => $value, name => $value_name },
        ];
        my $checked = $self->{checked} || $self->{option_selected};
        delete $self->{checked};
        delete $self->{option_selected};
        if ( exists $self->{multiple} ) {
            unshift( @{ $self->{menu} }, { value => undef, name => "off" } );
            $self->{current} = $checked ? 1 : 0;
        }
        else {
            $self->{current} = 0 if $checked;
        }
    }
    $self;
}

sub add_to_form {
    my ( $self, $form ) = @_;
    my $type = $self->type;

    return $self->SUPER::add_to_form($form)
        if $type eq "checkbox";

    if ( $type eq "option" && exists $self->{multiple} ) {
        $self->{disabled} ||= delete $self->{option_disabled};
        return $self->SUPER::add_to_form($form);
    }

    Carp::croak "Assert" if @{ $self->{menu} } != 1;
    my $m = $self->{menu}[0];
    $m->{disabled}++ if delete $self->{option_disabled};

    # if there was no name we have to search for an input that explicitly has
    # no name either, because otherwise the name attribute would be ignored
    my $prev = $form->find_input(
        $self->{name} || \undef, $self->{type},
        $self->{idx}
    );
    return $self->SUPER::add_to_form($form) unless $prev;

    # merge menus
    $prev->{current} = @{ $prev->{menu} } if exists $self->{current};
    push( @{ $prev->{menu} }, $m );
}

sub fixup {
    my $self = shift;
    if ( $self->{type} eq "option" && !( exists $self->{current} ) ) {
        $self->{current} = 0;
    }
    $self->{menu}[ $self->{current} ]{seen}++ if exists $self->{current};
}

sub disabled {
    my $self = shift;
    my $type = $self->type;

    my $old = $self->{disabled} || _menu_all_disabled( @{ $self->{menu} } );
    if (@_) {
        my $v = shift;
        $self->{disabled} = $v;
        for ( @{ $self->{menu} } ) {
            $_->{disabled} = $v;
        }
    }
    return $old;
}

sub _menu_all_disabled {
    for (@_) {
        return 0 unless $_->{disabled};
    }
    return 1;
}

sub value {
    my $self = shift;
    my $old;
    $old = $self->{menu}[ $self->{current} ]{value}
        if exists $self->{current};
    $old = $self->{value} if exists $self->{value};
    if (@_) {
        my $i   = 0;
        my $val = shift;
        my $cur;
        my $disabled;
        for ( @{ $self->{menu} } ) {
            if (
                (
                       defined($val)
                    && defined( $_->{value} )
                    && $val eq $_->{value}
                )
                || ( !defined($val) && !defined( $_->{value} ) )
            ) {
                $cur      = $i;
                $disabled = $_->{disabled};
                last unless $disabled;
            }
            $i++;
        }
        if ( !( defined $cur ) || $disabled ) {
            if ( defined $val ) {

                # try to search among the alternative names as well
                my $i = 0;
                my $cur_ignorecase;
                my $lc_val = lc($val);
                for ( @{ $self->{menu} } ) {
                    if ( defined $_->{name} ) {
                        if ( $val eq $_->{name} ) {
                            $disabled = $_->{disabled};
                            $cur      = $i;
                            last unless $disabled;
                        }
                        if ( !defined($cur_ignorecase)
                            && $lc_val eq lc( $_->{name} ) ) {
                            $cur_ignorecase = $i;
                        }
                    }
                    $i++;
                }
                unless ( defined $cur ) {
                    $cur = $cur_ignorecase;
                    if ( defined $cur ) {
                        $disabled = $self->{menu}[$cur]{disabled};
                    }
                    elsif ( $self->{strict} ) {
                        my $n = $self->name;
                        Carp::croak("Illegal value '$val' for field '$n'");
                    }
                }
            }
            elsif ( $self->{strict} ) {
                my $n = $self->name;
                Carp::croak("The '$n' field can't be unchecked");
            }
        }
        if ( $self->{strict} && $disabled ) {
            my $n = $self->name;
            Carp::croak("The value '$val' has been disabled for field '$n'");
        }
        if ( defined $cur ) {
            $self->{current} = $cur;
            $self->{menu}[$cur]{seen}++;
            delete $self->{value};
        }
        else {
            $self->{value} = $val;
            delete $self->{current};
        }
    }
    $old;
}

sub check {
    my $self = shift;
    $self->{current} = 1;
    $self->{menu}[1]{seen}++;
}

sub possible_values {
    my $self = shift;
    map $_->{value}, grep !$_->{disabled}, @{ $self->{menu} };
}

sub other_possible_values {
    my $self = shift;
    map $_->{value}, grep !$_->{seen} && !$_->{disabled}, @{ $self->{menu} };
}

sub value_names {
    my $self = shift;
    my @names;
    for ( @{ $self->{menu} } ) {
        my $n = $_->{name};
        $n = $_->{value} unless defined $n;
        push( @names, $n );
    }
    @names;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

HTML::Form::ListInput - An HTML form list input element for use with HTML::Form

=head1 VERSION

version 6.11

=head1 AUTHOR

Gisle Aas <gisle@activestate.com>

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 1998 by Gisle Aas.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
