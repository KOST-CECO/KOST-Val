@echo off & SETLOCAL

REM Abfrage Log-Ordner
SET _prompt=%1
REM VBS script mit einem Echo Inputbox statement:
ECHO Wscript.Echo Inputbox("Bitte geben Sie den Namen für den Log-Ordner ein: %_prompt%","Name")>%TEMP%\~input.vbs

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
ECHO Wscript.Echo Inputbox("Bitte geben Sie den Pfad zum Ordner mit den zu validierenden Dateien oder die einzelne Datei an:%_prompt%","Pfad", "C:\TEMP\TIFF")>%TEMP%\~input.vbs
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

REM Abfrage Verbose oder nicht
REM VBS script mit einem Echo Msgbox statement:
set M=%temp%\MsgBox.vbs 
>%M% echo WScript.Quit MsgBox("Wollen Sie die Originalreports erhalten?",vbYesNo + vbDefaultButton2,"Verbose?") 
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
ECHO Aufbau KOST-Val Befehl: 
ECHO Java-Pfad  -jar  kostval.jar-Pfad  %DATEIEN%  logs\%LogOrdner% %Option%
ECHO.
ECHO ============================== S T A R T ==============================   
ECHO.
    REM Datei oder Ordner
    java -jar KOST-Val\kostval.jar --format "logs\%LogOrdner%" "%DATEIEN%" %Option%
ECHO ================================ E N D ================================   
ECHO.
PAUSE
EXIT /B 
