/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

plugins {

    // Add support for AsciidoctorJ
    id("org.asciidoctor.jvm.convert") version "3.3.2"

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.30-RC"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
}

dependencies {
    // Include JAR files in libs folder
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // JSON Kotlin Parser.
    implementation("com.beust:klaxon:5.5")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Default JVM settings

	// G1GC (default), Java 11
    applicationDefaultJvmArgs = listOf("-server", "-Xms4096m", "-Xmx4096m", "-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled")

    // applicationDefaultJvmArgs = listOf("-server", "-Xms4096m", "-Xmx4096m", "-XX:+UseZGC")
    // applicationDefaultJvmArgs = listOf("-server", "-Xmx4096m", "-Xmx4096m", "-XX:+UseShenandoahGC")
	
	// Java 8
	//, "-XX:+UseParallelGC")

    // Define the main class for the application
    mainClass.set("LoadTest1")
}
