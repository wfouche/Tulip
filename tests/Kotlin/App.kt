///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.4
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
//DEPS org.slf4j:slf4j-api:2.0.16
//DEPS ch.qos.logback:logback-core:1.5.16
//DEPS ch.qos.logback:logback-classic:1.5.16
//SOURCES HttpUser.kt
//JAVA 21
//KOTLIN 2.0.21

import io.github.wfouche.tulip.api.TulipApi

fun main(args: Array<String>) {
    TulipApi.runTulip("benchmark_config.jsonc")
}
