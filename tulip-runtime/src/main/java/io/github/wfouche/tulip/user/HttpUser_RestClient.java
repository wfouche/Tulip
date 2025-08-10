package io.github.wfouche.tulip.user;

import io.github.wfouche.tulip.api.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class HttpRecord {
    public RestClient client = null;
    public String url = "";
    public String urlProtocol = "";
    public String urlHost = "";
    public int urlPort = -1;
    public String urlPath = "";
}

/** The HttpUser class. */
public class HttpUser_RestClient extends TulipUser {

    /**
     * HttpUser() constructor
     *
     * @param userId - User object id
     * @param threadId - Worker thread id
     */
    public HttpUser_RestClient(int userId, int threadId) {
        super(userId, threadId);
    }

    /**
     * onStart() method
     *
     * @return boolean
     */
    public boolean onStart() {
        // Initialize the shared RestClient object only once
        if (getUserId() != 0) {
            return true;
        }

        var url_ = getUserParamValue("url");

        var connectTimeout_ = getUserParamValue("connectTimeoutMillis");
        var readTimeout_ = getUserParamValue("readTimeoutMillis");
        var httpVersion_ = getUserParamValue("httpVersion").toUpperCase();

        if (url_.isEmpty()) {
            getLogger().error("\"url\" property is empty");
            return false;
        }

        String[] urls = url_.split(",");
        https = new HttpRecord[urls.length];
        int idx = 0;
        for (String url : urls) {
            https[idx] = new HttpRecord();
            https[idx].client =
                    createRestClient(idx, url.trim(), connectTimeout_, readTimeout_, httpVersion_);
            idx += 1;
        }

        return true;
    }

    /**
     * @param url_
     * @param connectTimeout_
     * @param readTimeout_
     * @param httpVersion_
     * @return
     */
    RestClient createRestClient(
            int idx,
            String url_,
            String connectTimeout_,
            String readTimeout_,
            String httpVersion_) {
        RestClient client = null;

        https[idx].url = url_;
        try {
            URL url = new URI(url_).toURL();
            https[idx].urlProtocol = url.getProtocol();
            https[idx].urlHost = url.getHost();
            https[idx].urlPort = url.getPort();
            https[idx].urlPath = url.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String baseUrl = https[idx].urlProtocol + "://" + https[idx].urlHost;
        if (https[idx].urlPort != -1) {
            baseUrl += ":" + https[idx].urlPort;
        }
        getLogger().info("[{}]baseUrl={}", idx, baseUrl);

        if (httpVersion_.isEmpty()) {
            httpVersion_ = "*";
        }
        getLogger().info("[{}]httpVersion={}", idx, httpVersion_);

        // HTTP 1.1 or HTTP/2
        HttpClient httpClient = null;
        if (httpVersion_.equals("HTTP_1_1")) {
            // HTTP 1.1
            if (!connectTimeout_.isEmpty()) {
                getLogger().info("[{}]connectTimeoutMillis={}", idx, connectTimeout_);
                httpClient =
                        HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_1_1)
                                .connectTimeout(
                                        Duration.ofMillis(Integer.parseInt(connectTimeout_)))
                                .build();
            } else {
                httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            }
        } else if (httpVersion_.equals("HTTP_2")) {
            // HTTP/2
            if (!connectTimeout_.isEmpty()) {
                getLogger().info("[{}]connectTimeoutMillis={}", idx, connectTimeout_);
                httpClient =
                        HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_2)
                                .connectTimeout(
                                        Duration.ofMillis(Integer.parseInt(connectTimeout_)))
                                .build();
            } else {
                httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
            }
        } else {
            var factory = new SimpleClientHttpRequestFactory();

            if (!connectTimeout_.isEmpty()) {
                factory.setConnectTimeout(Integer.parseInt(connectTimeout_));
                getLogger().info("[{}]connectTimeoutMillis={}", idx, connectTimeout_);
            }

            if (!readTimeout_.isEmpty()) {
                factory.setReadTimeout(Integer.parseInt(readTimeout_));
                getLogger().info("[{}]readTimeoutMillis={}", idx, readTimeout_);
            }
            client = RestClient.builder().requestFactory(factory).baseUrl(baseUrl).build();
        }
        if (client == null) {
            var factory = new JdkClientHttpRequestFactory(httpClient);
            if (!readTimeout_.isEmpty()) {
                factory.setReadTimeout(Integer.parseInt(readTimeout_));
                getLogger().info("[{}]readTimeoutMillis={}", idx, readTimeout_);
            }
            client = RestClient.builder().requestFactory(factory).baseUrl(baseUrl).build();
        }
        return client;
    }

    /**
     * onStop() method
     *
     * @return boolean
     */
    public boolean onStop() {
        return true;
    }

    /**
     * restClient() method
     *
     * @return RestClient
     */
    public RestClient restClient() {
        return https[getUserId() % https.length].client;
    }

    // RestClient objects
    private static HttpRecord[] https = null;

    /**
     * getUrlProtocol() method
     *
     * @return String
     */
    public String getUrlProtocol() {
        return https[getUserId() % https.length].urlProtocol;
    }

    /**
     * getUrlHost() method
     *
     * @return String
     */
    public String getUrlHost() {
        return https[getUserId() % https.length].urlHost;
    }

    /**
     * getUrlPort() method
     *
     * @return int
     */
    public int getUrlPort() {
        return https[getUserId() % https.length].urlPort;
    }

    /**
     * getUrlPath() method
     *
     * @return String
     */
    public String getUrlPath() {
        return https[getUserId() % https.length].urlPath;
    }
}
