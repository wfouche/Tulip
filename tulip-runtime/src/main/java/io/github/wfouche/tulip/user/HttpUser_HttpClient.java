package io.github.wfouche.tulip.user;

import io.github.wfouche.tulip.api.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The HttpClientUser class. */
public class HttpUser_HttpClient extends TulipUser {

  /**
   * HttpClientUser() constructor
   *
   * @param userId - User object id
   * @param threadId - Worker thread id
   */
  public HttpUser_HttpClient(int userId, int threadId) {
    super(userId, threadId);
  }

  /**
   * onStart() method
   *
   * @return boolean
   */
  public boolean onStart() {
    // Initialize the shared HttpClient object only once
    if (getUserId() == 0) {
      //      // var factory = new HttpComponentsClientHttpRequestFactory();
      //      var factory = new SimpleClientHttpRequestFactory();
      //
      //      var connectTimeout_ = getUserParamValue("connectTimeoutMillis");
      //      if (!connectTimeout_.isEmpty()) {
      //        factory.setConnectTimeout(Integer.parseInt(connectTimeout_));
      //        logger.info("connectTimeoutMillis={}", connectTimeout_);
      //      }
      //
      //      var readTimeout_ = getUserParamValue("readTimeoutMillis");
      //      if (!readTimeout_.isEmpty()) {
      //        factory.setReadTimeout(Integer.parseInt(readTimeout_));
      //        logger.info("readTimeoutMillis={}", readTimeout_);
      //      }
      //
      //      var url = getUserParamValue("protocol") + "://" + getUserParamValue("host");
      //      logger.info("url={}", url);
      //
      //      client = RestClient.builder().requestFactory(factory).baseUrl(url).build();
      //
      var verify_ = getUserParamValue("verify");
      if (!verify_.isEmpty()) {
        var sslVerify = Boolean.parseBoolean(getUserParamValue("verify"));
        if (!sslVerify) {
          try {
            disableSSLValidation();
          } catch (Exception e) {
            logger.info("SSL/TLS verify disable failed");
            throw new RuntimeException(e);
          }
        }
      }
    }
    return true;
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
   * http_GET() method
   *
   * @param url - url to invoke
   * @return boolean
   */
  public HttpRequest http_GET(String url) {
    try {
      return HttpRequest.newBuilder().uri(new URI(url)).GET().build();
    } catch (java.lang.Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * serviceCall - invoke remote service call
   *
   * @param request - HTTP request object
   * @return
   */
  public boolean serviceCall(HttpRequest request) {
    try {
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());

      // println(id)
      // println(name)
      // println(response.statusCode())
      // println(response.body())

      return (response.statusCode() == 200);
    } catch (java.lang.Exception e) {
      throw new RuntimeException(e);
    }
  }

  //    // Action 1: GET /posts/{id}
  //    public boolean action1() {
  //        int id = ThreadLocalRandom.current().nextInt(100)+1;
  //        return http_GET("/posts/{id}", id);
  //    }

  /**
   * disableSSLValidation() method
   *
   * @throws Exception - error is SSL validation cannot be disabled
   */
  public void disableSSLValidation() throws Exception {
    final SSLContext sslContext = SSLContext.getInstance("TLS");

    sslContext.init(
        null,
        new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }
          }
        },
        null);

    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier(
        new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession session) {
            return true;
          }
        });
  }

  /**
   * restClient() method
   *
   * @return RestClient
   */
  public HttpClient httpClient() {
    return client;
  }

  // RestClient object
  private static final HttpClient client = HttpClient.newHttpClient();

  /**
   * logger() method
   *
   * @return Logger
   */
  public Logger logger() {
    return logger;
  }

  // Logger
  private static final Logger logger = LoggerFactory.getLogger(HttpUser_HttpClient.class);
}
