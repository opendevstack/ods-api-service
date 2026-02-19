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
import org.springframework.security.core.context.SecurityContextHolder;

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
    void getProjectPlatforms_whenGetProjectPlatforms_thenReturnMappedApiModel() throws Exception {
        //given
        String projectKey = "PROJ";

        List<PlatformSection> sections = List.of();
        Platforms externalPlatforms = new Platforms(sections);
        when(projectsInfoService.getProjectPlatforms(projectKey)).thenReturn(externalPlatforms);

        ProjectPlatforms mapped = new ProjectPlatforms();
        when(mapper.toApiModel(externalPlatforms)).thenReturn(mapped);

        //when
        ProjectPlatforms result = sut.getProjectPlatforms(projectKey);

        //then
        verify(projectsInfoService).getProjectPlatforms(projectKey);
        verify(mapper).toApiModel(externalPlatforms);

        assertThat(result).isSameAs(mapped);
    }

    @Test
    void getProjectPlatforms_whenInfoServiceThrowsException_thenWrapInProjectPlatformsException() throws Exception {
        //given
        String projectKey = "PROJ";

        when(projectsInfoService.getProjectPlatforms(projectKey))
                .thenThrow(new ProjectsInfoServiceException("boom"));

        //when/then
        assertThatThrownBy(() -> sut.getProjectPlatforms(projectKey))
                .isInstanceOf(ProjectPlatformsException.class)
                .hasMessageContaining("Failed to retrieve project platforms");
    }

}