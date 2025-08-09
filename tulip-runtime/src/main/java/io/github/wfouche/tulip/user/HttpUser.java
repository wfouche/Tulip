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
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public String http_GET(String uri, Object... uriVariables) throws RestClientException {
        String rsp = restClient().get().uri(uri, uriVariables).retrieve().body(String.class);
        if (rsp != null && !rsp.isEmpty()) {
            return rsp;
        } else {
            return "";
        }
    }

    /**
     * http_POST() method
     *
     * @param reqBodyJson - JSON string
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public String http_POST(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
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
    }

    /**
     * http_PUT() method
     *
     * @param reqBodyJson - JSON string
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public String http_PUT(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
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
    }

    /**
     * http_PATCH() method
     *
     * @param reqBodyJson - JSON string
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public String http_PATCH(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
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
    }

    /**
     * http_DELETE() method
     *
     * @param uri - uri to invoke
     * @param uriVariables - sequence of variables to replace in uri
     * @return boolean
     * @throws RestClientException - Spring exception
     */
    @NotNull
    public String http_DELETE(String uri, Object... uriVariables) throws RestClientException {
        String rsp = restClient().delete().uri(uri, uriVariables).retrieve().body(String.class);
        if (rsp != null && !rsp.isEmpty()) {
            return rsp;
        } else {
            return "";
        }
    }
}
