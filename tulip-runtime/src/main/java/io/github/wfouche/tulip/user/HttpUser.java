package io.github.wfouche.tulip.user;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.jetbrains.annotations.NotNull;
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
  @NotNull public String http_GET(String uri, Object... uriVariables) {
    try {
      String rsp = restClient().get().uri(uri, uriVariables).retrieve().body(String.class);
      if (rsp != null && !rsp.isEmpty()) {
        return rsp;
      } else {
        return "";
      }
    } catch (RestClientException e) {
      e.printStackTrace();
      return "";
    }
  }

  @NotNull public String http_POST(String reqBodyJson, String uri, Object... uriVariables) {
    try {
      String rsp =
          restClient()
              .post()
              .uri(uri, uriVariables)
              .contentType(APPLICATION_JSON)
              .body(reqBodyJson)
              .retrieve()
              .body(String.class);
      if (rsp != null && !rsp.isEmpty()) {
        return rsp;
      } else {
        return "";
      }
    } catch (RestClientException e) {
      e.printStackTrace();
      return "";
    }
  }

  @NotNull public String http_PUT(String reqBodyJson, String uri, Object... uriVariables) {
    try {
      String rsp =
          restClient()
              .put()
              .uri(uri, uriVariables)
              .contentType(APPLICATION_JSON)
              .body(reqBodyJson)
              .retrieve()
              .body(String.class);
      if (rsp != null && !rsp.isEmpty()) {
        return rsp;
      } else {
        return "";
      }
    } catch (RestClientException e) {
      e.printStackTrace();
      return "";
    }
  }

  @NotNull public String http_PATCH(String reqBodyJson, String uri, Object... uriVariables) {
    try {
      String rsp =
          restClient()
              .patch()
              .uri(uri, uriVariables)
              .contentType(APPLICATION_JSON)
              .body(reqBodyJson)
              .retrieve()
              .body(String.class);
      if (rsp != null && !rsp.isEmpty()) {
        return rsp;
      } else {
        return "";
      }
    } catch (RestClientException e) {
      e.printStackTrace();
      return "";
    }
  }

  @NotNull public String http_DELETE(String uri, Object... uriVariables) {
    try {
      String rsp = restClient().delete().uri(uri, uriVariables).retrieve().body(String.class);
      if (rsp != null && !rsp.isEmpty()) {
        return rsp;
      } else {
        return "";
      }
    } catch (RestClientException e) {
      e.printStackTrace();
      return "";
    }
  }

  //    // Action 1: GET /posts/{id}
  //    public boolean action1() {
  //        int id = ThreadLocalRandom.current().nextInt(100)+1;
  //        return !http_GET("/posts/{id}", id).isEmpty();
  //    }

}
