/*-------------------------------------------------------------------------*/

@file:JvmName("benchmark00")

/*-------------------------------------------------------------------------*/

import tulip.User
import tulip.runTests
import okhttp3.OkHttpClient
import okhttp3.Request
import tulip.Console
import tulip.delay
import tulip.delayMillisRandom
import tulip.NUM_ACTIONS

/*-------------------------------------------------------------------------*/

class UserHttp(userId: Int) : User(userId) {

    // ----------------------------------------------------------------- //

    private var httpClient = OkHttpClient()

    private val request = Request.Builder()
        .url("https://jsonplaceholder.typicode.com/photos/${userId + 1}")
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
        // https://square.github.io/okhttp/recipes/

        httpClient.newCall(request).execute().use { response ->
            return response.isSuccessful

            //for ((name, value) in response.headers) {
            //    println("$name: $value")
            //}

            //println(response.body!!.string())
        }
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

fun main() {
    tulip.initConfig()
    runTests(tulip.g_contexts, tulip.g_tests, g_actionNames, ::getUser)
}

/*-------------------------------------------------------------------------*/
