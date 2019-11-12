set JAVA_HOME="c:\Java\jdk-11.0.5+10"

call .\gradlew --stop

timeout 5
rd/q/s %USERPROFILE%\.gradle
timeout 5
rd/q/s .gradle
timeout 5
rd/q/s .idea

call .\gradlew wrapper --gradle-version=5.6.4