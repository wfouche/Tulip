#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
./gradlew -q --stop
rm -f -r ./bin ./build
./gradlew -q clean
./gradlew -q run
./gradlew -q --stop

./text_report.sh
