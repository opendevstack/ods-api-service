package org.opendevstack.apiservice.serviceproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.serviceproject.service.GenerateProjectKeyService;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private BitbucketService bitbucketService;

    @Mock
    private JiraService jiraService;

    @Mock
    private OpenshiftService openshiftService;
    
    @Mock
    private GenerateProjectKeyService generateProjectKeyService;

    private ProjectServiceImpl sut;

    @BeforeEach
    void setup() {
        sut = new ProjectServiceImpl(bitbucketService, jiraService, openshiftService, generateProjectKeyService);
    }
}

