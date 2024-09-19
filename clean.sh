./gradlew  --stop

rm -f -r .gradle
rm -f -r .idea
rm -f -r .kotlin

pushd ./tulip-runtime
source clean.sh
popd

pushd ./tulip-app
source clean.sh
popd

pushd ./tulip-standalone
source clean.sh
popd
