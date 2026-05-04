package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.http.RestClientFactory;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.ApiClient;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.api.ProjectsApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for the Projects Info Service external service.
 *
 * <p>SSL wiring is delegated to {@link RestClientFactory} in {@code external-service-api}.
 * A {@link RestTemplate} is produced (not {@code RestClient}) because the OpenAPI-generated
 * {@link ApiClient} only accepts {@code RestTemplate}.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(ProjectsInfoServiceSslProperties.class)
@Slf4j
public class ProjectsInfoServiceConfig {

    @Value("${externalservices.projects-info-service.connect-timeout:30000}")
    private int connectTimeoutMs;

    @Value("${externalservices.projects-info-service.read-timeout:30000}")
    private int readTimeoutMs;

    private final ProjectsInfoServiceSslProperties sslProperties;

    public ProjectsInfoServiceConfig(ProjectsInfoServiceSslProperties sslProperties) {
        this.sslProperties = sslProperties;
    }

    /**
     * {@link RestTemplate} used by the OpenAPI-generated {@link ApiClient}.
     */
    @Bean
    public RestTemplate projectsInfoServiceRestTemplate() {
        log.info("Creating ProjectsInfoService RestTemplate (connect={}ms, read={}ms)",
                connectTimeoutMs, readTimeoutMs);
        return RestClientFactory.buildRestTemplate(sslProperties, connectTimeoutMs, readTimeoutMs);
    }

    @Bean
    public ApiClient apiClient(RestTemplate projectsInfoServiceRestTemplate) {
        return new ApiClient(projectsInfoServiceRestTemplate);
    }

    @Qualifier("ProjectsInfoServiceApiClient")
    @Bean
    public ProjectsApi projectsApi(ApiClient apiClient) {
        return new ProjectsApi(apiClient);
    }
}
