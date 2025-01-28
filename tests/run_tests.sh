export TULIP_VERSION="2.1.5-dev"
export TULIP_OPTS="10000.0 http localhost:7070"

rm -f -r Java
mkdir -p Java
cd Java || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Java $TULIP_OPTS
. ./run_bench.sh
cd ..

rm -f -r Groovy
mkdir -p Groovy
cd Groovy || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Groovy $TULIP_OPTS

. ./run_bench.sh
cd ..

rm -f -r Kotlin
mkdir -p Kotlin
cd Kotlin || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Kotlin $TULIP_OPTS
. ./run_bench.sh
cd ..

rm -f -r Scala
mkdir -p Scala
cd Scala || exit
echo ""
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Scala $TULIP_OPTS
. ./run_bench.sh
cd ..

echo ""
firefox Java/benchmark_report.html Groovy/benchmark_report.html Kotlin/benchmark_report.html Scala/benchmark_report.html
