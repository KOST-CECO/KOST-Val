/* BuildInfo.h
 *
 * Copyright (c) 1998-2004 ActiveState Corp.  All rights reserved.
 *
 */

#ifndef ___BuildInfo__h___
#define ___BuildInfo__h___

#define PRODUCT_BUILD_NUMBER	"810"
#define PERLFILEVERSION		"5,8,4,810\0"
#define PERLRC_VERSION		5,8,4,810
#define ACTIVEPERL_CHANGELIST   ""
#define PERLPRODUCTVERSION	"Build " PRODUCT_BUILD_NUMBER ACTIVEPERL_CHANGELIST "\0"
#define PERLPRODUCTNAME		"ActivePerl\0"

#define PERL_VENDORLIB_NAME	"ActiveState"

#define ACTIVEPERL_VERSION	"Built " __DATE__ " " __TIME__
#define ACTIVEPERL_LOCAL_PATCHES_ENTRY	"ActivePerl Build " PRODUCT_BUILD_NUMBER ACTIVEPERL_CHANGELIST
#define BINARY_BUILD_NOTICE	PerlIO_printf(PerlIO_stdout(), "\n\
Binary build " PRODUCT_BUILD_NUMBER ACTIVEPERL_CHANGELIST " provided by ActiveState Corp. http://www.ActiveState.com\n\
ActiveState is a division of Sophos.\n\
" ACTIVEPERL_VERSION "\n");

#endif  /* ___BuildInfo__h___ */
