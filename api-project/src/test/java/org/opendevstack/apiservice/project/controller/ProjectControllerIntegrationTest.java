package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.opendevstack.apiservice.project.facade.impl.ProjectsFacadeImpl;
import org.opendevstack.apiservice.project.mapper.ProjectMapper;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ProjectControllerIntegrationTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @MockitoBean
    private ProjectService projectService;

    @Test
    void createProject_withProvidedProjectKey_returnsInitiatedResponse() throws Exception {
        org.opendevstack.apiservice.serviceproject.model.CreateProjectResponse serviceResponse =
            new org.opendevstack.apiservice.serviceproject.model.CreateProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus("Initiated");
        when(projectService.createProject(org.mockito.ArgumentMatchers.any(
            org.opendevstack.apiservice.serviceproject.model.CreateProjectRequest.class)))
            .thenReturn(serviceResponse);

        String payload = """
            {
              \"projectKey\": \"PROJ01\",
              \"projectName\": \"My Project\",
              \"projectDescription\": \"desc\"
            }
            """;

        mockMvc.perform(post("/api/v0/projects")
                .contentType("application/json")
                .content(payload == null ? "" : payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("PROJ01"))
                .andExpect(jsonPath("$.status").value("Initiated"));
    }

    @Test
    void getProject_whenNotFound_returns404() throws Exception {
        when(projectService.getProject("UNKNOWN")).thenReturn(null);

        mockMvc.perform(get("/api/v0/projects/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorKey").value("PROJECT_NOT_FOUND"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({ProjectController.class, ProjectsFacadeImpl.class})
    static class TestConfig {

        @Bean
        ProjectMapper projectMapper() {
            return Mappers.getMapper(ProjectMapper.class);
        }
    }
}