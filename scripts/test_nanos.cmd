del app.aot
call jbang run --java 25 --runtime-option=-XX:AOTCacheOutput=app.aot nanos.java

call jbang run --java 25 --runtime-option=-XX:AOTCache=app.aot       nanos.java
call jbang run --java 25 --runtime-option=-XX:AOTCache=app.aot       nanos.java
call jbang run --java 25 --runtime-option=-XX:AOTCache=app.aot       nanos.java