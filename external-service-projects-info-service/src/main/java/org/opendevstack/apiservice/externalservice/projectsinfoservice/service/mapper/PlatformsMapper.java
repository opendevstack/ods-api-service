package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.mapper;

import lombok.AllArgsConstructor;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.model.ProjectPlatforms;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Platforms;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PlatformsMapper {

    private final PlatformSectionMapper platformSectionMapper;

    public Platforms asPlatforms(ProjectPlatforms projectPlatforms) {
        var sections = projectPlatforms.getSections().stream()
                .map(platformSectionMapper::asPlatformSection)
                .toList();

        return new Platforms(sections);
    }
}
