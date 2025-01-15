import io.github.wfouche.tulip.api.*;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class HttpUser extends TulipUser {

    public HttpUser(int userId, int threadId) {
        super(userId, threadId);
    }

    public boolean onStart() {
        // Initialize the shared restClient instance only once
        if (getUserId() == 0) {
            restClient = RestClient.builder()
                            .baseUrl(getUserParamValue("baseURI"))
                            .build();
        }
        return true;
    }

    // Action 1: GET /posts/{id}
    public boolean action1() {

        boolean rc = true;
        try {
            String response = restClient.get()
              .uri("/posts/{id}", getUserId()+1)
              .retrieve()
              .body(String.class);
            //System.out.println(response);
        } catch (RestClientException e) {
           rc = false;
        }
        return rc;        
    }

    // Action 2: GET /comments/{id}
    public boolean action2() {

        boolean rc = true;
        try {
            String response = restClient.get()
                .uri("/comments/{id}", getUserId()+1)
                .retrieve()
                .body(String.class);
            //System.out.println(response);
        } catch (RestClientException e) {
            rc = false;
        }
        return rc;        
    }

    // Action 3: GET /todos/{id}
    public boolean action3() {

        boolean rc = true;
        try {
            String response = restClient.get()
                .uri("/todos/{id}", getUserId()+1)
                .retrieve()
                .body(String.class);
            //System.out.println(response);
        } catch (RestClientException e) {
            rc = false;
        }
        return rc;        
    }

    public boolean onStop() {
        return true;
    }

    // Action 1 support data and methods
    private static RestClient restClient;

}
