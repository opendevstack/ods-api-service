package org.opendevstack.apiservice.externalservice.projectsinfoservice.service.mapper;

import lombok.AllArgsConstructor;
import org.opendevstack.apiservice.externalservice.projects_info_service.v1_0_0.client.model.Section;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSection;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.PlatformSectionLink;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PlatformSectionMapper {

    public PlatformSection asPlatformSection(Section section) {
        var links = section.getLinks().stream()
                .map(PlatformSectionLink::new)
                .toList();

        return new PlatformSection(section.getSection(), section.getTooltip(), links);
    }
}
