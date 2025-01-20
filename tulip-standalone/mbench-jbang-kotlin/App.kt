///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github.ajalt.clikt:clikt-jvm:5.0.1
//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.3
//SOURCES HttpUser.kt

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

class TulipCli : CliktCommand() {
    private val configOpt by option("--config").default("config.json")
    override fun run() {
        TulipApi.runTulip(configOpt, TulipUserFactory())
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli().main(args)

/*-------------------------------------------------------------------------*/
