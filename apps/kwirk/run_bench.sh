#!/bin/bash
rm -f benchmark_report.html
export JBANG_JAVA_OPTIONS="-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational"
#jbang run kwrik.kt --rate 4.0 --threads 2 --duration 20 --repeat 4 --url https://jsonplaceholder.typicode.com/users/1
jbang run kwrik.kt --rate 10000.0 --threads 2 --duration 20 --repeat 4 --url http://localhost:7070
echo ""
#w3m -dump -cols 205 benchmark_report.html
lynx -dump -width 205 benchmark_report.html
#jbang run https://gist.github.com/wfouche/70738de122128bbc19ea888799151699 benchmark_config.adoc
