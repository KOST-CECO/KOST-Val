@echo off & SETLOCAL

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

ECHO Validierungsdatum: %date% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO Start der Validierung: %time% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO =========================== K O S T - V A L ===========================  >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log


REM setzen der variablen für die zusammenfassung der errorlevel
set g=0
REM g=gut=Datei Valid -> errorlevel=0
set h=0
REM h=hilfe=Datei-Aufruf-Fehler -> errorlevel=1
set i=0
REM i=invalid=Datei Invalid -> errorlevel=2

ECHO.
ECHO Aufbau KOST-Val Befehl: 
ECHO Java-Pfad  -jar  kostval.jar-Pfad  %DATEIEN%  logs\%LogOrdner% %Option%
ECHO.
ECHO ============================== S T A R T ==============================   
ECHO.
IF exist "%DATEIEN%\" (
    REM It's a directory
    REM --- FOR Schleife für die Validierung aller TIFF- & SIARD-Dateien inkl.Unterordner --- 
    FOR /R "%DATEIEN%" %%J In (*.tif *.siard *.pdf) DO (
        SET Datei=%%J
        ECHO.
        Drittapplikationen\jre6\bin\java.exe -jar KOST-Val\kostval.jar "%%J" "logs\%LogOrdner%" %Option%
        CALL :sub_ord "Datei" "LogOrdner"
        ECHO.
        ECHO --------------------
        REM mit ping -n 1 > eine Sekunde warten
        ping -n 1 127.0.0.1 > NUL
    )
) ELSE (
    REM It's a file
    REM --- Fals eine Datei eingeben wurde
    Drittapplikationen\jre6\bin\java.exe -jar KOST-Val\kostval.jar "%DATEIEN%" "logs\%LogOrdner%" %Option%
    CALL :sub_ord_d "DATEIEN" "LogOrdner"
)
ECHO.
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO ==================== Z U S A M M E N F A S S U N G ====================  >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO          Valide = %g%    Invalide = %i%   Fehler im Aufruf = %h% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO ================================ E N D ================================  >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO Validierung beendet: %time% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO ==================== Z U S A M M E N F A S S U N G ====================  
ECHO.
ECHO           Valide = %g%    Invalide = %i%   Fehler im Aufruf = %h%
ECHO.
ECHO ================================ E N D ================================  
ECHO.
PAUSE
EXIT /B 

REM muss via "CALL :sub_ord" erfolgen weil dies in FOR nicht funktioniert
:sub_ord
 ECHO return code %errorlevel%
 IF %errorlevel% == 0 (
  set /a g+=1
  ECHO Valide: %Datei% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
 ) ELSE (
  IF %errorlevel% == 2 (
   set /a i+=1
   ECHO INVALIDE: %Datei% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
  ) ELSE (
   set /a h+=1
   ECHO ERROR: %Datei% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
  )
 )
 GOTO :eof

REM muss via "CALL :sub_ord" erfolgen weil dies in FOR nicht funktioniert
:sub_ord_d
 ECHO return code %errorlevel%
 IF %errorlevel% == 0 (
  set /a g+=1
  ECHO Valide: %DATEIEN% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
 ) ELSE (
  IF %errorlevel% == 2 (
   set /a i+=1
   ECHO INVALIDE: %DATEIEN% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
  ) ELSE (
   set /a h+=1
   ECHO ERROR: %DATEIEN% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
  )
 )
 GOTO :eof
