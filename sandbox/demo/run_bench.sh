#!/bin/bash
rm -f benchmark_report.html
export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
jbang run App.java
echo ""
w3m -dump -cols 205 benchmark_report.html
#lynx -dump -width 205 benchmark_report.html
#jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
