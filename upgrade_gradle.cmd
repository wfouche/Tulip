set JAVA_HOME="c:\Java\jdk-11.0.5+10"
call .\gradlew --stop
rd/q/s %USERPROFILE%\.gradle
call .\gradlew wrapper --gradle-version=5.6.4