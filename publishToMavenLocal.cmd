rq/q/s %USERPROFILE%\.m2\repository\io\github\wfouche\tulip\tulip-runtime
call .\gradlew :tulip-runtime:publishToMavenLocal
dir /s %USERPROFILE%\.m2\tulip-runtime*.jar
