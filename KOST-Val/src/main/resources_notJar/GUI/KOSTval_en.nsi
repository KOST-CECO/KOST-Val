; The name of the installer
Name "KOST-Val v1.9.0"
; Sets the icon of the installer
Icon "val.ico"
; remove the text 'Nullsoft Install System vX.XX' from the installer window 
BrandingText "Copyright (c) KOST/CECO"
; The file to write
OutFile "KOST-Val_en.exe"
; The default installation directory
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
!include langKOSTVal_en.nsh
!include nsDialogs.nsh
!include XML.nsh
!include NTProfiles.nsh

;--------------------------------
!define INIFILE       "KOSTval.ini"
!define KOSTHELP      "doc\KOST-Val_Manual_*.pdf"
!define CONFIG        "kostval.conf.xml"
!define CONFIGPATH    ".kost-val\configuration"
!define BACKUP        "~backup"
!define WORKDIR       ".kost-val\temp_KOST-Val"
!define LOG           ".kost-val\logs"
!define JARFILE       "kostval_en.jar"
!define XTRANS        "resources\XTrans_1.8.0.4\XTrans.exe"

;--------------------------------
Var USER
Var DIALOG
Var KOSTVAL
Var LOGFILE
Var HEAPSIZE
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
  ; looking for java directory
  ReadINIStr $JAVA $DIALOG "${JAVA_Exe}" "State"
  DetailPrint "java: $JAVA"
  
  ; initial setting for validation folder/file
  StrCpy $KOSTVAL $EXEDIR
  
  System::Call "advapi32::GetUserName(t .r0, *i ${NSIS_MAX_STRLEN} r1) i.r2"
  ; MessageBox MB_OK "User name: $0 | Number of characters: $1 | Return value (OK if non-zero): $2"  
  ${ProfilePathDefaultUser} $3
  StrCpy $USER "$3$0"
  ; MessageBox MB_OK `Default User profile path:$\n"$USER"`
  
  IfFileExists "$USER\${CONFIGPATH}\*.*" +4 0
  CreateDirectory "$USER\.kost-val"
  CreateDirectory "$USER\${CONFIGPATH}"
  CopyFiles /SILENT /FILESONLY "$KOSTVAL\configuration\${CONFIG}" "$USER\${CONFIGPATH}"
  
  ; create configuration backup
  CreateDirectory $USER\${CONFIGPATH}\${BACKUP}
  CopyFiles /SILENT /FILESONLY "$USER\${CONFIGPATH}\${CONFIG}" "$USER\${CONFIGPATH}\${BACKUP}"

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
  CopyFiles /SILENT /FILESONLY "$USER\${CONFIGPATH}\${BACKUP}\${CONFIG}" "$USER\${CONFIGPATH}"
  RMDir /r  $USER\${CONFIGPATH}\${BACKUP}
FunctionEnd

Function checkHeapsize
  ${If} $HEAPSIZE == '-default'
    StrCpy $HEAPSIZE ''
  ${EndIf}
FunctionEnd

;--------------------------------
Function ShowDialog
  ; Writes entry_name=value into [section_name] of ini file
  WriteINIStr $DIALOG "Settings" "NextButtonText" "${NextButtonText}"
  
  ;WriteINIStr $DIALOG "${INTRO}"                 "Text"  "${INTROTXT}"
  WriteINIStr $DIALOG "${HELP_Button}"           "Text"  "${HELP_ButtonTXT}"
  WriteINIStr $DIALOG "${FORMAT_RadioButton}"    "Text"  "${FORMAT_RadioButtonTXT}"
  WriteINIStr $DIALOG "${SIP_RadioButton}"       "Text"  "${SIP_RadioButtonTXT}"
  WriteINIStr $DIALOG "${LOG_RadioButton}"       "Text"  "${LOG_RadioButtonTXT}"
  WriteINIStr $DIALOG "${JVM_Droplist}"          "Text"  "${JVM_DroplistTXT}"
  WriteINIStr $DIALOG "${JAVA_Exe}"              "Text"  "${JAVA_ExeTXT}"
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
  ; If file, truncate KOSTVAL to folder  name
  ${GetFileExt} $KOSTVAL $R0
  ${If} $R0 != ''
    ${GetParent} $KOSTVAL $KOSTVAL
  ${EndIf}

  ; To get the input of the user, read the State value of a Field 
  ReadINIStr $0 $DIALOG "Settings" "State"
  ReadINIStr $HEAPSIZE $DIALOG "${JVM_Value}" "State" 
  
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
        nsDialogs::SelectFileDialog 'open' '$KOSTVAL\*.zip' 'SIP Files|*.zip'
      ${Else}
        nsDialogs::SelectFileDialog 'open' '$KOSTVAL\*' ''
      ${EndIf}
      Pop $R0
      ${If} $R0 == ''
        MessageBox MB_OK "${FILE_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTVAL $R0
      ${EndIf}
      Abort
    ${Break}
    
    ${Case} '${INPUT_FolderRequest}'
      nsDialogs::SelectFolderDialog "${FOLDER_SelectTXT}" "$KOSTVAL"
      Pop $R0
      ${If} $R0 == 'error'
        MessageBox MB_OK "${FOLDER_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${SEL_FileFolder}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R0'
        StrCpy $KOSTVAL $R0
      ${EndIf}
      Abort
    ${Break}

    ${Case} '${START_Validation}'
      ReadINIStr $R0 $DIALOG "${SEL_FileFolder}" "State"
      ; Trim path or file name
      StrCpy $R1 $R0 1 -1
      StrCmp $R1 '\' 0 +2
        StrCpy $R0 $R0 -1
        GetFullPathName $KOSTVAL $R0
      ReadINIStr "$JAVA" $DIALOG "${JAVA_Exe}" "State"
      Call RunJar
      Abort
    ${Break}

    ${Case} '${EDIT_Konfiguration}'
      ClearErrors
      ExecWait '${XTRANS} "$USER\${CONFIGPATH}\${CONFIG}"'
      ${If} ${Errors}
        ExecWait '"notepad.exe" "$USER\${CONFIGPATH}\${CONFIG}"'
      ${EndIf}
      ; ExecWait '"notepad.exe" "$USER\${CONFIGPATH}\${CONFIG}"'
      ; ExecShell "open" "$USER\${CONFIGPATH}\${CONFIG}"
      Abort
    ${Break}

    ${Case} '${RESET_Konfiguration}'
      CopyFiles /SILENT /FILESONLY $USER\${CONFIGPATH}\${BACKUP}\${CONFIG} $USER\${CONFIGPATH}
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
  ; normalize java heap and stack
  Call checkHeapsize

  ; get logfile name
  ${GetFileName} $KOSTVAL $LOGFILE

  ; Launch java program
  ClearErrors
  ExecWait '"$JAVA" $HEAPSIZE -jar ${JARFILE} $T_FLAG "$KOSTVAL" $P_FLAG'
  ; MessageBox MB_OK `LOGFILE:$\n"$USER\${LOG}\$LOGFILE.kost-val.log.xml"`
  IfFileExists $USER\${LOG}\$LOGFILE.kost-val.log.xml 0 prog_err
  IfErrors goto_err goto_ok
  
goto_err:
    ; validation with error
    IfFileExists $USER\${LOG}\$LOGFILE.kost-val.log.xml 0 prog_err
    ${If} $T_FLAG == '--sip'
      MessageBox MB_YESNO|MB_ICONEXCLAMATION "$KOSTVAL$\n${SIP_FALSE}" IDYES showlog
      Goto rm_workdir
  ${Else}
      MessageBox MB_YESNO|MB_ICONEXCLAMATION "$KOSTVAL$\n${FORMAT_FALSE}" IDYES showlog
      Goto rm_workdir
  ${EndIf}
prog_err:
    MessageBox MB_OK|MB_ICONEXCLAMATION "${PROG_ERR} $\n$JAVA -jar ${JARFILE} $T_FLAG $KOSTVAL $P_FLAG"
    Goto rm_workdir
goto_ok:
  ; validation without error completed
  ${If} $T_FLAG == '--sip'
    MessageBox MB_YESNO "$KOSTVAL$\n${SIP_OK}" IDYES showlog
    Goto rm_workdir
  ${Else}
    MessageBox MB_YESNO "$KOSTVAL$\n${FORMAT_OK}" IDYES showlog
    Goto rm_workdir
  ${EndIf}
showlog:
  ; read logfile in detail view
  GetFullPathName $1 $USER\${LOG}
  IfFileExists "$USER\${LOG}\$LOGFILE.kost-val.log.xml" 0 +3
  ExecShell "" "iexplore.exe" "$1\$LOGFILE.kost-val.log.xml"
  Goto rm_workdir
  ExecShell "" "iexplore.exe" "$1\$LOGFILE.kost-val.log.xml"
  Goto rm_workdir
rm_workdir:
  GetFullPathName $1 $USER\${WORKDIR}
  RMDir /r $1
FunctionEnd

;--------------------------------
; Sections
Section "Install"
SectionEnd
