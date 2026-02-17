package org.opendevstack.apiservice.projectplatform.facade.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.ProjectsInfoServiceException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSection;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.service.ProjectsInfoService;
import org.opendevstack.apiservice.projectplatform.exception.ProjectPlatformsException;
import org.opendevstack.apiservice.projectplatform.mapper.ProjectPlatformsMapper;
import org.opendevstack.apiservice.projectplatform.model.ProjectPlatforms;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectsFacadeImplTest {

    private ProjectsInfoService projectsInfoService;
    private ProjectPlatformsMapper mapper;

    private ProjectsFacadeImpl sut;

    @BeforeEach
    void setup() {
        projectsInfoService = mock(ProjectsInfoService.class);
        mapper = mock(ProjectPlatformsMapper.class);

        sut = new ProjectsFacadeImpl(projectsInfoService, mapper);

        // Reset security context before each test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProjectPlatforms_whenValidBearerToken_thenReturnMappedApiModel() throws Exception {
        //given
        String projectKey = "PROJ";
        String tokenValue = "id-token-123";

        prepareMocksForTokenExtraction(tokenValue);

        List<PlatformSection> sections = List.of();
        Platforms externalPlatforms = new Platforms(sections);
        when(projectsInfoService.getProjectPlatforms(projectKey, tokenValue)).thenReturn(externalPlatforms);

        ProjectPlatforms mapped = new ProjectPlatforms();
        when(mapper.toApiModel(externalPlatforms)).thenReturn(mapped);

        //when
        ProjectPlatforms result = sut.getProjectPlatforms(projectKey);

        //then
        verify(projectsInfoService).getProjectPlatforms(projectKey, tokenValue);
        verify(mapper).toApiModel(externalPlatforms);

        assertThat(result).isSameAs(mapped);
    }

    @Test
    void getProjectPlatforms_whenInfoServiceThrowsException_thenWrapInProjectPlatformsException() throws Exception {
        //given
        String projectKey = "PROJ";
        String tokenValue = "id-token-123";

        prepareMocksForTokenExtraction(tokenValue);

        when(projectsInfoService.getProjectPlatforms(projectKey, tokenValue))
                .thenThrow(new ProjectsInfoServiceException("boom"));

        //when/then
        assertThatThrownBy(() -> sut.getProjectPlatforms(projectKey))
                .isInstanceOf(ProjectPlatformsException.class)
                .hasMessageContaining("Failed to retrieve project platforms");
    }

    @Test
    void getIdToken_whenNoBearerAuthentication_thenReturnInvalidToken() {
        //given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "pwd")
        );

        //when
        String token = sut.getIdToken();

        //then
        assertThat(token).isEqualTo("INVALID token");
    }

    private void prepareMocksForTokenExtraction(String tokenValue) {
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        BearerTokenAuthentication  authentication = mock(BearerTokenAuthentication.class);

        when(authentication.getToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(tokenValue);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}