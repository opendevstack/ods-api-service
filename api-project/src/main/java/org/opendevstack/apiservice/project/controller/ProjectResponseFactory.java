package org.opendevstack.apiservice.project.controller;

import org.opendevstack.apiservice.project.model.CreateProjectResponse;

public final class ProjectResponseFactory {

    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ProjectResponseFactory() {
    }

    public static CreateProjectResponse conflict(String message) {
        return error("CONFLICT", "PROJECT_ALREADY_EXISTS", message);
    }

    public static CreateProjectResponse projectKeyGenerationFailed() {
        return error(INTERNAL_ERROR, "PROJECT_KEY_GENERATION_FAILED",
                "Failed to generate a unique project key.");
    }

    public static CreateProjectResponse notFound(String projectKey) {
        return error("NOT_FOUND", "PROJECT_NOT_FOUND",
                String.format("Project with key '%s' not found", projectKey));
    }

    public static CreateProjectResponse internalError() {
        return error(INTERNAL_ERROR, INTERNAL_ERROR,
                "An error occurred while processing the request.");
    }

    private static CreateProjectResponse error(String error, String errorKey, String message) {
        CreateProjectResponse response = new CreateProjectResponse();
        response.setError(error);
        response.setErrorKey(errorKey);
        response.setMessage(message);
        return response;
    }
}