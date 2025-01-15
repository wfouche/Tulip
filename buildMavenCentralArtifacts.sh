#!/bin/bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env

# Tulip version
export version="2.1.2"

# Remove staging folder
rm -f -r tulip-runtime/build/staging-deploy

# Publish JARs to staging folder
./gradlew publish

# Sign the artifacts
pushd ./tulip-runtime/build/staging-deploy/io/github/wfouche/tulip/tulip-runtime/$version
gpg -ab tulip-runtime-$version-javadoc.jar
gpg -ab tulip-runtime-$version-sources.jar
gpg -ab tulip-runtime-$version.jar
gpg -ab tulip-runtime-$version.module
gpg -ab tulip-runtime-$version.pom
popd

# ZIP the files to be uploaded to Maven Central
pushd ./tulip-runtime/build/staging-deploy
zip -r tulip-runtime-$version.zip io
popd


find tulip-runtime/build/staging-deploy -print | sort
