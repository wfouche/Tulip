#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Publish tulip-runtime-1.0.0-SNAPSHOT.jar to local Maven
./gradlew :tulip-runtime:publishToMavenLocal

find ~/.m2 -name 'tulip-runtime*.jar' -print
