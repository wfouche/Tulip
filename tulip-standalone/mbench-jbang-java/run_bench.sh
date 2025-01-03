#!/bin/bash

export JBANG_JAVA_OPTIONS="-server -Xms2048m -Xmx2048m -XX:+UseZGC -XX:+ZGenerational"

# https://www.infoq.com/news/2023/06/jbang-107/
jbang --java 21 --enable-preview App.java

echo ""
w3m -dump -cols 205 benchmark_report.html
#lynx -dump -width 205 benchmark_report.html

