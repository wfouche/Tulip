#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Publish tulip-runtime-jvm-1.0.0-SNAPSHOT.jar to local Maven
./gradlew -q :tulip-runtime:jvmTest
./gradlew -q :tulip-runtime:publishToMavenLocal 

find ~/.m2 -name 'tulip-runtime-jvm*.jar' -print
