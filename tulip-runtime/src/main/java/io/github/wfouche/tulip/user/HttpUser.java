package io.github.wfouche.tulip.user;

import io.github.wfouche.tulip.api.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HttpUser class is an experimental addition to Tulip.
 */
public class HttpUser extends TulipUser {

    public HttpUser(int userId, int threadId) {
        super(userId, threadId);
    }

    public boolean onStart() {
        // Initialize the shared RestClient object only once
        if (getUserId() == 0) {
            var factory = new SimpleClientHttpRequestFactory();

            var connectTimeout_ = getUserParamValue("connectTimeoutMillis");
            if (!connectTimeout_.isEmpty()) {
                factory.setConnectTimeout(Integer.parseInt(connectTimeout_));
                logger.info("connectTimeoutMillis={}", connectTimeout_);
            }

            var readTimeout_ = getUserParamValue("readTimeoutMillis");
            if (!readTimeout_.isEmpty()) {
                factory.setReadTimeout(Integer.parseInt(readTimeout_));
                logger.info("readTimeoutMillis={}", readTimeout_);
            }

            var url = getUserParamValue("protocol") + "://" + getUserParamValue("host");
            logger.info("url=" + url);

            client = RestClient.builder()
                    .requestFactory(factory)
                    .baseUrl(url)
                    .build();

            // debug = Boolean.valueOf(getUserParamValue("debug"));
        }
        return true;
    }

//    // Action 1: GET /posts/{id}
//    public boolean action1() {
//        int id = ThreadLocalRandom.current().nextInt(100)+1;
//        return http_GET("/posts/{id}", id);
//    }
//
//    // Action 2: GET /comments/{id}
//    public boolean action2() {
//        int id = ThreadLocalRandom.current().nextInt(500)+1;
//        return http_GET("/comments/{id}", id);
//    }
//
//    // Action 3: GET /todos/{id}
//    public boolean action3() {
//        int id = ThreadLocalRandom.current().nextInt(200)+1;
//        return http_GET("/todos/{id}", id);
//    }

    public boolean onStop() {
        return true;
    }

    public boolean http_GET(String uri, Object... uriVariables) {
        boolean rc;
        try {
            String rsp = client.get()
                    .uri(uri, uriVariables)
                    .retrieve()
                    .body(String.class);
            rc = (rsp != null && !rsp.isEmpty());
        } catch (RestClientException e) {
            rc = false;
        }
        return rc;
    }

    public RestClient getRestClient()  {
        return client;
    }

    // RestClient object
    private static RestClient client;

    // Debug flag
    // private static boolean debug = false;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(HttpUser.class);

}