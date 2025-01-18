md Java
cd Java
call jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Java
call run_bench.cmd
cd ..

md Groovy
cd Groovy
call jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Groovy
call run_bench.cmd
cd ..

md Kotlin
cd Kotlin
call jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Kotlin
call run_bench.cmd
cd ..

md Scala
cd Scala
call jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Scala
call run_bench.cmd
cd ..