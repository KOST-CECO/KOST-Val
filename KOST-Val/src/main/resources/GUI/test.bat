@ECHO OFF
SETLOCAL

java -jar kostval_de.jar --sip ..\Testdaten_SIP\SIP_20110310_KOST_1a-3d-IO.zip
ECHO %errorlevel%

PAUSE
