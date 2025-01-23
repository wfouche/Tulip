import io.github.wfouche.tulip.api.*
import java.util.concurrent.ThreadLocalRandom
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpUser(userId: Int, threadId: Int) : TulipUser(userId, threadId) {

    // Action 0
    override fun onStart(): Boolean {
        // Initialize the shared RestClient object only once
        if (userId == 0) {
            logger.info("Kotlin")
            logger.info("Initializing static data")
            val connectTimeout = getUserParamValue("connectTimeoutMillis").toInt()
            val readTimeout = getUserParamValue("readTimeoutMillis").toInt()
            val factory = SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(connectTimeout)
                setReadTimeout(readTimeout)
            }
            restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(getUserParamValue("baseURI"))
                .build()
            debug = getUserParamValue("debug").toBoolean()
            logger.info("debug = " + debug)
        }
        return true
    }

    // Action 1: GET /posts/{id}
    override fun action1(): Boolean {
        val id: Int = if (debug) 1 else ThreadLocalRandom.current().nextInt(100)+1
        return try {
            val rsp: String? = restClient.get()
                .uri("/posts/${id}")
                .retrieve()
                .body(String::class.java)
            //Postcondition
            (rsp != null && rsp.length > 2)
        } catch (e: RestClientException) {
            false
        }
    }

    // Action 2: GET /comments/{id}
    override fun action2(): Boolean {
        val id: Int = if (debug) 1 else ThreadLocalRandom.current().nextInt(500)+1
        return try {
            val rsp: String? = restClient.get()
                .uri("/comments/${id}")
                .retrieve()
                .body(String::class.java)
            //Postcondition
            (rsp != null && rsp.length > 2)
        } catch (e: RestClientException) {
            false
        }
    }

    // Action 3: GET /todos/{id}
    override fun action3(): Boolean {
        val id: Int = if (debug) 1 else ThreadLocalRandom.current().nextInt(200)+1
        return try {
            val rsp: String? = restClient.get()
                .uri("/todos/${id}")
                .retrieve()
                .body(String::class.java)
            //Postcondition
            (rsp != null && rsp.length > 2)
        } catch (e: RestClientException) {
            false
        }
    }

    // Action 99
    override fun onStop(): Boolean {
        return true
    }

    // RestClient object
    companion object {
        private lateinit var restClient: RestClient
        private var debug: Boolean = false
        private val logger = LoggerFactory.getLogger(HttpUser::class.java)
    }
}
