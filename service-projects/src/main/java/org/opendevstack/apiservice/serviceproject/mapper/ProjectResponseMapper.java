package org.opendevstack.apiservice.serviceproject.mapper;

import org.mapstruct.Mapper;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;

@Mapper(componentModel = "spring")
public interface ProjectResponseMapper {
    
    ProjectResponse toCreateProjectResponse(ProjectEntity entity);
}
