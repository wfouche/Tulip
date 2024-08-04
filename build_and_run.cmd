call .\gradlew -q --stop
if exist .\bin   rd/q/s .\bin
if exist .\build rd/q/s .\build
call .\gradlew -q clean
call .\gradlew -q :tulip-benchmarks:run %*
call .\gradlew -q --stop

call .\text_report.cmd