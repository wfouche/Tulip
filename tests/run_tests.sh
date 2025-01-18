export TULIP_VERSION="2.1.3-dev"

mkdir -p Java
cd Java
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Java
. ./run_bench.sh
cd ..

mkdir -p Groovy
cd Groovy
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Groovy
. ./run_bench.sh
cd ..

mkdir -p Kotlin
cd Kotlin
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Kotlin
. ./run_bench.sh
cd ..

mkdir -p Scala
cd Scala
jbang io.github.wfouche.tulip:tulip-runtime:$TULIP_VERSION Scala
. ./run_bench.sh
cd ..