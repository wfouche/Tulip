#!/bin/bash

pushd ../../
./publish.sh
popd

export JBANG_JAVA_OPTIONS="-server -Xms2048m -Xmx2048m -XX:+UseZGC -XX:+ZGenerational -XX:+UseDynamicNumberOfGCThreads"

jbang run main.kt --config=benchmark_config.json

echo ""
w3m -dump -cols 200 benchmark_report.html

