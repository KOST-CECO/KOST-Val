; The name of the installer
Name "KOST-Val v1.2.5"
; Sets the icon of the installer
Icon "val.ico"
; remove the text 'Nullsoft Install System vX.XX' from the installer window 
BrandingText "Copyright © KOST/CECO"
; The file to write
OutFile "KOST-Val_en.exe"
; The default installation directory
; InstallDir $DESKTOP
InstallDir $EXEDIR
; Request application privileges for Windows Vista
RequestExecutionLevel user
; Sets the text for the titlebar of the installer
Caption "$(^Name)"
; Makes the installer controls use the new XP style when running on Windows XP
XPStyle on
;--------------------------------
!include WinMessages.nsh
!include FileFunc.nsh
!include LogicLib.nsh
!include getJavaHome.nsh
!include langKOSTVal_en.nsh
!include nsDialogs.nsh
!include "XML.nsh"

;--------------------------------
!define INIFILE       "KOSTval.ini"
!define KOSTHELP      "doc\KOST-Val_Anwendungshandbuch_*.pdf"
!define LOG           "logs"
!define CONFIG        "kostval.conf.xml"
!define CONFIGPATH    "configuration"
!define BACKUP        "~backup"
!define KOSTVAL       "kostval_en.jar"
!define JAVAPATH      "jre6"

;--------------------------------
Var DIALOG
Var KOSTFILE
Var LOGFILE
Var WORKDIR
Var JAVA
Var T_FLAG
Var P_FLAG
Var HWND

;--------------------------------
; Pages
#LicenseData license.txt
#Page license
#Page instfiles
Page Custom ShowDialog LeaveDialog

;--------------------------------
; Functions
Function .onInit
  ; looking for java home directory
  push ${JAVAPATH}
  Call getJavaHome
  pop $JAVA
  DetailPrint "java home: $JAVA"
  
  ; create configuration backup
  CreateDirectory $EXEDIR\${CONFIGPATH}\${BACKUP}
  CopyFiles /SILENT /FILESONLY $EXEDIR\${CONFIGPATH}\*.* $EXEDIR\${CONFIGPATH}\${BACKUP}

  ; Initializes the plug-ins dir ($PLUGINSDIR) if not already initialized
  InitPluginsDir
  
  ; Assign to the user variable $DIALOG, the name of a temporary file
  GetTempFileName $DIALOG $PLUGINSDIR
  
  ; Adds file(s) to be extracted to the current output path
  ;   Use /oname=X switch to change the output name
  File /oname=$DIALOG ${INIFILE}
FunctionEnd

Function .onGUIEnd
  ; reset configuration and delete backup folder
  CopyFiles /SILENT /FILESONLY $EXEDIR\${CONFIGPATH}\${BACKUP}\*.* $EXEDIR\${CONFIGPATH}
  RMDir /r  $EXEDIR\${CONFIGPATH}\${BACKUP}
FunctionEnd

Function check4Workdir
  StrCpy $WORKDIR ''
  ${xml::LoadFile} "$EXEDIR\${CONFIGPATH}\${CONFIG}" $0
  ${xml::RootElement} $0 $1
  ${xml::XPathString} "//configuration/pathtoworkdir/text()" $WORKDIR $1
  ${xml::Unload}
  GetFullPathName $1 $WORKDIR
  IfFileExists $1 fex not_fex
fex:
  StrCpy $WORKDIR ''
not_fex:
FunctionEnd

;--------------------------------
Function ShowDialog
  ; Writes entry_name=value into [section_name] of ini file
  WriteINIStr $DIALOG "Settings" "NextButtonText" "${NextButtonText}"
  
  WriteINIStr $DIALOG "${INTRO}"                 "Text"  "${INTROTXT}"
  WriteINIStr $DIALOG "${HELP_Button}"           "Text"  "${HELP_ButtonTXT}"
  WriteINIStr $DIALOG "${FORMAT_RadioButton}"    "Text"  "${FORMAT_RadioButtonTXT}"
  WriteINIStr $DIALOG "${SIP_RadioButton}"       "Text"  "${SIP_RadioButtonTXT}"
  WriteINIStr $DIALOG "${LOG_RadioButton}"       "Text"  "${LOG_RadioButtonTXT}"
  WriteINIStr $DIALOG "${INPUT_Group}"           "Text"  "${INPUT_GroupTXT}"
  WriteINIStr $DIALOG "${INPUT_FolderRequest}"   "Text"  "${INPUT_FolderRequestTXT}"
  WriteINIStr $DIALOG "${INPUT_FileRequest}"     "Text"  "${INPUT_FileRequestTXT}"
  WriteINIStr $DIALOG "${SEL_FileFolder}"        "State" "${SEL_FileFolderTXT}"
  WriteINIStr $DIALOG "${START_Validation}"      "Text"  "${START_ValidationTXT}"
  WriteINIStr $DIALOG "${EDIT_Konfiguration}"    "Text"  "${EDIT_KonfigurationTXT}"
  WriteINIStr $DIALOG "${RESET_Konfiguration}"   "Text"  "${RESET_KonfigurationTXT}"

  ; Display the validation options dialog
  InstallOptions::initDialog $DIALOG
  Pop $HWND
  
  ; set button "Cancel" active 
  #GetDlgItem $1 $HWNDPARENT 2
  #EnableWindow $1 1
  ; set button "Cancel" invisible 
  GetDlgItem $1 $HWNDPARENT 2
  ShowWindow $1 0
  ; set button "Back" invisible 
  GetDlgItem $1 $HWNDPARENT 3
  ShowWindow $1 0
  ; change button font
  GetDlgItem $1 $HWND 1209
  #CreateFont $R1 "Arial" "8" "600"
  #SendMessage $1 ${WM_SETFONT} $R1 0
  SetCtlColors $1 0x000000 0x05D62A 
 
  ; Set radio button flag
  StrCpy $T_FLAG '--format'
  StrCpy $P_FLAG ''

  ; Display the validation options dialog
  InstallOptions::show
FunctionEnd

;--------------------------------
Function LeaveDialog
  ; To get the input of the user, read the State value of a Field 
  ReadINIStr $0 $DIALOG "Settings" "State"
  
  ${Switch} "Field $0"
    
    ${Case} '${HELP_Button}'
      GetFullPathName $1 ${KOSTHELP}
      ExecShell "open" $1
      Abort
    ${Break}
    
    ${Case} '${FORMAT_RadioButton}'
      ReadINIStr $1 $DIALOG '${SIP_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
      ReadINIStr $1 $DIALOG '${INPUT_Group}' 'HWND'
      SendMessage $1 ${WM_SETTEXT} 1 'STR:${FORMAT_RadioButtonTXT}'
      StrCpy $T_FLAG '--format'
      Abort
    ${Break}
    
    ${Case} '${SIP_RadioButton}'
      ReadINIStr $1 $DIALOG '${FORMAT_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
      ReadINIStr $1 $DIALOG '${INPUT_Group}' 'HWND'
      SendMessage $1 ${WM_SETTEXT} 1 'STR:${SIP_RadioButtonTXT}'
      StrCpy $T_FLAG '--sip'
      Abort
    ${Break}
    
    ${Case} '${LOG_RadioButton}'
      ${If} $P_FLAG == ''
        StrCpy $P_FLAG '-v'
        ReadINIStr $1 $DIALOG '${LOG_RadioButton}' 'HWND'
        SendMessage $1 ${BM_SETCHECK} 1 0
      ${Else}
        StrCpy $P_FLAG ''
        ReadINIStr $1 $DIALOG '${LOG_RadioButton}' 'HWND'
        SendMessage $1 ${BM_SETCHECK} 0 0
      ${EndIf}
      Abort
    ${Break}
    
    ${Case} '${INPUT_FileRequest}'
      ${If} $T_FLAG == '--sip'
        nsDialogs::SelectFileDialog 'open' '$EXEDIR\*.zip' 'SIP Files|*.zip'
      ${Else}
        nsDialogs::SelectFileDialog 'open' '$EXEDIR\*' ''
      ${EndIf}
      Pop $R0
      ${If} $R0 == ''
        MessageBox MB_OK "${FILE_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTFILE $R0
      ${EndIf}
      Abort
    ${Break}
    
    ${Case} '${INPUT_FolderRequest}'
      nsDialogs::SelectFolderDialog "Select the folder" $EXEDIR
      Pop $R0
      ${If} $R0 == 'error'
        MessageBox MB_OK "${FOLDER_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTFILE $R0
      ${EndIf}
      Abort
    ${Break}

    ${Case} '${START_Validation}'
      ReadINIStr $KOSTFILE $DIALOG "${SEL_FileFolder}" "State"
      Call RunJar
      Abort
    ${Break}

    ${Case} '${EDIT_Konfiguration}'
      ExecWait '"notepad.exe" "$EXEDIR\${CONFIGPATH}\${CONFIG}"'
      Abort
    ${Break}

    ${Case} '${RESET_Konfiguration}'
      CopyFiles /SILENT /FILESONLY $EXEDIR\${CONFIGPATH}\${BACKUP}\*.* $EXEDIR\${CONFIGPATH}
      Abort
    ${Break}

    ${Default}
      ; Abort prevents from leaving the current page
      ; Abort
    ${Break}
  ${EndSwitch}
  
FunctionEnd

;--------------------------------
Function RunJar
  ; get logfile name
  ${GetFileName} $KOSTFILE $LOGFILE
  delete "$EXEDIR\${LOG}\$LOGFILE.kost-val.log"
  ; get workdir
  Call check4Workdir

  ; Launch java program
  ClearErrors
  ExecWait '"$JAVA\bin\java.exe" -Xmx1024m -jar ${KOSTVAL} $T_FLAG "$KOSTFILE" $P_FLAG'
  IfFileExists "$EXEDIR\${LOG}\$LOGFILE.kost-val.log" 0 prog_err
  IfErrors goto_err goto_ok
  
goto_err:
    ; validation with error
    IfFileExists "$EXEDIR\${LOG}\$LOGFILE.kost-val.log" 0 prog_err
    ${If} $T_FLAG == '--sip'
      MessageBox MB_YESNO|MB_ICONEXCLAMATION "$KOSTFILE$\n${SIP_FALSE}" IDYES showlog
      Goto rm_workdir
  ${Else}
      MessageBox MB_YESNO|MB_ICONEXCLAMATION "$KOSTFILE$\n${FORMAT_FALSE}" IDYES showlog
      Goto rm_workdir
  ${EndIf}
prog_err:
    MessageBox MB_OK|MB_ICONEXCLAMATION "${PROG_ERR} $\n$JAVA\bin\java.exe -jar ${KOSTVAL} $T_FLAG $KOSTFILE $P_FLAG"
    Goto rm_workdir
goto_ok:
  ; validation without error completed
  ${If} $T_FLAG == '--sip'
    MessageBox MB_YESNO "$KOSTFILE$\n${SIP_OK}" IDYES showlog
    Goto rm_workdir
  ${Else}
    MessageBox MB_YESNO "$KOSTFILE$\n${FORMAT_OK}" IDYES showlog
    Goto rm_workdir
  ${EndIf}
showlog:
  ; read logfile in detail view
  ExecShell "open" "$EXEDIR\${LOG}\$LOGFILE.kost-val.log"
rm_workdir:
  GetFullPathName $1 $WORKDIR
  RMDir /r $1
FunctionEnd

;--------------------------------
; Sections
Section "Install"
SectionEnd
