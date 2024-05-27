/*-------------------------------------------------------------------------*/

@file:JvmName("benchmark00")

/*-------------------------------------------------------------------------*/

import kotlinx.cli.*
import tulip.User
import user.UserHttp

/*-------------------------------------------------------------------------*/

fun getUser(userId: Int, userClass: String): User {
    return when (userClass) {
        "user.UserHttp" -> user.UserHttp(userId)
        else -> throw Exception("Unknown user class name provided - $userClass")
    }
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) {
    val parser = ArgParser("Tulip")
    val configFilename by parser.option(ArgType.String, shortName = "c", description = "JSON configuration file", fullName = "config").default("config.json")
    parser.parse(args)
    tulip.initConfig(configFilename)
    tulip.runTests(::getUser)
    tulip.logger.info {"Done"}
}

/*-------------------------------------------------------------------------*/
