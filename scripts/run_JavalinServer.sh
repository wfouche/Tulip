# Shell 1 - run JavalinServer
#
# Create a 2 GB regions of Large Pages
#
# $ ls /sys/kernel/mm/hugepages/
#   hugepages-1048576kB  hugepages-2048kB
#
# # Reserve 2 GB of RAM
# $ echo 1024 > /sys/kernel/mm/hugepages/hugepages-2048kB/nr_hugepages
#
# "-XX:+UseLargePages",

export JBANG_JAVA_OPTIONS="-server -Xms512m -Xmx512m -XX:+UseZGC -XX:+ZGenerational"
export JBANG_JAVA_OPTIONS="-server -Xms512m -Xmx512m -XX:+UseZGC -XX:+ZGenerational -XX:+UseLargePages"
export JBANG_JAVA_OPTIONS="-server -Xms512m -Xmx512m             -XX:+UseParallelGC -XX:+UseLargePages"
export JBANG_JAVA_OPTIONS="-server -Xms512m -Xmx512m             -XX:+UseParallelGC"

jbang run JavalinServer.java

# Shell 2 - benchmark JavalinServer with Apache Bench
# ab -c 4 -t 5 http://localhost:7070/hello

# Shell 3 - benchmark JavalinServer with Tulip
