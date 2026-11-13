package org.opendevstack.apiservice.core.security.authorization.evaluator;

import org.opendevstack.apiservice.core.contracts.auth.AuthorizationDecision;
import org.opendevstack.apiservice.core.contracts.policy.PolicyContext;
import org.opendevstack.apiservice.core.contracts.policy.PolicyEvaluator;
import org.opendevstack.apiservice.core.contracts.policy.PolicyRule;
import org.opendevstack.apiservice.core.contracts.policy.PolicyTypes;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates ALLOWED_HTTP_VERBS policy rules against the current request method.
 */
@Component
public class AllowedHttpVerbsEvaluator implements PolicyEvaluator {

    @Override
    public boolean supports(String policyType) {
        return PolicyTypes.ALLOWED_HTTP_VERBS.equals(policyType);
    }

    @Override
    public AuthorizationDecision evaluate(PolicyContext context) {
        PolicyRule rule = context.getActiveRule();
        if (rule == null || rule.getConfig() == null) {
            return AuthorizationDecision.ABSTAIN;
        }

        Object configuredVerbs = rule.getConfig().get("allowedHttpVerbs");
        if (!(configuredVerbs instanceof List<?> allowedHttpVerbs) || allowedHttpVerbs.isEmpty()) {
            return AuthorizationDecision.ABSTAIN;
        }

        if (context.getRequest() == null || context.getRequest().getMethod() == null) {
            return AuthorizationDecision.ABSTAIN;
        }

        String requestMethod = context.getRequest().getMethod();
        return allowedHttpVerbs.stream()
                .anyMatch(requestMethod::equals)
                ? AuthorizationDecision.PERMIT
                : AuthorizationDecision.DENY;
    }
}
