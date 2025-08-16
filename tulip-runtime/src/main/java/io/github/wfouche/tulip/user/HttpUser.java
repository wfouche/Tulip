package io.github.wfouche.tulip.user;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.HashMap;
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
     * HttpUser() constructor
     *
     * @param config - test config
     */
    public HttpUser(HashMap<String, String> config) {
        super(0, 0);
        this.config = config;
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

    /**
     * getUserParamValue() method
     *
     * @param paramName - key to return value for from config
     */
    @NotNull
    public String getUserParamValue(@NotNull String paramName) {
        if (config == null) {
            return super.getUserParamValue(paramName);
        }
        String value = config.get(paramName);
        if (value == null) {
            return "";
        }
        return value;
    }

    private HashMap<String, String> config = null;
}
