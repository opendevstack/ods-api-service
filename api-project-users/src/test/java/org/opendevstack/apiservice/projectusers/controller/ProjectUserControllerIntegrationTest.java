package org.opendevstack.apiservice.projectusers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.opendevstack.apiservice.projectusers.exception.InvalidTokenException;
import org.opendevstack.apiservice.projectusers.exception.ProjectUserException;
import org.opendevstack.apiservice.projectusers.model.AddUserToProjectRequest;
import org.opendevstack.apiservice.projectusers.model.MembershipRequestResponse;
import org.opendevstack.apiservice.projectusers.model.MembershipRequestStatusResponse;
import org.opendevstack.apiservice.projectusers.service.MembershipRequestStatusService;
import org.opendevstack.apiservice.projectusers.service.ProjectUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProjectUserController.
 * Tests all HTTP endpoints and their integration with services.
 */
@SpringBootTest(classes = ProjectUserControllerIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "server.error.include-message=always",
        "server.error.include-binding-errors=always",
        "spring.mvc.throw-exception-if-no-handler-found=false",
        "spring.web.resources.add-mappings=false",
        "spring.main.allow-bean-definition-overriding=true"
})
@DisplayName("ProjectUserController Integration Tests")
class ProjectUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectUserService projectUserService;

    @Autowired
    private MembershipRequestStatusService membershipRequestStatusService;

    private static final String BASE_URL = "/api/v1";
    private static final String PROJECT_KEY = "MYPROJECT";
        private static final String USER_NAME = "johndoe";
        private static final String ACCOUNT_NAME = "janedoe";
        private static final String ROLE = "TEAM";
        private static final String ENVIRONMENT = "DEVELOPMENT";
    private static final String COMMENTS = "Please grant access";

        @SpringBootConfiguration
        @EnableAutoConfiguration
        @ComponentScan(basePackages = "org.opendevstack.apiservice.projectusers")
        static class TestApplication {
            @Bean
            @Primary
            ProjectUserService projectUserService() {
                return org.mockito.Mockito.mock(ProjectUserService.class);
            }

            @Bean
            @Primary
            MembershipRequestStatusService membershipRequestStatusService() {
                return org.mockito.Mockito.mock(MembershipRequestStatusService.class);
            }
        }

    @Nested
    @DisplayName("POST /api/v1/project/{projectKey}/users - Trigger Membership Request")
    class TriggerMembershipRequestTests {

        @BeforeEach
        void setUp() {
            reset(projectUserService, membershipRequestStatusService);
        }

        @Test
        @DisplayName("Should successfully trigger membership request with all valid fields")
        void testTriggerMembershipRequest_Success() throws Exception {
            // Given
            AddUserToProjectRequest request = createValidRequest();
            MembershipRequestResponse serviceResponse = createMembershipResponse();
            when(projectUserService.addUserToProject(eq(PROJECT_KEY), any(AddUserToProjectRequest.class)))
                    .thenReturn(serviceResponse);

            // When
            MvcResult result = mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    // Then
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", containsString("successfully")))
                    .andExpect(jsonPath("$.data.requestId", is(serviceResponse.getRequestId())))
                    .andExpect(jsonPath("$.data.status", is(serviceResponse.getStatus().getValue())))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andReturn();

            // Verify service was called correctly
            ArgumentCaptor<AddUserToProjectRequest> requestCaptor = ArgumentCaptor.forClass(AddUserToProjectRequest.class);
            verify(projectUserService, times(1)).addUserToProject(eq(PROJECT_KEY), requestCaptor.capture());
            
            AddUserToProjectRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.getUser()).isEqualTo(USER_NAME);
            assertThat(capturedRequest.getAccount()).isEqualTo(ACCOUNT_NAME);
            assertThat(capturedRequest.getRole()).isEqualTo(ROLE);
            assertThat(capturedRequest.getEnvironment()).isEqualTo(ENVIRONMENT);
            assertThat(capturedRequest.getComments()).isEqualTo(COMMENTS);
        }

        @Test
        @DisplayName("Should successfully trigger membership request with minimal fields")
        void testTriggerMembershipRequest_MinimalFields() throws Exception {
            // Given
            AddUserToProjectRequest request = new AddUserToProjectRequest();
            request.setUser(USER_NAME);
            request.setRole(ROLE);
            request.setEnvironment(ENVIRONMENT);
            
            MembershipRequestResponse serviceResponse = createMembershipResponse();
            
            doReturn(serviceResponse)
             .when(projectUserService)
             .addUserToProject(eq(PROJECT_KEY), any(AddUserToProjectRequest.class));

            // When & Then
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data").exists());

            verify(projectUserService, times(1)).addUserToProject(eq(PROJECT_KEY), any(AddUserToProjectRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is missing user field")
        void testTriggerMembershipRequest_MissingUserField() throws Exception {
            // Given
            AddUserToProjectRequest request = new AddUserToProjectRequest();
            request.setRole(ROLE);
            // user is not set

            // When & Then - validation should fail
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(projectUserService, never()).addUserToProject(anyString(), any(AddUserToProjectRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is missing role field")
        void testTriggerMembershipRequest_MissingRoleField() throws Exception {
            // Given
            AddUserToProjectRequest request = new AddUserToProjectRequest();
            request.setUser(USER_NAME);
            // role is not set

            // When & Then - validation should fail
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(projectUserService, never()).addUserToProject(anyString(), any(AddUserToProjectRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void testTriggerMembershipRequest_EmptyBody() throws Exception {
            // When & Then
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(projectUserService, never()).addUserToProject(anyString(), any(AddUserToProjectRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when content type is not JSON")
        void testTriggerMembershipRequest_InvalidContentType() throws Exception {
            // When & Then
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .content("user=" + USER_NAME + "&role=" + ROLE))
                    .andExpect(status().isUnsupportedMediaType());

            verify(projectUserService, never()).addUserToProject(anyString(), any(AddUserToProjectRequest.class));
        }

        @Test
        @DisplayName("Should handle service exception and return 500")
        void testTriggerMembershipRequest_ServiceException() throws Exception {
            // Given
            AddUserToProjectRequest request = createValidRequest();
            when(projectUserService.addUserToProject(eq(PROJECT_KEY), any(AddUserToProjectRequest.class)))
                    .thenThrow(new ProjectUserException("Service error", "PROJECT_USER_ERROR"));

            // When & Then
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());

            verify(projectUserService, times(1)).addUserToProject(eq(PROJECT_KEY), any(AddUserToProjectRequest.class));
        }

        @Test
        @DisplayName("Should include timestamp in response")
        void testTriggerMembershipRequest_ResponseIncludesTimestamp() throws Exception {
            // Given
            AddUserToProjectRequest request = createValidRequest();
            MembershipRequestResponse serviceResponse = createMembershipResponse();
            when(projectUserService.addUserToProject(eq(PROJECT_KEY), any(AddUserToProjectRequest.class)))
                    .thenReturn(serviceResponse);

            // When
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", PROJECT_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    // Then
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should pass projectKey correctly to service")
        void testTriggerMembershipRequest_ProjectKeyPassing() throws Exception {
            // Given
            String customProjectKey = "CUSTOM-PROJECT-123";
            AddUserToProjectRequest request = createValidRequest();
            MembershipRequestResponse serviceResponse = createMembershipResponse();
            when(projectUserService.addUserToProject(eq(customProjectKey), any(AddUserToProjectRequest.class)))
                    .thenReturn(serviceResponse);

            // When & Then
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", customProjectKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(projectUserService, times(1)).addUserToProject(eq(customProjectKey), any(AddUserToProjectRequest.class));
        }

        @Test
        @DisplayName("Should handle special characters in projectKey")
        void testTriggerMembershipRequest_SpecialCharactersInProjectKey() throws Exception {
            // Given
            String projectKeyWithSpecialChars = "MY-PROJECT_123";
            AddUserToProjectRequest request = createValidRequest();
            MembershipRequestResponse serviceResponse = createMembershipResponse();
            when(projectUserService.addUserToProject(eq(projectKeyWithSpecialChars), any(AddUserToProjectRequest.class)))
                    .thenReturn(serviceResponse);

            // When & Then
            mockMvc.perform(post(BASE_URL + "/project/{projectKey}/users", projectKeyWithSpecialChars)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(projectUserService).addUserToProject(eq(projectKeyWithSpecialChars), any(AddUserToProjectRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/project/{projectKey}/users/{requestId}/status - Get Request Status")
    class GetRequestStatusTests {

        private static final String REQUEST_ID = "req-123456-789";

        @BeforeEach
        void setUp() {
            reset(projectUserService, membershipRequestStatusService);
        }

        @Test
        @DisplayName("Should successfully retrieve request status with valid inputs")
        void testGetRequestStatus_Success() throws Exception {
            // Given
            String user = USER_NAME;
            MembershipRequestStatusResponse statusResponse = createStatusResponse();
            when(membershipRequestStatusService.validateRequestToken(REQUEST_ID, PROJECT_KEY, user))
                    .thenReturn(true);
            when(membershipRequestStatusService.getRequestStatus(REQUEST_ID))
                    .thenReturn(statusResponse);

            // When
            mockMvc.perform(get(BASE_URL + "/project/{projectKey}/users/{userId}/status", PROJECT_KEY, USER_NAME)
                    .param("requestId", REQUEST_ID))
                    // Then
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", containsString("successfully")))
                    .andExpect(jsonPath("$.data.requestId", is(statusResponse.getRequestId())))
                    .andExpect(jsonPath("$.data.status", is(statusResponse.getStatus().getValue())))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(membershipRequestStatusService, times(1)).validateRequestToken(REQUEST_ID, PROJECT_KEY, user);
            verify(membershipRequestStatusService, times(1)).getRequestStatus(REQUEST_ID);
        }

        @Test
        @DisplayName("Should return 400 when user parameter is missing")
        void testGetRequestStatus_MissingUserParameter() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL + "/project/{projectKey}/users/{requestId}/status", PROJECT_KEY, REQUEST_ID))
                    .andExpect(status().isBadRequest());

            verify(membershipRequestStatusService, never()).validateRequestToken(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 400 when invalid request token")
        void testGetRequestStatus_InvalidToken() throws Exception {
            // Given
            String user = USER_NAME;
            when(membershipRequestStatusService.validateRequestToken(REQUEST_ID, PROJECT_KEY, user))
                    .thenReturn(false);

            // When & Then
            mockMvc.perform(get(BASE_URL + "/project/{projectKey}/users/{userId}/status", PROJECT_KEY, USER_NAME)
                    .param("requestId", REQUEST_ID))
                    .andExpect(status().isBadRequest());

            verify(membershipRequestStatusService, times(1)).validateRequestToken(REQUEST_ID, PROJECT_KEY, user);
            verify(membershipRequestStatusService, never()).getRequestStatus(anyString());
        }

        @Test
        @DisplayName("Should throw InvalidTokenException when token validation fails")
        void testGetRequestStatus_TokenValidationThrowsException() throws Exception {
            // Given
            String user = USER_NAME;
            when(membershipRequestStatusService.validateRequestToken(REQUEST_ID, PROJECT_KEY, user))
                    .thenThrow(new InvalidTokenException("Token validation failed"));

            // When & Then
            mockMvc.perform(get(BASE_URL + "/project/{projectKey}/users/{userId}/status", PROJECT_KEY, USER_NAME)
                    .param("requestId", REQUEST_ID))
                    .andExpect(status().isBadRequest());

            verify(membershipRequestStatusService, times(1)).validateRequestToken(REQUEST_ID, PROJECT_KEY, user);
        }

        @Test
        @DisplayName("Should pass all parameters correctly to service")
        void testGetRequestStatus_ParameterPassing() throws Exception {
            // Given
            String user = "jane.smith";
            String projectKeyCustom = "CUSTOM-PROJ";
            String requestIdCustom = "req-custom-id";
            MembershipRequestStatusResponse statusResponse = createStatusResponse();
            
            when(membershipRequestStatusService.validateRequestToken(requestIdCustom, projectKeyCustom, user))
                    .thenReturn(true);
            when(membershipRequestStatusService.getRequestStatus(requestIdCustom))
                    .thenReturn(statusResponse);

            // When & Then
            mockMvc.perform(get(BASE_URL + "/project/{projectKey}/users/{userId}/status", projectKeyCustom, user)
                    .param("requestId", requestIdCustom))
                    .andExpect(status().isOk());

            verify(membershipRequestStatusService).validateRequestToken(requestIdCustom, projectKeyCustom, user);
            verify(membershipRequestStatusService).getRequestStatus(requestIdCustom);
        }

        @Test
        @DisplayName("Should include timestamp in status response")
        void testGetRequestStatus_ResponseIncludesTimestamp() throws Exception {
            // Given
            String user = USER_NAME;
            MembershipRequestStatusResponse statusResponse = createStatusResponse();
            when(membershipRequestStatusService.validateRequestToken(REQUEST_ID, PROJECT_KEY, user))
                    .thenReturn(true);
            when(membershipRequestStatusService.getRequestStatus(REQUEST_ID))
                    .thenReturn(statusResponse);

            // When
            mockMvc.perform(get(BASE_URL + "/project/{projectKey}/users/{userId}/status", PROJECT_KEY, USER_NAME)
                    .param("requestId", REQUEST_ID))
                    // Then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should handle service exception")
        void testGetRequestStatus_ServiceException() throws Exception {
            // Given
            String user = USER_NAME;
            when(membershipRequestStatusService.validateRequestToken(REQUEST_ID, PROJECT_KEY, user))
                    .thenReturn(true);
            when(membershipRequestStatusService.getRequestStatus(REQUEST_ID))
                    .thenThrow(new RuntimeException("Service error"));

            // When & Then
            mockMvc.perform(get(BASE_URL + "/project/{projectKey}/users/{userId}/status", PROJECT_KEY, USER_NAME)
                    .param("requestId", REQUEST_ID))
                    .andExpect(status().isInternalServerError());

            verify(membershipRequestStatusService, times(1)).validateRequestToken(REQUEST_ID, PROJECT_KEY, USER_NAME);
            verify(membershipRequestStatusService, times(1)).getRequestStatus(REQUEST_ID);
        }
    }

    // Helper methods

    private AddUserToProjectRequest createValidRequest() {
        AddUserToProjectRequest request = new AddUserToProjectRequest();
        request.setUser(USER_NAME);
        request.setAccount(ACCOUNT_NAME);
        request.setRole(ROLE);
        request.setEnvironment(ENVIRONMENT);
        request.setComments(COMMENTS);
        return request;
    }

    private MembershipRequestResponse createMembershipResponse() {
        MembershipRequestResponse response = new MembershipRequestResponse();
        response.setRequestId("req-1234567890");
                response.setStatus(MembershipRequestResponse.StatusEnum.PENDING);
        return response;
    }

    private MembershipRequestStatusResponse createStatusResponse() {
        MembershipRequestStatusResponse response = new MembershipRequestStatusResponse();
        response.setRequestId("req-1234567890");
                response.setStatus(MembershipRequestStatusResponse.StatusEnum.COMPLETED);
                response.setCompleted(true);
                response.setSuccessful(true);
        return response;
    }
}
