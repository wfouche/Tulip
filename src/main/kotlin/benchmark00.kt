/*-------------------------------------------------------------------------*/

@file:JvmName("benchmark00")

/*-------------------------------------------------------------------------*/

import kotlinx.cli.*
import tulip.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/*-------------------------------------------------------------------------*/

private val client = HttpClient.newHttpClient()

class UserHttp(userId: Int) : User(userId) {

    // ----------------------------------------------------------------- //

    private val request = HttpRequest.newBuilder()
        .uri(URI("https://jsonplaceholder.typicode.com/photos/${userId + 1}"))
        .GET()
        .build()

    // ----------------------------------------------------------------- //

    override fun start(): Boolean {
        val actionId = 0
        Console.put("  $userId -> $actionId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action1(): Boolean {
        // 6 ms delay (average)
        delayMillisRandom(1, 11)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action2(): Boolean {
        // 14 ms delay (average)
        delayMillisRandom(1, 27)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action3(): Boolean {
        // https://www.baeldung.com/java-httpclient-connection-management
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        //println(response.statusCode())
        //println(response.body())

        return (response.statusCode() == 200)
    }

    // ----------------------------------------------------------------- //

    override fun action4(): Boolean {
        // 14 ms delay (average)
        //delayMillisRandom(1, 27)
        val actionId = 4
        Console.put("  $userId -> $actionId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action5(): Boolean {
        // 14 ms delay (average)
        //delayMillisRandom(1, 27)
        val actionId = 5
        Console.put("  $userId -> $actionId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action6(): Boolean {
        // 14 ms delay (average)
        //delayMillisRandom(1, 27)
        val actionId = 6
        Console.put("  $userId -> $actionId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action7(): Boolean {
        val actionId = 7
        Console.put("  $userId -> $actionId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action8(): Boolean {
        //val actionId = 8
        return true
    }

    // ----------------------------------------------------------------- //

    override fun stop(): Boolean {
        Console.put("  Terminate: UserId = $userId")
        delay(100)
        return true
    }

    // ----------------------------------------------------------------- //

}

/*-------------------------------------------------------------------------*/

val g_actionNames = mapOf(
    0 to "init",
    3 to "REST-photos",
    NUM_ACTIONS-1 to "done")

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
    runTests(tulip.g_contexts, tulip.g_tests, g_actionNames, ::getUser)
}

/*-------------------------------------------------------------------------*/
