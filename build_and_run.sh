sdk env
./gradlew --stop
rm -f -r ./bin ./build
./gradlew clean
rm -f json_results.txt
./gradlew run
