#!/bin/bash
rm -f benchmark_report.html
export JBANG_JAVA_OPTIONS="-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational"
jbang run kwrk.kt --rate 10.0 --threads 2 --duration 30,s --repeat 4 --url https://jsonplaceholder.typicode.com/posts/1
echo ""
#w3m -dump -cols 205 benchmark_report.html
lynx -dump -width 205 benchmark_report.html
#jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
