@ECHO OFF
SETLOCAL

REM Make KOST-Val_de.exe
C:\Tools\NSIS\makensis.exe KOSTval_de.nsi

REM Make KOST-Val_fr.exe
DEL KOSTval_fr.nsi
C:\Tools\PCUnixUtils\sed.exe -f KOSTval_fr.script KOSTval_de.nsi > KOSTval_fr.nsi
C:\Tools\NSIS\makensis.exe KOSTval_fr.nsi

REM Make KOST-Val_en.exe
DEL KOSTval_en.nsi
C:\Tools\PCUnixUtils\sed.exe -f KOSTval_en.script KOSTval_de.nsi > KOSTval_en.nsi
C:\Tools\NSIS\makensis.exe KOSTval_en.nsi

MOVE /Y KOST-Val_fr.exe ..\KOST-Val_fr.exe
MOVE /Y KOST-Val_en.exe ..\KOST-Val_en.exe
MOVE /Y KOST-Val_de.exe ..\KOST-Val_de.exe

PAUSE