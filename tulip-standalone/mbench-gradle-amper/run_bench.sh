#!/bin/bash

./gradlew -q run --args="--config benchmark_config.json"

echo ""
w3m -dump -cols 205 benchmark_report.html
#lynx -dump -width 205 benchmark_report.html
