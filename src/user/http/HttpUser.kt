package user.http

/*-------------------------------------------------------------------------*/

import tulip.Console
import tulip.VirtualUser
import tulip.delayMillisRandom
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/*-------------------------------------------------------------------------*/

private val client = HttpClient.newHttpClient()

private fun serviceCall(user: VirtualUser, resource: String, userId: Int): Boolean {
    // https://www.baeldung.com/java-httpclient-connection-management
    val id = userId + 1
    val url: String = user.getUserParamValue("url")
    val request = HttpRequest.newBuilder()
        .uri(URI("${url}/${resource}/${id}"))
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

class HttpUser(userId: Int) : VirtualUser(userId) {

    // ----------------------------------------------------------------- //

    override fun start(): Boolean {
        val actionId = 0
        Console.put("  $userId -> $actionId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action1(): Boolean {
        // 6 ms delay (average)
        delayMillisRandom(0, 12)
        return true
    }

    override fun action2(): Boolean {
        // 14 ms delay (average)
        delayMillisRandom(0, 28)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action3(): Boolean {
        return serviceCall(this,"posts", userId)
    }

    override fun action4(): Boolean {
        return serviceCall(this,"comments", userId)
    }

    override fun action5(): Boolean {
        return serviceCall(this,"albums", userId)
    }

    override fun action6(): Boolean {
        return serviceCall(this,"photos", userId)
    }

    override fun action7(): Boolean {
        return serviceCall(this,"todos", userId)
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

    override fun action10(): Boolean {
        Thread.sleep(10)
        return true
    }

    // ----------------------------------------------------------------- //
    override fun stop(): Boolean {
        Console.put("  Terminate: UserId = $userId")
        Thread.sleep(100)
        return true
    }

    // ----------------------------------------------------------------- //

    companion object {
        init {
            Console.put("Loading .... Kotlin class ... HttpUser")
        }
    }

    // ----------------------------------------------------------------- //

}

/*-------------------------------------------------------------------------*/