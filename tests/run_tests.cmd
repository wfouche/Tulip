set version=2.1.4

if not exist "Java" (md Java)
cd Java
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Java
call run_bench.cmd
cd ..

if not exist "Groovy" (md Groovy)
cd Groovy
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Groovy
call run_bench.cmd
cd ..

if not exist "Kotlin" (md Kotlin)
cd Kotlin
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Kotlin
call run_bench.cmd
cd ..

if not exist "Scala" (md Scala)
cd Scala
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Scala
call run_bench.cmd
cd ..
