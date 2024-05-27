call init_env.cmd
call .\gradlew -q --stop
if exist .\bin   rd/q/s .\bin
if exist .\build rd/q/s .\build
call .\gradlew -q clean
call .\gradlew -q run
call .\gradlew -q --stop
