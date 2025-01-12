#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Clean
find ~/.m2 -name 'tulip-runtime*.jar' -print | sort
rm -f -r ~/.m2/repository/io/github/wfouche/tulip/tulip-runtime

echo ""
read -p "Press ENTER to continue ..."

# Publish tulip-runtime.jar to local Maven
./gradlew :tulip-runtime:publishToMavenLocal

find ~/.m2 -name 'tulip-runtime*.jar' -print | sort

echo ""
echo "jbang io.github.wfouche.tulip:tulip-runtime:<version>"

