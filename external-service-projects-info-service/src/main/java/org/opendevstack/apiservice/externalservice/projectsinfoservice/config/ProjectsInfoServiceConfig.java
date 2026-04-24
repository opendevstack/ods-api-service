package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.ApiClient;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.api.ProjectsApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
@EnableConfigurationProperties(ProjectsInfoServiceSslProperties.class)
@Slf4j
public class ProjectsInfoServiceConfig {

    @Value("${externalservices.projects-info-service.base-url:http://localhost:8080}")
    private String baseUrl;

    private final ProjectsInfoServiceSslProperties sslProperties;

    public ProjectsInfoServiceConfig(ProjectsInfoServiceSslProperties sslProperties) {
        this.sslProperties = sslProperties;
    }

    /**
     * Creates a RestTemplate bean for HTTP client operations with configurable SSL settings.
     *
     * @return RestTemplate instance with SSL configuration
     */
    @Bean
    public RestTemplate projectsInfoServiceRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        log.info("SSL certificate verification is ENABLED");
        return createSecureRestTemplate();
    }

    @Bean
    public ApiClient apiClient(RestTemplate restTemplate) {
        return new ApiClient(restTemplate);
    }

    @Qualifier("ProjectsInfoServiceApiClient")
    @Bean
    public ProjectsApi projectsApi(ApiClient apiClient) {
        return new ProjectsApi(apiClient);
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

            return new RestTemplate(requestFactory);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to load custom trust store '{}', falling back to JVM default: {}",
                    sslProperties.getTrustStorePath(), e.getMessage());
            return new RestTemplate();
        }
    }
}
