@echo off & SETLOCAL

REM Abfrage Log-Ordner
SET _prompt=%1
REM VBS script mit einem Echo Inputbox statement:
ECHO Wscript.Echo Inputbox("S'il vous plaît entrer un nom pour le dossier log: %_prompt%","Name")>%TEMP%\~input.vbs

REM vbScript ausführen und output speichern
FOR /f "delims=/" %%G IN ('cscript //nologo %TEMP%\~input.vbs') DO set _string=%%G

REM VBS-Datei löschen und Input speichern
DEL %TEMP%\~input.vbs
ENDLOCAL & SET _input=%_string%

REM Wenn Abbrechen gewählt wird Abgebrochen und ansonsten weitergefahren
IF "%_input%" == "" (
	echo Abbbrechen...
	PAUSE
	EXIT /B
) 

SET LogOrdner=%_input%

REM Abfrage Validierungsdatei / -ordner
SET _prompt=%1
REM VBS script mit einem Echo Inputbox statement:
ECHO Wscript.Echo Inputbox("S'il vous plaît entrer le lien vers le dossier contenant les fichiers à valider ou le lien vers un fichier: %_prompt%","Pfad", "C:\TEMP\TIFF")>%TEMP%\~input.vbs
REM vbScript ausführen und output speichern
FOR /f "delims=/" %%G IN ('cscript //nologo %TEMP%\~input.vbs') DO set _string=%%G

REM VBS-Datei löschen und Input speichern
DEL %TEMP%\~input.vbs
ENDLOCAL & SET _input=%_string%

REM Wenn Abbrechen gewählt wird Abgebrochen und ansonsten weitergefahren
IF "%_input%" == "" (
	echo Abbbrechen...
	PAUSE
	EXIT /B
) 

SET DATEIEN=%_input%

REM Abfrage Formatvalidierung oder SIP-Validierung
REM VBS script mit einem Echo Msgbox statement:
set M=%temp%\MsgBox.vbs 
>%M% echo WScript.Quit MsgBox("Voulez-vous effectuer seulement une validation des formats?",vbYesNo + vbDefaultButton1,"Format- / SIP-Validierung?") 
%M% 

if %errorlevel%==6 ( 
   REM Format-Mode gewählt 
   SET Typ=--format
) else (
   REM SIP-Mode gewählt 
   SET Typ=--sip
)

REM Abfrage Verbose oder nicht
REM VBS script mit einem Echo Msgbox statement:
set M=%temp%\MsgBox.vbs 
>%M% echo WScript.Quit MsgBox("Voulez-vous recevoir les rapports originaux?",vbYesNo + vbDefaultButton2,"Verbose?") 
%M% 

if %errorlevel%==6 ( 
   REM Verbose-Mode gewählt 
   SET Option=-v
)

@echo off
REM Ordner "logs" anlegen
MKDIR logs\"%LogOrdner%"

REM Nach den Abfragen kommt die eigentliche Ausführung...

ECHO.
ECHO Commande pour KOST-Val: 
ECHO java  -jar  KOST-Val\kostval.jar  %Typ%  logs\%LogOrdner%  %DATEIEN%  %Option%
ECHO.
ECHO ========================== D E M A R R A G E ==========================  
ECHO.
    REM Datei oder Ordner
    java -jar KOST-Val\kostval.jar %Typ% "logs\%LogOrdner%" "%DATEIEN%" %Option%
ECHO ================================ F I N ================================  
ECHO.
PAUSE
EXIT /B 
