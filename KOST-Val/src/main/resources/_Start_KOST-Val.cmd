@echo off & SETLOCAL

REM Abfrage Validierungsdatei / -ordner
SET _prompt=%1
REM VBS script mit einem Echo Inputbox statement:
ECHO Wscript.Echo Inputbox("Bitte geben Sie den Pfad zum Ordner mit den zu validierenden Dateien oder die einzelne Datei an:%_prompt%","Pfad", "C:\TEMP\2validate")>%TEMP%\~input.vbs
REM vbScript ausführen und output speichern
FOR /f "delims=/" %%G IN ('cscript //nologo %TEMP%\~input.vbs') DO set _string=%%G

REM VBS-Datei löschen und Input speichern
DEL %TEMP%\~input.vbs
ENDLOCAL & SET _input=%_string%

REM Wenn Abbrechen gewählt wird Abgebrochen und ansonsten weitergefahren
IF "%_input%" == "" (
	echo Abbrechen...
	PAUSE
	EXIT /B
) 

SET DATEIEN=%_input%

REM Abfrage Formatvalidierung oder SIP-Validierung
REM VBS script mit einem Echo Msgbox statement:
set M=%temp%\MsgBox.vbs 
>%M% echo WScript.Quit MsgBox("Wollen Sie nur eine Formatvalidierung durchführen?" ^& vbCrLf ^& " [Ja]      Formatvalidierung" ^& vbCrLf ^& " [Nein] SIP-Validierung inkl. Formatvalidierung",vbYesNo + vbDefaultButton1,"Format- / SIP-Validierung?") 
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
>%M% echo WScript.Quit MsgBox("Wollen Sie die Originalreports erhalten?" ^& vbCrLf ^& " [Ja]      Behält zusätzlich die Reports von PDFTron & Co." ^& vbCrLf ^& " [Nein] Behält einzig den KOST-Val Report",vbYesNo + vbDefaultButton2,"Verbose?") 
%M% 

if %errorlevel%==6 ( 
   REM Verbose-Mode gewählt 
   SET Option=-v
)

@echo off
REM Nach den Abfragen kommt die eigentliche Ausführung...

ECHO.
    REM Datei oder Ordner
    java -Xmx512m -jar kostval_de.jar %Typ% "%DATEIEN%" %Option%
ECHO ================================ E N D ================================   
ECHO.
PAUSE
EXIT /B 
