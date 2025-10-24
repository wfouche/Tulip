call .\gradlew --stop
REM timeout 5
REM rd/q/s %USERPROFILE%\.gradle
timeout 5
rd/q/s .gradle
timeout 5
REM rd/q/s .idea

md %USERPROFILE%\.m2
dir /s %USERPROFILE%\.m2\tulip-runtime*.jar
rd/q/s %USERPROFILE%\.m2\repository\io\github\wfouche\tulip\tulip-runtime
