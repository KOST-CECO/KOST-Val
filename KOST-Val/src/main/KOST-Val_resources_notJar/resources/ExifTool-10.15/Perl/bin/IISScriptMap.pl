###############################################################################
#
# File:         IISScriptMap.pl
# Description:  Creates script mappings in the IIS metabase.
#
# Copyright (c) 2000-2003 ActiveState Corp.  All rights reserved.
# ActiveState is a division of Sophos Plc.
#
###############################################################################
BEGIN {
    $tmp = $ENV{'TEMP'} || $ENV{'tmp'} || 
	($Config{'osname'} eq 'MSWin32' ? 'c:/temp' : '/tmp');
    open(STDERR, ">> $tmp/ActivePerlInstall.log");
}

use strict;
use Win32::OLE;

my($server_id,$virt_dir,$file_ext,$exec_path,$flags,$methods) = @ARGV;
my @dirs = split /;/, $virt_dir, -1;
push @dirs, "" unless @dirs;

# Add script mappings
foreach my $id (split /;/, $server_id) {
    foreach my $dir (@dirs) {
	my $node = "IIS://localhost/W3SVC";
	# NOTE: A serverID of "0" is treated as the W3SVC root; any supplied
	# virtual directory for this case is ignored.
	$node .= "/$id/ROOT" if $id;
	$node .= "/$dir"     if $id and length($dir);

	my $server = Win32::OLE->GetObject($node) or next;
	my @list = grep { !/^\Q$file_ext,\E/ } @{$server->{ScriptMaps}};
	$server->{ScriptMaps} = [@list, "$file_ext,$exec_path,$flags,$methods"];
	$server->SetInfo(); # save!
    }
}

my $info = Win32::OLE->GetObject("IIS://localhost/W3SVC/Info") or exit;
my $version = $info->{MajorIIsVersionNumber};
exit unless defined $version; # IIS6 and later

# Add Web Server Extension entry
my %types = (".pl"   => "Perl CGI",
	     ".plx"  => "Perl ISAPI",
	     ".plex" => "PerlEx ISAPI");
my $type = $types{$file_ext} || "Perl $file_ext";

my $server = Win32::OLE->GetObject("IIS://localhost/W3SVC") or exit;
my @list = @{$server->{WebSvcExtRestrictionList}};
$server->{WebSvcExtRestrictionList} = [@list, "0,$exec_path,1,,$type Extension"];
$server->SetInfo();
