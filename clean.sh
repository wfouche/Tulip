./gradlew  --stop

rm -f -r .gradle
#m -f -r .idea
rm -f -r .kotlin

pushd ./tulip-runtime || exit
source clean.sh
popd || exit

pushd ./tulip-main || exit
source clean.sh
popd || exit
