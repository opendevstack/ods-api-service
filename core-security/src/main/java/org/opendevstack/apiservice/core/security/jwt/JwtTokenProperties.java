package org.opendevstack.apiservice.core.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Data
public class JwtTokenProperties {

    private String tokenUrl;

    private String clientId;

    private String clientSecret;
}
