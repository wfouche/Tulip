package io.github.wfouche.tulip.user;

import org.springframework.web.client.RestClientException;

/** The HttpUser class. */
public class HttpUser extends HttpUser_RestClient {

  /**
   * HttpUser() constructor
   *
   * @param userId - User object id
   * @param threadId - Worker thread id
   */
  public HttpUser(int userId, int threadId) {
    super(userId, threadId);
  }

  /**
   * http_GET() method
   *
   * @param uri - uri to invoke
   * @param uriVariables - sequence of variables to replace in uri
   * @return boolean
   */
  public boolean http_GET(String uri, Object... uriVariables) {
    boolean rc;
    try {
      String rsp = restClient().get().uri(uri, uriVariables).retrieve().body(String.class);
      rc = (rsp != null && !rsp.isEmpty());
    } catch (RestClientException e) {
      rc = false;
    }
    return rc;
  }

  //    // Action 1: GET /posts/{id}
  //    public boolean action1() {
  //        int id = ThreadLocalRandom.current().nextInt(100)+1;
  //        return http_GET("/posts/{id}", id);
  //    }

}
