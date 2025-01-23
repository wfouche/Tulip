///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.4
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.1
//DEPS org.slf4j:slf4j-api:2.0.16
//DEPS ch.qos.logback:logback-core:1.5.16
//DEPS ch.qos.logback:logback-classic:1.5.16
//SOURCES HttpUser.groovy
//JAVA 21
//GROOVY 4.0.24

import io.github.wfouche.tulip.api.TulipApi

class App {
    static void main(String[] args) {
        TulipApi.runTulip("benchmark_config.jsonc")
    }
}
