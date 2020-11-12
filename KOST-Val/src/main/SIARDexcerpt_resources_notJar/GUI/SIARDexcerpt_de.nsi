; The name of the installer
Name "SIARDexcerpt v0.9.0"
; Sets the icon of the installer
Icon "excerpt.ico"
; remove the text 'Nullsoft Install System vX.XX' from the installer window 
BrandingText "Copyright: KOST/CECO"
; The file to write
OutFile "SIARDexcerpt_de.exe"
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
#!include getJavaHome.nsh
!include ReplaceSubStr.nsh
!include langSIARDexcerpt_de.nsh
!include nsDialogs.nsh
!include XML.nsh
!include NTProfiles.nsh

;--------------------------------
!define INIFILE       "SIARDexcerpt.ini"
!define KOSTHELP      "doc\SIARDexcerpt_Anwendungshandbuch_*.pdf"
!define CONFIG        "siardexcerpt.conf.xml"
!define CONFIGPATH    ".siardexcerpt\configuration"
#!define WORKDIR       ".siardexcerpt\temp_SIARDexcerpt"
!define LOG           ".siardexcerpt\Output"
!define JARFILE       "siardexcerpt_de.jar"
#!define JAVAPATH      "resources\jre6"

;--------------------------------
Var USER
Var DIALOG
Var CONFIG
Var SEARCHEXCERPT
Var SEARCHEXCERPTNAME
Var SIARDORIG
Var SIARDNAME
Var SIARDEXCERPT
Var LOGFILE
Var HEAPSIZE
#Var JAVA
Var T_FLAG
Var HWND
Var MAINTABLE

;--------------------------------
; Pages
#LicenseData license.txt
#Page license
Page Custom ShowDialog LeaveDialog

;--------------------------------
; Functions
Function .onInit
  ; looking for java home directory
  #push ${JAVAPATH}
  #Call getJavaHome
  #pop $JAVA
  #DetailPrint "java home: $JAVA"
  
  ; initial setting for validation folder/file
  StrCpy $SIARDEXCERPT $EXEDIR
  
  System::Call "advapi32::GetUserName(t .r0, *i ${NSIS_MAX_STRLEN} r1) i.r2"
  ; MessageBox MB_OK "User name: $0 | Number of characters: $1 | Return value (OK if non-zero): $2"  
  ${ProfilePathDefaultUser} $3
  StrCpy $USER "$3$0"
  ; MessageBox MB_OK `Default User profile path:$\n"$USER"`

  IfFileExists "$USER\${CONFIGPATH}\*.*" +4 0
  CreateDirectory "$USER\.siardexcerpt"
  CreateDirectory "$USER\${CONFIGPATH}"
  CopyFiles /SILENT /FILESONLY "$SIARDEXCERPT\configuration\${CONFIG}" "$USER\${CONFIGPATH}"

  ; Initializes the plug-ins dir ($PLUGINSDIR) if not already initialized
  InitPluginsDir
  
  ; Assign to the user variable $DIALOG, the name of a temporary file
  GetTempFileName $DIALOG $PLUGINSDIR
  
  ; Adds file(s) to be extracted to the current output path
  ;   Use /oname=X switch to change the output name
  File /oname=$DIALOG ${INIFILE}
FunctionEnd

Function .onGUIEnd
  ; MessageBox MB_OK 'java $HEAPSIZE -jar ${JARFILE}  "$SIARDORIG" "$CONFIG" --finish'
  ExecWait 'java -jar ${JARFILE} "$SIARDORIG" "$CONFIG" --finish'
  #GetFullPathName $1 $WORKDIR
  #RMDir /r $1
  delete "$USER\${CONFIGPATH}\SIARDexcerpt.conf.xml" 
FunctionEnd

#Function check4Dir
#  StrCpy $WORKDIR ''
#  StrCpy $LOG ''
#  ${xml::LoadFile} "$EXEDIR\${CONFIGPATH}\${CONFIG}" $0
#  ${xml::RootElement} $0 $1
#  ${xml::XPathString} "//configuration/pathtoworkdir/text()" #$WORKDIR $1
#  ${xml::XPathString} "//configuration/pathtooutput/text()" $LOG $1
#  ${xml::Unload}
#  GetFullPathName $1 $WORKDIR
#  IfFileExists $1 fex not_fex
#fex:
#  StrCpy $WORKDIR ''
#not_fex:
#FunctionEnd

Function checkHeapsize
  ${If} $HEAPSIZE == '-default'
    StrCpy $HEAPSIZE ''
  ${EndIf}
FunctionEnd

;--------------------------------
Function ShowDialog
  ; Writes entry_name=value into [section_name] of ini file
  WriteINIStr $DIALOG "Settings" "NextButtonText" "${NextButtonText}"
  
  WriteINIStr $DIALOG "${WILDCARD}"              	"Text"  "${WILDCARDTXT}" 
  WriteINIStr $DIALOG "${HELP_Button}"           	"Text"  "${HELP_ButtonTXT}"
  WriteINIStr $DIALOG "${1INIT_RadioButton}"    	"Text"  "${1INIT_RadioButtonTXT}"
  WriteINIStr $DIALOG "${2SEARCH_RadioButton}"     	"Text"  "${2SEARCH_RadioButtonTXT}"
  WriteINIStr $DIALOG "${3EXCERPT_RadioButton}"   	"Text"  "${3EXCERPT_RadioButtonTXT}"
  WriteINIStr $DIALOG "${JVM_Droplist}"          	"Text"  "${JVM_DroplistTXT}"
  WriteINIStr $DIALOG "${SIARD_Lable}"           	"Text"  "${SIARD_LableTXT}"
;  WriteINIStr $DIALOG "${INPUT_FolderRequest}"   	"Text"  "${INPUT_FolderRequestTXT}"
  WriteINIStr $DIALOG "${SIARD_FileRequest}"     	"Text"  "${SIARD_FileRequestTXT}"
  WriteINIStr $DIALOG "${SIARD_FileSelect}"        	"State" "${SIARD_FileSelectTXT}"
;  WriteINIStr $DIALOG "${START_Button}"      		"Text"  "${START_ButtonTXT}"
;  WriteINIStr $DIALOG "${EDIT_Konfiguration}"    	"Text"  "${EDIT_KonfigurationTXT}"
;  WriteINIStr $DIALOG "${RESET_Konfiguration}"   	"Text"  "${RESET_KonfigurationTXT}"
;  WriteINIStr $DIALOG "${MAINTABLE_Lable}"          "Text"  "${MAINTABLE_LableTXT}"
  WriteINIStr $DIALOG "${CONFIG_Lable}"           	"Text"  "${CONFIG_LableTXT}"
  WriteINIStr $DIALOG "${CONFIG_FileRequest}"     	"Text"  "${CONFIG_FileRequestTXT}"
  WriteINIStr $DIALOG "${CONFIG_FileSelect}"      	"State" "${CONFIG_FileSelectTXT}"
  WriteINIStr $DIALOG "${SEARCHEXCERPT_Lable}"    	"Text"  "${SEARCHEXCERPT_LableTXT}"
  WriteINIStr $DIALOG "${SEARCHEXCERPT_Text}"     	"State" "${SEARCHEXCERPT_TextTXT}"

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
  
  GetDlgItem $0 $HWND 1201
  ShowWindow $0 1	; Helpbutton
  GetDlgItem $0 $HWND 1202
  ShowWindow $0 1   ; 1INIT_RadioButton
  GetDlgItem $0 $HWND 1203
  ShowWindow $0 1   ; 2SEARCH_RadioButton
  GetDlgItem $0 $HWND 1204
  ShowWindow $0 1   ; 3EXCERPT_RadioButton
  GetDlgItem $0 $HWND 1205
  ShowWindow $0 1   ; SIARD-Lable
  GetDlgItem $0 $HWND 1207
  ShowWindow $0 1   ; SIARD-FileButton
  GetDlgItem $0 $HWND 1208
  ShowWindow $0 1   ; SIARD-FileSelected
  GetDlgItem $0 $HWND 1211
  ShowWindow $0 0   ; Maintable-Lable
  GetDlgItem $0 $HWND 1215
  ShowWindow $0 1   ; Config-Lable
  GetDlgItem $0 $HWND 1217
  ShowWindow $0 1   ; Config-FileButton
  GetDlgItem $0 $HWND 1218
  ShowWindow $0 1   ; Config-FileSelected
  GetDlgItem $0 $HWND 1225
  ShowWindow $0 0   ; SearchExcerpt-Lable
  GetDlgItem $0 $HWND 1228
  ShowWindow $0 0   ; SearchExcerpt text
  SendMessage $0 ${WM_SETTEXT} 1 "STR:"
  GetDlgItem $0 $HWND 1200
  ShowWindow $0 0   ; Wildcard Lable

  #CreateFont $R1 "Arial" "8" "600"
  #SendMessage $1 ${WM_SETFONT} $R1 0
  SetCtlColors $1 0x000000 0x05D62A 
 
  ; Set radio button flag
  StrCpy $T_FLAG '--init'

  ; Display the validation options dialog
  InstallOptions::show
FunctionEnd

;--------------------------------
Function LeaveDialog
  ; If file, truncate SIARDEXCERPT to folder  name
  ${GetFileExt} $SIARDEXCERPT $R0
  ${If} $R0 != ''
    ${GetParent} $SIARDEXCERPT $SIARDEXCERPT
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
    
    ${Case} '${1INIT_RadioButton}'
      ReadINIStr $1 $DIALOG '${2SEARCH_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
      ReadINIStr $1 $DIALOG '${3EXCERPT_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
;      ReadINIStr $1 $DIALOG '${1INIT_Text}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${1INIT_TextTXT}'
;      ReadINIStr $1 $DIALOG '${START_Button}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${START_TextTXT} ${1INIT_TextTXT}'
      StrCpy $T_FLAG '--init'
	  GetDlgItem $0 $HWND 1205
	  ShowWindow $0 1   ; SIARD-Lable
	  GetDlgItem $0 $HWND 1207
	  ShowWindow $0 1   ; SIARD-FileButton
	  GetDlgItem $0 $HWND 1208
	  ShowWindow $0 1   ; SIARD-FileSelected
	  EnableWindow $0 1
	  GetDlgItem $0 $HWND 1211
	  ShowWindow $0 0   ; Maintable-Lable
	  GetDlgItem $0 $HWND 1215
	  ShowWindow $0 1   ; Config-Lable
	  GetDlgItem $0 $HWND 1217
	  ShowWindow $0 1   ; Config-FileButton
	  GetDlgItem $0 $HWND 1218
	  ShowWindow $0 1   ; Config-FileSelected
	  EnableWindow $0 1
	  GetDlgItem $0 $HWND 1225
	  ShowWindow $0 0   ; SearchExcerpt-Lable
	  GetDlgItem $0 $HWND 1228
	  ShowWindow $0 0   ; SearchExcerpt text
	  SendMessage $0 ${WM_SETTEXT} 1 "STR:"
	  GetDlgItem $0 $HWND 1200
	  ShowWindow $0 0   ; Wildcard Lable
      Abort
    ${Break}
    
    ${Case} '${2SEARCH_RadioButton}'
      ReadINIStr $1 $DIALOG '${1INIT_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
      ReadINIStr $1 $DIALOG '${3EXCERPT_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
;      ReadINIStr $1 $DIALOG '${1INIT_Text}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${2SEARCH_TextTXT}'
      ReadINIStr $1 $DIALOG '${SEARCHEXCERPT_Lable}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${2SEARCH_TextTXT}:'
;      ReadINIStr $1 $DIALOG '${START_Button}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${START_TextTXT} ${2SEARCH_TextTXT}'
      StrCpy $T_FLAG '--search'
	  ShowWindow $0 1   ; SIARD-Lable
	  GetDlgItem $0 $HWND 1207
	  ShowWindow $0 0   ; SIARD-FileButton
	  GetDlgItem $0 $HWND 1208
	  ShowWindow $0 1   ; SIARD-FileSelected
	  EnableWindow $0 0
;	  GetDlgItem $0 $HWND 1211
;	  ShowWindow $0 1   ; Maintable-Lable
	  GetDlgItem $0 $HWND 1215
	  ShowWindow $0 1   ; Config-Lable
	  GetDlgItem $0 $HWND 1217
	  ShowWindow $0 0   ; Config-FileButton
	  GetDlgItem $0 $HWND 1218
	  ShowWindow $0 1   ; Config-FileSelected
	  EnableWindow $0 0
	  GetDlgItem $0 $HWND 1225
	  ShowWindow $0 1   ; SearchExcerpt-Lable
	  GetDlgItem $0 $HWND 1228
	  ShowWindow $0 1   ; SearchExcerpt text
	  SendMessage $0 ${WM_SETTEXT} 1 "${SEARCHEXCERPT_TextTXT}"
	  GetDlgItem $0 $HWND 1200
	  ShowWindow $0 1   ; Wildcard Lable
      Abort
    ${Break}
    
    ${Case} '${3EXCERPT_RadioButton}'
      ReadINIStr $1 $DIALOG '${1INIT_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
      ReadINIStr $1 $DIALOG '${2SEARCH_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
;      ReadINIStr $1 $DIALOG '${1INIT_Text}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${3EXCERPT_TextTXT}'
      ReadINIStr $1 $DIALOG '${SEARCHEXCERPT_Lable}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${3EXCERPT_TextTXT}:'
;      ReadINIStr $1 $DIALOG '${START_Button}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${START_TextTXT} ${3EXCERPT_TextTXT}'
      StrCpy $T_FLAG '--excerpt'
	  ShowWindow $0 1   ; SIARD-Lable
	  GetDlgItem $0 $HWND 1207
	  ShowWindow $0 0   ; SIARD-FileButton
	  GetDlgItem $0 $HWND 1208
	  ShowWindow $0 1   ; SIARD-FileSelected
	  EnableWindow $0 0
;	  GetDlgItem $0 $HWND 1211
;	  ShowWindow $0 1   ; Maintable-Lable
	  GetDlgItem $0 $HWND 1215
	  ShowWindow $0 1   ; Config-Lable
	  GetDlgItem $0 $HWND 1217
	  ShowWindow $0 0   ; Config-FileButton
	  GetDlgItem $0 $HWND 1218
	  ShowWindow $0 1   ; Config-FileSelected
	  EnableWindow $0 0
	  GetDlgItem $0 $HWND 1225
	  ShowWindow $0 1   ; SearchExcerpt-Lable
	  GetDlgItem $0 $HWND 1228
	  ShowWindow $0 1   ; SearchExcerpt text
	  SendMessage $0 ${WM_SETTEXT} 1 "${SEARCHEXCERPT_TextTXT}"
	  GetDlgItem $0 $HWND 1200
	  ShowWindow $0 0   ; Wildcard Lable
      Abort
    ${Break}
    
    ${Case} '${SIARD_FileRequest}'
        nsDialogs::SelectFileDialog 'open' '$SIARDEXCERPT\*.siard' '|*.*'
      Pop $R3
      ${If} $R3 == ''
        MessageBox MB_OK "${FILE_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${SIARD_FileSelect}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R3'
        StrCpy $SIARDORIG $R3
      ${EndIf}
      Abort
    ${Break}
    
    ${Case} '${CONFIG_FileRequest}'
      nsDialogs::SelectFileDialog 'open' '$SIARDEXCERPT\*.xml' '|*.*'
      Pop $R2
      ${If} $R2 == ''
        MessageBox MB_OK "${FILE_SelectTXT}"
      ${Else}
        ReadINIStr $1 $DIALOG '${CONFIG_FileSelect}' 'HWND'
        SendMessage $1 ${WM_SETTEXT} 1 'STR:$R2'
        StrCpy $CONFIG $R2
      ${EndIf}
      Abort
    ${Break}

/*  ${Case} '${START_Button}'
			ReadINIStr $R3 $DIALOG "${SIARD_FileSelect}" "State"
			StrCpy $SIARDORIG $R3
			ReadINIStr $R2 $DIALOG "${CONFIG_FileSelect}" "State"
			StrCpy $CONFIG $R2
			ReadINIStr $R4 $DIALOG "${SEARCHEXCERPT_Text}" "State"
            ${If} $T_FLAG == '--init'
			  StrCpy $R4 ''
            ${EndIf}
			StrCpy $SEARCHEXCERPT $R4
			StrCpy $SEARCHEXCERPTNAME $R4
			!insertmacro ReplaceSubStr $SEARCHEXCERPTNAME "*" "_"
			ReadINIStr $R3 $DIALOG "${SIARD_FileSelect}" "State"
			StrCpy $SIARDORIG $R3
			${GetFileName} $R3 $SIARDNAME 
      Call RunJar
      Abort
    ${Break}*/

   ${Default}
      ; Dito ${Case} '${START_Button}'
	  ; Statt Beenden ist es jetzt ausf?hren, damit Enter funktioniert
 			ReadINIStr $R3 $DIALOG "${SIARD_FileSelect}" "State"
			StrCpy $SIARDORIG $R3
			ReadINIStr $R2 $DIALOG "${CONFIG_FileSelect}" "State"
			StrCpy $CONFIG $R2
			ReadINIStr $R4 $DIALOG "${SEARCHEXCERPT_Text}" "State"
            ${If} $R4 == '*'
			  StrCpy $R4 'row'
            ${EndIf}
            ${If} $R4 == '.'
			  StrCpy $R4 'row'
            ${EndIf}
            ${If} $T_FLAG == '--init'
			  StrCpy $R4 ''
            ${EndIf}
			StrCpy $SEARCHEXCERPT $R4
			StrCpy $SEARCHEXCERPTNAME $R4
			!insertmacro ReplaceSubStr $SEARCHEXCERPTNAME "*" "_"
			ReadINIStr $R3 $DIALOG "${SIARD_FileSelect}" "State"
			StrCpy $SIARDORIG $R3
			${GetFileName} $R3 $SIARDNAME 
      Call RunJar
      Abort
   ${Break}
  ${EndSwitch}
  
FunctionEnd

;--------------------------------
Function RunJar
  ; normalize java heap and stack
  Call checkHeapsize

  ${If} $T_FLAG == '--init'
    ; Launch java program
	ClearErrors
	; MessageBox MB_OK 'java $HEAPSIZE -jar ${JARFILE}  "$SIARDORIG" "$CONFIG" $T_FLAG'
	ExecWait 'java -jar ${JARFILE} "$SIARDORIG" "$CONFIG" $T_FLAG'
	IfFileExists "$USER\${CONFIGPATH}\SIARDexcerpt.conf.xml" 0 prog_err
	IfErrors goto_err goto_ok

  ${Else}
    ; get workdir and logdir
    # Call check4Dir

	; get logfile name
	${GetFileName} $SIARDEXCERPT $LOGFILE

    ${If} $T_FLAG == '--search'
		; Launch java program
		ClearErrors
		; MessageBox MB_OK 'java $HEAPSIZE -jar ${JARFILE}  "$SIARDORIG" "$CONFIG" $T_FLAG "$SEARCHEXCERPT"' 
		ExecWait 'java $HEAPSIZE -jar ${JARFILE} "$SIARDORIG" "$CONFIG" $T_FLAG "$SEARCHEXCERPT"'
		IfFileExists "$USER\${LOG}\$SIARDNAME_$SEARCHEXCERPT_SIARDsearch.xml" 0 prog_err
		IfErrors goto_err goto_ok
    ${Else}
		; MessageBox MB_OK 'java $HEAPSIZE -jar ${JARFILE}  "$SIARDORIG" "$CONFIG" $T_FLAG "$SEARCHEXCERPT" '
		ExecWait 'java $HEAPSIZE -jar ${JARFILE} "$SIARDORIG" "$CONFIG" $T_FLAG "$SEARCHEXCERPT"'
		IfFileExists "$USER\${LOG}\$SIARDNAME_$SEARCHEXCERPT_SIARDexcerpt.xml" 0 prog_err
		IfErrors goto_err goto_ok
    ${EndIf}
  ${EndIf}
  
goto_err:
    ; ... with error
    IfFileExists "$USER\${CONFIGPATH}\SIARDexcerpt.conf.xml" 0 prog_err
    ${If} $T_FLAG == '--init'
      MessageBox MB_OK|MB_ICONEXCLAMATION "$SIARDEXCERPT$\n${INIT_FALSE}"
      Goto rm_workdir
    ${Else}
      ${If} $T_FLAG == '--search'
        MessageBox MB_OK|MB_ICONEXCLAMATION "$SIARDEXCERPT$\n${SEARCH_FALSE}"
		Abort
      ${Else}
        MessageBox MB_YESNO|MB_ICONEXCLAMATION "$SIARDEXCERPT$\n${EXCERPT_FALSE}" IDYES showlog
		Abort
      ${EndIf}
    ${EndIf}

prog_err:
;    MessageBox MB_OK|MB_ICONEXCLAMATION "${PROG_ERR} $\n$\n java -jar ${JARFILE} $SIARDORIG $CONFIG $T_FLAG $SEARCHEXCERPTNAME"
    MessageBox MB_OK|MB_ICONEXCLAMATION "${PROG_ERR} $\n java -jar ${JARFILE} $SIARDORIG $CONFIG $T_FLAG $SEARCHEXCERPTNAME $\n$$USER\{LOG}\$SIARDNAME_$SEARCHEXCERPTNAME_SIARDxxx.xml"
;    MessageBox MB_OK|MB_ICONEXCLAMATION "${PROG_ERR} $\n java -jar ${JARFILE} "$SIARDORIG" "$CONFIG" $T_FLAG"

goto_ok:
  ; ... without error completed
    Call MainTable
    ${If} $T_FLAG == '--init'
      MessageBox MB_OK "$\n${INIT_OK} $\n $MAINTABLE"
      ReadINIStr $1 $DIALOG '${1INIT_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 0 0
      ReadINIStr $1 $DIALOG '${2SEARCH_RadioButton}' 'HWND'
      SendMessage $1 ${BM_SETCHECK} 1 0
;      ReadINIStr $1 $DIALOG '${START_Button}' 'HWND'
;      SendMessage $1 ${WM_SETTEXT} 1 'STR:${START_TextTXT} ${2SEARCH_TextTXT}'
      StrCpy $T_FLAG '--search'
	  ShowWindow $0 1   ; SIARD-Lable
	  GetDlgItem $0 $HWND 1207
	  ShowWindow $0 0   ; SIARD-FileButton
	  GetDlgItem $0 $HWND 1208
	  ShowWindow $0 1   ; SIARD-FileSelected
	  EnableWindow $0 0
;	  GetDlgItem $0 $HWND 1211
;	  ShowWindow $0 1   ; Maintable-Lable
	  GetDlgItem $0 $HWND 1215
	  ShowWindow $0 1   ; Config-Lable
	  GetDlgItem $0 $HWND 1217
	  ShowWindow $0 0   ; Config-FileButton
	  GetDlgItem $0 $HWND 1218
	  ShowWindow $0 1   ; Config-FileSelected
	  EnableWindow $0 0
	  GetDlgItem $0 $HWND 1225
	  ShowWindow $0 1   ; SearchExcerpt-Lable
	  GetDlgItem $0 $HWND 1228
	  ShowWindow $0 1   ; SearchExcerpt text
	  SendMessage $0 ${WM_SETTEXT} 1 "${SEARCHEXCERPT_TextTXT}"
	  GetDlgItem $0 $HWND 1200
	  ShowWindow $0 1   ; Wildcard Lable
	  Abort
    ${Else}
      ${If} $T_FLAG == '--search'
        MessageBox MB_YESNO "$SIARDEXCERPT$\n${SEARCH_OK}" IDYES showlog
		Abort
      ${Else}
        MessageBox MB_YESNO "$SIARDEXCERPT$\n${EXCERPT_OK}" IDYES showlog
		Abort
      ${EndIf}
    ${EndIf}
  
showlog:
  ; read logfile in detail view
  GetFullPathName $1 $USER\${LOG}
    ${If} $T_FLAG == '--search'
        IfFileExists "$1\$SIARDNAME_$SEARCHEXCERPTNAME_SIARDsearch.xml" 0 prog_err
		ExecShell "" "iexplore.exe" "$1\$SIARDNAME_$SEARCHEXCERPTNAME_SIARDsearch.xml"
    ${Else}
        IfFileExists "$1\$SIARDNAME_$SEARCHEXCERPTNAME_SIARDexcerpt.xml" 0 prog_err
		ExecShell "" "iexplore.exe" "$1\$SIARDNAME_$SEARCHEXCERPTNAME_SIARDexcerpt.xml"
    ${EndIf}
	Abort
  
rm_workdir:
  ; MessageBox MB_OK 'java $HEAPSIZE -jar ${JARFILE}  "$SIARDORIG" "$CONFIG" --finish'
  ExecWait 'java -jar ${JARFILE} "$SIARDORIG" "$CONFIG" --finish'
  #GetFullPathName $1 $WORKDIR
  #RMDir /r $1
  delete "$USER\${CONFIGPATH}\SIARDexcerpt.conf.xml" 
  Abort 
FunctionEnd

;--------------------------------
Function MainTable
  StrCpy $MAINTABLE ''
  ${xml::LoadFile} "$USER\${CONFIGPATH}\${CONFIG}" $0
  ${xml::RootElement} $0 $1
  ${xml::XPathString} "//configuration/maintable/mainname/text()" $MAINTABLE $1
  ${xml::Unload}
FunctionEnd

;--------------------------------
; Sections
Section "Install"
SectionEnd
