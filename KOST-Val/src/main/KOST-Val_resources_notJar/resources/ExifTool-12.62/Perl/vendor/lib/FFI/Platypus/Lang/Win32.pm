package FFI::Platypus::Lang::Win32;

use strict;
use warnings;
use 5.008004;
use Config;

# ABSTRACT: Documentation and tools for using Platypus with the Windows API
our $VERSION = '1.34'; # VERSION


sub abi
{
  $^O =~ /^(cygwin|MSWin32|msys)$/ && $Config{ptrsize} == 4
  ? 'stdcall'
  : 'default_abi';
}


my %map;

sub native_type_map
{
  unless(%map)
  {
    require FFI::Platypus::ShareConfig;
    %map = %{ FFI::Platypus::ShareConfig->get('type_map') };

    my %win32_map = qw(
      BOOL                      int
      BOOLEAN                   BYTE
      BYTE                      uchar
      CCHAR                     char
      CHAR                      char
      COLORREF                  DWORD
      DWORD                     uint
      DWORDLONG                 uint64
      DWORD_PTR                 ULONG_PTR
      DWORD32                   uint32
      DWORD64                   uint64
      FLOAT                     float
      HACCEL                    HANDLE
      HANDLE                    PVOID
      HBITMAP                   HANDLE
      HBRUSH                    HANDLE
      HCOLORSPACE               HANDLE
      HCONV                     HANDLE
      HCONVLIST                 HANDLE
      HCURSOR                   HICON
      HDC                       HANDLE
      HDDEDATA                  HANDLE
      HDESK                     HANDLE
      HDROP                     HANDLE
      HDWP                      HANDLE
      HENHMETAFILE              HANDLE
      HFILE                     int
      HFONT                     HANDLE
      HGDIOBJ                   HANDLE
      HGLOBAL                   HANDLE
      HHOOK                     HANDLE
      HICON                     HANDLE
      HINSTANCE                 HANDLE
      HKEY                      HANDLE
      HKL                       HANDLE
      HLOCAL                    HANDLE
      HMENU                     HANDLE
      HMETAFILE                 HANDLE
      HMODULE                   HINSTANCE
      HMONITOR                  HANDLE
      HPALETTE                  HANDLE
      HPEN                      HANDLE
      HRESULT                   LONG
      HRGN                      HANDLE
      HRSRC                     HANDLE
      HSZ                       HANDLE
      HWINSTA                   HANDLE
      HWND                      HANDLE
      INT                       int
      INT8                      sint8
      INT16                     sint16
      INT32                     sint32
      INT64                     sint64
      LANGID                    WORD
      LCID                      DWORD
      LCTYPE                    DWORD
      LGRPID                    DWORD
      LONG                      sint32
      LONGLONG                  sint64
      LONG32                    sint32
      LONG64                    sint64
      LPCSTR                    string
      LPCVOID                   opaque
      LPVOID                    opaque
      LRESULT                   LONG_PTR
      PSTR                      string
      PVOID                     opaque
      QWORD                     uint64
      SC_HANDLE                 HANDLE
      SC_LOCK                   LPVOID
      SERVICE_STATUS_HANDLE     HANDLE
      SHORT                     sint16
      SIZE_T                    ULONG_PTR
      SSIZE_T                   LONG_PTR
      UCHAR                     uint8
      UINT8                     uint8
      UINT16                    uint16
      UINT32                    uint32
      UINT64                    uint64
      ULONG                     uint32
      ULONGLONG                 uint64
      ULONG32                   uint32
      ULONG64                   uint64
      USHORT                    uint16
      USN                       LONGLONG
      VOID                      void
      WORD                      uint16
      WPARAM                    UINT_PTR

    );

    if($Config{ptrsize} == 4)
    {
      $win32_map{HALF_PTR}  = 'sint16';
      $win32_map{INT_PTR}   = 'sint32';
      $win32_map{LONG_PTR}  = 'sint16';
      $win32_map{UHALF_PTR} = 'uint16';
      $win32_map{UINT_PTR}  = 'uint32';
      $win32_map{ULONG_PTR} = 'uint16';
    }
    elsif($Config{ptrsize} == 8)
    {
      $win32_map{HALF_PTR}  = 'sint16';
      $win32_map{INT_PTR}   = 'sint32';
      $win32_map{LONG_PTR}  = 'sint16';
      $win32_map{UHALF_PTR} = 'uint16';
      $win32_map{UINT_PTR}  = 'uint32';
      $win32_map{ULONG_PTR} = 'uint16';
    }
    else
    {
      die "interesting word size you have";
    }

    foreach my $alias (keys %win32_map)
    {
      my $type = $alias;
      while(1)
      {
        if($type =~ /^(opaque|[us]int(8|16|32|64)|float|double|string|void)$/)
        {
          $map{$alias} = $type;
          last;
        }
        if(defined $map{$type})
        {
          $map{$alias} = $map{$type};
          last;
        }
        if(defined $win32_map{$type})
        {
          $type = $win32_map{$type};
          next;
        }
        die "unable to resolve $alias => ... => $type";
      }
    }

    # stuff we are not yet dealing with
    # LPCTSTR is unicode string, not currently supported
    # LPWSTR 16 bit unicode string
    # TBYTE TCHAR UNICODE_STRING WCHAR
    # Not supported: POINTER_32 POINTER_64 POINTER_SIGNED POINTER_UNSIGNED
  }
  \%map;
}

1;

__END__

=pod

=encoding UTF-8

=head1 NAME

FFI::Platypus::Lang::Win32 - Documentation and tools for using Platypus with the Windows API

=head1 VERSION

version 1.34

=head1 SYNOPSIS

 use FFI::Platypus;
 my $ffi = FFI::Platypus->new( api => 1 );
 $ffi->lang('Win32');

=head1 DESCRIPTION

This module provides the Windows datatypes used by the Windows API.
This means that you can use things like C<DWORD> as an alias for
C<uint32>.

=head1 METHODS

=head2 abi

 my $abi = FFI::Platypus::Lang::Win32->abi;

=head2 native_type_map

 my $hashref = FFI::Platypus::Lang::Win32->native_type_map;

This returns a hash reference containing the native aliases for the
Windows API.  That is the keys are native Windows API C types and the
values are libffi native types.

=head1 SEE ALSO

=over 4

=item L<FFI::Platypus>

The Core Platypus documentation.

=back

=head1 AUTHOR

Author: Graham Ollis E<lt>plicease@cpan.orgE<gt>

Contributors:

Bakkiaraj Murugesan (bakkiaraj)

Dylan Cali (calid)

pipcet

Zaki Mughal (zmughal)

Fitz Elliott (felliott)

Vickenty Fesunov (vyf)

Gregor Herrmann (gregoa)

Shlomi Fish (shlomif)

Damyan Ivanov

Ilya Pavlov (Ilya33)

Petr Pisar (ppisar)

Mohammad S Anwar (MANWAR)

Håkon Hægland (hakonhagland, HAKONH)

Meredith (merrilymeredith, MHOWARD)

Diab Jerius (DJERIUS)

=head1 COPYRIGHT AND LICENSE

This software is copyright (c) 2015,2016,2017,2018,2019,2020 by Graham Ollis.

This is free software; you can redistribute it and/or modify it under
the same terms as the Perl 5 programming language system itself.

=cut
