package Net::DNS::RR::SVCB;

use strict;
use warnings;
our $VERSION = (qw$Id: SVCB.pm 1823 2020-11-16 16:29:45Z willem $)[2];

use base qw(Net::DNS::RR);


=head1 NAME

Net::DNS::RR::SVCB - DNS SVCB resource record

=cut

use integer;

use Carp;
use MIME::Base64;
use Net::DNS::DomainName;
use Net::DNS::RR::A;
use Net::DNS::RR::AAAA;
use Net::DNS::Text;


sub _decode_rdata {			## decode rdata from wire-format octet string
	my $self = shift;
	my ( $data, $offset ) = @_;

	my $rdata = substr $$data, $offset, $self->{rdlength};
	$self->{SvcPriority} = unpack( 'n', $rdata );

	my $index;
	( $self->{TargetName}, $index ) = Net::DNS::DomainName->decode( \$rdata, 2 );

	my $params = $self->{SvcParams} = {};
	my $limit  = length($rdata) - 4;
	while ( $index < $limit ) {
		my ( $key, $size ) = unpack( "\@$index n2", $rdata );
		$params->{$key} = substr $rdata, $index + 4, $size;
		$index += ( $size + 4 );
	}
	die 'corrupt RDATA in ' . $self->type unless $index == $self->{rdlength};
	$self->_post_parse;
	return;
}


sub _encode_rdata {			## encode rdata as wire-format octet string
	my $self = shift;

	my ($params) = grep {defined} $self->{SvcParams}, {};
	my @params;
	foreach ( sort { $a <=> $b } keys %$params ) {
		my $value = $params->{$_};
		next unless defined $value;
		push @params, pack( 'n2a*', $_, length($value), $value );
	}
	return pack 'n a* a*', $self->{SvcPriority}, $self->{TargetName}->encode, join '', @params;
}


sub _format_rdata {			## format rdata portion of RR string.
	my $self = shift;

	my ($params) = grep {defined} $self->{SvcParams}, {};
	my @keys = keys %$params;
	return ( $self->{SvcPriority}, $self->{TargetName}->string ) unless scalar @keys;

	my @rdata  = unpack 'H4', pack 'n', $self->{SvcPriority};
	my $target = $self->{TargetName}->encode();
	my $length = 2 + length $target;
	my @target = split /(\S{32})/, unpack 'H*', $target;
	$target[-1] .= join ' ', "\t;", $self->{TargetName}->string if $length > 3;
	push @rdata, $length > 18 ? "\n" : (), @target, "\n";

	foreach ( sort { $a <=> $b } @keys ) {
		my $value = $params->{$_};
		next unless defined $value;
		push @rdata, "; key$_=...\n" if $_ > 15;
		push @rdata, unpack 'H4H4', pack( 'n2', $_, length $value );
		push @rdata, split /(\S{32})/, unpack 'H*', $value;
		push @rdata, "\n";
		$length += 4 + length $value;
	}
	return ( "\\# $length", @rdata );
}


sub _parse_rdata {			## populate RR from rdata in argument list
	my $self = shift;

	$self->svcpriority(shift);
	$self->targetname(shift);

	while ( my $attribute = shift ) {
		for ($attribute) {
			my @argument = '';
			if (/=(.*)$/) {
				for ( my $rhs = length($1) ? $1 : shift ) {
					s/^(["'])(.*)\1$/$2/;	# strip paired quotes
					s/\\,/\\044/g;		# disguise escaped comma
					@argument = split /,/;	# potentially multi-valued
				}
			}

			s/[-]/_/g;				# extract attribute identifier
			m/^([^=]+)/;
			$self->$1(@argument);
		}
	}
	return;
}


sub _post_parse {			## parser post processing
	my $self = shift;

	my $params = $self->{SvcParams} || return;
	my %unique;
	my @unique = grep { !$unique{$_}++ } unpack 'n*', ( $params->{0} || return );
	croak( $self->type . qq(: mandatory "key0" not permitted) ) if $unique{0};
	foreach (@unique) {
		croak( $self->type . qq(: duplicate "key$_" in mandatory list) ) if --$unique{$_};
		croak( $self->type . qq(: mandatory "key$_" not defined) )	 if !defined( $params->{$_} );
	}
	return;
}


sub _defaults {				## specify RR attribute default values
	my $self = shift;

	$self->_parse_rdata(qw(0 .));
	return;
}


sub svcpriority {
	my $self = shift;					# uncoverable pod

	$self->{SvcPriority} = 0 + shift if scalar @_;
	return $self->{SvcPriority} || 0;
}


sub targetname {
	my $self = shift;					# uncoverable pod

	$self->{TargetName} = Net::DNS::DomainName->new(shift) if scalar @_;

	my $target = $self->{TargetName} ? $self->{TargetName}->name : return;
	return $target unless $self->{SvcPriority};
	return ( $target eq '.' ) ? $self->owner : $target;
}


########################################

use constant ASCII => chr(65) eq 'A';

my %escape = do {			## precalculated ASCII escape table
	my @escape = ( 0 .. 32, 34, 92, 127 .. 255 );		# numerical escape

	my %table = map { ( chr($_) => chr($_) ) } ( 0 .. 255 );

	foreach my $codepoint (@escape) {
		my $ddd = sprintf( '%03u', $codepoint );
		$ddd =~ tr [0-9] [\060-\071];			# transliterate non-ASCII
		$table{pack( 'C', $codepoint )} = pack 'C a3', 92, $ddd;
	}

	%table;
};

sub _raw {				## concatenate @_ and escape non-printable
	return () unless scalar @_;
	local $_ = join '', map { $escape{$_} } map { split( //, $_ ) } @_;

	# partial transliteration for non-ASCII character encodings
	tr
	[\040-\176\000-\377]
	[ !"#$%&'()*+,\-./0-9:;<=>?@A-Z\[\\\]^_`a-z{|}~?] unless ASCII;

	return $_;
}

sub _base64 {
	return _raw( map { MIME::Base64::decode($_) } @_ );
}

sub _integer16 {
	return _raw( map { pack( 'n', $_ ) } @_ );
}

sub _ipv4 {
	return _raw( map { bless( {}, 'Net::DNS::RR::A' )->address($_) } @_ );
}

sub _ipv6 {
	return _raw( map { bless( {}, 'Net::DNS::RR::AAAA' )->address($_) } @_ );
}

sub _string {
	return _raw( map { Net::DNS::Text->new($_)->encode() } @_ );
}


my %keybyname = (
	mandatory	  => 0,
	alpn		  => 1,
	'no-default-alpn' => 2,
	port		  => 3,
	ipv4hint	  => 4,
	echconfig	  => 5,
	ipv6hint	  => 6,
	);


sub mandatory {				## mandatory=key1,port,...
	my ( $self, @arg ) = grep {defined} @_;
	my @keys = map { $keybyname{lc $_} || ( /^key(\d+)$/i ? $1 : croak qq["$_" unknown] ) } @arg;
	return $self->key0( _integer16( sort { $a <=> $b } @keys ) );
}

sub alpn {				## alpn=h3,h2,...
	my $self = shift;
	return $self->key1( _string(@_) );
}

sub no_default_alpn {			## no-default-alpn
	return shift->key2( map {''} @_ );			# uncoverable pod
}

sub port {				## port=1234
	my $self = shift;
	return $self->key3( map { _integer16($_) } @_ );
}

sub ipv4hint {				## ipv4hint=192.0.2.1,...
	my $self = shift;
	return $self->key4( _ipv4(@_) );
}

sub echconfig {				## echconfig=base64string
	my $self = shift;
	return $self->key5( map { _base64($_) } @_ );
}

sub ipv6hint {				## ipv6hint=2001:DB8::1,...
	my $self = shift;
	return $self->key6( _ipv6(@_) );
}


our $AUTOLOAD;

sub AUTOLOAD {				## Dynamic constructor/accessor methods
	my $self = shift;

	my ($method) = reverse split /::/, $AUTOLOAD;

	my $default = join '::', 'SUPER', $method;
	return $self->$default(@_) unless $method =~ /^key(\d+)$/i;
	my $key = $1;

	my ($params) = grep {defined} $self->{SvcParams}, {};

	if ( scalar @_ ) {
		my $arg = shift;				# keyNN($value);
		croak 'unexpected number of arguments' if scalar @_;

		$params->{$key} = defined($arg) ? Net::DNS::Text->new($arg)->raw : undef;
		$self->{SvcParams} = $params;
	}

	my $value = $params->{$key};
	return defined($value) ? _raw($value) : $value;
}


1;
__END__


=head1 SYNOPSIS

    use Net::DNS;
    $rr = Net::DNS::RR->new('name HTTPS SvcPriority TargetName alpn=h3,...');

=head1 DESCRIPTION

DNS Service Binding (SVCB) resource record

Service binding and parameter specification
via the DNS (SVCB and HTTPS RRs)

=head1 METHODS

The available methods are those inherited from the base class augmented
by the type-specific methods defined in this package.

Use of undocumented package features or direct access to internal data
structures is discouraged and could result in program termination or
other unpredictable behaviour.


=head2 SvcPriority

    $svcpriority = $rr->svcpriority;
    $rr->svcpriority( $svcpriority );

The priority of this record
(relative to others, with lower values preferred). 
A value of 0 indicates AliasMode.

=head2 TargetName

    $rr->targetname( $targetname );
    $effecivetarget = $rr->targetname;

The domain name of either the alias target (for AliasMode)
or the alternative endpoint (for ServiceMode).

For AliasMode SVCB RRs, a TargetName of "." indicates that the
service is not available or does not exist.

For ServiceMode SVCB RRs, a TargetName of "." indicates that the
owner name of this record must be used as the effective TargetName.

=head2 mandatory, alpn, no-default-alpn, port, ipv4hint, echconfig, ipv6hint

    $rr = Net::DNS::RR->new( 'svc.example. SVCB 1 svc.example. port=1234' );

    $rr->port(1234);
    $string = $rr->port();	# \004\210
    $rr->key3($string);

Constructor methods for mnemonic SvcParams defined in draft-ietf-dnsop-svcb-https-01.
When invoked without arguments, the methods return the presentation format
value for the underlying key.
The behaviour with undefined arguments is not specified.

=head2 keyNN

    $keynn = $rr->keyNN;
    $rr->keyNN( $keynn );

Generic constructor and accessor methods for SvcParams.
The key index NN is a decimal integer in the range 0 .. 65534.
The method argument and returned value are both presentation format strings.
The method returns the undefined value if the key is not present.
A (key,value) pair will be ignored if the value is undefined.


=head1 COPYRIGHT

Copyright (c)2020 Dick Franks. 

All rights reserved.

Package template (c)2009,2012 O.M.Kolkman and R.W.Franks.


=head1 LICENSE

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose and without fee is hereby granted, provided
that the above copyright notice appear in all copies and that both that
copyright notice and this permission notice appear in supporting
documentation, and that the name of the author not be used in advertising
or publicity pertaining to distribution of the software without specific
prior written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.


=head1 SEE ALSO

L<perl>, L<Net::DNS>, L<Net::DNS::RR>, draft-ietf-dnsop-svcb-https-01

=cut
