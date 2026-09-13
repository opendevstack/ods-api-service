package org.opendevstack.apiservice.externalservice.aap.config;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.api.http.RestClientFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for the Ansible Automation Platform external service.
 *
 * <p>SSL wiring is delegated to {@link RestClientFactory} in {@code external-service-api};
 * no SSL boilerplate lives here.
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(SslProperties.class)
@Slf4j
public class ExternalServiceConfig {

    private final SslProperties sslProperties;

    @Value("${automation.platform.ansible.timeout:30000}")
    private int timeoutMs;

    public ExternalServiceConfig(@Qualifier("aapSslProperties") SslProperties sslProperties) {
        this.sslProperties = sslProperties;
    }

    /**
     * {@link RestClient} bean for the Ansible Automation Platform.
     *
     * <p>SSL and timeouts are configured via {@code automation.platform.ansible.ssl.*}
     * and {@code automation.platform.ansible.timeout} properties respectively.
     *
     * @param builder Spring prototype builder (injected fresh per bean definition)
     * @return configured {@link RestClient}
     */
    @Bean
    public RestClient aapRestClient(RestClient.Builder builder) {
        log.info("Creating AAP RestClient (connect/read timeout={}ms)", timeoutMs);
        return RestClientFactory.build(builder, sslProperties, timeoutMs, timeoutMs);
    }
}
