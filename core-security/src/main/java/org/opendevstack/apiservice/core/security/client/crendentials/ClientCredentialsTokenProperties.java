package org.opendevstack.apiservice.core.security.client.crendentials;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.client-credentials")
@Data
public class ClientCredentialsTokenProperties {

    private String tokenUrl;

    private String clientId;

    private String clientSecret;
}
