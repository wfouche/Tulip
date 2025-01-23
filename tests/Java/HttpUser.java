import io.github.wfouche.tulip.api.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUser extends TulipUser {

    public HttpUser(int userId, int threadId) {
        super(userId, threadId);
    }

    public boolean onStart() {
        // Initialize the shared RestClient object only once
        if (getUserId() == 0) {
            logger.info("Java");
            logger.info("Initializing static data");
            var connectTimeout = Integer.valueOf(getUserParamValue("connectTimeoutMillis"));
            var readTimeout = Integer.valueOf(getUserParamValue("readTimeoutMillis"));
            var factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(connectTimeout);
            factory.setReadTimeout(readTimeout);
            restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(getUserParamValue("baseURI"))
                .build();
            debug = Boolean.valueOf(getUserParamValue("debug"));
            logger.info("debug = " + debug);
        }
        return true;
    }

    // Action 1: GET /posts/{id}
    public boolean action1() {
        boolean rc;
        try {
            int id = debug ? 1 : ThreadLocalRandom.current().nextInt(100)+1;
            String rsp = restClient.get()
              .uri("/posts/{id}", id)
              .retrieve()
              .body(String.class);
            rc = (rsp != null && rsp.length() > 2);
        } catch (RestClientException e) {
           rc = false;
        }
        return rc;
    }

    // Action 2: GET /comments/{id}
    public boolean action2() {
        boolean rc;
        try {
            int id = debug ? 1 : ThreadLocalRandom.current().nextInt(500)+1;
            String rsp = restClient.get()
                .uri("/comments/{id}", id)
                .retrieve()
                .body(String.class);
            rc = (rsp != null && rsp.length() > 2);
        } catch (RestClientException e) {
            rc = false;
        }
        return rc;
    }

    // Action 3: GET /todos/{id}
    public boolean action3() {
        boolean rc;
        try {
            int id = debug ? 1 : ThreadLocalRandom.current().nextInt(200)+1;
            String rsp = restClient.get()
                .uri("/todos/{id}", id)
                .retrieve()
                .body(String.class);
            rc = (rsp != null && rsp.length() > 2);
        } catch (RestClientException e) {
            rc = false;
        }
        return rc;
    }

    // Action 99
    public boolean onStop() {
        return true;
    }

    // RestClient object
    private static RestClient restClient;

    // Debug flag
    private static boolean debug = false;

    /// Logger
    private static final Logger logger = LoggerFactory.getLogger(HttpUser.class);

}
