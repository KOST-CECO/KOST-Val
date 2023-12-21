
#if defined __CYGWIN__
#include <dlfcn.h>
#elif defined _WIN32
#include <windows.h>
#else
#include <dlfcn.h>
#endif
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#if defined __CYGWIN__
typedef void * dlib;
#elif defined _WIN32

#define RTLD_LAZY 0
typedef HMODULE dlib;

dlib
dlopen(const char *filename, int flags)
{
  (void)flags;
  return LoadLibrary(filename);
}

void *
dlsym(dlib handle, const char *symbol_name)
{
  return GetProcAddress(handle, symbol_name);
}

int
dlclose(dlib handle)
{
  FreeLibrary(handle);
  return 0;
}

const char *
dlerror()
{
  return "an error";
}

#else
typedef void * dlib;
#endif

int
main(int argc, char **argv)
{
  char *filename;
  int flags;
  int (*dlmain)(int, char **);
  char **dlargv;
  dlib handle;
  int n;
  int ret;

  if(argc < 3)
  {
    fprintf(stderr, "usage: %s dlfilename dlflags [ ... ]\n", argv[0]);
    return 1;
  }

  if(!strcmp(argv[1], "verify") && !strcmp(argv[2], "self"))
  {
    printf("dlrun verify self ok\n");
    return 0;
  }

#if defined WIN32
  SetErrorMode(SetErrorMode(0) | SEM_NOGPFAULTERRORBOX);
#endif

  dlargv = malloc(sizeof(char*)*(argc-2));
  dlargv[0] = argv[0];
  filename = argv[1];
  flags = !strcmp(argv[2], "-") ? RTLD_LAZY : atoi(argv[2]);
  for(n=3; n<argc; n++)
    dlargv[n-2] = argv[n];

  handle = dlopen(filename, flags);

  if(handle == NULL)
  {
    fprintf(stderr, "error loading %s (%d|%s): %s", filename, flags, argv[2], dlerror());
    return 1;
  }

  dlmain = dlsym(handle, "dlmain");

  if(dlmain == NULL)
  {
    fprintf(stderr, "no dlmain symbol");
    return 1;
  }

  ret = dlmain(argc-2, dlargv);

  dlclose(handle);

  return ret;
}
