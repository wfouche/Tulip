/*-------------------------------------------------------------------------*/

import tulip.Console
import tulip.User
import tulip.delay
import tulip.delayMillisRandom

/*-------------------------------------------------------------------------*/

import okhttp3.OkHttpClient
import okhttp3.Request

/*-------------------------------------------------------------------------*/

class UserHttp(userId: Int) : User(userId) {

    // ----------------------------------------------------------------- //

    var httpClient = OkHttpClient()

    val request = Request.Builder()
            .url("http://jsonplaceholder.typicode.com/photos/${userId+1}")
            .build()

    // ----------------------------------------------------------------- //

    override fun init(): Boolean {
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

    override fun done(): Boolean {
        Console.put("  Terminate: UserId = $userId")
        delay(100)
        return true
    }

    // ----------------------------------------------------------------- //

}

/*-------------------------------------------------------------------------*/

fun getUser(userId: Int): User {
    return UserHttp(userId)
}

/*-------------------------------------------------------------------------*/
