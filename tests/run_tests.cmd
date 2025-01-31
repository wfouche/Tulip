set TULIP_VERSION=2.1.5
set TULIP_OPTS=10000.0 http localhost:7070

if not exist "Java" (md Java)
cd Java
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Java %TULIP_OPTS%
call run_bench.cmd
cd ..

if not exist "Groovy" (md Groovy)
cd Groovy
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Groovy %TULIP_OPTS%
call run_bench.cmd
cd ..

if not exist "Kotlin" (md Kotlin)
cd Kotlin
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Kotlin %TULIP_OPTS%
call run_bench.cmd
cd ..

if not exist "Scala" (md Scala)
cd Scala
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% init Scala %TULIP_OPTS%
call run_bench.cmd
cd ..
