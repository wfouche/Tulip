./gradlew  --stop

rm -f -r .gradle
rm -f -r .idea
rm -f -r .kotlin

pushd ./tulip-runtime
source clean.sh
popd

pushd ./tulip-main
source clean.sh
popd
