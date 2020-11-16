import tulip.Console
import tulip.User
import tulip.delay
import tulip.delayMillisRandom

import okhttp3.OkHttpClient
import okhttp3.Request

class UserHttp(userId: Int) : User(userId) {

    val httpClient = OkHttpClient()

    val request = Request.Builder()
            .url("http://jsonplaceholder.typicode.com/photos/${userId+1}")
            .build()

    override fun init(): Boolean {
        Console.put("  $userId")
        return true
    }

    override fun action1(): Boolean {
        httpClient.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

    override fun done(): Boolean {
        Console.put("  $userId")
        return true
    }

}