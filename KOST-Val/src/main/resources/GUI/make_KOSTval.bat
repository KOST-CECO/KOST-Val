@ECHO OFF
SETLOCAL

REM Make KOST-Val_de.exe
Q:\KOST\Software\NSIS\makensis.exe KOSTval_de.nsi

REM Make KOST-Val_fr.exe
DEL KOSTval_fr.nsi
Q:\KOST\Software\PCUnixUtils\sed.exe -f KOSTval_fr.script KOSTval_de.nsi > KOSTval_fr.nsi
Q:\KOST\Software\NSIS\makensis.exe KOSTval_fr.nsi

REM Make KOST-Val_en.exe
DEL KOSTval_en.nsi
Q:\KOST\Software\PCUnixUtils\sed.exe -f KOSTval_en.script KOSTval_de.nsi > KOSTval_en.nsi
Q:\KOST\Software\NSIS\makensis.exe KOSTval_en.nsi

CALL KOST-Val_de.exe
