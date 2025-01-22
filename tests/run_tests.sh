export TULIP_VERSION="2.1.4"

export TULIP_OPTS="10000.0 http://localhost:7070"

mkdir -p Java
cd Java || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Java $TULIP_OPTS
. ./run_bench.sh
cd ..

mkdir -p Groovy
cd Groovy || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Groovy $TULIP_OPTS

. ./run_bench.sh
cd ..

mkdir -p Kotlin
cd Kotlin || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Kotlin $TULIP_OPTS
. ./run_bench.sh
cd ..

mkdir -p Scala
cd Scala || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Scala $TULIP_OPTS
. ./run_bench.sh
cd ..

echo ""
google-chrome Java/benchmark_report.html Groovy/benchmark_report.html Kotlin/benchmark_report.html Scala/benchmark_report.html
