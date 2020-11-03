call %KOTLIN_HOME%\bin\kotlinc timing.kt -include-runtime -d timing.jar

call %JAVA_HOME%\bin\java.exe -jar timing.jar