# Shell 1 - run JavalinServer
#
# export JBANG_JAVA_OPTIONS="-server -Xms1024m -Xmx1024m -XX:+UseZGC -XX:+ZGenerational"
export JBANG_JAVA_OPTIONS="-server -Xms1024m -Xmx1024m -XX:+UseParallelGC"

jbang JavalinServer.java

# Shell 2 - benchmark JavalinServer with Apache Bench
# ab -c 4 -t 5 http://localhost:7070/hello

# Shell 3 - benchmark JavalinServer with Tulip
