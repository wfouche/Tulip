#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

./gradlew publish

find tulip-runtime/build/staging-deploy -print
