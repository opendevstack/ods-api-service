package org.opendevstack.apiservice.core.security.authorization.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.policy.PolicyTypes;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllowedHttpVerbsEvaluatorTest {

    private AllowedHttpVerbsEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new AllowedHttpVerbsEvaluator();
    }

    @Test
    void supports_allowedHttpVerbsPolicy() {
        assertTrue(evaluator.supports(PolicyTypes.ALLOWED_HTTP_VERBS));
    }

    @Test
    void evaluate_allowedMethod_returnsPermit() {
        assertEquals(AuthorizationDecision.PERMIT,
                evaluate("GET", Map.of("allowedHttpVerbs", List.of("GET", "POST"))));
    }

    @Test
    void evaluate_disallowedMethod_returnsDeny() {
        assertEquals(AuthorizationDecision.DENY,
                evaluate("DELETE", Map.of("allowedHttpVerbs", List.of("GET", "POST"))));
    }

    @Test
    void evaluate_missingOrEmptyAllowedVerbs_returnsAbstain() {
        assertEquals(AuthorizationDecision.ABSTAIN, evaluate("GET", Map.of()));
        assertEquals(AuthorizationDecision.ABSTAIN,
                evaluate("GET", Map.of("allowedHttpVerbs", List.of())));
    }

    @Test
    void evaluate_malformedAllowedVerbs_returnsAbstain() {
        assertEquals(AuthorizationDecision.ABSTAIN,
                evaluate("GET", Map.of("allowedHttpVerbs", "GET")));
    }

    private AuthorizationDecision evaluate(String method, Map<String, Object> config) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/test");
        PolicyRule rule = new PolicyRule(UUID.randomUUID(), "api-1", "client-1",
                PolicyTypes.ALLOWED_HTTP_VERBS, config);
        PolicyContext context = new PolicyContext("client-1", null, Map.of(), null, request)
                .withRule(rule);

        return evaluator.evaluate(context);
    }
}
