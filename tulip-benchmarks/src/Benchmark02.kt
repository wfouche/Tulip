///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS io.javalin:javalin:6.2.0
//DEPS org.slf4j:slf4j-simple:2.0.13
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
val name = """                                       
  _____      _ _         ____    ___  
 |_   _|   _| (_)_ __   |___ \  / _ \ 
   | || | | | | | '_ \    __) || | | |
   | || |_| | | | |_) |  / __/ | |_| |
   |_| \__,_|_|_| .__/  |_____(_)___/ 
                |_|                   
"""

/*-------------------------------------------------------------------------*/

class UserFactory: TulipUserFactory() {

    override fun getUser(userId: Int, className: String): TulipUser {
        return when (className) {
            "user.http.HttpUser" -> user.http.HttpUser(userId)
            "user.http.HttpUser2" -> user.http.HttpUser2(userId)
            else -> throw Exception("Unknown user class name provided - $className")
        }
    }
}

/*-------------------------------------------------------------------------*/

class TulipCli : CliktCommand() {
    val configOpt by option("--config").default("user/http/config.json")
    val resultOpt by option("--result")
    val reportOpt by option("--report")
    override fun run() {
        echo(name)
        TulipApi.runTulip(configOpt, UserFactory())
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) = TulipCli().main(args)

/*-------------------------------------------------------------------------*/
