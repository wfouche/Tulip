package org.example.user

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipConsole
import io.github.wfouche.tulip.api.TulipUtils
import io.github.wfouche.tulip.api.TulipUser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/*-------------------------------------------------------------------------*/

private val client = HttpClient.newHttpClient()

private fun serviceCall(request:HttpRequest): Boolean {
    // https://www.baeldung.com/java-httpclient-connection-management

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    //println(id)
    //println(name)
    //println(response.statusCode())
    //println(response.body())

    return (response.statusCode() == 200)
}

/*-------------------------------------------------------------------------*/

class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    // ----------------------------------------------------------------- //

    private val requestPosts = createRequest("posts")
    private val requestComments = createRequest("comments")
    private val requestAlbums = createRequest("albums")
    private val requestPhotos = createRequest("photos")
    private val requestTodos = createRequest("todos")

    override fun onStart(): Boolean {
        val actionId = 0
        TulipConsole.put("  $userId -> $actionId -> ${getActionName(actionId)} -> $threadId")
        //TulipConsole.put(listOf("a", "b"))
        if (userId == 0) {
            var s = ""
            s = "debug: " + getUserParamValue("debug")
            TulipConsole.put(s)
            s = "http_port: " + getUserParamValue("http_port")
            TulipConsole.put(s)
        }
        return true
    }

    // ----------------------------------------------------------------- //

    // 0.25*6 + 0.75*14 = 12.0 ms

    override fun action1(): Boolean {
        // 6 ms delay (average)
        TulipUtils.delayMillisRandom(0, 12)
        return true
    }

    override fun action2(): Boolean {
        // 14 ms delay (average)
        TulipUtils.delayMillisRandom(0, 28)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action3(): Boolean {
        return serviceCall(requestPosts)
    }

    override fun action4(): Boolean {
        return serviceCall(requestComments)
    }

    override fun action5(): Boolean {
        return serviceCall(requestAlbums)
    }

    override fun action6(): Boolean {
        return serviceCall(requestPhotos)
    }

    override fun action7(): Boolean {
        return serviceCall(requestTodos)
    }

    // ----------------------------------------------------------------- //

    override fun action8(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action9(): Boolean {
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action10(): Boolean {
        TulipUtils.delayMillisFixed(10)
        return true
    }

    // ----------------------------------------------------------------- //

    override fun action11(): Boolean {
        val map = mapOf(
            "entityId"          to "8a8294174b7ecb28014b9699220015ca",
            "amount"            to "92.00",
            "currency"          to "EUR",
            "paymentBrand"      to "VISA",
            "paymentType"       to "PA",
            "card.number"       to "4200000000000000",
            "card.holder"       to "Jane Jones",
            "card.expiryMonth"  to "05",
            "card.expiryYear"   to "2034",
            "card.cvv"          to "123")
        val body: String = map.entries.joinToString("&")

        val token = "OGE4Mjk0MTc0YjdlY2IyODAxNGI5Njk5MjIwMDE1Y2N8c3k2S0pzVDg="

        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://eu-test.oppwa.com/v1/payments"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        val success = (response.statusCode() == 200)
        if (!success) {
            TulipConsole.put("statusCode = ${response.statusCode()}")
            TulipConsole.put("response = ${response.body()}")
        }
        return success
    }

    // ----------------------------------------------------------------- //

    override fun action18(): Boolean {
        val actionId = 18
        TulipConsole.put("  $userId -> $actionId -> ${getActionName(actionId)} -> $threadId")
        return true
    }

    // ----------------------------------------------------------------- //

    override fun onStop(): Boolean {
        TulipConsole.put("  Terminate: UserId = $userId")
        Thread.sleep(100)
        return true
    }

    // ----------------------------------------------------------------- //

    private fun createRequest(name: String): HttpRequest {
        val id = userId + 1
        val url: String = this.getUserParamValue("url")
        val request:HttpRequest = HttpRequest.newBuilder()
            .uri(URI("${url}/${name}/${id}"))
            .GET()
            .build()
        return request
    }

    // ----------------------------------------------------------------- //

//    companion object {
//        init {
//            //TulipConsole.put("Loading .... Kotlin class ... HttpUser")
//        }
//    }

    // ----------------------------------------------------------------- //

}

/*-------------------------------------------------------------------------*/
