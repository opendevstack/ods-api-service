package org.opendevstack.apiservice.serviceproject.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.serviceproject.model.CreateProjectResponse;

@Mapper(componentModel = "spring")
public interface CreateProjectResponseMapper {

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "error", ignore = true)
    @Mapping(target = "errorKey", ignore = true)
    @Mapping(target = "errorDescription", ignore = true)
    CreateProjectResponse toCreateProjectResponse(ProjectEntity entity);
}
