package org.example

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

private class UserFactory00: TulipUserFactory() {

    override fun getUser(userId: Int, className: String, threadId: Int): TulipUser {
        return when (className) {
            "user.http.HttpUser" -> HttpUser(userId, threadId)
            else -> throw Exception("Unknown user class name provided - $className")
        }
    }
}

/*-------------------------------------------------------------------------*/

private class TulipCli00 : CliktCommand() {
    private val configOpt by option("--config").default("benchmark_config.jsonc")
    private val resultOpt by option("--result").default("benchmark_output.json")
    override fun run() {
        if (configOpt != "") {
            TulipApi.runTulip(configOpt, UserFactory00())
        } else if (resultOpt != "") {
	        echo(resultOpt)
            TulipApi.createHtmlReport(resultOpt)
        }
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli00().main(args)

/*-------------------------------------------------------------------------*/
