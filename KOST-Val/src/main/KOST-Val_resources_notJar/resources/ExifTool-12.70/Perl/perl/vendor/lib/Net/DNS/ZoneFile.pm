package Net::DNS::ZoneFile;

use strict;
use warnings;

our $VERSION = (qw$Id: ZoneFile.pm 1910 2023-03-30 19:16:30Z willem $)[2];


=head1 NAME

Net::DNS::ZoneFile - DNS zone file

=head1 SYNOPSIS

    use Net::DNS::ZoneFile;

    $zonefile = Net::DNS::ZoneFile->new( 'named.example' );

    while ( $rr = $zonefile->read ) {
	$rr->print;
    }

    @zone = $zonefile->read;


=head1 DESCRIPTION

Each Net::DNS::ZoneFile object instance represents a zone file
together with any subordinate files introduced by the $INCLUDE
directive.  Zone file syntax is defined by RFC1035.

A program may have multiple zone file objects, each maintaining
its own independent parser state information.

The parser supports both the $TTL directive defined by RFC2308
and the BIND $GENERATE syntax extension.

All RRs in a zone file must have the same class, which may be
specified for the first RR encountered and is then propagated
automatically to all subsequent records.

=cut


use integer;
use Carp;
use IO::File;

use base qw(Exporter);
our @EXPORT = qw(parse read readfh);

use constant PERLIO => defined eval { require PerlIO };

use constant UTF8 => scalar eval {	## not UTF-EBCDIC  [see Unicode TR#16 3.6]
	require Encode;
	Encode::encode_utf8( chr(182) ) eq pack( 'H*', 'C2B6' );
};

require Net::DNS::Domain;
require Net::DNS::RR;


=head1 METHODS


=head2 new

    $zonefile = Net::DNS::ZoneFile->new( 'filename', ['example.com'] );

    $handle   = IO::File->new( 'filename', '<:encoding(ISO8859-7)' );
    $zonefile = Net::DNS::ZoneFile->new( $handle, ['example.com'] );

The new() constructor returns a Net::DNS::ZoneFile object which
represents the zone file specified in the argument list.

The specified file or file handle is open for reading and closed when
exhausted or all references to the ZoneFile object cease to exist.

The optional second argument specifies $ORIGIN for the zone file.

Zone files are presumed to be UTF-8 encoded where that is supported.

Alternative character encodings may be specified indirectly by creating
a file handle with the desired encoding layer, which is then passed as
an argument to new(). The specified encoding is propagated to files
introduced by $INCLUDE directives.

=cut

sub new {
	my ( $class, $filename, $origin ) = @_;
	my $self = bless {fileopen => {}}, $class;

	$self->_origin($origin);

	if ( ref($filename) ) {
		$self->{filehandle} = $self->{filename} = $filename;
		return $self if ref($filename) =~ /IO::File|FileHandle|GLOB|Text/;
		croak 'argument not a file handle';
	}

	croak 'filename argument undefined' unless $filename;
	my $discipline = UTF8 ? '<:encoding(UTF-8)' : '<';
	$self->{filehandle} = IO::File->new( $filename, $discipline ) or croak "$filename: $!";
	$self->{fileopen}->{$filename}++;
	$self->{filename} = $filename;
	return $self;
}


=head2 read

    $rr = $zonefile->read;
    @rr = $zonefile->read;

When invoked in scalar context, read() returns a Net::DNS::RR object
representing the next resource record encountered in the zone file,
or undefined if end of data has been reached.

When invoked in list context, read() returns the list of Net::DNS::RR
objects in the order that they appear in the zone file.

Comments and blank lines are silently disregarded.

$INCLUDE, $ORIGIN, $TTL and $GENERATE directives are processed
transparently.

=cut

sub read {
	my ($self) = @_;

	return &_read unless ref $self;				# compatibility interface

	if (wantarray) {
		my @zone;					# return entire zone
		eval {
			local $SIG{__DIE__};
			while ( my $rr = $self->_getRR ) {
				push( @zone, $rr );
			}
		};
		croak join ' ', $@, ' file', $self->name, 'line', $self->line, "\n " if $@;
		return @zone;
	}

	my $rr = eval {
		local $SIG{__DIE__};
		$self->_getRR;					# return single RR
		};
	croak join ' ', $@, ' file', $self->name, 'line', $self->line, "\n " if $@;
	return $rr;
}


=head2 name

    $filename = $zonefile->name;

Returns the name of the current zone file.
Embedded $INCLUDE directives will cause this to differ from the
filename argument supplied when the object was created.

=cut

sub name {
	return shift->{filename};
}


=head2 line

    $line = $zonefile->line;

Returns the number of the last line read from the current zone file.

=cut

sub line {
	my $self = shift;
	return $self->{eom} if defined $self->{eom};
	return $self->{filehandle}->input_line_number;
}


=head2 origin

    $origin = $zonefile->origin;

Returns the fully qualified name of the current origin within the
zone file.

=cut

sub origin {
	my $context = shift->{context};
	return &$context( sub { Net::DNS::Domain->new('@') } )->string;
}


=head2 ttl

    $ttl = $zonefile->ttl;

Returns the default TTL as specified by the $TTL directive.

=cut

sub ttl {
	return shift->{TTL};
}


=head1 COMPATIBILITY WITH Net::DNS::ZoneFile 1.04

Applications which depended on the defunct Net::DNS::ZoneFile 1.04
CPAN distribution will continue to operate with minimal change using
the compatibility interface described below.
New application code should use the object-oriented interface.

    use Net::DNS::ZoneFile;

    $listref = Net::DNS::ZoneFile->read( $filename );
    $listref = Net::DNS::ZoneFile->read( $filename, $include_dir );

    $listref = Net::DNS::ZoneFile->readfh( $filehandle );
    $listref = Net::DNS::ZoneFile->readfh( $filehandle, $include_dir );

    $listref = Net::DNS::ZoneFile->parse(  $string );
    $listref = Net::DNS::ZoneFile->parse( \$string );
    $listref = Net::DNS::ZoneFile->parse(  $string, $include_dir );
    $listref = Net::DNS::ZoneFile->parse( \$string, $include_dir );

    $_->print for @$listref;

The optional second argument specifies the default path for filenames.
The current working directory is used by default.

Although not available in the original implementation, the RR list can
be obtained directly by calling any of these methods in list context.

    @rr = Net::DNS::ZoneFile->read( $filename, $include_dir );

The partial result is returned if an error is encountered by the parser.


=head2 read

    $listref = Net::DNS::ZoneFile->read( $filename );
    $listref = Net::DNS::ZoneFile->read( $filename, $include_dir );

read() parses the contents of the specified file
and returns a reference to the list of Net::DNS::RR objects.
The return value is undefined if an error is encountered by the parser.

=cut

our $include_dir;			## dynamically scoped

sub _filename {				## rebase unqualified filename
	my $name = shift;
	return $name if ref($name);	## file handle
	return $name unless $include_dir;
	require File::Spec;
	return $name if File::Spec->file_name_is_absolute($name);
	return $name if -f $name;	## file in current directory
	return File::Spec->catfile( $include_dir, $name );
}


sub _read {
	my ($arg1) = @_;
	shift if !ref($arg1) && $arg1 eq __PACKAGE__;
	my $filename = shift;
	local $include_dir = shift;

	my $zonefile = Net::DNS::ZoneFile->new( _filename($filename) );
	my @zone;
	eval {
		local $SIG{__DIE__};
		my $rr;
		push( @zone, $rr ) while $rr = $zonefile->_getRR;
	};
	return wantarray ? @zone : \@zone unless $@;
	carp $@;
	return wantarray ? @zone : undef;
}


{

	package Net::DNS::ZoneFile::Text;	## no critic ProhibitMultiplePackages

	use overload ( '<>' => 'readline' );

	sub new {
		my ( $class, $data ) = @_;
		my $self = bless {}, $class;
		$self->{data} = [split /\n/, ref($data) ? $$data : $data];
		return $self;
	}

	sub readline {
		my $self = shift;
		$self->{line}++;
		return shift( @{$self->{data}} );
	}

	sub close {
		shift->{data} = [];
		return 1;
	}

	sub input_line_number {
		return shift->{line};
	}

}


=head2 readfh

    $listref = Net::DNS::ZoneFile->readfh( $filehandle );
    $listref = Net::DNS::ZoneFile->readfh( $filehandle, $include_dir );

readfh() parses data from the specified file handle
and returns a reference to the list of Net::DNS::RR objects.
The return value is undefined if an error is encountered by the parser.

=cut

sub readfh {
	return &_read;
}


=head2 parse

    $listref = Net::DNS::ZoneFile->parse(  $string );
    $listref = Net::DNS::ZoneFile->parse( \$string );
    $listref = Net::DNS::ZoneFile->parse(  $string, $include_dir );
    $listref = Net::DNS::ZoneFile->parse( \$string, $include_dir );

parse() interprets the text in the argument string
and returns a reference to the list of Net::DNS::RR objects.
The return value is undefined if an error is encountered by the parser.

=cut

sub parse {
	my ($arg1) = @_;
	shift if $arg1 eq __PACKAGE__;
	my $string  = shift;
	my @include = grep {defined} shift;
	return &readfh( Net::DNS::ZoneFile::Text->new($string), @include );
}


########################################


{

	package Net::DNS::ZoneFile::Generator;	## no critic ProhibitMultiplePackages

	use overload ( '<>' => 'readline' );

	sub new {
		my ( $class, $range, $template, $line ) = @_;
		my $self = bless {}, $class;

		my ( $bound, $step ) = split m#[/]#, $range;	# initial iterator state
		my ( $first, $last ) = split m#[-]#, $bound;
		$first ||= 0;
		$last  ||= $first;
		$step  ||= 1;					# coerce step to match range
		$step = ( $last < $first ) ? -abs($step) : abs($step);
		$self->{count} = int( ( $last - $first ) / $step ) + 1;

		for ($template) {
			s/\\\$/\\036/g;				# disguise escaped dollar
			s/\$\$/\\036/g;				# disguise escaped dollar
			s/^"(.*)"$/$1/s;			# unwrap BIND's quoted template
			@{$self}{qw(instant step template line)} = ( $first, $step, $_, $line );
		}
		return $self;
	}

	sub readline {
		my $self = shift;
		return unless $self->{count}-- > 0;		# EOF

		my $instant = $self->{instant};			# update iterator state
		$self->{instant} += $self->{step};

		local $_ = $self->{template};			# copy template
		while (/\$\{(.*)\}/) {				# interpolate ${...}
			my $s = _format( $instant, split /\,/, $1 );
			s/\$\{$1\}/$s/eg;
		}

		s/\$/$instant/eg;				# interpolate $
		s/\\036/\$/g;					# reinstate escaped $
		return $_;
	}

	sub close {
		shift->{count} = 0;				# suppress iterator
		return 1;
	}

	sub input_line_number {
		return shift->{line};				# fixed: identifies $GENERATE
	}


	sub _format {			## convert $GENERATE iteration number to specified format
		my $number = shift;				# per ISC BIND 9.7
		my $offset = shift || 0;
		my $length = shift || 0;
		my $format = shift || 'd';

		my $value = $number + $offset;
		my $digit = $length || 1;
		return substr sprintf( "%01.$digit$format", $value ), -$length if $format =~ /[doxX]/;

		my $nibble = join( '.', split //, sprintf ".%32.32lx", $value );
		return reverse lc( substr $nibble, -$length ) if $format =~ /[n]/;
		return reverse uc( substr $nibble, -$length ) if $format =~ /[N]/;
		die "unknown $format format";
	}

}


sub _generate {				## expand $GENERATE into input stream
	my ( $self, $range, $template ) = @_;

	my $handle = Net::DNS::ZoneFile::Generator->new( $range, $template, $self->line );

	delete $self->{latest};					# forget previous owner
	$self->{parent} = bless {%$self}, ref($self);		# save state, create link
	return $self->{filehandle} = $handle;
}


my $LEX_REGEX = q/("[^"]*"|"[^"]*$)|;[^\n]*|([()])|[ \t\n\r\f]+/;

sub _getline {				## get line from current source
	my $self = shift;

	my $fh = $self->{filehandle};
	while (<$fh>) {
		next if /^\s*;/;				# discard comment line
		next unless /\S/;				# discard blank line

		if (/["(]/) {
			s/\\\\/\\092/g;				# disguise escaped escape
			s/\\"/\\034/g;				# disguise escaped quote
			s/\\\(/\\040/g;				# disguise escaped bracket
			s/\\\)/\\041/g;				# disguise escaped bracket
			s/\\;/\\059/g;				# disguise escaped semicolon
			my @token = grep { defined && length } split /(^\s)|$LEX_REGEX/o;

			while ( $token[-1] =~ /^"[^"]*$/ ) {	# multiline quoted string
				$_ = pop(@token) . <$fh>;	# reparse fragments
				s/\\\\/\\092/g;			# disguise escaped escape
				s/\\"/\\034/g;			# disguise escaped quote
				s/\\\(/\\040/g;			# disguise escaped bracket
				s/\\\)/\\041/g;			# disguise escaped bracket
				s/\\;/\\059/g;			# disguise escaped semicolon
				push @token, grep { defined && length } split /$LEX_REGEX/o;
				$_ = join ' ', @token;		# reconstitute RR string
			}

			if ( grep { $_ eq '(' } @token ) {	# concatenate multiline RR
				until ( grep { $_ eq ')' } @token ) {
					$_ = pop(@token) . <$fh>;
					s/\\\\/\\092/g;		# disguise escaped escape
					s/\\"/\\034/g;		# disguise escaped quote
					s/\\\(/\\040/g;		# disguise escaped bracket
					s/\\\)/\\041/g;		# disguise escaped bracket
					s/\\;/\\059/g;		# disguise escaped semicolon
					push @token, grep { defined && length } split /$LEX_REGEX/o;
					chomp $token[-1] unless $token[-1] =~ /^"[^"]*$/;
				}
				$_ = join ' ', @token;		# reconstitute RR string
			}
		}

		return $_ unless /^[\$]/;			# RR string

		my @token = grep { defined && length } split /$LEX_REGEX/o;
		if (/^\$INCLUDE/) {				# directive
			my ( $keyword, @argument ) = @token;
			die '$INCLUDE incomplete' unless @argument;
			$fh = $self->_include(@argument);

		} elsif (/^\$GENERATE/) {			# directive
			my ( $keyword, $range, @template ) = @token;
			die '$GENERATE incomplete' unless @template;
			$fh = $self->_generate( $range, "@template" );

		} elsif (/^\$ORIGIN/) {				# directive
			my ( $keyword, $origin ) = @token;
			die '$ORIGIN incomplete' unless defined $origin;
			$self->_origin($origin);

		} elsif (/^\$TTL/) {				# directive
			my ( $keyword, $ttl ) = @token;
			die '$TTL incomplete' unless defined $ttl;
			$self->{TTL} = Net::DNS::RR::ttl( {}, $ttl );

		} else {					# unrecognised
			my ($keyword) = @token;
			die qq[unknown "$keyword" directive];
		}
	}

	$self->{eom} = $self->line;				# end of file
	$fh->close();
	my $link = $self->{parent} || return;			# end of zone
	%$self = %$link;					# end $INCLUDE
	return $self->_getline;					# resume input
}


sub _getRR {				## get RR from current source
	my $self = shift;

	local $_;
	$self->_getline || return;				# line already in $_

	my $noname = s/^\s/\@\t/;				# placeholder for empty RR name

	# construct RR object with context specific dynamically scoped $ORIGIN
	my $context = $self->{context};
	my $rr	    = &$context( sub { Net::DNS::RR->_new_string($_) } );

	my $latest = $self->{latest};				# overwrite placeholder
	$rr->{owner} = $latest->{owner} if $noname && $latest;

	$self->{class} = $rr->class unless $self->{class};	# propagate RR class
	$rr->class( $self->{class} );

	unless ( defined $self->{TTL} ) {
		$self->{TTL} = $rr->minimum if $rr->type eq 'SOA';    # default TTL
	}
	$rr->{ttl} = $self->{TTL} unless defined $rr->{ttl};

	return $self->{latest} = $rr;
}


sub _include {				## open $INCLUDE file
	my ( $self, $include, $origin ) = @_;

	my $filename = _filename($include);
	die qq(\$INCLUDE $filename: Unexpected recursion) if $self->{fileopen}->{$filename}++;

	my $discipline = PERLIO ? join( ':', '<', PerlIO::get_layers $self->{filehandle} ) : '<';
	my $filehandle = IO::File->new( $filename, $discipline ) or die qq(\$INCLUDE $filename: $!);

	delete $self->{latest};					# forget previous owner
	$self->{parent} = bless {%$self}, ref($self);		# save state, create link
	$self->_origin($origin) if $origin;
	$self->{filename} = $filename;
	return $self->{filehandle} = $filehandle;
}


sub _origin {				## change $ORIGIN (scope: current file)
	my ( $self, $name ) = @_;
	my $context = $self->{context};
	$context = Net::DNS::Domain->origin(undef) unless $context;
	$self->{context} = &$context( sub { Net::DNS::Domain->origin($name) } );
	delete $self->{latest};					# forget previous owner
	return;
}


1;
__END__


=head1 ACKNOWLEDGEMENTS

This package is designed as an improved and compatible replacement
for Net::DNS::ZoneFile 1.04 which was created by Luis Munoz in 2002
as a separate CPAN module.

The present implementation is the result of an agreement to merge our
two different approaches into one package integrated into Net::DNS.
The contribution of Luis Munoz is gratefully acknowledged.

Thanks are also due to Willem Toorop for his constructive criticism
of the initial version and invaluable assistance during testing.


=head1 COPYRIGHT

Copyright (c)2011-2012 Dick Franks.

All rights reserved.


=head1 LICENSE

Permission to use, copy, modify, and distribute this software and its
documentation for any purpose and without fee is hereby granted, provided
that the original copyright notices appear in all copies and that both
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

L<perl> L<Net::DNS> L<Net::DNS::RR>
L<RFC1035(5.1)|https://tools.ietf.org/html/rfc1035>
L<RFC2308(4)|https://tools.ietf.org/html/rfc2308>

L<BIND Administrator Reference Manual|http://bind.isc.org/>

=cut

