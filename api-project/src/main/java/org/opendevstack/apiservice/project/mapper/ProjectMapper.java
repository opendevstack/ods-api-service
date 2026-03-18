package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    
    org.opendevstack.apiservice.serviceproject.model.CreateProjectRequest toServiceRequest(
            CreateProjectRequest apiRequest);
    
    CreateProjectResponse toApiResponse(
            org.opendevstack.apiservice.serviceproject.model.CreateProjectResponse serviceResponse);
}
