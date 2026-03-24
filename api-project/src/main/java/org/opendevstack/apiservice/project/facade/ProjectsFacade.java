package org.opendevstack.apiservice.project.facade;

import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.project.exception.ProjectCreationException;
import org.opendevstack.apiservice.project.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;

import java.util.UUID;

public interface ProjectsFacade {

    CreateProjectResponse createProject(CreateProjectRequest request, UUID clientId)
            throws ProjectCreationException, ProjectKeyGenerationException, AutomationPlatformException;

    CreateProjectResponse getProject(String projectKey);
}