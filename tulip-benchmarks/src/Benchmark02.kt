///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.tulip:tulip-runtime-jvm:0.1.0-SNAPSHOT
//DEPS com.github.ajalt.clikt:clikt-jvm:5.0.0
//DEPS org.slf4j:slf4j-nop:2.0.13
//SOURCES user/http/HttpUser.kt user/http/HttpUser2.java

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import io.github.wfouche.tulip.api.TulipApi
import io.github.wfouche.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

private class UserFactory02: TulipUserFactory() {

    override fun getUser(userId: Int, className: String, threadId: Int): TulipUser {
        return when (className) {
            "user.http.HttpUser" -> user.http.HttpUser(userId, threadId)
            "user.http.HttpUser2" -> user.http.HttpUser2(userId, threadId)
            else -> throw Exception("Unknown user class name provided - $className")
        }
    }
}

/*-------------------------------------------------------------------------*/

private class TulipCli02 : CliktCommand() {
    private val configOpt by option("--config").default("")
    override fun run() {
        TulipApi.runTulip(configOpt, UserFactory02())
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli02().main(args)

/*-------------------------------------------------------------------------*/
