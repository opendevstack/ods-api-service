package org.opendevstack.apiservice.core.audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Servlet filter that captures API request/response metadata and persists it
 * asynchronously as an audit log entry. Only requests matching the configured URL
 * patterns ({@code app.audit.url-patterns}) are audited.
 *
 * <p>
 * Runs after Spring Security filters so that the {@link SecurityContextHolder} is
 * populated with the authenticated principal.
 * </p>
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
@RequiredArgsConstructor
public class AuditLoggingFilter extends OncePerRequestFilter {

	private final AuditService auditService;

	private final AuditProperties auditProperties;

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!auditProperties.isEnabled()) {
			return true;
		}

		String requestUri = request.getRequestURI();
		return auditProperties.getUrlPatterns().stream().noneMatch(pattern -> pathMatcher.match(pattern, requestUri));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request,
				auditProperties.getMaxBodySize());
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		long startTime = System.currentTimeMillis();

		try {
			filterChain.doFilter(requestWrapper, responseWrapper);
		}
		finally {
			try {
				long durationMs = System.currentTimeMillis() - startTime;
				AuditLogEntry entry = buildAuditEntry(requestWrapper, responseWrapper, durationMs);
				auditService.saveAuditLog(entry);
			}
			catch (Exception ex) {
				log.error("Error building audit log entry: {}", ex.getMessage(), ex);
			}
			finally {
				// Ensure the response body is written back to the client
				responseWrapper.copyBodyToResponse();
			}
		}
	}

	private AuditLogEntry buildAuditEntry(ContentCachingRequestWrapper request,
			ContentCachingResponseWrapper response, long durationMs) {

		AuditLogEntry.AuditLogEntryBuilder builder = AuditLogEntry.builder()
			.timestamp(Instant.now())
			.httpMethod(request.getMethod())
			.requestUri(request.getRequestURI())
			.queryString(request.getQueryString())
			.sourceIp(extractClientIp(request))
			.responseStatus(response.getStatus())
			.durationMs(durationMs)
			.userAgent(request.getHeader("User-Agent"));

		// Capture request body for POST/PUT/PATCH if enabled
		if (auditProperties.isLogRequestBody()) {
			String body = extractRequestBody(request);
			if (body != null && !body.isBlank()) {
				builder.requestBody(body);
			}
		}

		// Extract client identity from JWT
		extractJwtClaims(builder);

		// Extract OpenTelemetry trace ID
		extractTraceId(builder);

		return builder.build();
	}

	private String extractRequestBody(ContentCachingRequestWrapper request) {
		byte[] content = request.getContentAsByteArray();
		if (content.length == 0) {
			return null;
		}
		int length = Math.min(content.length, auditProperties.getMaxBodySize());
		return new String(content, 0, length, StandardCharsets.UTF_8);
	}

	private void extractJwtClaims(AuditLogEntry.AuditLogEntryBuilder builder) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication instanceof JwtAuthenticationToken jwtAuth) {
				Jwt jwt = jwtAuth.getToken();

				// Client ID: try "azp" (authorized party) first, then "client_id"
				String clientId = jwt.getClaimAsString("azp");
				if (clientId == null) {
					clientId = jwt.getClaimAsString("client_id");
				}
				builder.clientId(clientId);

				// User ID: try "preferred_username" first, then "sub"
				String userId = jwt.getClaimAsString("preferred_username");
				if (userId == null) {
					userId = jwt.getSubject();
				}
				builder.userId(userId);
			}
		}
		catch (Exception ex) {
			log.debug("Could not extract JWT claims for audit: {}", ex.getMessage());
		}
	}

	private void extractTraceId(AuditLogEntry.AuditLogEntryBuilder builder) {
		try {
			SpanContext spanContext = Span.current().getSpanContext();
			if (spanContext.isValid()) {
				builder.traceId(spanContext.getTraceId());
			}
		}
		catch (Exception ex) {
			log.debug("Could not extract OpenTelemetry trace ID for audit: {}", ex.getMessage());
		}
	}

	private String extractClientIp(HttpServletRequest request) {
		// Respect X-Forwarded-For header when behind a reverse proxy
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

}
