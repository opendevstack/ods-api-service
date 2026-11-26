package org.opendevstack.apiservice.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@ConfigurationProperties(prefix = "apis.projects.provisioned-components")
@Validated
public record ProjectComponentsValues (
        Set<String> allowedValues,
        Set<String> mandatoryValues
) {
    public String allowedValuesCommaSeparated() {
        if (allowedValues == null) {
            return null;
        }
        return String.join(", ", allowedValues);
    }
}
