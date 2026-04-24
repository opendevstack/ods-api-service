package org.opendevstack.apiservice.externalservice.uipath.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for UIPath service components.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(UiPathProperties.class)
@Slf4j
public class UiPathServiceConfig {

    private final UiPathProperties uiPathProperties;

    public UiPathServiceConfig(@org.springframework.beans.factory.annotation.Qualifier("uiPathOrchestratorProperties") UiPathProperties uiPathProperties) {
        this.uiPathProperties = uiPathProperties;
    }

    /**
     * Creates a RestTemplate bean for HTTP client operations with configurable SSL settings.
     * Uses a different bean name to avoid conflicts with other RestTemplate beans.
     *
     * @return RestTemplate instance with SSL configuration
     */
    @Bean(name = "uiPathRestTemplate")
    public RestTemplate uiPathRestTemplate() {
        log.info("UIPath SSL certificate verification is ENABLED");
        return createSecureRestTemplate();
    }

    private RestTemplate createSecureRestTemplate() {
        SslProperties ssl = uiPathProperties.getSsl();

        if (!StringUtils.hasText(ssl.getTrustStorePath())) {
            log.info("No custom trust store configured for UIPath, using JVM default trust store");
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(uiPathProperties.getTimeout());
            requestFactory.setReadTimeout(uiPathProperties.getTimeout());
            return new RestTemplate(requestFactory);
        }

        try {
            log.info("Loading custom trust store for UIPath from: {}", ssl.getTrustStorePath());
            KeyStore trustStore = KeyStore.getInstance(ssl.getTrustStoreType());
            try (FileInputStream fis = new FileInputStream(ssl.getTrustStorePath())) {
                char[] password = StringUtils.hasText(ssl.getTrustStorePassword())
                        ? ssl.getTrustStorePassword().toCharArray()
                        : new char[0];
                trustStore.load(fis, password);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    if (connection instanceof HttpsURLConnection httpsConnection) {
                        httpsConnection.setSSLSocketFactory(sslSocketFactory);
                    }
                    super.prepareConnection(connection, httpMethod);
                }
            };
            requestFactory.setConnectTimeout(uiPathProperties.getTimeout());
            requestFactory.setReadTimeout(uiPathProperties.getTimeout());
            return new RestTemplate(requestFactory);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to load custom trust store '{}' for UIPath, falling back to JVM default: {}",
                    ssl.getTrustStorePath(), e.getMessage());
            SimpleClientHttpRequestFactory fallback = new SimpleClientHttpRequestFactory();
            fallback.setConnectTimeout(uiPathProperties.getTimeout());
            fallback.setReadTimeout(uiPathProperties.getTimeout());
            return new RestTemplate(fallback);
        }
    }
}
