#=============================================================================
# Package: PPM::PPD
# Purpose: Exposes a simple, object-oriented interfaces to PPDs.
# Notes:
# Author:  Neil Watkiss
#=============================================================================
package PPM::PPD;

use strict;
use Data::Dumper;
use XML::Simple ();

$PPM::PPD::VERSION = '3.05';

sub new {
    my $this = shift;
    my $ppd  = shift;
    my $rep  = shift;	# the repository object that retrieved this PPD
    my $id   = shift;	# the unique key in the repository of this PPD
    die "Error: PPM::PPD constructor called with undef ppd\n" .
      Dumper(caller(0))
        unless defined $ppd;
    my $class = ref($this) || $this;
    my $self = bless {
	rep => $rep,
	id  => $id,
	from => 'unknown',
    }, $class;
    $self->init($ppd);
    return $self;
}

sub repository {
    my $o = shift;
    $o->{rep};
}
sub id {
    my $o = shift;
    $o->{id};
}

# Whenever PPM::Repository or a subclass creates a PPM::PPD object, it sets
# 'from' to "Repository". This allows the client to decide whether a PPM::PPD
# object is the result of a query, a search, or a describe. Etc.
sub from {
    my $o = shift;
    $o->{from};
}

sub is_complete {
    my $o = shift;
    $o->{is_complete};
}

sub find_impl_raw {
    my $o = shift;
    my $target = shift;
    for my $impl ($o->implementations) {
	my $match = 1;
	for my $field (keys %$impl) {
	    next if ref($impl->{$field});
	    my $value = $target->config_get($field);
	    if ($value && ref($value) && eval { $value->isa("PPM::Result") }) {
		next unless $value->is_success;
		$match &&= ($value->result eq $impl->{$field});
	    }
	    else {
		next unless defined $value;
		$match &&= ($value eq $impl->{$field});
	    }
	}
	return $impl if $match == 1;
    }
    return undef;
}

sub find_impl {
    my $o = shift;
    my $target = shift;
    my $impl = $o->find_impl_raw($target);
    # We must not 'use' this, because the ppminst code also uses PPM::PPD, and
    # it doesn't have PPM::Result available.
    require PPM::Result;
    return PPM::Result::Ok($impl) if $impl;
    PPM::Result::Error("no suitable implementation found for '"
		       . $o->name . "'.");
}

sub name {
    my $o = shift;
    my $r = $o->{parsed}{NAME};
    return defined $r ? $r : "";
}

sub title {
    my $o = shift;
    my $r = $o->{parsed}{TITLE};
    return defined $r ? $r : "";
}

sub version_osd {
    my $o = shift;
    my $r = $o->{parsed}{VERSION};
    return defined $r ? $r : "";
}

sub version {
    my $o = shift;
    my $v = $o->version_osd;
    printify($v);
}

sub printify {
    my $v = shift;
    $v =~ s/(?:[\.,]0)*$//;
    $v .= '.0' unless ($v =~ /[\.,]/ or $v eq '');
    $v = "(any version)" if $v eq '';
    $v =~ tr/,/./;
    $v;
}

# This sub returns 1 if $ver is >= to $o->version. It returns 0 otherwise.
# Note: this is only used if the repository doesn't know how to compare
# version numbers. The PPM3Server knows how to do it, the others don't.
sub uptodate {
    my $o = shift;
    my $ver = shift;

    return 1 if $ver eq $o->version_osd; # shortcut

    my @required = split /[\.,]/, $o->version_osd;
    my @proposed = split /[\.,]/, $ver;

    for (my $i=0; $i<@required; $i++) {
	no warnings;
	return 0 if $proposed[$i] < $required[$i];	# too old
	return 1 if $proposed[$i] > $required[$i];	# even newer
    }
    return 1; # They're equal
}

sub abstract {
    my $o = shift;
    my $r = $o->{parsed}{ABSTRACT};
    return defined $r ? $r : "";
}

sub author {
    my $o = shift;
    my $r = $o->{parsed}{AUTHOR};
    return defined $r ? $r : "";
}

sub implementations {
    my $o = shift;
    return @{$o->{parsed}{IMPLEMENTATION} || []};
}

sub ppd {
    my $o = shift;
    return $o->{ppd};
}

sub init {
    my $o = shift;
    my $ppd = shift;

    if ($ppd =~ /<SOFTPKG/) {
	$o->{ppd} = $ppd;
	$o->{source} = caller;
    }
    elsif ($ppd !~ m![\n]! && -f $ppd) {
	$o->loadfile($ppd);
	$o->{source} = $ppd;
    }
    else {
	die "PPM::PPD::init: not a PPD and not a file:\n$ppd";
    }

    $o->parse;
}

sub loadfile {
    my $o = shift;
    my $file = shift;
    open FILE, $file		|| die "can't read $file: $!";
    $o->{ppd} = do { local $/; <FILE> };
    close FILE			|| die "can't close $file: $!";
}

sub parse {
    my $o = shift;
    my $parser = XML::Simple->new(
	forcearray	=> 1,
	forcecontent	=> 1,
	keyattr		=> [],
	suppressempty	=> undef,
    );
    my $tree = eval { $parser->XMLin($o->{ppd}) };
    die "error: can't parse $o->{ppd}: $@" if $@;

    # First: SOFTPKG attributes:
    $o->{parsed}{NAME}		= $o->conv($tree->{NAME});
    $o->{parsed}{VERSION}	= $o->conv($tree->{VERSION});

    # Next: childless elements:
    $o->{parsed}{ABSTRACT}	= $o->conv($tree->{ABSTRACT}[0]{content});
    $o->{parsed}{AUTHOR}	= $o->conv($tree->{AUTHOR}[0]{content});
    $o->{parsed}{TITLE}		= $o->conv($tree->{TITLE}[0]{content});

    # Next: IMPLEMENTATION:
    my @impls;
    for my $impl (@{$tree->{IMPLEMENTATION}}) {
	my $i = PPM::PPD::Implementation->new({});
	for my $key (keys %$impl) {
	    # Next: DEPENDENCY:
	    if ($key eq 'DEPENDENCY') {
		my @deps = @{$impl->{$key}};
		$i->{DEPENDENCY} = 
		  [map { PPM::PPD::Dependency->new($_) } @deps];
		next;
	    }
	    # Next: LANGUAGE:
	    if ($key eq 'LANGUAGE') {
		my $v = $impl->{$key}[0];
		my $lang = {
		    NAME	=> $o->conv($v->{NAME}),
		    VERSION	=> $o->conv(
			$v->{COMPAT}[0]{VERSION} || $v->{VERSION}
		    ),
		    TYPE	=> $o->conv($v->{COMPAT}[0]{TYPE}),
		};
		$i->{LANGUAGE} = PPM::PPD::Language->new($lang);
		next;
	    }
	    # Next: INSTALL or UNINSTALL.
	    if ($key eq 'INSTALL' or $key eq 'UNINSTALL') {
		my $v = $impl->{$key}[0];
		$i->{"${key}_SCRIPT"} = PPM::PPD::Script->new({
		    EXEC	=> $o->conv($v->{EXEC}),
		    HREF	=> $o->conv($v->{HREF}),
		    SCRIPT	=> $o->conv($v->{content}),
		});
	    }
	    # Next: CODEBASE, OS, OSVERSION, etc.
	    my @keys = qw(NAME VALUE);
	    push @keys, qw(HREF) if $key eq 'CODEBASE';
	    for (@keys) {
		next unless exists $impl->{$key}[0]{$_};
		$i->{$key} = $o->conv($impl->{$key}[0]{$_});
		last;
	    }
	}
	push @impls, $i;
    }
    $o->{parsed}{IMPLEMENTATION} = \@impls;
    $o->{is_complete} = @impls;
}

sub conv {
    use Unicode::String qw(utf8);
    my $o = shift;
    my $u = utf8(shift(@_) || '');
    my $use_utf8 = 0;
    for my $env (qw(LC_ALL LC_CTYPE LANG PPM_LANG)) {
	$use_utf8 = 1, last if $ENV{$env} and $ENV{$env} =~ /UTF-8/;
    }
    # silence "Data outside latin1 range" warnings
    local $SIG{__WARN__} = sub {};
    $u->stringify_as('latin1') unless $use_utf8;
    "$u";
}

package PPM::PPD::Base;

sub new {
    my $cls = shift;
    my $obj = shift;
    bless $obj, $cls;
}

sub AUTOLOAD {
    my $method = $PPM::PPD::Base::AUTOLOAD;
    $method =~ s/^.+:://;
    my $o = shift;
    my $r = $o->{uc($method)};
    defined $r ? $r : '';
}

sub version_printable { die }
sub osversion_printable { die }

#=============================================================================
# PPM::PPD::Implementation.
# Exposes the following methods:
#
# architecture
# codebase
# os
# osversion_osd
# osversion
# perlcore
# install_script
# uninstall_script
# pythoncore
# 
# prereqs	# returns a list of PPM::PPD::Dependency objects
# language	# returns a PPM::PPD::Language object
#=============================================================================
package PPM::PPD::Implementation;
our @ISA = qw(PPM::PPD::Base);

sub osversion_osd {
    my $o = shift;
    my $r = $o->{OSVERSION};
    defined $r ? $r : '';
}

sub osversion {
    my $o = shift;
    my $r = $o->osversion_osd;
    PPM::PPD::printify($r);
}

sub prereqs {
    my $o = shift;
    return @{$o->{DEPENDENCY} || []};
}

sub language {
    my $o = shift;
    $o->{LANGUAGE};
}

#=============================================================================
# PPM::PPD::Script
# Exposes the following methods:
#
# exec			# a shell/interpreter to use to run the script
# href			# a script to download
# script		# the content of the script (if href not specified)
#=============================================================================
package PPM::PPD::Script;
our @ISA = qw(PPM::PPD::Base);

#=============================================================================
# PPM::PPD::Language.
# Exposes the following methods:
#
# name
# version		# no OSD version for LANGUAGE tag
# type			# one of 'SYNTAX' or 'BINARY'
#
# matches_target($target)	# returns 1 if $target can install PPD, else 0
#=============================================================================
package PPM::PPD::Language;
our @ISA = qw(PPM::PPD::Base);

sub matches_target {
    my $o = shift;
    my $t = shift;
    $t->can_install($o->name, $o->version, $o->type);
}

#=============================================================================
# PPM::PPD::Dependency.
# Exposes the following methods:
#
# name
# version
# version_osd
# 
# uptodate($ppd)	# returns 1 if the given PPM::PPD object satisfies the
# 			# dependency, or 0 otherwise.
#=============================================================================
package PPM::PPD::Dependency;
our @ISA = qw(PPM::PPD::Base);

sub version_osd {
    my $o = shift;
    my $r = $o->{VERSION};
    defined $r ? $r : '';
}

sub version {
    goto &PPM::PPD::version;
}

sub uptodate {
    goto &PPM::PPD::uptodate;
}

package PPM::PPD::Search;
@PPM::PPD::Search::ISA = 'PPM::Search';

use Data::Dumper;

sub matchimpl {
    my $self = shift;
    my ($impl, $field, $re) = @_;
    if ($field eq 'OS')			{ return $impl->os =~ $re }
    elsif ($field eq 'OSVERSION')	{ return $impl->osversion =~ $re }
    elsif ($field eq 'ARCHITECTURE')	{ return $impl->architecture =~ $re}
    elsif ($field eq 'CODEBASE')	{ return $impl->codebase =~ $re }
    elsif ($field eq 'PYTHONCORE')	{ return $impl->pythoncore =~ $re }
    elsif ($field eq 'PERLCORE')	{ return $impl->perlcore =~ $re }
    else {
	warn "unknown search field '$field'" if $^W;
    }
}

sub match {
    my $self = shift;
    my ($ppd, $field, $match) = @_;
    my $re = qr/$match/;
    $field = uc($field);
    if ($field eq 'NAME')	 { return $ppd->name =~ $re }
    if ($field eq 'AUTHOR')      { return $ppd->author =~ $re }
    if ($field eq 'ABSTRACT')    { return $ppd->abstract =~ $re }
    if ($field eq 'TITLE')       { return $ppd->title =~ $re }
    if ($field eq 'VERSION')     { return $ppd->version_printable =~ $re }
    return (grep { $_ }
	    map { $self->matchimpl($_, $field, $re) }
	    $ppd->implementations);
}

1;
