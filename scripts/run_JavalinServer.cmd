REM Shell 1 - run JavalinServer
REM
set JBANG_JAVA_OPTIONS=
set JBANG_APP_JAVA_OPTIONS=
jbang run JavalinServer.java
REM Shell 2 - benchmark JavalinServer with Apache Bench
REM ab -c 4 -t 5 http://localhost:7070/hello

REM Shell 3 - benchmark JavalinServer with Tulip
