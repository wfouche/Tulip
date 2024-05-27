sdk env
./gradlew --stop
rm -f -r ./bin ./build
./gradlew clean
./gradlew run
./gradlew --stop
