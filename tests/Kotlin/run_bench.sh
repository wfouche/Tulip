#!/bin/bash
# jbang io.github.wfouche.tulip:tulip-runtime:2.1.4 Kotlin
rm -f benchmark_report.html
export JBANG_JAVA_OPTIONS="-server -Xms2g -Xmx2g -XX:+UseZGC -XX:+ZGenerational"
jbang run App.kt
echo ""
#w3m -dump -cols 205 benchmark_report.html
lynx -dump -width 205 benchmark_report.html
#jbang run asciidoc@wfouche benchmark_config.adoc
