package org.opendevstack.apiservice.core.audit;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLoggingFilterTest {

    @Mock
    private AuditService auditService;

    @Captor
    private ArgumentCaptor<AuditLogEntry> entryCaptor;

    private AuditProperties auditProperties;

    private AuditLoggingFilter filter;

    @BeforeEach
    void setUp() {
        auditProperties = new AuditProperties();
        auditProperties.setEnabled(true);
        auditProperties.setUrlPatterns(List.of("/api/v1/**"));
        auditProperties.setLogRequestBody(true);
        auditProperties.setMaxBodySize(10240);
        filter = new AuditLoggingFilter(auditService, auditProperties);
    }

    @Test
    void doFilterInternal_capturesRequestMetadata() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/projects");
        request.setQueryString("page=1&size=10");
        request.addHeader("User-Agent", "TestClient/1.0");
        request.setRemoteAddr("192.168.1.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        FilterChain filterChain = (req, res) -> res.getWriter().write("OK");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(auditService).saveAuditLog(entryCaptor.capture());
        AuditLogEntry entry = entryCaptor.getValue();

        assertAll(
                () -> assertEquals("GET", entry.getHttpMethod()),
                () -> assertEquals("/api/v1/projects", entry.getRequestUri()),
                () -> assertEquals("page=1&size=10", entry.getQueryString()),
                () -> assertEquals("192.168.1.1", entry.getSourceIp()),
                () -> assertEquals(200, entry.getResponseStatus()),
                () -> assertNotNull(entry.getTimestamp()),
                () -> assertTrue(entry.getDurationMs() >= 0),
                () -> assertEquals("TestClient/1.0", entry.getUserAgent())
        );
    }

    @Test
    void doFilterInternal_respectsXForwardedForHeader() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/projects");
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        request.setRemoteAddr("192.168.1.1");
        request.setContent("{\"name\":\"test\"}".getBytes());
        request.setContentType("application/json");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            // Read the request body so ContentCachingRequestWrapper caches it
            req.getInputStream().readAllBytes();
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(201);
        };

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(auditService).saveAuditLog(entryCaptor.capture());
        AuditLogEntry entry = entryCaptor.getValue();

        assertEquals("10.0.0.1", entry.getSourceIp());
        assertEquals("{\"name\":\"test\"}", entry.getRequestBody());
    }

    @Test
    void shouldNotFilter_returnsTrueWhenDisabled() {
        // Given
        auditProperties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/projects");

        // When / Then
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_returnsTrueWhenUriDoesNotMatchPatterns() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");

        // When / Then
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_returnsFalseWhenUriMatchesPattern() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");

        // When / Then
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_doesNotBreakResponseOnAuditError() throws Exception {
        // Given
        doThrow(new RuntimeException("audit boom")).when(auditService).saveAuditLog(any());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/projects");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> res.getWriter().write("OK");

        // When / Then — no exception propagated to the client
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
    }

}
