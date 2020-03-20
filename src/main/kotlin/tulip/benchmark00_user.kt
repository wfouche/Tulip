/*-------------------------------------------------------------------------*/

package tulip

/*-------------------------------------------------------------------------*/

import java.io.IOException
import java.net.URL

/*-------------------------------------------------------------------------*/

class UserHttp(userId: Int) : User(userId) {

    // ----------------------------------------------------------------- //

    override fun init(): Boolean {
        val actionId = 0
        Console.put("  ${userId} -> ${actionId}")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action01(): Boolean {
        // 6 ms delay (average)
        delayMillisRandom(1, 11)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action02(): Boolean {
        // 14 ms delay (average)
        delayMillisRandom(1, 27)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action03(): Boolean {  /*
        val response = try {
            URL("https://jsonplaceholder.typicode.com/photos/${userId+1}")
            .openStream()
            .bufferedReader()
            .use { it.readText() }
        } catch (e: IOException) {
            "Error with ${e.message}."
        }  */
        //println(response)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action04(): Boolean {
        // 14 ms delay (average)
        //delayMillisRandom(1, 27)
        val actionId = 4
        Console.put("  ${userId} -> ${actionId}")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action05(): Boolean {
        // 14 ms delay (average)
        //delayMillisRandom(1, 27)
        val actionId = 5
        Console.put("  ${userId} -> ${actionId}")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action06(): Boolean {
        // 14 ms delay (average)
        //delayMillisRandom(1, 27)
        val actionId = 6
        Console.put("  ${userId} -> ${actionId}")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action07(): Boolean {
        val actionId = 7
        Console.put("  ${userId} -> ${actionId}")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action08(): Boolean {
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
