///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.apache.httpcomponents.client5:httpclient5:5.4.4
//DEPS org.springframework.boot:spring-boot-starter-web:3.4.5

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.time.Duration;

public class RestClientExample {

    public static RestTemplate restTemplateWithKeystoreAndTruststore(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) throws Exception {
        //KeyStore keyStore = KeyStore.getInstance("PKCS12");
        //try (FileInputStream is = new FileInputStream(new File(keystorePath))) {
        //    keyStore.load(is, keystorePassword.toCharArray());
        //}

        //KeyStore trustStore = KeyStore.getInstance("JKS");
        //try (FileInputStream is = new FileInputStream(new File(truststorePath))) {
        //    trustStore.load(is, truststorePassword.toCharArray());
        //}

        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(new File(keystorePath), keystorePassword.toCharArray(), keystorePassword.toCharArray())
                .loadTrustMaterial(new File(truststorePath), truststorePassword.toCharArray())
                .build();

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(new org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager(socketFactoryRegistry))
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectTimeout(Timeout.of(Duration.ofSeconds(10)))
                        .setResponseTimeout(Timeout.of(Duration.ofSeconds(10)))
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    public static RestTemplate restTemplateAcceptingAllCertificates() throws Exception {
         SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true)
                .build();

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(new org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager(socketFactoryRegistry))
                 .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectTimeout(Timeout.of(Duration.ofSeconds(10)))
                        .setResponseTimeout(Timeout.of(Duration.ofSeconds(10)))
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    public static void main(String[] args) throws Exception {
        // Example Usage with Keystore and Truststore
        RestTemplate restTemplateWithKeys = restTemplateWithKeystoreAndTruststore(
                "/path/to/your/keystore.p12",
                "keystorePassword",
                "/path/to/your/truststore.jks",
                "truststorePassword"
        );
        String responseWithKeys = restTemplateWithKeys.getForObject("https://your-secured-endpoint", String.class);
        System.out.println("Response with keys: " + responseWithKeys);

       // Example Usage accepting all certificates (for testing purposes only)
        RestTemplate restTemplateTrustAll = restTemplateAcceptingAllCertificates();
        String responseTrustAll = restTemplateTrustAll.getForObject("https://your-secured-endpoint", String.class);
        System.out.println("Response trusting all: " + responseTrustAll);

    }
}
