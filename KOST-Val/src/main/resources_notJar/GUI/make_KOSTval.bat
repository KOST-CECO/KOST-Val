@ECHO OFF
SETLOCAL

MOVE /Y C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar\GUI C:\Tools\_TEMP_GUI

REM Make KOST-Val_de.exe
C:\Tools\NSIS\makensis.exe C:\Tools\_TEMP_GUI\GUI\KOSTval_de.nsi

REM Make KOST-Val_fr.exe
DEL C:\Tools\_TEMP_GUI\GUI\KOSTval_fr.nsi
C:\Tools\PCUnixUtils\sed.exe -f C:\Tools\_TEMP_GUI\GUI\KOSTval_fr.script C:\Tools\_TEMP_GUI\GUI\KOSTval_de.nsi > C:\Tools\_TEMP_GUI\GUI\KOSTval_fr.nsi
C:\Tools\NSIS\makensis.exe C:\Tools\_TEMP_GUI\GUI\KOSTval_fr.nsi

REM Make KOST-Val_en.exe
DEL C:\Tools\_TEMP_GUI\GUI\KOSTval_en.nsi
C:\Tools\PCUnixUtils\sed.exe -f C:\Tools\_TEMP_GUI\GUI\KOSTval_en.script C:\Tools\_TEMP_GUI\GUI\KOSTval_de.nsi > C:\Tools\_TEMP_GUI\GUI\KOSTval_en.nsi
C:\Tools\NSIS\makensis.exe C:\Tools\_TEMP_GUI\GUI\KOSTval_en.nsi

MOVE /Y C:\Tools\_TEMP_GUI\GUI C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar

MOVE /Y C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar\GUI\KOST-Val_fr.exe C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar\KOST-Val_fr.exe
MOVE /Y C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar\GUI\KOST-Val_en.exe C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar\KOST-Val_en.exe
MOVE /Y C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar\GUI\KOST-Val_de.exe C:\Users\U80809724\GIT\KOST-Val\KOST-Val\src\main\resources_notJar\KOST-Val_de.exe

PAUSE