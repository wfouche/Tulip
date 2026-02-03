plugins {
    kotlin("jvm") version "2.3.0" // Use a compatible Kotlin version
    kotlin("plugin.serialization") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm") // Use the latest Ktor version
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-host-common") // Often useful
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("ch.qos.logback:logback-classic:1.5.25")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.3.0")
}

application {
    mainClass.set("com.example.ApplicationKt")

    applicationDefaultJvmArgs = listOf(

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

        // ZGC - https://joelsiks.com/posts/zgc-heap-memory-allocation/
        //
        // "-XX:+UseParallelGC",

        // Java 17
        //"-XX:+UseG1GC", "-XX:+UseDynamicNumberOfGCThreads",

        // Java 21
        // "-XX:+UseZGC", "-XX:+ZGenerational",

        // Java 25
        // "-XX:+UseZGC",

        // VisualVM
        // "-Dcom.sun.management.jmxremote.port=3333",
        // "-Dcom.sun.management.jmxremote.ssl=false",
        // "-Dcom.sun.management.jmxremote.authenticate=false"
    )
}

