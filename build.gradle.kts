application {
    // Java 21 - https://docs.oracle.com/en/java/javase/21/gctuning/z-garbage-collector.html
    applicationDefaultJvmArgs = listOf(

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

        "-XX:+UseParallelGC",
        // "-XX:+UseG1GC", "-XX:+UseDynamicNumberOfGCThreads",
        // "-XX:+UseZGC", "-XX:+ZGenerational", "-XX:+UseDynamicNumberOfGCThreads",

        // "-Dcom.sun.management.jmxremote.port=3333",
        // "-Dcom.sun.management.jmxremote.ssl=false",
        // "-Dcom.sun.management.jmxremote.authenticate=false"

        )

    // Define the main class for the application
    mainClass.set("benchmark00")
}

plugins {
    // Gradle versions plugin
    // $ ./gradlew dependencyUpdates
    id("com.github.ben-manes.versions") version "0.51.0"

    // Add support for AsciidoctorJ
    // id("org.asciidoctor.jvm.convert") version "4.0.2"

    // Kotlin Dokka
    // ./gradlew dokkaHtml
    id("org.jetbrains.dokka") version "1.9.20"

    // Apply GraalVM Native Image plugin
    // https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
    // id("org.graalvm.buildtools.native") version "0.10.2"
}
