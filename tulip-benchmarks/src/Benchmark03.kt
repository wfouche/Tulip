///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.tulip:tulip-runtime-jvm:2.0.0-SNAPSHOT
//DEPS commons-cli:commons-cli:1.8.0
//DEPS org.slf4j:slf4j-nop:2.0.13
//SOURCES user/http/HttpUser.kt user/http/HttpUser2.java

/*-------------------------------------------------------------------------*/

import org.apache.commons.cli.*
import org.tulip.api.TulipApi
import org.tulip.api.TulipUser
import org.tulip.api.TulipUserFactory

/*-------------------------------------------------------------------------*/

// https://devops.datenkollektiv.de/banner.txt/index.html
// <standard>
private const val banner03 = """                                       
  _____      _ _         ____    ___  
 |_   _|   _| (_)_ __   |___ \  / _ \ 
   | || | | | | | '_ \    __) || | | |
   | || |_| | | | |_) |  / __/ | |_| |
   |_| \__,_|_|_| .__/  |_____(_)___/ 
                |_|                   
"""

/*-------------------------------------------------------------------------*/

private class UserFactory03: TulipUserFactory() {

    override fun getUser(userId: Int, className: String): TulipUser {
        return when (className) {
            "user.http.HttpUser" -> user.http.HttpUser(userId)
            "user.http.HttpUser2" -> user.http.HttpUser2(userId)
            else -> throw Exception("Unknown user class name provided - $className")
        }
    }
}


/*-------------------------------------------------------------------------*/

fun main(args: Array<String>) {
    val options = Options()
    options.addOption("c", "config", true, "Benchmark configuration file")

    val parser: CommandLineParser = DefaultParser()
    try {
        val cmd = parser.parse(options, args)
        val configFile = cmd.getOptionValue("config")
        TulipApi.runTulip(configFile, UserFactory03())
    } catch (e: ParseException) {
        System.err.println("Error: " + e.message)
        System.exit(1)
    }
}

/*-------------------------------------------------------------------------*/
