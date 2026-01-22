package org.example.lib

import io.github.wfouche.tulip.user.HttpUser
import java.util.concurrent.ThreadLocalRandom
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TulipLibHttpUserTest {

    val config = HashMap<String, String>()
    val user: HttpUser

    init {
        config["url"] = "http://jsonplaceholder.typicode.com/posts/1"
        config["httpVersion"] = "HTTP_1_1"
        config["connectTimeoutMillis"] = "10000"
        config["readTimeoutMillis"] = "10000"
        user = HttpUser()
        user.initRuntime(0, 0)
        user.initConfig(config)
        user.onStart()
    }

    // Action 1: GET /posts/{id}
    @Test
    fun action1() {
        logger().info("action1: GET /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val rsp: HttpUser.Response = user.get("/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to GET /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("GET /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    // Action 2: POST /posts
    @Test
    fun action2() {
        logger().info("action2: POST /posts")
        val body = "{\"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}"
        val rsp: HttpUser.Response = user.post(body, "/posts")
        if (!rsp.isSuccessful) {
            logger().error("Failed to POST /posts")
            assertEquals(0, 1)
        }
        logger().info("POST /posts response: {}", rsp)
        assertEquals(0, 0)
    }

    // Action 3: PUT /posts/{id}
    @Test
    fun action3() {
        logger().info("action3: PUT /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val body =
            "{\"id\": " +
                id +
                ", \"title\": \"updated title\"" +
                ", \"body\": \"updated body\", \"userId\": 1}"
        val rsp: HttpUser.Response = user.put(body, "/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to PUT /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("PUT /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    // Action 4: PATCH /posts/{id}
    @Test
    fun action4() {
        logger().info("action4: PATCH /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val body = "{\"title\": \"patched title\"}"
        val rsp: HttpUser.Response = user.patch(body, "/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to PATCH /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("PATCH /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    // Action 5: DELETE /posts/{id}
    @Test
    fun action5() {
        logger().info("action5: DELETE /posts/{id}")
        val id = ThreadLocalRandom.current().nextInt(100) + 1
        val rsp: HttpUser.Response = user.delete("/posts/{id}", id)
        if (!rsp.isSuccessful) {
            logger().error("Failed to DELETE /posts/{}", id)
            assertEquals(0, 1)
        }
        logger().info("DELETE /posts/{} response: {}", id, rsp)
        assertEquals(0, 0)
    }

    fun logger(): Logger {
        return logger
    }

    // RestClient object
    companion object {
        private val logger = LoggerFactory.getLogger(TulipLibHttpUserTest::class.java)
    }
}
