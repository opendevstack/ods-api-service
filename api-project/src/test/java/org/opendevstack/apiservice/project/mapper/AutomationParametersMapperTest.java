package org.opendevstack.apiservice.project.mapper;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.project.facade.impl.ProjectCreationCommand;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AutomationParametersMapperTest {

    private final AutomationParametersMapper sut = new AutomationParametersMapper() {};

    @Test
    void to_workflow_parameters_passes_project_managers_and_existing_parameters() {
        List<String> projectManagers = List.of("alex", "taylor");
        ProjectCreationCommand command = build_command(projectManagers);

        Map<String, Object> result = sut.toWorkflowParameters(command, "project-id");

        assertEquals(projectManagers, result.get("project_managers"));
        assertEquals("eu", result.get("geographic_region"));
        assertEquals("DLSS", result.get("project_flavor"));
        assertEquals("owner", result.get("project_owner"));
        assertEquals("project-id", result.get("project_id"));
        assertEquals("CI-001", result.get("configuration_item"));
        assertEquals("DLSS01", result.get("project_key"));
        assertEquals("x2test", result.get("special_account"));
        assertEquals("description", result.get("description"));
        assertEquals("project-name", result.get("project_name"));
        assertEquals("00000000-0000-0000-0000-000000000001", result.get("client_id"));
        assertEquals(11, result.size());
    }

    @Test
    void to_workflow_parameters_omits_project_managers_when_missing_or_empty() {
        Map<String, Object> missing = sut.toWorkflowParameters(build_command(null), "project-id");
        Map<String, Object> empty = sut.toWorkflowParameters(build_command(List.of()), "project-id");

        assertFalse(missing.containsKey("project_managers"));
        assertFalse(empty.containsKey("project_managers"));
        assertEquals(10, missing.size());
        assertEquals(10, empty.size());
    }

    private ProjectCreationCommand build_command(List<String> projectManagers) {
        return new ProjectCreationCommand(
                "DLSS01",
                "project-name",
                "description",
                "DLSS",
                "CI-001",
                "eu",
                "x2test",
                "owner",
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                projectManagers);
    }
}