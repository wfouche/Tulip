import io.github.wfouche.tulip.api.*
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.http.client.SimpleClientHttpRequestFactory
import java.util.concurrent.ThreadLocalRandom
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpUser extends TulipUser {

    HttpUser(int userId, int threadId) {
        super(userId, threadId)
    }

    boolean onStart() {
        // Initialize the shared RestClient object only once
        if (userId == 0) {
            logger.info("Groovy")
            logger.info("Initializing static data")
            def connectTimeout = getUserParamValue("connectTimeoutMillis") as Integer
            def readTimeout = getUserParamValue("readTimeoutMillis") as Integer
            def factory = new SimpleClientHttpRequestFactory()
            factory.setConnectTimeout(connectTimeout)
            factory.setReadTimeout(readTimeout)
            restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(getUserParamValue("baseURI"))
                .build()
            def debug = Boolean.valueOf(getUserParamValue("debug"))
            logger.info("debug = " + debug)
        }
        return true
    }

    // Action 1: GET /posts/{id}
    boolean action1() {
        boolean rc
        try {
            int id = debug ? 1 : ThreadLocalRandom.current().nextInt(100) + 1
            String rsp = restClient.get()
                .uri("/posts/${id}")
                .retrieve()
                .body(String.class)
            rc = (rsp != null && rsp.length() > 2)
        } catch (RestClientException e) {
            rc = false
        }
        return rc
    }

    // Action 2: GET /comments/{id}
    boolean action2() {
        boolean rc
        try {
            int id = debug ? 1 : ThreadLocalRandom.current().nextInt(500) + 1
            String rsp = restClient.get()
                .uri("/comments/${id}")
                .retrieve()
                .body(String.class)
            rc = (rsp != null && rsp.length() > 2)
        } catch (RestClientException e) {
            rc = false
        }
        return rc
    }

    // Action 3: GET /todos/{id}
    boolean action3() {
        boolean rc
        try {
            int id = debug ? 1 : ThreadLocalRandom.current().nextInt(200) + 1
            String rsp = restClient.get()
                .uri("/todos/${id}")
                .retrieve()
                .body(String.class)
            rc = (rsp != null && rsp.length() > 2)
        } catch (RestClientException e) {
            rc = false
        }
        return rc
    }

    // Action 99
    boolean onStop() {
        return true
    }

    // RestClient object
    static RestClient restClient

    // Debug flag
    static boolean debug = false

    // Logger
    static Logger logger = LoggerFactory.getLogger(HttpUser.class)

}
