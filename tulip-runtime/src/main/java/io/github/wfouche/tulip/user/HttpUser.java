package io.github.wfouche.tulip.user;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/** The HttpUser class. */
public class HttpUser extends HttpUser_RestClient {

    /**
     * initConfig() method
     *
     * @param config - test config
     */
    public void initConfig(HashMap<String, String> config) {
        this.config = config;
    }

    /**
     * Response record
     *
     * @param statusCode - HTTP status code
     * @param body - response body
     */
    public record Response(int statusCode, String body) {

        /**
         * isSuccessful() method
         *
         * @return True if status code is 2xx
         */
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
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
    public Response get(String uri, Object... uriVariables) throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .get()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getBody());
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
    public Response post(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .post()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .contentType(APPLICATION_JSON)
                        .body(reqBodyJson)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getBody());
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
    public Response put(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .put()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .contentType(APPLICATION_JSON)
                        .body(reqBodyJson)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getBody());
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
    public Response patch(String reqBodyJson, String uri, Object... uriVariables)
            throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .patch()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .contentType(APPLICATION_JSON)
                        .body(reqBodyJson)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getBody());
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
    public Response delete(String uri, Object... uriVariables) throws RestClientException {
        ResponseEntity<String> entity =
                restClient()
                        .delete()
                        .uri(uri, uriVariables)
                        .header(http_header_key, http_header_val)
                        .retrieve()
                        .toEntity(String.class);
        return new Response(entity.getStatusCode().value(), entity.getBody());
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
