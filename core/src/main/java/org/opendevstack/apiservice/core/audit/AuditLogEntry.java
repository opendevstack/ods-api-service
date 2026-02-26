package org.opendevstack.apiservice.core.audit;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Plain POJO representing an API audit log entry. Serialized to JSON and emitted via a
 * dedicated SLF4J logger ({@code AUDIT}) instead of being persisted to a database.
 *
 * <p>
 * This approach eliminates the dependency on a datasource for audit, ensuring that:
 * <ul>
 *   <li>Audit logging never blocks the JDBC connection pool.</li>
 *   <li>The application starts and runs normally even if no database is available.</li>
 * </ul>
 * Log aggregation tools (ELK, Loki, Splunk, OpenTelemetry) consume the structured JSON
 * lines for querying and alerting.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogEntry {

	private Instant timestamp;

	@JsonProperty("http_method")
	private String httpMethod;

	@JsonProperty("request_uri")
	private String requestUri;

	@JsonProperty("query_string")
	private String queryString;

	@JsonProperty("request_body")
	private String requestBody;

	@JsonProperty("client_id")
	private String clientId;

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("source_ip")
	private String sourceIp;

	@JsonProperty("response_status")
	private int responseStatus;

	@JsonProperty("duration_ms")
	private long durationMs;

	@JsonProperty("trace_id")
	private String traceId;

	@JsonProperty("user_agent")
	private String userAgent;

}
