///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS io.github.wfouche.tulip:tulip-runtime:2.1.9-dev
//DEPS org.slf4j:slf4j-api:2.0.17
//DEPS ch.qos.logback:logback-core:1.5.18
//DEPS ch.qos.logback:logback-classic:1.5.18

import io.github.wfouche.tulip.user.HttpUser;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.*;

public class HttpUserTest extends HttpUser {

    public HttpUserTest(HashMap<String, String> config) {
        super(config);
    }   

    // Action 1: GET /posts/{id}
    @Override public boolean action1() {
        logger().info("action1: GET /posts/{id}");
        int id = ThreadLocalRandom.current().nextInt(100)+1;
        String rsp = http_GET("/posts/{id}", id);
        if (rsp.isEmpty()) {
            logger().error("Failed to GET /posts/{}", id);
            return false;
        }
        logger().info("GET /posts/{} response: {}", id, rsp);
        return true;
    }

    // Action 2: POST /posts
    @Override public boolean action2() {
        logger().info("action2: POST /posts");
        String body = "{\"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}";
        String rsp = http_POST(body, "/posts");
        if (rsp.isEmpty()) {
            logger().error("Failed to POST /posts");
            return false;
        }
        logger().info("POST /posts response: {}", rsp);
        return true;
    }

    // Action 3: PUT /posts/{id}
    @Override public boolean action3() {
        logger().info("action3: PUT /posts/{id}");
        int id = ThreadLocalRandom.current().nextInt(100)+1;
        String body = "{\"id\": " + id + ", \"title\": \"updated title\"" + ", \"body\": \"updated body\", \"userId\": 1}";
        String rsp = http_PUT(body, "/posts/{id}", id);
        if (rsp.isEmpty()) {
            logger().error("Failed to PUT /posts/{}", id);
            return false;
        }
        logger().info("PUT /posts/{} response: {}", id, rsp);
        return true;
    }

    // Action 4: PATCH /posts/{id}
    @Override public boolean action4() {
        logger().info("action4: PATCH /posts/{id}");
        int id = ThreadLocalRandom.current().nextInt(100)+1;
        String body = "{\"title\": \"patched title\"}";
        String rsp = http_PATCH(body, "/posts/{id}", id);
        if (rsp.isEmpty()) {
            logger().error("Failed to PATCH /posts/{}", id);
            return false;
        }
        logger().info("PATCH /posts/{} response: {}", id, rsp);
        return true;
    }

    // Action 5: DELETE /posts/{id}
    @Override public boolean action5() {
        logger().info("action5: DELETE /posts/{id}");
        int id = ThreadLocalRandom.current().nextInt(100)+1;
        String rsp = http_DELETE("/posts/{id}", id);
        if (rsp.isEmpty()) {
            logger().error("Failed to DELETE /posts/{}", id);
            return false;
        }
        logger().info("DELETE /posts/{} response: {}", id, rsp);
        return true;
    }

    public static void main(String... args) {
        HashMap<String, String> config = new HashMap<>();
        config.put("url", "http://jsonplaceholder.typicode.com/posts/1");
        config.put("httpVersion", "HTTP_1_1");
        config.put("connectTimeoutMillis", "500");
        config.put("readTimeoutMillis", "2000");

        HttpUserTest user = new HttpUserTest(config);
        user.onStart();

        user.action1();
        user.action2();
        user.action3();
        user.action4();
        user.action5();

        out.println("Hello World");
    }

    @Override public Logger logger() {
        return logger;
    }
    
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(HttpUserTest.class);
}
