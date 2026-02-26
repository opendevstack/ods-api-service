package org.opendevstack.apiservice.core.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for persisting audit log entries asynchronously. Uses a dedicated
 * thread pool ({@code auditTaskExecutor}) to avoid blocking the HTTP request thread.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository auditLogRepository;

	/**
	 * Persist an audit log entry asynchronously. Any persistence failure is logged but
	 * never propagated — audit must not break API responses.
	 * @param entry the audit log entry to save
	 */
	@Async("auditTaskExecutor")
	public void saveAuditLog(AuditLogEntry entry) {
		try {
			auditLogRepository.save(entry);
			log.debug("Audit log saved: {} {} -> {}", entry.getHttpMethod(), entry.getRequestUri(),
					entry.getResponseStatus());
		}
		catch (Exception ex) {
			log.error("Failed to persist audit log entry for {} {}: {}", entry.getHttpMethod(),
					entry.getRequestUri(), ex.getMessage(), ex);
		}
	}

}
