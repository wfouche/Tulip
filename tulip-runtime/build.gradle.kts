/*
 * This file was generated by the Gradle 'init' task.
 */
import org.jreleaser.model.Active
import org.jetbrains.dokka.gradle.DokkaTask

group = "io.github.wfouche.tulip"
version = "2.1.6-dev"

plugins {
    id("com.github.ben-manes.versions") version "0.52.0"
    id("buildlogic.kotlin-library-conventions")
    `maven-publish`
    id("org.jreleaser") version "1.15.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("org.jetbrains.dokka") version "2.0.0"
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    // https://mvnrepository.com/artifact/io.micrometer/micrometer-registry-jmx
    // implementation("io.micrometer:micrometer-registry-jmx:1.14.3")

    // https://mvnrepository.com/artifact/org.hdrhistogram/HdrHistogram
    implementation("org.hdrhistogram:HdrHistogram:2.2.2")

    // https://mvnrepository.com/artifact/org.python/jython-standalone
    implementation("org.python:jython-standalone:2.7.4")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // https://mvnrepository.com/artifact/io.leego/banana
    implementation("io.leego:banana:2.1.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.11.0")

    // https://mvnrepository.com/artifact/com.glureau/html-mermaid-dokka-plugin
    dokkaPlugin("com.glureau:html-mermaid-dokka-plugin:0.6.0")

    // https://mvnrepository.com/artifact/io.github.oshai/kotlin-logging-jvm
    //implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.wfouche.tulip.api.TulipApi"
    }
}

// https://dev.to/tschuehly/how-to-publish-a-kotlinjava-spring-boot-library-with-gradle-to-maven-central-complete-guide-402a
// https://jreleaser.org/guide/latest/examples/maven/staging-artifacts.html
//
// $ ./gradlew publish    # Output in folder tulip-runtime/build/staging-deploy
//
publishing {
    publications {
        create<MavenPublication>("Tulip") {
            from(components["java"])
            groupId = "io.github.wfouche.tulip"
            artifactId = "tulip-runtime"
            description = "Tulip Runtime"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("tulip-runtime")
                description.set("Tulip Runtime")
                // url.set("https://wfouche.github.io/Tulip")
                url.set("https://github.com/wfouche/Tulip")
                inceptionYear.set("2020")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("wfouche")
                        name.set("Werner Fouché")
                        email.set("werner.fouche@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:wfouche/Tulip.git")
                    developerConnection.set("scm:git:ssh:git@github.com:wfouche/Tulip.git")
                    url.set("https://github.com/wfouche/Tulip")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        copyright.set("Werner Fouché")
    }
    gitRootSearch.set(true)
    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            nexus2 {
                create("maven-central") {
                    active.set(Active.ALWAYS)
                    url.set("https://s01.oss.sonatype.org/service/local")
                    closeRepository.set(false)
                    releaseRepository.set(false)
                    stagingRepositories.add("build/staging-deploy")
                }
            }
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        configureEach {
            val markdowns = fileTree("src/main/kotlin") {
                include("**/*.md")
            }
            includes.from(markdowns.files)
        }
    }
}
