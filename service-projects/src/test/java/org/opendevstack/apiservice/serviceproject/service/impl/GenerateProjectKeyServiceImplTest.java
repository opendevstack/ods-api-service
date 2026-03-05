package org.opendevstack.apiservice.serviceproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.jira.service.JiraService;
import org.opendevstack.apiservice.externalservice.ocp.service.OpenshiftService;
import org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException;

import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateProjectKeyServiceImplTest {

    @Mock
    private BitbucketService bitbucketService;

    @Mock
    private JiraService jiraService;

    @Mock
    private OpenshiftService openshiftService;

    @Mock
    private Random random;

    private GenerateProjectKeyServiceImpl tested;

    @BeforeEach
    void setup() {
        tested = new GenerateProjectKeyServiceImpl(bitbucketService, jiraService, openshiftService, random);
        when(bitbucketService.getAvailableInstances()).thenReturn(Set.of("dev"));
        when(jiraService.getAvailableInstances()).thenReturn(Set.of("default"));
        when(openshiftService.getAvailableInstances()).thenReturn(Set.of());
    }

    @Test
    void generateProjectKey_whenFirstCandidateIsFree_thenReturnKey() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(7);
        when(bitbucketService.projectExists(anyString(), anyString())).thenReturn(false);
        when(jiraService.projectExists(anyString(), anyString())).thenReturn(false);

        String result = tested.generateProjectKey(null);

        assertThat(result).isEqualTo("SS000007");
    }

    @Test
    void generateProjectKey_whenFirstCandidateExists_thenRetryUntilUnique() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(1, 2);
        when(bitbucketService.projectExists(anyString(), anyString())).thenReturn(true, false);
        when(jiraService.projectExists(anyString(), anyString())).thenReturn(true, false);

        String result = tested.generateProjectKey("SS%06d");

        assertThat(result).isEqualTo("SS000002");
    }

    @Test
    void generateProjectKey_whenNoUniqueKeyAfterMaxRetries_thenThrowException() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(1);
        when(bitbucketService.projectExists(anyString(), anyString())).thenReturn(true);
        when(jiraService.projectExists(anyString(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> tested.generateProjectKey("SS%06d"))
                .isInstanceOf(ProjectKeyGenerationException.class)
                .hasMessageContaining("Failed to generate unique project key after 10 retries");
    }

    @Test
    void generateProjectKey_whenCustomPatternProvided_thenUseIt() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(42);
        when(bitbucketService.projectExists(anyString(), anyString())).thenReturn(false);
        when(jiraService.projectExists(anyString(), anyString())).thenReturn(false);

        String result = tested.generateProjectKey("AB%04d");

        assertThat(result).isEqualTo("AB0042");
    }
}
