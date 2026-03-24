package org.opendevstack.apiservice.serviceproject.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.persistence.entity.ProjectEntity;
import org.opendevstack.apiservice.persistence.repository.ProjectRepository;
import org.opendevstack.apiservice.serviceproject.mapper.ProjectResponseMapper;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.opendevstack.apiservice.serviceproject.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectRepository projectRepository;
    
    private final ProjectResponseMapper projectResponseMapper;
    
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectResponseMapper projectResponseMapper) {
        this.projectRepository = projectRepository;
        this.projectResponseMapper = projectResponseMapper;
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        ProjectEntity entity = new ProjectEntity();
        entity.setProjectKey(request.getProjectKey());
        entity.setProjectName(request.getProjectName());   
        entity.setProjectFlavor(request.getProjectFlavor());
        entity.setConfigurationItem(request.getConfigurationItem());
        entity.setDescription(request.getProjectFlavor() + " project");
        entity.setLdapGroupManager(getLdapGroup("MANAGER", request.getProjectKey()));
        entity.setLdapGroupTeam(getLdapGroup("TEAM", request.getProjectKey()));
        entity.setLdapGroupStakeholder(getLdapGroup("STAKEHOLDER", request.getProjectKey()));
        entity.setStatus(Status.PENDING.getDbValue());
        entity.setLocation(request.getLocation());
        ProjectEntity save = projectRepository.save(entity);
        return projectResponseMapper.toCreateProjectResponse(save);
    }

    @Override
    public ProjectResponse getProject(String projectKey) {
        Optional<ProjectEntity> project = projectRepository.findByProjectKeyIgnoreCase(projectKey);
        
        if (project.isPresent()) {
            return projectResponseMapper.toCreateProjectResponse(project.get());
        }
        
        return null;
    }

    private String getLdapGroup(String role, String projectKey) {
        return "CN=BI-AS-ATLASSIAN-P-" + projectKey + "-" + role + ",OU=BIDS-managed,DC=eu,DC=boehringer,DC=com";
    }
}

