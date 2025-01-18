/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt-jvm:5.0.1")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-core:1.5.16")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation(project(":tulip-runtime"))
}

application {
    // Define the main class for the application.
    mainClass = "org.example.app.AppKt"

    // Java 21 - https://docs.oracle.com/en/java/javase/21/gctuning/z-garbage-collector.html
    applicationDefaultJvmArgs = listOf(

        // -server or -client
        "-server",

        // Create a 2 GB regions of Large Pages
        //
        // $ ls /sys/kernel/mm/hugepages/
        //   hugepages-1048576kB  hugepages-2048kB
        // // # Reserve 2 GB of RAM
        // $ echo 1024 > /sys/kernel/mm/hugepages/hugepages-2048kB/nr_hugepages
        //
        // "-XX:+UseLargePages",

        "-Xms2048m",
        "-Xmx2048m",

        // "-XX:+UseParallelGC",
        // "-XX:+UseG1GC", "-XX:+UseDynamicNumberOfGCThreads",
        // "-XX:+UseZGC", "-XX:+ZGenerational", "-XX:+UseDynamicNumberOfGCThreads",
        "-XX:+UseZGC", "-XX:+ZGenerational",

        // VisualVM
        // "-Dcom.sun.management.jmxremote.port=3333",
        // "-Dcom.sun.management.jmxremote.ssl=false",
        // "-Dcom.sun.management.jmxremote.authenticate=false"
    )

}
