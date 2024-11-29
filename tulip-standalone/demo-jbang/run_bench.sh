#!/bin/bash
export JBANG_JAVA_OPTIONS="-server -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
jbang run App.kt --config=benchmark_config.json
echo ""
lynx -dump -width 200 benchmark_report.html
