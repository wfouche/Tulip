#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

./gradlew -q --stop
rm -f -r ./bin ./build

./gradlew -q clean

param1="$1"
if [[ "$param1" == "" ]]; then
   ./gradlew -q :tulip-app:run
else
   ./gradlew -q :tulip-app:run "$param1"
fi

./gradlew -q --stop

./text_report.sh
