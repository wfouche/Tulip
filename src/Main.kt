import user.*
import kotlinx.cli.*

fun main(args: Array<String>) {
    val parser = ArgParser("Tulip")
    val configFilename by parser.option(ArgType.String, shortName = "c", description = "JSON configuration file", fullName = "config").default("config.json")
    parser.parse(args)
    tulip.initConfig(configFilename)
    tulip.runTests(tulip.g_contexts, tulip.g_tests, g_actionNames, ::getUser)
}

/*-------------------------------------------------------------------------*/
