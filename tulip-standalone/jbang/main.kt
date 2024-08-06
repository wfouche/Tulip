///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.tulip:tulip-runtime-jvm:2.0.0-SNAPSHOT
//DEPS com.github.ajalt.clikt:clikt-jvm:4.4.0
//SOURCES HttpUser.kt

/*-------------------------------------------------------------------------*/

import org.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import org.tulip.api.TulipApi
import org.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

class UserFactory: TulipUserFactory() {

    override fun getUser(userId: Int, className: String): TulipUser {
        return when (className) {
            "user.http.HttpUser" -> HttpUser(userId)
            else -> throw Exception("Unknown user class name provided - $className")
        }
    }
}

/*-------------------------------------------------------------------------*/

class TulipCli : CliktCommand() {
    private val configOpt by option("--config").default("config.json")
    private val resultOpt by option("--result")
    private val reportOpt by option("--report")
    override fun run() {
        TulipApi.runTulip(configOpt, UserFactory())
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli().main(args)

/*-------------------------------------------------------------------------*/
