package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.ApiClient;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.api.ProjectsApi;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.auth.HttpBearerAuth;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.model.ProjectPlatforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSection;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.mapper.PlatformsMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectsInfoServiceImplTest {

    private ApiClient apiClient;
    private ProjectsApi projectsApi;
    private PlatformsMapper platformsMapper;

    private ProjectsInfoServiceImpl projectsInfoService;

    @BeforeEach
    void setup() {
        apiClient = mock(ApiClient.class);
        projectsApi = mock(ProjectsApi.class);
        platformsMapper = mock(PlatformsMapper.class);

        projectsInfoService = new ProjectsInfoServiceImpl(apiClient, projectsApi, platformsMapper);
    }

    @Test
    void configureApiClient_whenPostConstructCalled_thenBasePathIsConfigured() {
        //given
        String expectedBaseUrl = "http://localhost:8080";
        ReflectionTestUtils.setField(projectsInfoService, "baseUrl", expectedBaseUrl);

        //when
        projectsInfoService.configureApiClient();

        //then
        verify(apiClient).setBasePath(expectedBaseUrl);
    }

    @Test
    void getProjectPlatforms_whenCalled_thenPlatformsMapped() throws Exception {
        //given
        String projectKey = "PROJ";

        var auth = mock(HttpBearerAuth.class);
        when(apiClient.getAuthentication("bearerAuth")).thenReturn(auth);

        var apiResponse = new ProjectPlatforms();
        when(projectsApi.getProjectPlatforms(projectKey)).thenReturn(apiResponse);

        List<PlatformSection> sections = List.of();
        Platforms mapped = new Platforms(sections);
        when(platformsMapper.asPlatforms(apiResponse)).thenReturn(mapped);

        //when
        Platforms result = projectsInfoService.getProjectPlatforms(projectKey);

        //then
        verify(projectsApi).getProjectPlatforms(projectKey);
        verify(platformsMapper).asPlatforms(apiResponse);

        assertThat(result).isSameAs(mapped);
    }

    @Test
    void validateConnection_whenCalled_thenReturnTrue() {
        //given

        //when
        boolean result = projectsInfoService.validateConnection();

        //then
        assertThat(result).isTrue();
    }

    @Test
    void isHealthy_whenCalled_thenReturnTrue() {
        //given

        //when
        boolean result = projectsInfoService.isHealthy();

        //then
        assertThat(result).isTrue();
    }
}