package org.opendevstack.apiservice.serviceproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.opendevstack.apiservice.serviceproject.mapper.ProjectResponseMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectServiceImplTest {

    @Mock
    private OpenshiftService openshiftService;

    @Mock
    private BitbucketService bitbucketService;

    @Mock
    private JiraService jiraService;

    @Mock
    private GenerateProjectKeyService generateProjectKeyService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectResponseMapper projectResponseMapper;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectService = new ProjectServiceImpl(
                openshiftService,
                bitbucketService,
                jiraService,
                generateProjectKeyService,
                projectRepository,
                projectResponseMapper
        );
    }

    @Test
    void get_project_returns_response_when_project_exists() {
        // GIVEN
        String projectKey = "MY-PROJECT";
        UUID projectId = UUID.randomUUID();

        ProjectEntity projectEntity = ProjectEntity.builder()
                .id(projectId)
                .projectKey(projectKey)
                .projectName("My Project")
                .description("Test project")
                .configurationItem("CI-123")
                .location("eu")
                .projectFlavor("AMP")
                .status("Completed")
                .deleted(false)
                .ldapGroupManager("cn=my-project-manager,ou=groups,dc=example,dc=com")
                .ldapGroupTeam("cn=my-project-team,ou=groups,dc=example,dc=com")
                .build();

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .projectKey(projectKey)
                .status("Completed")
                .build();

        when(projectRepository.findByProjectKey(projectKey)).thenReturn(Optional.of(projectEntity));
        when(projectResponseMapper.toCreateProjectResponse(projectEntity)).thenReturn(expectedResponse);

        // WHEN
        ProjectResponse result = projectService.getProject(projectKey);

        // THEN
        assertNotNull(result);
        assertEquals(projectKey, result.getProjectKey());
        assertEquals("Completed", result.getStatus());
        verify(projectRepository).findByProjectKey(projectKey);
        verify(projectResponseMapper).toCreateProjectResponse(projectEntity);
    }

    @Test
    void get_project_returns_null_when_project_does_not_exist() {
        // GIVEN
        String projectKey = "NON-EXISTING";

        when(projectRepository.findByProjectKey(projectKey)).thenReturn(Optional.empty());

        // WHEN
        ProjectResponse result = projectService.getProject(projectKey);

        // THEN
        assertNull(result);
        verify(projectRepository).findByProjectKey(projectKey);
        verify(projectResponseMapper, never()).toCreateProjectResponse(any());
    }

    @Test
    void create_project_returns_empty_response() {
        // GIVEN
        ProjectRequest request = new ProjectRequest();
        request.setProjectKey("NEW-PROJECT");
        request.setProjectKeyPattern("NEW%06d");
        request.setProjectName("New Project");
        request.setProjectDescription("New test project");

        // WHEN
        ProjectResponse result = projectService.createProject(request);

        // THEN
        assertNotNull(result);
        assertNull(result.getProjectKey());
    }

    @Test
    void get_project_propagates_repository_exception() {
        // GIVEN
        String projectKey = "ERROR-PROJECT";

        when(projectRepository.findByProjectKey(projectKey))
                .thenThrow(new RuntimeException("Database connection error"));

        // WHEN / THEN
        assertThrows(RuntimeException.class, () -> projectService.getProject(projectKey));
        verify(projectRepository).findByProjectKey(projectKey);
    }

    @Test
    void get_project_returns_null_when_project_key_is_null() {
        // GIVEN
        when(projectRepository.findByProjectKey(null)).thenReturn(Optional.empty());

        // WHEN
        ProjectResponse result = projectService.getProject(null);

        // THEN
        assertNull(result);
        verify(projectRepository).findByProjectKey(null);
    }

    @Test
    void get_project_returns_response_for_soft_deleted_project() {
        // GIVEN
        String projectKey = "DELETED-PROJECT";

        ProjectEntity deletedEntity = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .projectKey(projectKey)
                .projectName("Deleted Project")
                .configurationItem("CI-456")
                .location("eu")
                .deleted(true)
                .build();

        ProjectResponse expectedResponse = ProjectResponse.builder()
                .projectKey(projectKey)
                .status("Deleted")
                .build();

        when(projectRepository.findByProjectKey(projectKey)).thenReturn(Optional.of(deletedEntity));
        when(projectResponseMapper.toCreateProjectResponse(deletedEntity)).thenReturn(expectedResponse);

        // WHEN
        ProjectResponse result = projectService.getProject(projectKey);

        // THEN
        assertNotNull(result);
        assertEquals(projectKey, result.getProjectKey());
        verify(projectRepository).findByProjectKey(projectKey);
    }
}