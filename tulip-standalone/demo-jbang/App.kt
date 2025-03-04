///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.6
//JAVA 21
//SOURCES DemoUser.kt

import io.github.wfouche.tulip.api.*

fun main(args: Array<String>) {
    TulipApi.runTulip("benchmark_config.jsonc")
}