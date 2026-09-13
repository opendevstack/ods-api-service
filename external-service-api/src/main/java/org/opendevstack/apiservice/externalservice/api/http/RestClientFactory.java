package org.opendevstack.apiservice.externalservice.api.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Shared factory for building configured {@link RestClient} instances.
 *
 * <p>All external service modules share identical SSL-wiring logic: optionally load a custom
 * trust store, build an {@link SSLContext}, and inject it into a
 * {@link SimpleClientHttpRequestFactory}. This class centralises that logic so each module
 * only needs to supply its own {@link ExternalServiceSslProperties} and timeout values.
 *
 * <p>This is a pure utility — not a Spring bean. Call
 * {@link #build(RestClient.Builder, ExternalServiceSslProperties, int, int)} from each
 * module's {@code @Configuration} class or factory component.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @Bean
 * public RestClient aapRestClient(RestClient.Builder builder) {
 *     return RestClientFactory.build(builder, sslProperties,
 *             aapProperties.getConnectTimeout(),
 *             aapProperties.getReadTimeout());
 * }
 * }</pre>
 */
@Slf4j
public final class RestClientFactory {

    private RestClientFactory() {
        // utility class — no instances
    }

    /**
     * Build a {@link RestClient} with the given SSL configuration and timeouts.
     *
     * @param builder          Spring's prototype {@link RestClient.Builder} (inject via constructor
     *                         or {@code @Bean} parameter — Spring provides a fresh instance each time)
     * @param ssl              SSL properties for this external service
     * @param connectTimeoutMs TCP connection timeout in milliseconds
     * @param readTimeoutMs    Socket read timeout in milliseconds
     * @return Fully configured {@link RestClient}
     */
    public static RestClient build(
            RestClient.Builder builder,
            ExternalServiceSslProperties ssl,
            int connectTimeoutMs,
            int readTimeoutMs) {

        SimpleClientHttpRequestFactory requestFactory = buildRequestFactory(ssl, connectTimeoutMs, readTimeoutMs);
        return builder.requestFactory(requestFactory).build();
    }

    /**
     * Build a {@link RestTemplate} with the given SSL configuration and timeouts.
     *
     * <p>Use this overload for modules whose OpenAPI-generated {@code ApiClient} only accepts a
     * {@link RestTemplate} (projects-info-service, jira, bitbucket). All other modules should
     * prefer {@link #build(RestClient.Builder, ExternalServiceSslProperties, int, int)}.
     *
     * @param ssl              SSL properties for this external service
     * @param connectTimeoutMs TCP connection timeout in milliseconds
     * @param readTimeoutMs    Socket read timeout in milliseconds
     * @return Fully configured {@link RestTemplate}
     */
    public static RestTemplate buildRestTemplate(
            ExternalServiceSslProperties ssl,
            int connectTimeoutMs,
            int readTimeoutMs) {

        SimpleClientHttpRequestFactory requestFactory = buildRequestFactory(ssl, connectTimeoutMs, readTimeoutMs);
        return new RestTemplate(requestFactory);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static SimpleClientHttpRequestFactory buildRequestFactory(
            ExternalServiceSslProperties ssl,
            int connectTimeoutMs,
            int readTimeoutMs) {

        SimpleClientHttpRequestFactory factory = buildSslRequestFactory(ssl);
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }

    /**
     * Build a {@link SimpleClientHttpRequestFactory} wired with the appropriate
     * {@link SSLSocketFactory}. Falls back to the JVM default trust store if no custom
     * trust store path is configured or if loading the trust store fails.
     */
    private static SimpleClientHttpRequestFactory buildSslRequestFactory(ExternalServiceSslProperties ssl) {

        if (!StringUtils.hasText(ssl.getTrustStorePath())) {
            log.debug("No custom trust store configured — using JVM default trust store");
            return new SimpleClientHttpRequestFactory();
        }

        try {
            log.info("Loading custom trust store from: {}", ssl.getTrustStorePath());
            KeyStore trustStore = KeyStore.getInstance(ssl.getTrustStoreType());
            try (FileInputStream fis = new FileInputStream(ssl.getTrustStorePath())) {
                char[] password = StringUtils.hasText(ssl.getTrustStorePassword())
                        ? ssl.getTrustStorePassword().toCharArray()
                        : new char[0];
                trustStore.load(fis, password);
            }

            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod)
                        throws IOException {
                    if (connection instanceof HttpsURLConnection https) {
                        https.setSSLSocketFactory(sslSocketFactory);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to load custom trust store '{}' — falling back to JVM default: {}",
                    ssl.getTrustStorePath(), e.getMessage());
            return new SimpleClientHttpRequestFactory();
        }
    }
}
