package org.opendevstack.apiservice.core.audit;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for API audit logging.
 * Configurable via {@code app.audit.*} in application.yaml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.audit")
public class AuditProperties {

	/**
	 * Enable or disable audit logging globally.
	 */
	private boolean enabled = true;

	/**
	 * URL patterns to audit (Ant-style). Only requests matching these patterns will be
	 * logged.
	 */
	private List<String> urlPatterns = List.of("/api/v1/**");

	/**
	 * Whether to capture the request body for POST/PUT/PATCH requests.
	 */
	private boolean logRequestBody = true;

	/**
	 * Maximum request body size (in bytes) to capture. Bodies larger than this are
	 * truncated.
	 */
	private int maxBodySize = 10240;

}
