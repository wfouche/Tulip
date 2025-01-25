set TULIP_VERSION=2.1.5-dev
set TULIP_OPTS=10000.0 http localhost:7070

if not exist "Java" (md Java)
cd Java
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% Java %TULIP_OPTS%
call run_bench.cmd
cd ..

if not exist "Groovy" (md Groovy)
cd Groovy
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% Groovy %TULIP_OPTS%
call run_bench.cmd
cd ..

if not exist "Kotlin" (md Kotlin)
cd Kotlin
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% Kotlin %TULIP_OPTS%
call run_bench.cmd
cd ..

if not exist "Scala" (md Scala)
cd Scala
call jbang io.github.wfouche.tulip:tulip-runtime:%TULIP_VERSION% Scala %TULIP_OPTS%
call run_bench.cmd
cd ..
