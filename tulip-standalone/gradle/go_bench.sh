#!/bin/bash

pushd ../../
./publish.sh
popd

../../gradlew run --args="--config benchmark_config.json"

./report.sh
