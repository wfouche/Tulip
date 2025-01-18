set version=2.1.3-dev

md Java
cd Java
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Java
call run_bench.cmd
cd ..

md Groovy
cd Groovy
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Groovy
call run_bench.cmd
cd ..

md Kotlin
cd Kotlin
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Kotlin
call run_bench.cmd
cd ..

md Scala
cd Scala
call jbang io.github.wfouche.tulip:tulip-runtime:%version% Scala
call run_bench.cmd
cd ..