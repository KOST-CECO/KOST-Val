package PPM::Result;

use strict;
use Exporter;
use Text::Autoformat;
use Data::Dumper;

$PPM::Result::VERSION	= '3.00';
@PPM::Result::ISA	= qw(Exporter);
@PPM::Result::EXPORT_OK	= qw(Error Warning Ok Success List Hash);

#=============================================================================
# Shortcut constructors for various types of results
#=============================================================================
sub Error {
    my $msg = shift;
    my $code = shift || 1;
    my $res = shift;
    PPM::Result->new($res, $code, $msg);
}

sub Warning {
    my $msg = shift;
    my $code = shift || -1;
    my $res = shift;
    PPM::Result->new($res, $code, $msg);
}

sub Ok {
    Success(@_);
}

sub Success {
    PPM::Result->new(@_);
}

# Shortcut for returning a list, successfully.
sub List {
    my @list = @_;
    Ok(\@list);
}

# Shortcut for returning a hash, successfully.
sub Hash {
    my %hash = @_;
    Ok(\%hash);
}

#=============================================================================
# The class implementation
#=============================================================================
sub new {
    my $self   = shift;
    my $class  = ref($self) || $self;
    my $result = shift;
    $result = '' unless defined $result;
    my $code   = shift || '0';
    my $msg    = shift || '';
    chomp $msg;
    bless {
	result => $result,
	code   => $code,
	msg    => $msg,
	on_destruct => [],
    }, $class;
}

sub DESTROY {
    my $o = shift;
    for my $cref (@{$o->{on_destruct}}) {
	$cref->($o);
    }
}

sub on_destruct {
    my $o = shift;
    my $cref = shift;
    push @{$o->{on_destruct}}, $cref;
}

sub ok {
    my $o = shift;
    $o->{code} <= 0;
}

sub is_success {
    my $o = shift;
    $o->{code} == 0;
}

sub is_warning {
    my $o = shift;
    $o->{code} < 0;
}

sub is_error {
    my $o = shift;
    $o->{code} > 0;
}

sub errorcode {
    my $o = shift;
    $o->{code};
}

sub msg {
    my $o = shift;
    my $w = $o->is_error	? "Error: "	:
	    $o->is_warning	? "Warning: "	:
				  "Success";
    $w .= $o->msg_raw;
    $w .= "\n";
    autoformat($w, { all => 1 } );
}

sub msg_raw {
    my $o = shift;
    defined $o->{msg} ? $o->{msg} : '';
}

sub result {
    my $o = shift;
    my $key = shift;
    return $o->{result} unless defined $key;
    return $o->{result}{$key} if eval { exists $o->{result}{$key} };
    return $o->{result}[$key] if eval { exists $o->{result}[$key] };
    return undef;
}
sub result_s {
    my $o = shift;
    scalar $o->result(@_);
}
sub result_l {
    my $o = shift;
    @{$o->result(@_)};
}
sub result_h {
    my $o = shift;
    %{$o->result(@_)};
}

1;
