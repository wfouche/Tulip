/*-------------------------------------------------------------------------*/

import kotlinx.cli.*
import tulip.User
import user.UserHttp

/*-------------------------------------------------------------------------*/

val userActions = mapOf(
    0  to "init",
    1  to "DELAY-6ms",
    2  to "DELAY-14ms",
    3  to "REST-posts",
    4  to "REST-comments",
    5  to "REST-albums",
    6  to "REST-photos",
    7  to "REST-todos",
    8  to "login",
    99 to "done")

/*-------------------------------------------------------------------------*/

fun getUser(userId: Int): User {
    return UserHttp(userId)
}

/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) {
    val parser = ArgParser("Tulip")
    val configFilename by parser.option(ArgType.String, shortName = "c", description = "JSON configuration file", fullName = "config").default("config.json")
    parser.parse(args)
    tulip.initConfig(configFilename)
    tulip.runTests(tulip.g_contexts, tulip.g_tests, userActions, ::getUser)
}

/*-------------------------------------------------------------------------*/
