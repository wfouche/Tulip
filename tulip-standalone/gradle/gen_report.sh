#!/bin/bash

./gradlew -q run --args="--result benchmark_output.json"

echo ""
#lynx -dump -width 200 benchmark_report.html
w3m -dump -cols 200 benchmark_report.html
