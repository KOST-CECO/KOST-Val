@ECHO OFF
SETLOCAL

REM Make SIARDexcerpt_de.exe
C:\Tools\NSIS\makensis.exe SIARDexcerpt_de.nsi

REM Make SIARDexcerpt_fr.exe
DEL SIARDexcerpt_fr.nsi
C:\Tools\PCUnixUtils\sed.exe -f SIARDexcerpt_fr.script SIARDexcerpt_de.nsi > SIARDexcerpt_fr.nsi
C:\Tools\NSIS\makensis.exe SIARDexcerpt_fr.nsi

REM Make SIARDexcerpt_en.exe
DEL SIARDexcerpt_en.nsi
C:\Tools\PCUnixUtils\sed.exe -f SIARDexcerpt_en.script SIARDexcerpt_de.nsi > SIARDexcerpt_en.nsi
C:\Tools\NSIS\makensis.exe SIARDexcerpt_en.nsi

MOVE /Y SIARDexcerpt_fr.exe ..\SIARDexcerpt_fr.exe
MOVE /Y SIARDexcerpt_en.exe ..\SIARDexcerpt_en.exe
MOVE /Y SIARDexcerpt_de.exe ..\SIARDexcerpt_de.exe

PAUSE