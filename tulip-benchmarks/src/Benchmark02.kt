///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.tulip:tulip-runtime-jvm:2.0.0-SNAPSHOT
//DEPS com.github.ajalt.clikt:clikt-jvm:4.4.0
//SOURCES user/http/HttpUser.kt user/http/HttpUser2.java

/*-------------------------------------------------------------------------*/

import org.tulip.api.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import org.tulip.api.TulipApi
import org.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

// https://devops.datenkollektiv.de/banner.txt/index.html
// <standard>
private const val banner02 = """                                       
  _____      _ _         ____    ___  
 |_   _|   _| (_)_ __   |___ \  / _ \ 
   | || | | | | | '_ \    __) || | | |
   | || |_| | | | |_) |  / __/ | |_| |
   |_| \__,_|_|_| .__/  |_____(_)___/ 
                |_|                   
"""

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
    private val configOpt by option("--config").default("config.json")
    private val resultOpt by option("--result")
    private val reportOpt by option("--report")
    override fun run() {
        echo(banner02)
        TulipApi.runTulip(configOpt, UserFactory02())
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli02().main(args)

/*-------------------------------------------------------------------------*/
