import io.github.wfouche.tulip.api.*;

import static io.restassured.RestAssured.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUser extends TulipUser {

    public HttpUser(int userId, int threadId) {
        super(userId, threadId);
    }

    public boolean onStart() {
        // Initialize RestAssured only once
        if (getUserId() == 0) {
            baseURI = baseURI = getUserParamValue("baseURI");
        }
        return true;
    }

    // Action 1: delay 10ms
    public boolean action1() {
        try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
        return true;
    }

    // Action 2: delay 20ms
    public boolean action2() {
        try { Thread.sleep(20); } catch (InterruptedException e) { e.printStackTrace(); }
        return true;
    }

    // Action 3: No-op
    public boolean action3() {
        return true;
    }

    // Action 4: GET /posts/{id} using Rest-Assured
    public boolean action4() {
        boolean rc = true;
        try {
            given()
            .when()
                .get("/posts/" + Integer.toString(getUserId()+1))
            .then()
                .statusCode(200);
        } catch (java.lang.AssertionError e) {
           rc = false;
        }
        return rc;
    }

    // Action 5: GET /posts/{id} using java.net.http.HttpClient
    public boolean action5() {
        boolean rc = true;
        try {
            var response = client.send(httpRequestPosts, HttpResponse.BodyHandlers.ofString());
            rc =  (response.statusCode() == 200);
        } catch (java.lang.Exception e) {
            rc = false;
        }
        return rc;
    }

    public boolean onStop() {
        return true;
    }

    // Action 5 support data and methods
    private static HttpClient client = HttpClient.newHttpClient();

    private HttpRequest httpRequestPosts = createHttpRequest("posts");

    private HttpRequest createHttpRequest(String name) {
        try {
            var id = getUserId() + 1;
            var url = this.getUserParamValue("baseURI");
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url + "/" + name + "/" + String.valueOf(id)))
                    .GET()
                    .build();
            return request;
        } catch (java.lang.Exception e) {
            throw new RuntimeException(e);
        }

    }

}
