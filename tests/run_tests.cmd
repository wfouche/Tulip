set TULIP_PARAMS=10000.0 http localhost:7070 2.1.7-dev
set TULIP_JAVA_OPTIONS=-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational

call jbang --fresh run tulip-cli@wfouche

if not exist "Java" (md Java)
cd Java
call jbang run tulip-cli@wfouche init Java %TULIP_PARAMS%
call run_bench.cmd
cd ..

if not exist "Groovy" (md Groovy)
cd Groovy
call jbang run tulip-cli@wfouche init Groovy %TULIP_PARAMS%
call run_bench.cmd
cd ..

if not exist "Kotlin" (md Kotlin)
cd Kotlin
call jbang run tulip-cli@wfouche init Kotlin %TULIP_PARAMS%
call run_bench.cmd
cd ..

if not exist "Scala" (md Scala)
cd Scala
call jbang run tulip-cli@wfouche init Scala %TULIP_PARAMS%
call run_bench.cmd
cd ..

if not exist "Jython" (md Jython)
cd Jython
call jbang run tulip-cli@wfouche init Jython %TULIP_PARAMS%
call run_bench.cmd
cd ..
