mkdir -p Java
cd Java
jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Java
. ./run_bench.sh
cd ..

mkdir -p Groovy
cd Groovy
jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Groovy
. ./run_bench.sh
cd ..

mkdir -p Kotlin
cd Kotlin
jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Kotlin
. ./run_bench.sh
cd ..

mkdir -p Scala
cd Scala
jbang io.github.wfouche.tulip:tulip-runtime:2.1.3-dev Scala
. ./run_bench.sh
cd ..