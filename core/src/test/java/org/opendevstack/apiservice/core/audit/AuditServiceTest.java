package org.opendevstack.apiservice.core.audit;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    private AuditService auditService;

    private ListAppender<ILoggingEvent> auditAppender;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        auditService = new AuditService(objectMapper);

        // Attach a ListAppender to the AUDIT logger so we can inspect emitted messages
        Logger auditLogger = (Logger) LoggerFactory.getLogger("AUDIT");
        auditAppender = new ListAppender<>();
        auditAppender.start();
        auditLogger.addAppender(auditAppender);
    }

    @Test
    void saveAuditLog_emitsJsonToAuditLogger() {
        // Given
        AuditLogEntry entry = AuditLogEntry.builder()
                .timestamp(Instant.parse("2026-02-26T10:00:00Z"))
                .httpMethod("GET")
                .requestUri("/api/v1/projects")
                .responseStatus(200)
                .durationMs(42)
                .clientId("my-app")
                .userId("john.doe")
                .sourceIp("10.0.0.1")
                .traceId("abc123def456")
                .userAgent("JUnit/5")
                .build();

        // When
        auditService.saveAuditLog(entry);

        // Then
        assertEquals(1, auditAppender.list.size());
        String json = auditAppender.list.get(0).getFormattedMessage();

        // Verify it's valid JSON containing expected fields
        assertAll(
                () -> assertTrue(json.contains("\"http_method\":\"GET\"")),
                () -> assertTrue(json.contains("\"request_uri\":\"/api/v1/projects\"")),
                () -> assertTrue(json.contains("\"response_status\":200")),
                () -> assertTrue(json.contains("\"duration_ms\":42")),
                () -> assertTrue(json.contains("\"client_id\":\"my-app\"")),
                () -> assertTrue(json.contains("\"user_id\":\"john.doe\"")),
                () -> assertTrue(json.contains("\"source_ip\":\"10.0.0.1\"")),
                () -> assertTrue(json.contains("\"trace_id\":\"abc123def456\"")),
                () -> assertTrue(json.contains("\"user_agent\":\"JUnit/5\""))
        );
    }

    @Test
    void saveAuditLog_omitsNullFields() {
        // Given — only required fields set
        AuditLogEntry entry = AuditLogEntry.builder()
                .timestamp(Instant.now())
                .httpMethod("DELETE")
                .requestUri("/api/v1/projects/123")
                .responseStatus(204)
                .durationMs(10)
                .build();

        // When
        auditService.saveAuditLog(entry);

        // Then
        assertEquals(1, auditAppender.list.size());
        String json = auditAppender.list.get(0).getFormattedMessage();

        assertAll(
                () -> assertTrue(json.contains("\"http_method\":\"DELETE\"")),
                () -> assertFalse(json.contains("\"client_id\"")),
                () -> assertFalse(json.contains("\"user_id\"")),
                () -> assertFalse(json.contains("\"query_string\"")),
                () -> assertFalse(json.contains("\"request_body\"")),
                () -> assertFalse(json.contains("\"trace_id\"")),
                () -> assertFalse(json.contains("\"user_agent\""))
        );
    }

    @Test
    void saveAuditLog_doesNotThrowOnSerializationError() {
        // Given — a broken ObjectMapper that always fails
        ObjectMapper brokenMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws com.fasterxml.jackson.core.JsonProcessingException {
                throw new com.fasterxml.jackson.core.JsonProcessingException("Simulated failure") {};
            }
        };
        AuditService brokenService = new AuditService(brokenMapper);

        AuditLogEntry entry = AuditLogEntry.builder()
                .timestamp(Instant.now())
                .httpMethod("POST")
                .requestUri("/api/v1/test")
                .responseStatus(500)
                .durationMs(1)
                .build();

        // When / Then — no exception propagated
        assertDoesNotThrow(() -> brokenService.saveAuditLog(entry));

        // And nothing was emitted to the AUDIT logger
        assertEquals(0, auditAppender.list.size());
    }

}
