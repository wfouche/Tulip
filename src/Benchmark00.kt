/*-------------------------------------------------------------------------*/

import tulip.user.VirtualUser
import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

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

fun getUser(userId: Int, userClass: String): VirtualUser {
    return when (userClass) {
        "user.http.HttpUser" -> user.http.HttpUser(userId)
        "user.http.HttpUser2" -> user.http.HttpUser2(userId)
        else -> throw Exception("Unknown user class name provided - $userClass")
    }
}

/*-------------------------------------------------------------------------*/

class TulipCli : CliktCommand() {
    val configOpt by option("--config").default("config.json")
    val resultOpt by option("--result")
    val reportOpt by option("--report")
    override fun run() {
        tulip.core.initConfig(configOpt)
        tulip.core.runTests(::getUser)
    }
}

fun main(args: Array<String>) = TulipCli().main(args)

/*-------------------------------------------------------------------------*/
