package org.opendevstack.apiservice.project.facade.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.service.AutomationPlatformService;
import org.opendevstack.apiservice.persistence.entity.ClientAppEntity;
import org.opendevstack.apiservice.persistence.entity.ClientAppProjectFlavorEntity;
import org.opendevstack.apiservice.project.exception.ClientAppNotRegisteredException;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.mapper.ProjectMapper;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.project.service.ClientAppService;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectsFacadeImplTest {

    private static final UUID CLIENT_ID = UUID.fromString("56a0fc62-bf77-4acb-8cd7-8cc9f5f2198f");
    private static final UUID PROJECT_ID = UUID.fromString("aaaa1111-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Mock
    private ProjectService projectService;

    @Mock
    private ClientAppService clientAppService;
    
    @Mock
    private GenerateProjectKeyService generateProjectKeyService;

    @Mock
    private AutomationPlatformService automationPlatformService;

    private final ProjectMapper projectMapper = Mappers.getMapper(ProjectMapper.class);

    private ProjectsFacadeImpl sut;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        sut = new ProjectsFacadeImpl(projectService, projectMapper, clientAppService, 
                generateProjectKeyService, automationPlatformService);
        
        Field workflowField = ProjectsFacadeImpl.class.getDeclaredField("createProjectWorkflow");
        workflowField.setAccessible(true);
        workflowField.set(sut, "create-project-workflow");
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }
    
    @Test
    void create_project_returns_response_when_resolved_by_flavor_name() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "defaultOwner");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "DLSS01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("DLSS01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        CreateProjectResponse result = sut.createProject(request, CLIENT_ID);

        assertNotNull(result);
        assertEquals(Status.PENDING.getDbValue(), result.getStatus());
        assertEquals("DLSS", result.getProjectFlavor());
        verify(clientAppService).findByClientId(CLIENT_ID);
        verify(projectService).createProject(any(ProjectRequest.class));
        verify(automationPlatformService).executeWorkflow(anyString(), anyMap());
    }

    @Test
    void create_project_returns_response_when_resolved_by_config_item() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "defaultOwner");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest(null, "CI-001", "DLSS01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("DLSS01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        CreateProjectResponse result = sut.createProject(request, CLIENT_ID);

        assertNotNull(result);
        assertEquals("DLSS", result.getProjectFlavor());
        verify(projectService).createProject(any(ProjectRequest.class));
    }

    @Test
    void create_project_fills_owner_from_flavor_when_not_provided() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "flavorOwner");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "DLSS01");
        request.setOwner(null); // not provided

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("DLSS01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        sut.createProject(request, CLIENT_ID);

        assertEquals("flavorOwner", request.getOwner());
    }

    @Test
    void create_project_fills_location_from_flavor_when_not_provided() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "us", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "DLSS01");
        request.setLocation(null); 

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("DLSS01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        sut.createProject(request, CLIENT_ID);

        assertEquals("us", request.getLocation());
    }

    @Test
    void create_project_fills_config_item_from_flavor_when_resolved_by_flavor_name() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-DEFAULT", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "DLSS01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("DLSS01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        sut.createProject(request, CLIENT_ID);

        assertEquals("CI-DEFAULT", request.getConfigurationItem());
    }

    @Test
    void create_project_fills_flavor_name_from_flavor_when_resolved_by_config_item() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest(null, "CI-001", "DLSS01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("DLSS01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        sut.createProject(request, CLIENT_ID);

        assertEquals("DLSS", request.getProjectFlavor());
    }

    @Test
    void create_project_does_not_override_owner_when_already_provided() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "flavorOwner");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "DLSS01");
        request.setOwner("customOwner");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("DLSS01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        sut.createProject(request, CLIENT_ID);

        assertEquals("customOwner", request.getOwner());
    }

    @Test
    void create_project_uses_existing_project_key_when_provided() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "MY_KEY");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("MY_KEY")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("MY_KEY", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        sut.createProject(request, CLIENT_ID);

        assertEquals("MY_KEY", request.getProjectKey());
        verify(generateProjectKeyService, never()).generateProjectKey(anyString());
    }

    @Test
    void create_project_generates_project_key_using_flavor_pattern_when_not_provided() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, null); // no project key

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(generateProjectKeyService.generateProjectKey("DLSS%06d")).thenReturn("DLSS000001");
        when(projectService.getProject("DLSS000001")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("DLSS000001", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        sut.createProject(request, CLIENT_ID);

        assertEquals("DLSS000001", request.getProjectKey());
        verify(generateProjectKeyService).generateProjectKey("DLSS%06d");
    }

    @Test
    void create_project_throws_validation_exception_when_existing_project_key_already_used() {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "EXISTING");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("EXISTING")).thenReturn(buildProjectResponse("EXISTING", Status.RUNNING));

        ProjectValidationException ex = assertThrows(
                ProjectValidationException.class,
                () -> sut.createProject(request, CLIENT_ID));
        assertEquals(ErrorKey.DUPLICATE_RECORD, ex.getErrorKey());
    }

    @Test
    void create_project_throws_key_generation_exception_when_service_fails() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, null);

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(generateProjectKeyService.generateProjectKey("DLSS%06d"))
                .thenThrow(new org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException("fail"));

        assertThrows(
                ProjectKeyGenerationException.class,
                () -> sut.createProject(request, CLIENT_ID));
    }
    
    @Test
    void create_project_throws_client_app_not_registered_exception_when_client_does_not_exist() {
        CreateProjectRequest request = buildFullRequest("DLSS", null, "KEY01");
        when(clientAppService.findByClientId(CLIENT_ID))
                .thenThrow(new ClientAppNotRegisteredException(CLIENT_ID.toString()));
        
        ClientAppNotRegisteredException exception = assertThrows(
                ClientAppNotRegisteredException.class,
                () -> sut.createProject(request, CLIENT_ID));

        assertNotNull(exception);
        verify(clientAppService).findByClientId(CLIENT_ID);
    }
    
    @Test
    void create_project_throws_validation_exception_when_flavor_is_not_configured_for_client() {
        ClientAppEntity clientApp = buildClientApp(List.of(buildFlavor("AMP", "CI-002", new String[]{}, "eu", "o")));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "KEY01");
        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        
        ProjectValidationException exception = assertThrows(
                ProjectValidationException.class,
                () -> sut.createProject(request, CLIENT_ID));

        assertEquals(ErrorKey.INVALID_PROJECT_FLAVOR, exception.getErrorKey());
        verify(clientAppService).findByClientId(CLIENT_ID);
    }

    @Test
    void create_project_throws_validation_exception_when_client_has_no_flavors() {
        ClientAppEntity clientApp = buildClientApp(Collections.emptyList());
        CreateProjectRequest request = buildFullRequest("DLSS", null, "KEY01");
        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        
        ProjectValidationException exception = assertThrows(
                ProjectValidationException.class,
                () -> sut.createProject(request, CLIENT_ID));

        assertEquals(ErrorKey.INVALID_PROJECT_FLAVOR, exception.getErrorKey());
        verify(clientAppService).findByClientId(CLIENT_ID);
    }

    @Test
    void create_project_throws_validation_exception_when_neither_flavor_nor_config_item_provided() {
        ClientAppEntity clientApp = buildClientApp(List.of(buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "o")));
        CreateProjectRequest request = buildFullRequest(null, null, "KEY01");
        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);

        ProjectValidationException exception = assertThrows(
                ProjectValidationException.class,
                () -> sut.createProject(request, CLIENT_ID));

        assertEquals(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM, exception.getErrorKey());
    }
    
    @Test
    void create_project_throws_validation_exception_when_config_item_does_not_match_any_flavor() {
        ClientAppEntity clientApp = buildClientApp(List.of(buildFlavor("DLSS", "CI-999", new String[]{}, "eu", "o")));
        CreateProjectRequest request = buildFullRequest(null, "CI-001", "KEY01");
        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);

        ProjectValidationException exception = assertThrows(
                ProjectValidationException.class,
                () -> sut.createProject(request, CLIENT_ID));

        assertEquals(ErrorKey.INVALID_CONFIG_ITEM, exception.getErrorKey());
        verify(clientAppService).findByClientId(CLIENT_ID);
    }

    @Test
    void create_project_throws_validation_exception_when_config_item_matches_multiple_flavors() {
        ClientAppEntity clientApp = buildClientApp(List.of(
                buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "o"),
                buildFlavor("AMP", "CI-001", new String[]{}, "eu", "o")));
        CreateProjectRequest request = buildFullRequest(null, "CI-001", "KEY01");
        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);

        ProjectValidationException exception = assertThrows(
                ProjectValidationException.class,
                () -> sut.createProject(request, CLIENT_ID));

        assertEquals(ErrorKey.INVALID_CONFIG_ITEM, exception.getErrorKey());
        verify(clientAppService).findByClientId(CLIENT_ID);
    }

    @Test
    void create_project_succeeds_when_config_item_matches_one_flavor_and_allowed_list_is_empty() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest(null, "CI-001", "KEY01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("KEY01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("KEY01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        assertDoesNotThrow(() -> sut.createProject(request, CLIENT_ID));
    }

    @Test
    void create_project_succeeds_when_config_item_is_present_in_allowed_list() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{"CI-001", "CI-002"}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest(null, "CI-001", "KEY01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("KEY01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("KEY01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.success("job1", "ok"));

        assertDoesNotThrow(() -> sut.createProject(request, CLIENT_ID));
    }

    @Test
    void create_project_throws_validation_exception_when_config_item_is_not_in_allowed_list() {
        ClientAppEntity clientApp = buildClientApp(
                List.of(buildFlavor("DLSS", "CI-001", new String[]{"CI-002", "CI-003"}, "eu", "o")));
        CreateProjectRequest request = buildFullRequest(null, "CI-001", "KEY01");
        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);

        ProjectValidationException exception = assertThrows(
                ProjectValidationException.class,
                () -> sut.createProject(request, CLIENT_ID));

        assertEquals(ErrorKey.INVALID_CONFIG_ITEM, exception.getErrorKey());
        verify(clientAppService).findByClientId(CLIENT_ID);
    }

    @Test
    void create_project_throws_automation_exception_when_automation_platform_fails() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "KEY01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("KEY01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("KEY01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenThrow(new AutomationPlatformException("connection error"));

        assertThrows(
                AutomationPlatformException.class,
                () -> sut.createProject(request, CLIENT_ID));
    }

    @Test
    void create_project_throws_automation_exception_when_automation_result_is_not_successful() throws Exception {
        ClientAppProjectFlavorEntity flavor = buildFlavor("DLSS", "CI-001", new String[]{}, "eu", "owner1");
        ClientAppEntity clientApp = buildClientApp(List.of(flavor));
        CreateProjectRequest request = buildFullRequest("DLSS", null, "KEY01");

        when(clientAppService.findByClientId(CLIENT_ID)).thenReturn(clientApp);
        when(projectService.getProject("KEY01")).thenReturn(null);
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(buildProjectResponse("KEY01", Status.PENDING));
        when(automationPlatformService.executeWorkflow(anyString(), anyMap()))
                .thenReturn(AutomationExecutionResult.failure("job1", "nope", "some error"));

        assertThrows(
                AutomationPlatformException.class,
                () -> sut.createProject(request, CLIENT_ID));
    }

    @Test
    void get_project_returns_mapped_response_when_service_returns_value() {
        ProjectResponse serviceResponse = buildProjectResponse("PROJ01", Status.RUNNING);
        when(projectService.getProject("PROJ01")).thenReturn(serviceResponse);
        
        CreateProjectResponse result = sut.getProject("PROJ01");
        
        assertNotNull(result);
        assertEquals("PROJ01", result.getProjectKey());
        assertEquals("Running", result.getStatus());
        verify(projectService).getProject("PROJ01");
    }

    @Test
    void get_project_returns_null_when_service_returns_null() {
        when(projectService.getProject("UNKNOWN")).thenReturn(null);
        
        CreateProjectResponse result = sut.getProject("UNKNOWN");
        
        assertNull(result);
        verify(projectService).getProject("UNKNOWN");
    }

    private CreateProjectRequest buildFullRequest(String projectFlavor, String configurationItem, String projectKey) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectKey(projectKey);
        request.setProjectFlavor(projectFlavor);
        request.setConfigurationItem(configurationItem);
        request.setProjectName("Test Project");
        request.setProjectDescription("A test project");
        request.setOwner("testowner");
        request.setLocation("eu");
        request.setX2OdsAccount("x2test");
        return request;
    }

    private ProjectResponse buildProjectResponse(String projectKey, Status status) {
        return ProjectResponse.builder()
                .projectId(PROJECT_ID)
                .projectKey(projectKey)
                .status(status)
                .build();
    }

    private ClientAppEntity buildClientApp(List<ClientAppProjectFlavorEntity> flavors) {
        ClientAppEntity entity = ClientAppEntity.builder()
                .clientId(CLIENT_ID.toString())
                .clientName("Test App")
                .build();
        entity.setProjectFlavors(flavors);
        return entity;
    }

    private ClientAppProjectFlavorEntity buildFlavor(
            String name, String configItem, String[] allowedConfigItems, String location, String projectOwner) {
        return ClientAppProjectFlavorEntity.builder()
                .name(name)
                .configItem(configItem)
                .allowedConfigItems(allowedConfigItems)
                .projectKeyPattern(name + "%06d")
                .location(location)
                .projectOwner(projectOwner)
                .build();
    }
}