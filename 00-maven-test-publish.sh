#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

rm -f -r tulip-runtime/build/staging-deploy

./gradlew publish

find tulip-runtime/build/staging-deploy -print
