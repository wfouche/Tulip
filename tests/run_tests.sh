export TULIP_VERSION="2.1.3-dev"

mkdir -p Java
cd Java
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Java 1000.0 http://localhost:7070
. ./run_bench.sh
cd ..

mkdir -p Groovy
cd Groovy
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Groovy 1000.0 http://localhost:7070

. ./run_bench.sh
cd ..

mkdir -p Kotlin
cd Kotlin
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Kotlin 1000.0 http://localhost:7070
. ./run_bench.sh
cd ..

mkdir -p Scala
cd Scala
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Scala 1000.0 http://localhost:7070
. ./run_bench.sh
cd ..

google-chrome Java/benchmark_report.html Groovy/benchmark_report.html Kotlin/benchmark_report.html Scala/benchmark_report.html
