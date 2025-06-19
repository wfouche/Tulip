#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Clean
mkdir -p ~/.m2
find ~/.m2 -name 'tulip-runtime*.jar' -print | sort
rm -f -r ~/.m2/repository/io/github/wfouche/tulip/tulip-runtime

# reports
pushd ./reports
./update_runtime.sh
popd

# spotless
./gradlew spotlessApply

echo ""
read -p "Press ENTER to continue ..."

# Publish tulip-runtime.jar to local Maven
./gradlew clean
./gradlew :tulip-runtime:publishToMavenLocal

find ~/.m2 -name 'tulip-runtime*.jar' -print | sort
