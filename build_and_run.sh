sdk env
./gradlew -q --stop
rm -f -r ./bin ./build
./gradlew -q clean
./gradlew -q run
./gradlew -q --stop

echo ""
pushd ./reports
./report.sh
echo ""
w3m -dump -cols 200 report.html
popd
