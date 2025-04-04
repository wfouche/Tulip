package io.github.wfouche.tulip.user;

import io.github.wfouche.tulip.api.*;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

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
    if (getUserId() == 0) {
      var url_ = getUserParamValue("url");
      var connectTimeout_ = getUserParamValue("connectTimeoutMillis");
      var connectionRequestTimeout_ = getUserParamValue("connectionRequestTimeout");
      var readTimeout_ = getUserParamValue("readTimeoutMillis");

      if (url_.isEmpty()) {
        return false;
      }

      try {
        URL url = new URI(url_).toURL();
        urlProtocol = url.getProtocol();
        urlHost = url.getHost();
        urlPort = url.getPort();
        urlPath = url.getPath();
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }

      String baseUrl = urlProtocol + "://" + urlHost;
      if (urlPort != -1) {
        baseUrl += ":" + urlPort;
      }

      // if (urlProtocol.equals("http")) {
      // var factory = new HttpComponentsClientHttpRequestFactory();
      var factory = new SimpleClientHttpRequestFactory();

      if (!connectTimeout_.isEmpty()) {
        factory.setConnectTimeout(Integer.parseInt(connectTimeout_));
        logger.info("connectTimeoutMillis={}", connectTimeout_);
      }

      if (!readTimeout_.isEmpty()) {
        factory.setReadTimeout(Integer.parseInt(readTimeout_));
        logger.info("readTimeoutMillis={}", readTimeout_);
      }

      logger.info("baseUrl={}", baseUrl);
      client = RestClient.builder().requestFactory(factory).baseUrl(baseUrl).build();
      //      } else {
      //        HttpComponentsClientHttpRequestFactory factory =
      //            new HttpComponentsClientHttpRequestFactory();
      //
      //        if (!connectTimeout_.isEmpty()) {
      //          factory.setConnectTimeout(Integer.parseInt(connectTimeout_));
      //          logger.info("connectTimeoutMillis={}", connectTimeout_);
      //        }
      //
      //        if (!connectionRequestTimeout_.isEmpty()) {
      //          factory.setConnectionRequestTimeout(Integer.parseInt(connectionRequestTimeout_));
      //          logger.info("connectionRequestTimeout_={}", connectionRequestTimeout_);
      //        }
      //        if (!readTimeout_.isEmpty()) {
      //          factory.setReadTimeout(Integer.parseInt(readTimeout_));
      //          logger.info("readTimeoutMillis={}", readTimeout_);
      //        }
      //
      //        try {
      //          factory.setHttpClient(httpClient());
      //        } catch (NoSuchAlgorithmException e) {
      //          System.out.println(e.toString());
      //          return false;
      //
      //        } catch (KeyManagementException e) {
      //          System.out.println(e.toString());
      //          return false;
      //        } catch (KeyStoreException e) {
      //          System.out.println(e.toString());
      //          return false;
      //        }
      //
      //        logger.info("baseUrl={}", baseUrl);
      //        client = RestClient.builder().requestFactory(factory).baseUrl(baseUrl).build();
      //      }
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

  // https://github.com/kvsravindrareddy/springboot-virtual-threads/blob/main/src/main/java/com/veera/config/HttpConfig.java
  // root@wfouche-e6540:/home/wfouche/IdeaProjects/Tulip/scripts/ssl#  jbang run JavalinServer.java
  //  public HttpClient httpClient()
  //      throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
  //    SSLContext sslContext =
  //        SSLContextBuilder.create()
  //            .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true)
  //            .build();
  //
  //    DefaultClientTlsStrategy tlsStrategy =
  //        new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
  //
  //    HttpClientConnectionManager connectionManager =
  //        PoolingHttpClientConnectionManagerBuilder.create()
  //            .setTlsSocketStrategy(tlsStrategy)
  //            .build();
  //
  //    return HttpClients.custom().setConnectionManager(connectionManager).build();
  //  }
  //
  //  public static class NullTrustManager implements X509TrustManager {
  //
  //    @Override
  //    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
  //        throws CertificateException {}
  //
  //    @Override
  //    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
  //        throws CertificateException {}
  //
  //    @Override
  //    public X509Certificate[] getAcceptedIssuers() {
  //      return new X509Certificate[0];
  //    }
  //  }
  //
  //  public static class NullHostnameVerifier implements javax.net.ssl.HostnameVerifier {
  //    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
  //      return true;
  //    }
  //  }

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

  private static String urlProtocol = "";
  private static String urlHost = "";
  private static int urlPort = -1;
  private static String urlPath = "";

  public String getUrlProtocol() {
    return urlProtocol;
  }

  public String getUrlHost() {
    return urlHost;
  }

  public int getUrlPort() {
    return urlPort;
  }

  public String getUrlPath() {
    return urlPath;
  }

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
