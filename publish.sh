#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Publish tulip-runtime-jvm-2.0.0-SNAPSHOT.jar to local Maven
./gradlew :tulip-runtime:publishToMavenLocal 

find ~/.m2 -name 'tulip-runtime-jvm*.jar' -print
