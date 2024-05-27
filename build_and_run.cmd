call init_env.cmd
.\gradlew -q --stop
rd/q/s .\bin
rd/q/s .\build
.\gradlew -q clean
.\gradlew -q run
.\gradlew -q --stop
