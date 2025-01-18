package org.example.user

/*-------------------------------------------------------------------------*/

import io.github.wfouche.tulip.api.TulipUtils
import io.github.wfouche.tulip.api.TulipUser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
        if (userId == 0) {
            var s = ""
            s = "debug: " + getUserParamValue("debug").toBoolean()
            logger.info(s)
            s = "http_port: " + getUserParamValue("http_port").toInt()
            logger.info(s)
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

    override fun onStop(): Boolean {
        logger.info("  Terminate: UserId = $userId")
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

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(HttpUser::class.java)
    }

}

/*-------------------------------------------------------------------------*/
