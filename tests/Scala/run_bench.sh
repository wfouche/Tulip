#!/bin/bash
# jbang io.github.wfouche.tulip:tulip-runtime:2.1.4 Scala
rm -f benchmark_report.html
scala-cli App.scala HttpUser.scala
echo ""
#w3m -dump -cols 205 benchmark_report.html
lynx -dump -width 205 benchmark_report.html
#jbang run asciidoc@wfouche benchmark_config.adoc
