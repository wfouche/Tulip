dir /s %USERPROFILE%\.m2\tulip-runtime*.jar
rd/q/s %USERPROFILE%\.m2\repository\io\github\wfouche\tulip\tulip-runtime
pause
call .\gradlew :tulip-runtime:publishToMavenLocal
dir /s %USERPROFILE%\.m2\tulip-runtime*.jar
REM echo.
REM echo "jbang io.github.wfouche.tulip:tulip-runtime:<version>"
