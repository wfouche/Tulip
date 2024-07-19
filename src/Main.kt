/*-------------------------------------------------------------------------*/

@file:JvmName("benchmark00")

/*-------------------------------------------------------------------------*/

import kotlinx.cli.*
import tulip.VirtualUser

/*-------------------------------------------------------------------------*/

val name = """                                       
888888888888          88  88               
     88               88  ""               
     88               88                   
     88  88       88  88  88  8b,dPPYba,   
     88  88       88  88  88  88P'    "8a  
     88  88       88  88  88  88       d8  
     88  "8a,   ,a88  88  88  88b,   ,a8"  
     88   `"YbbdP'Y8  88  88  88`YbbdP"'   
                              88           
                              88           
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
