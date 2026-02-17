package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class ProjectsInfoServiceImpl implements ProjectsInfoService {

    @Qualifier("projectsInfoServiceRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${externalservices.projects-info-service.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProjectsInfoServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Platforms getProjectPlatforms(String projectKey) throws ProjectsInfoServiceException {
        return null;
    }

    @Override
    public boolean validateConnection() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = baseUrl + "/actuator/health";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});

            boolean isValid = response.getStatusCode().is2xxSuccessful();
            log.debug("Connection validation: {}", isValid ? "successful" : "failed");
            return isValid;

        } catch (Exception e) {
            log.warn("Connection validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Use validateConnection for health checks, but don't log warnings on failure
            // as health checks are frequent and failures are expected to be handled by the health indicator
            return validateConnection();
        } catch (Exception e) {
            log.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
