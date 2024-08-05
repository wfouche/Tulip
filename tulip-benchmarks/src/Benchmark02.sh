#!/bin/bash
export JBANG_JAVA_OPTIONS="-server -Xms2048m -Xmx2048m -XX:+UseZGC -XX:+ZGenerational -XX:+UseDynamicNumberOfGCThreads"
jbang run Benchmark02.kt --config=./user/http/config.json