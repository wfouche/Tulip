#!/bin/bash

export JBANG_JAVA_OPTIONS="-server -Xms2048m -Xmx2048m -XX:+UseZGC -XX:+ZGenerational -XX:+UseDynamicNumberOfGCThreads"

jbang run App.kt --config=benchmark_config.json

echo ""
lynx -dump -width 200 benchmark_report.html

