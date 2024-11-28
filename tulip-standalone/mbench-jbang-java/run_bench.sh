#!/bin/bash

export JBANG_JAVA_OPTIONS="-server -Xms2048m -Xmx2048m -XX:+UseZGC -XX:+ZGenerational -XX:+UseDynamicNumberOfGCThreads"

# https://www.infoq.com/news/2023/06/jbang-107/
jbang --java 21 --enable-preview App.java

echo ""
lynx -dump -width 200 benchmark_report.html

