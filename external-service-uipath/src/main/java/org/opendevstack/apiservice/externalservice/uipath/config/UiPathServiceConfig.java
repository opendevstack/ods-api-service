package org.opendevstack.apiservice.externalservice.uipath.config;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.http.RestClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for the UiPath Orchestrator external service.
 *
 * <p>SSL wiring is delegated to {@link RestClientFactory} in {@code external-service-api};
 * no SSL boilerplate lives here.
 */
@Configuration
@EnableAsync
@Slf4j
public class UiPathServiceConfig {

    private final UiPathProperties uiPathProperties;

    public UiPathServiceConfig(UiPathProperties uiPathProperties) {
        this.uiPathProperties = uiPathProperties;
    }

    /**
     * {@link RestClient} bean for UiPath Orchestrator.
     *
     * <p>SSL and timeouts are configured via {@code automation.platform.uipath.ssl.*}
     * and {@code automation.platform.uipath.timeout} properties respectively.
     *
     * @param builder Spring prototype builder (injected fresh per bean definition)
     * @return configured {@link RestClient}
     */
    @Bean(name = "uiPathRestClient")
    public RestClient uiPathRestClient(RestClient.Builder builder) {
        int timeout = uiPathProperties.getTimeout();
        log.info("Creating UiPath RestClient (connect/read timeout={}ms)", timeout);
        return RestClientFactory.build(builder, uiPathProperties.getSsl(), timeout, timeout);
    }
}
