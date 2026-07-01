package org.opendevstack.apiservice.serviceproject.mapper;

import java.util.Arrays;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectSummary;
import org.opendevstack.apiservice.serviceproject.model.Status;

@Mapper(componentModel = "spring")
public interface ProjectResponseMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
    @Mapping(source = "id", target = "projectId")
    @Named("mapEntityToResponse")
    ProjectResponse toCreateProjectResponse(ProjectEntity entity);

    @IterableMapping(qualifiedByName = "mapEntityToResponse")
    List<ProjectResponse> toCreateProjectResponse(List<ProjectEntity> entities);

    @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
    @Named("mapEntityToSummary")
    ProjectSummary toProjectSummary(ProjectEntity entity);

    @IterableMapping(qualifiedByName = "mapEntityToSummary")
    List<ProjectSummary> toProjectSummary(List<ProjectEntity> entities);

    @Named("mapStatus")
    default Status mapStatus(String value) {
        if (value == null) {
            return null;
        }
        
        return Arrays.stream(Status.values())
                .filter(s -> s.getDbValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown Status db value: '" + value + "'"));
    }
}
