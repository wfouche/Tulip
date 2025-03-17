#!/bin/bash

# Create a 2 GB regions of Large Pages
#
# $ ls /sys/kernel/mm/hugepages/
#   hugepages-1048576kB  hugepages-2048kB
#
# # (As root) Reserve 2 GB of RAM
# $ echo 1024 > /sys/kernel/mm/hugepages/hugepages-2048kB/nr_hugepages
#
# "-XX:+UseLargePages",
jbang --fresh run tulip-cli@wfouche

export TULIP_PARAMS="10000.0 http localhost:7070 2.1.7-dev"
export TULIP_JAVA_OPTIONS="-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational"

#unset TULIP_JAVA_OPTIONS
#export TULIP_JAVA_OPTIONS="-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational -XX:+UseLargePages"

rm -f -r Java
mkdir -p Java
cd Java || exit
echo ""
jbang run tulip-cli@wfouche init Java $TULIP_PARAMS
. ./run_bench.sh
cd ..

rm -f -r Groovy
mkdir -p Groovy
cd Groovy || exit
echo ""
jbang run tulip-cli@wfouche init Groovy $TULIP_PARAMS

. ./run_bench.sh
cd ..

rm -f -r Kotlin
mkdir -p Kotlin
cd Kotlin || exit
echo ""
jbang run tulip-cli@wfouche init Kotlin $TULIP_PARAMS
. ./run_bench.sh
cd ..

rm -f -r Scala
mkdir -p Scala
cd Scala || exit
echo ""
jbang run tulip-cli@wfouche init Scala $TULIP_PARAMS
. ./run_bench.sh
cd ..

rm -f -r Jython
mkdir -p Jython
cd Jython || exit
echo ""
jbang run tulip-cli@wfouche init Jython $TULIP_PARAMS
. ./run_bench.sh
cd ..

echo ""
firefox Java/benchmark_report.html Groovy/benchmark_report.html Kotlin/benchmark_report.html Scala/benchmark_report.html Jython/benchmark_report.html
