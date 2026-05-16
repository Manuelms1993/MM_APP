@ECHO OFF
SETLOCAL

WHERE gradle >NUL 2>NUL
IF %ERRORLEVEL% EQU 0 (
  gradle %*
  EXIT /B %ERRORLEVEL%
)

IF EXIST "%USERPROFILE%\gradle\bin\gradle.bat" (
  CALL "%USERPROFILE%\gradle\bin\gradle.bat" %*
  EXIT /B %ERRORLEVEL%
)

ECHO No se encontro un binario de Gradle accesible. Configura Gradle en PATH o genera el wrapper estandar desde el IDE.
EXIT /B 1

