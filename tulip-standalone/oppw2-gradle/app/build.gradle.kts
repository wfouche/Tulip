/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.10.1/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the application plugin to add support for building a CLI application in Java.
    application

    // Apply the Kotlin serialization plugin
    alias(libs.plugins.kotlin.serialization)

    // Gradle versions plugin
    alias(libs.plugins.gradle.versions.plugin )
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.1")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
    implementation(libs.clikt.jvm)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.tulip.runtime)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.example.AppKt"

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

        //"-Xms1024m",
        "-Xmx1024m",

        // "-XX:+UseParallelGC",
        // "-XX:+UseG1GC", "-XX:+UseDynamicNumberOfGCThreads",
        "-XX:+UseZGC", "-XX:+ZGenerational",

        // VisualVM
        // "-Dcom.sun.management.jmxremote.port=3333",
        // "-Dcom.sun.management.jmxremote.ssl=false",
        // "-Dcom.sun.management.jmxremote.authenticate=false"
    )

}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
