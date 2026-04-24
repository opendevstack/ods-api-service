package org.opendevstack.apiservice.externalservice.aap.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Configuration class for external service components.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(SslProperties.class)
@Slf4j
public class ExternalServiceConfig {

    private final SslProperties sslProperties;

    public ExternalServiceConfig(@Qualifier("aapSslProperties") SslProperties sslProperties) {
        this.sslProperties = sslProperties;
    }

    /**
     * Creates a RestTemplate bean for HTTP client operations with configurable SSL settings.
     *
     * @return RestTemplate instance with SSL configuration
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        log.info("SSL certificate verification is ENABLED");
        return createSecureRestTemplate();
    }

    private RestTemplate createSecureRestTemplate() {
        if (!StringUtils.hasText(sslProperties.getTrustStorePath())) {
            log.info("No custom trust store configured, using JVM default trust store");
            return new RestTemplate();
        }

        try {
            log.info("Loading custom trust store from: {}", sslProperties.getTrustStorePath());
            KeyStore trustStore = KeyStore.getInstance(sslProperties.getTrustStoreType());
            try (FileInputStream fis = new FileInputStream(sslProperties.getTrustStorePath())) {
                char[] password = StringUtils.hasText(sslProperties.getTrustStorePassword())
                        ? sslProperties.getTrustStorePassword().toCharArray()
                        : new char[0];
                trustStore.load(fis, password);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    if (connection instanceof HttpsURLConnection httpsConnection) {
                        httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };

            return new RestTemplate(requestFactory);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to load custom trust store '{}', falling back to JVM default: {}",
                    sslProperties.getTrustStorePath(), e.getMessage());
            return new RestTemplate();
        }
    }
}
