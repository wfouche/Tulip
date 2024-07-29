/*-------------------------------------------------------------------------*/

import tulip.user.TulipUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

import tulip.api.TulipApi
import tulip.api.TulipUserFactory

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
    val configOpt by option("--config").default("config.json")
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
