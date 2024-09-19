call .\gradlew :tulip-runtime:publishToMavenLocal
dir /s %USERPROFILE%\.m2\tulip-runtime*.jar
