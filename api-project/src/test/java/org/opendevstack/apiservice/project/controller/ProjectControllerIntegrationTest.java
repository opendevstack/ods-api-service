package org.opendevstack.apiservice.project.controller;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.project.facade.ProjectsFacade;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
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

    @MockitoBean
    private ProjectsFacade facade;

    @Test
    void createProject_withProvidedProjectKey_returnsInitiatedResponse() throws Exception {
        CreateProjectResponse serviceResponse =
            new CreateProjectResponse();
        serviceResponse.setProjectKey("PROJ01");
        serviceResponse.setStatus("Initiated");
        when(facade.createProject(any(CreateProjectRequest.class)))
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
                .andExpect(jsonPath("$.status").value("Initiated"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.errorKey").doesNotExist())
                .andExpect(jsonPath("$.errorDescription").doesNotExist());
    }

    @Test
    void getProject_whenNotFound_returns404() throws Exception {
        when(facade.getProject("UNKNOWN")).thenReturn(null);

        mockMvc.perform(get("/api/v0/projects/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorKey").value("PROJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Project with key 'UNKNOWN' not found"))
                .andExpect(jsonPath("$.projectKey").doesNotExist())
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.errorDescription").doesNotExist());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
    })
    @Import({ProjectController.class})
    static class TestConfig {
    }
}