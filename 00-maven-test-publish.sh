#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Tulip version
export tver="2.0.0"

# Remove staging folder
rm -f -r tulip-runtime/build/staging-deploy

# Publish JARs to staging folder
./gradlew publish

# Sign the artifacts
pushd ./tulip-runtime/build/staging-deploy/io/github/wfouche/tulip/tulip-runtime/$tver
gpg -ab tulip-runtime-$tver-javadoc.jar
gpg -ab tulip-runtime-$tver-sources.jar
gpg -ab tulip-runtime-$tver.jar
gpg -ab tulip-runtime-$tver.module
gpg -ab tulip-runtime-$tver.pom
popd

# ZIP the files to be uploaded to Maven Central
pushd ./tulip-runtime/build/staging-deploy
zip -r tulip-runtime-$tver.zip io
popd


find tulip-runtime/build/staging-deploy -print
