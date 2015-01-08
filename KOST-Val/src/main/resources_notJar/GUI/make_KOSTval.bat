@ECHO OFF
SETLOCAL

REM Make KOST-Val_de.exe
C:\Software\NSIS\makensis.exe KOSTval_de.nsi

REM Make KOST-Val_fr.exe
DEL KOSTval_fr.nsi
C:\Software\PCUnixUtils\sed.exe -f KOSTval_fr.script KOSTval_de.nsi > KOSTval_fr.nsi
C:\Software\NSIS\makensis.exe KOSTval_fr.nsi

REM Make KOST-Val_en.exe
DEL KOSTval_en.nsi
C:\Software\PCUnixUtils\sed.exe -f KOSTval_en.script KOSTval_de.nsi > KOSTval_en.nsi
C:\Software\NSIS\makensis.exe KOSTval_en.nsi

MOVE /Y KOST-Val_fr.exe ..\
MOVE /Y KOST-Val_en.exe ..\
MOVE /Y KOST-Val_de.exe ..\

CALL ..\KOST-Val_de.exe
