set TULIP_VERSION=2.1.5
set TULIP_PARAMS=10000.0 http localhost:7070

set TULIP_JAVA_OPTIONS=-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational

if not exist "Java" (md Java)
cd Java
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Java %TULIP_PARAMS%
call run_bench.cmd
cd ..

if not exist "Groovy" (md Groovy)
cd Groovy
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Groovy %TULIP_PARAMS%
call run_bench.cmd
cd ..

if not exist "Kotlin" (md Kotlin)
cd Kotlin
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Kotlin %TULIP_PARAMS%
call run_bench.cmd
cd ..

if not exist "Scala" (md Scala)
cd Scala
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Scala %TULIP_PARAMS%
call run_bench.cmd
cd ..
