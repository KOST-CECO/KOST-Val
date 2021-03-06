#ifndef XLIB_VT
#define XLIB_VT
typedef struct XlibVtab
{
 unsigned (*tabSize)(void);
#define VFUNC(type,name,mem,args) type (*mem) args;
#define VVAR(type,name,mem)       type (*mem);
#include "Xlib.t"
#undef VFUNC
#undef VVAR
} XlibVtab;
extern XlibVtab *XlibVptr;
extern XlibVtab *XlibVGet(void);
extern int XRectInRegion _ANSI_ARGS_(( Region,int,int,unsigned,unsigned));
#endif /* XLIB_VT */
