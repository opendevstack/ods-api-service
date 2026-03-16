package org.opendevstack.apiservice.project.mapper;

import org.apache.logging.log4j.util.Strings;
import org.mapstruct.Mapper;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    ProjectRequest toServiceRequest(
            CreateProjectRequest apiRequest);

    default CreateProjectResponse toApiResponse(ProjectResponse serviceResponse) {
        if (serviceResponse == null) {
                return null;
        }

        CreateProjectResponse response = new CreateProjectResponse();
        response.setProjectKey(serviceResponse.getProjectKey());
        
        if (!Strings.isEmpty(serviceResponse.getStatus())) {
            response.setStatus(serviceResponse.getStatus());
        }

        if (!Strings.isEmpty(serviceResponse.getProjectKey())) {
                response.setLocation("/api/pub/v0/projects/" + serviceResponse.getProjectKey());
        }

        return response;
        }
}
