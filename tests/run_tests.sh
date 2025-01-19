export TULIP_VERSION="2.1.3"

export TULIP_OPTS="10000.0 http://localhost:7070"

mkdir -p Java
cd Java
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Java $TULIP_OPTS
. ./run_bench.sh
cd ..

mkdir -p Groovy
cd Groovy
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Groovy $TULIP_OPTS

. ./run_bench.sh
cd ..

mkdir -p Kotlin
cd Kotlin
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Kotlin $TULIP_OPTS
. ./run_bench.sh
cd ..

mkdir -p Scala
cd Scala
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Scala $TULIP_OPTS
. ./run_bench.sh
cd ..

google-chrome Java/benchmark_report.html Groovy/benchmark_report.html Kotlin/benchmark_report.html Scala/benchmark_report.html
