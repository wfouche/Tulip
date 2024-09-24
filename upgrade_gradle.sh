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
# https://github.com/Kotlin/kotlinx.serialization - 1.7.2 requries Kotlin 2.0.20
#
GRADLE_VERSION=8.10.1

./gradlew wrapper --gradle-version $GRADLE_VERSION

pushd tulip-standalone/mbench-gradle
./gradlew wrapper --gradle-version $GRADLE_VERSION
popd

pushd tulip-standalone/oppwa-gradle
./gradlew wrapper --gradle-version $GRADLE_VERSION
popd
