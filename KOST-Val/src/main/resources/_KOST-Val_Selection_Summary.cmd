@echo off & SETLOCAL

SET _prompt=%1

REM VBS script mit einem Echo Inputbox statement:
ECHO Wscript.Echo Inputbox("S'il vous plaît entrer un nom pour le dossier log: %_prompt%","Nom")>%TEMP%\~input.vbs

REM vbScript ausführen und output speichern
FOR /f "delims=/" %%G IN ('cscript //nologo %TEMP%\~input.vbs') DO set _string=%%G

REM VBS-Datei löschen und Input speichern
DEL %TEMP%\~input.vbs
ENDLOCAL & SET _input=%_string%

REM Wenn Abbrechen gewählt wird Abgebrochen und ansonsten weitergefahren
IF "%_input%" == "" (
	echo Rompre...
	PAUSE
	EXIT /B
) 

SET LogOrdner=%_input%

SET _prompt=%1

REM VBS script mit einem Echo Inputbox statement:
ECHO Wscript.Echo Inputbox("S'il vous plaît entrer le lien vers le dossier contenant les fichiers à valider ou le lien vers un fichier:%_prompt%","Lien", "C:\TEMP\TIFF")>%TEMP%\~input.vbs

REM vbScript ausführen und output speichern
FOR /f "delims=/" %%G IN ('cscript //nologo %TEMP%\~input.vbs') DO set _string=%%G

REM VBS-Datei löschen und Input speichern
DEL %TEMP%\~input.vbs
ENDLOCAL & SET _input=%_string%

REM Wenn Abbrechen gewählt wird Abgebrochen und ansonsten weitergefahren
IF "%_input%" == "" (
	echo Rompre...
	PAUSE
	EXIT /B
) 

SET DATEIEN=%_input%

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
MKDIR logs\%LogOrdner%

ECHO Date de validation: %date% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO Début de validation: %time% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO =========================== T I F F - V A L ===========================  >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log


REM setzen der variablen für die zusammenfassung der errorlevel
set g=0
REM g=gut=Datei Valid -> errorlevel=0
set h=0
REM h=hilfe=Datei-Aufruf-Fehler -> errorlevel=1
set i=0
REM i=invalid=Datei Invalid -> errorlevel=2

ECHO.
ECHO Commande pour KOST-Val: 
ECHO Java-lien   -jar   kostval.jar-lien  %DATEIEN%  logs\%LogOrdner% %Option%
ECHO.
ECHO ========================== D E M A R R A G E ==========================  
ECHO.
IF exist "%DATEIEN%\" (
    REM It's a directory
    REM --- FOR Schleife für die Validierung aller TIFF- & SIARD-Dateien inkl.Unterordner --- 
    FOR /R "%DATEIEN%" %%J In (*.tif *.siard *.pdf) DO (
        SET Datei=%%J
        ECHO.
        java -jar KOST-Val\kostval.jar --format "logs\%LogOrdner%" "%%J" %Option%
        CALL :sub_ord "Datei" "LogOrdner"
        ECHO.
        ECHO --------------------
        REM mit ping -n 1 > eine Sekunde warten
        ping -n 1 127.0.0.1 > NUL
    )
) ELSE (
    REM It's a file
    REM --- Fals eine Datei eingeben wurde
    java -jar KOST-Val\kostval.jar --format "logs\%LogOrdner%" "%DATEIEN%" %Option%
    CALL :sub_ord_d "DATEIEN" "LogOrdner"
)
ECHO.
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO ============================  R E S U M E =============================  >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO       Valide = %g%   Invalide = %i%   Erreur dans le demarrage = %h% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO ================================ F I N ================================  >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO Validation terminée: %time% >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO. >> logs\"%LogOrdner%"\_KOST-Val-Summary.log
ECHO ============================  R E S U M E =============================  
ECHO.
ECHO       Valide = %g%   Invalide = %i%   Erreur dans le demarrage = %h%
ECHO.
ECHO ================================ F I N ================================  
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
