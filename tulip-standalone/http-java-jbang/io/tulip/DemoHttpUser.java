package io.tulip;

import io.github.wfouche.tulip.user.HttpUser;
import java.util.concurrent.ThreadLocalRandom;

public class DemoHttpUser extends HttpUser {

    public DemoHttpUser(int userId, int threadId) {
        super(userId, threadId);
    }

    // Action 1: GET /posts/{id}
    public boolean action1() {
        int id = ThreadLocalRandom.current().nextInt(100)+1;
        return http_GET("/posts/{id}", id);
    }

    // Action 2: GET /comments/{id}
    public boolean action2() {
        int id = ThreadLocalRandom.current().nextInt(500)+1;
        return http_GET("/comments/{id}", id);
    }

    // Action 3: GET /todos/{id}
    public boolean action3() {
        int id = ThreadLocalRandom.current().nextInt(200)+1;
        return http_GET("/todos/{id}", id);
    }

}    