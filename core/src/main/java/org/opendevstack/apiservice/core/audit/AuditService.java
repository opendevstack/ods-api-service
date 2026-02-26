package org.opendevstack.apiservice.core.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for emitting audit log entries as structured JSON via a dedicated
 * SLF4J logger ({@code AUDIT}). This replaces the previous JPA/PostgreSQL persistence
 * approach, eliminating the database dependency for audit and ensuring:
 * <ul>
 *   <li>Audit never blocks the JDBC connection pool.</li>
 *   <li>The application remains healthy even if no database is available.</li>
 * </ul>
 *
 * <p>
 * The {@code AUDIT} logger is configured in {@code logback-spring.xml} with an
 * {@code AsyncAppender} and {@code neverBlock=true}, so logging calls return immediately
 * and can never slow down API responses.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

	private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT");

	private final ObjectMapper objectMapper;

	/**
	 * Emit an audit log entry as a single-line JSON message on the {@code AUDIT} logger.
	 * Any serialization failure is logged on the class logger but never propagated —
	 * audit must not break API responses.
	 * @param entry the audit log entry to log
	 */
	public void saveAuditLog(AuditLogEntry entry) {
		try {
			String json = objectMapper.writeValueAsString(entry);
			AUDIT_LOG.info(json);
			log.debug("Audit log emitted: {} {} -> {}", entry.getHttpMethod(), entry.getRequestUri(),
					entry.getResponseStatus());
		}
		catch (Exception ex) {
			log.error("Failed to serialize audit log entry for {} {}: {}", entry.getHttpMethod(),
					entry.getRequestUri(), ex.getMessage(), ex);
		}
	}

}
