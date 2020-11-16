import tulip.Console
import tulip.User

import okhttp3.OkHttpClient
import okhttp3.Request

class UserHttp(userId: Int) : User(userId) {

    val httpClient = OkHttpClient()
    val httpURL1 = "http://jsonplaceholder.typicode.com/photos/${userId+1}"
    val request1 = Request.Builder().url(httpURL1).build()

    override fun action1(): Boolean {
        httpClient.newCall(request1).execute().use { response ->
            return response.isSuccessful
        }
    }
}
