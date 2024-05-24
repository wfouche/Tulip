application {
    // Java 21 - https://docs.oracle.com/en/java/javase/21/gctuning/z-garbage-collector.html
    applicationDefaultJvmArgs = listOf(
        "-server",
        "-Xmx4096m",
        "-XX:+UseZGC", "-XX:+ZGenerational", "-XX:+UseDynamicNumberOfGCThreads",
        "-Dcom.sun.management.jmxremote.port=3333",
        "-Dcom.sun.management.jmxremote.ssl=false",
        "-Dcom.sun.management.jmxremote.authenticate=false")

    // Define the main class for the application
    //mainClass.set("benchmark00")
}