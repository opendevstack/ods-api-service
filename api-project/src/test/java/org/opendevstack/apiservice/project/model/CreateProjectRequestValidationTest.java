package org.opendevstack.apiservice.project.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateProjectRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void project_managers_are_optional_and_allow_up_to_five_identifiers() {
        CreateProjectRequest omitted = new CreateProjectRequest();
        omitted.setProjectFlavor("DLSS");
        CreateProjectRequest empty = new CreateProjectRequest();
        empty.setProjectFlavor("DLSS");
        empty.setProjectManagers(List.of());
        CreateProjectRequest maximum = new CreateProjectRequest();
        maximum.setProjectFlavor("DLSS");
        maximum.setProjectManagers(List.of("alex", "jamie", "taylor", "morgan", "casey"));

        assertTrue(validator.validate(omitted).isEmpty());
        assertTrue(validator.validate(empty).isEmpty());
        assertTrue(validator.validate(maximum).isEmpty());
    }

    @Test
    void project_managers_reject_more_than_five_identifiers() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectFlavor("DLSS");
        request.setProjectManagers(List.of("alex", "jamie", "taylor", "morgan", "casey", "riley"));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void project_managers_allow_owner_identifier_format() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectFlavor("DLSS");
        request.setProjectManagers(List.of("abcdefghij"));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void project_managers_reject_identifiers_not_matching_owner_format() {
        CreateProjectRequest tooLong = new CreateProjectRequest();
        tooLong.setProjectFlavor("DLSS");
        tooLong.setProjectManagers(List.of("abcdefghijk"));
        CreateProjectRequest withSpaces = new CreateProjectRequest();
        withSpaces.setProjectFlavor("DLSS");
        withSpaces.setProjectManagers(List.of("Alex Example"));

        assertFalse(validator.validate(tooLong).isEmpty());
        assertFalse(validator.validate(withSpaces).isEmpty());
    }

    @Test
    void project_managers_reject_empty_and_whitespace_only_names() {
        CreateProjectRequest empty = new CreateProjectRequest();
        empty.setProjectFlavor("DLSS");
        empty.setProjectManagers(List.of(""));
        CreateProjectRequest whitespace = new CreateProjectRequest();
        whitespace.setProjectFlavor("DLSS");
        whitespace.setProjectManagers(List.of("   "));

        assertFalse(validator.validate(empty).isEmpty());
        assertFalse(validator.validate(whitespace).isEmpty());
    }

}