package io.github.wfouche.tulip.user;

import io.github.wfouche.tulip.api.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
    var protocol_ = getUserParamValue("protocol");
    var url = protocol_ + "://" + getUserParamValue("host");
    var connectTimeout_ = getUserParamValue("connectTimeoutMillis");
    var connectionRequestTimeout_ = getUserParamValue("connectionRequestTimeout");
    var readTimeout_ = getUserParamValue("readTimeoutMillis");

    if (getUserId() == 0) {
      if (protocol_.equals("http")) {
        // var factory = new HttpComponentsClientHttpRequestFactory();
        var factory = new SimpleClientHttpRequestFactory();

        if (!connectTimeout_.isEmpty()) {
          factory.setConnectTimeout(Integer.parseInt(connectTimeout_));
          logger.info("http:connectTimeoutMillis={}", connectTimeout_);
        }

        if (!readTimeout_.isEmpty()) {
          factory.setReadTimeout(Integer.parseInt(readTimeout_));
          logger.info("http:readTimeoutMillis={}", readTimeout_);
        }

        logger.info("http:url={}", url);
        client = RestClient.builder().requestFactory(factory).baseUrl(url).build();
      } else {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();

        if (!connectTimeout_.isEmpty()) {
          factory.setConnectTimeout(Integer.parseInt(connectTimeout_));
          logger.info("https:connectTimeoutMillis={}", connectTimeout_);
        }

        if (!connectionRequestTimeout_.isEmpty()) {
          factory.setConnectionRequestTimeout(Integer.parseInt(connectionRequestTimeout_));
          logger.info("https:connectionRequestTimeout_={}", connectionRequestTimeout_);
        }
        if (!readTimeout_.isEmpty()) {
          factory.setReadTimeout(Integer.parseInt(readTimeout_));
          logger.info("https:readTimeoutMillis={}", readTimeout_);
        }

        try {
          factory.setHttpClient(httpClient());
        } catch (NoSuchAlgorithmException e) {
          System.out.println(e.toString());
          return false;

        } catch (KeyManagementException e) {
          System.out.println(e.toString());
          return false;
        } catch (KeyStoreException e) {
          System.out.println(e.toString());
          return false;
        }

        logger.info("https:url={}", url);
        client = RestClient.builder().requestFactory(factory).baseUrl(url).build();
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

  // https://github.com/kvsravindrareddy/springboot-virtual-threads/blob/main/src/main/java/com/veera/config/HttpConfig.java
  // root@wfouche-e6540:/home/wfouche/IdeaProjects/Tulip/scripts/ssl#  jbang run JavalinServer.java
  public HttpClient httpClient()
      throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
    SSLContext sslContext =
        SSLContextBuilder.create()
            .loadTrustMaterial(
                null, (TrustStrategy) (chain, authType) -> true)
            .build();

    DefaultClientTlsStrategy tlsStrategy =
        new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);

    HttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setTlsSocketStrategy(tlsStrategy)
            .build();

    return HttpClients.custom().setConnectionManager(connectionManager).build();
  }

  public static class NullTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
        throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
        throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  public static class NullHostnameVerifier implements javax.net.ssl.HostnameVerifier {
    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
      return true;
    }
  }

  /**
   * restClient() method
   *
   * @return RestClient
   */
  public RestClient restClient() {
    return client;
  }

  // RestClient object
  private static RestClient client;

  /**
   * logger() method
   *
   * @return Logger
   */
  public Logger logger() {
    return logger;
  }

  // Logger
  private static final Logger logger = LoggerFactory.getLogger(HttpUser_RestClient.class);
}
