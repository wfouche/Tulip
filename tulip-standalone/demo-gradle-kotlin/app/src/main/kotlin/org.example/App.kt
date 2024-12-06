package org.example

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUserFactory

fun main(args: Array<String>) {
    TulipApi.runTulip("./benchmark_config.json", TulipUserFactory())
}

