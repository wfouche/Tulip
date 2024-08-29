#!/bin/bash

pushd ../../
./publish.sh
popd

../../gradlew run --args="--config benchmark_config.json"

echo ""
w3m -dump -cols 200 benchmark_report.html
