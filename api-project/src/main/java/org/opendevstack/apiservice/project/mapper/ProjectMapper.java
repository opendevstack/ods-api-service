package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectRequest toServiceRequest(CreateProjectRequest apiRequest);

    @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
    @Mapping(source = "projectKey", target = "location", qualifiedByName = "mapLocation")
    CreateProjectResponse toApiResponse(ProjectResponse serviceResponse);

    @Named("mapStatus")
    default String mapStatus(Status status) {
        if (status == null) {
            return null;
        }
        return status.getDbValue();
    }

    @Named("mapLocation")
    default String mapLocation(String projectKey) {
        if (projectKey == null || projectKey.isEmpty()) {
            return null;
        }
        return "/api/pub/v0/projects/" + projectKey;
    }
}
