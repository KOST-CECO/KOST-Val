@ECHO OFF
SETLOCAL

DEL KOSTval_fr.nsi

Q:\KOST\Software\PCUnixUtils\sed.exe -f KOSTval_fr.script KOSTval_de.nsi > KOSTval_fr.nsi

Q:\KOST\Software\NSIS\makensis.exe KOSTval_fr.nsi

PAUSE
