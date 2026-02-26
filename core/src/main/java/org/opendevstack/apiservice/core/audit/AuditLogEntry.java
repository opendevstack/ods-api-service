package org.opendevstack.apiservice.core.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing an API audit log entry persisted to PostgreSQL.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private Instant timestamp;

	@Column(name = "http_method", nullable = false, length = 10)
	private String httpMethod;

	@Column(name = "request_uri", nullable = false, length = 2048)
	private String requestUri;

	@Column(name = "query_string", length = 4096)
	private String queryString;

	@Column(name = "request_body", columnDefinition = "TEXT")
	private String requestBody;

	@Column(name = "client_id")
	private String clientId;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "source_ip", length = 45)
	private String sourceIp;

	@Column(name = "response_status", nullable = false)
	private int responseStatus;

	@Column(name = "duration_ms", nullable = false)
	private long durationMs;

	@Column(name = "trace_id", length = 32)
	private String traceId;

	@Column(name = "user_agent", length = 512)
	private String userAgent;

}
