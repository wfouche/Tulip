# ./gradlew wrapper --gradle-version latest
#
# https://github.com/JetBrains/amper/blob/release/0.4/docs/Usage.md
#
# https://github.com/JetBrains/amper/blob/release/0.4/syncVersions.sh
#
# https://github.com/JetBrains/amper/releases
#
# https://gradle.org/releases/
#
GRADLE_VERSION=8.6

./gradlew wrapper --gradle-version $GRADLE_VERSION

pushd tulip-standalone/gradle
./gradlew wrapper --gradle-version $GRADLE_VERSION
popd

pushd tulip-standalone/oppwa
./gradlew wrapper --gradle-version $GRADLE_VERSION
popd
