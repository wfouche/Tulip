package user

/*-------------------------------------------------------------------------*/

import tulip.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/*-------------------------------------------------------------------------*/

private val client = HttpClient.newHttpClient()

private fun serviceCall(resource: String, userId: Int): Boolean {
    // https://www.baeldung.com/java-httpclient-connection-management
    val id = userId + 1
    val request = HttpRequest.newBuilder()
        .uri(URI("https://jsonplaceholder.typicode.com/${resource}/${id}"))
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    //println(id)
    //println(name)
    //println(response.statusCode())
    //println(response.body())

    return (response.statusCode() == 200)
}

/*-------------------------------------------------------------------------*/

class UserHttp(userId: Int) : User(userId) {

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

    override fun action2(): Boolean {
        // 14 ms delay (average)
        delayMillisRandom(1, 27)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action3(): Boolean {
        return serviceCall("posts", userId)
    }

    override fun action4(): Boolean {
        return serviceCall("comments", userId)
    }

    override fun action5(): Boolean {
        return serviceCall("albums", userId)
    }

    override fun action6(): Boolean {
        return serviceCall("photos", userId)
    }

    override fun action7(): Boolean {
        return serviceCall("todos", userId)
    }

    // ----------------------------------------------------------------- //

    override fun action8(): Boolean {
        val actionId = 8
        Console.put("  $userId -> $actionId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action9(): Boolean {
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