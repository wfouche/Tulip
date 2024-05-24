sdk env
./gradlew --stop
rm -f -r ./bin ./build
./gradlew clean
rm -f tulip_results.json
./gradlew run
./gradlew --stop
