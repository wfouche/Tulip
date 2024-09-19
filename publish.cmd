call .\gradlew :tulip-runtime:jvmTest
call .\gradlew :tulip-runtime:publishToMavenLocal
dir /s %USERPROFILE%\.m2\tulip-runtime-jvm*.jar