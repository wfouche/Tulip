call init_env.cmd

call .\gradlew --stop

timeout 5
rd/q/s %USERPROFILE%\.gradle
timeout 5
rd/q/s .gradle
timeout 5
rd/q/s .idea