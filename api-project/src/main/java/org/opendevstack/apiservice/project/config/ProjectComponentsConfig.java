package org.opendevstack.apiservice.project.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ProjectComponentsValues.class)
public class ProjectComponentsConfig {
}
