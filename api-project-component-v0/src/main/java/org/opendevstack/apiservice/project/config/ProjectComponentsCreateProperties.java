package org.opendevstack.apiservice.project.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "apis.project-components.create")
public class ProjectComponentsCreateProperties {

    private List<String> reservedParams = new ArrayList<>(
            List.of("workflow", "ods_namespace", "component_type", "quickstarter_repo")
    );
}
