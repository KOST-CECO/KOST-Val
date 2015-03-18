Function GetJavaHome
    ; This function returns the Java Home Directory
    ; A predicted path can be put on the stack
    ; If no JVM is found, aborts with an error Message
    ; Usage example:
    ; call GetJavaHome
    ; pop (home directory)
    
  ; get the predicted path from the stack
  pop $1
  IfFileExists "$1\bin\java.exe" goto_return 0
   
  ; read the appropriate value from the registry into the Stack
  readRegStr $0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" CurrentVersion
  readRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$0" JavaHome
  IfFileExists "$1\bin\java.exe" goto_return 0

  ; look in JAVA_HOME environment variable
  ReadEnvStr $1 "JAVA_HOME"
  IfFileExists "$1\bin\java.exe" goto_return 0

  ; look in JAVA_HOME environment variable
  ReadEnvStr $1 "JAVA_HOME"
  IfFileExists "$1\bin\java.exe" goto_return 0
  
  ; look in PATH environment variable or current Path

goto_return:
  IfFileExists "$1\bin\java.exe" 0 goto_exit
  push $1
  Return

goto_exit:
  MessageBox MB_OK \
  'No Java Virtual Machine was found $\nPlease install Java Runtime Environment (JRE) \
  at least version 1.6.x   http://www.java.com/getjava/'
  Quit
FunctionEnd
