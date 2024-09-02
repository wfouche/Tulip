#!/bin/bash

pushd ../../
./publish.sh
popd

../../gradlew run --args="--config benchmark_config.json"

echo ""
lynx -dump -width 200 benchmark_report.html
