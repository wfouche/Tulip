/*-------------------------------------------------------------------------*/

@file:JvmName("benchmark00")

/*-------------------------------------------------------------------------*/

import kotlinx.cli.*
import tulip.VirtualUser

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

fun main(args: Array<String>) {
    tulip.Console.put(name)
    val parser = ArgParser("Tulip")
    val configFilename by parser.option(
        ArgType.String,
        shortName = "c",
        description = "JSON configuration file",
        fullName = "config"
    ).default("config.json")
    parser.parse(args)
    tulip.initConfig(configFilename)
    tulip.runTests(::getUser)
    tulip.logger.info { "Done" }
}

/*-------------------------------------------------------------------------*/
