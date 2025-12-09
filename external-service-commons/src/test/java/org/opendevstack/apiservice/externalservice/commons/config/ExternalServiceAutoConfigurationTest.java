package org.opendevstack.apiservice.externalservice.commons.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.commons.ExternalServiceException;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.registry.ExternalServiceRegistry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ExternalServiceRegistry.
 * Verifies that services and commands can be registered and retrieved.
 */
class ExternalServiceAutoConfigurationTest {
    
    private ExternalServiceRegistry registry;
    private TestService testService;
    private TestCommand testCommand;
    
    @BeforeEach
    void setUp() {
        registry = new ExternalServiceRegistry();
        testService = new TestService();
        testCommand = new TestCommand();
        
        // Manually register for testing
        registry.registerService(testService);
        registry.registerCommand(testCommand);
    }
    
    @Test
    void testServiceRegistration() {
        // Verify the test service was registered
        ExternalService service = registry.getService("test-service");
        assertNotNull(service, "Test service should be registered");
        assertEquals("test-service", service.getServiceName());
    }
    
    @Test
    void testCommandRegistration() {
        // Verify the test command was registered
        ExternalServiceCommand<String, String> command = registry.getCommand("test-service", "test-command");
        assertNotNull(command, "Test command should be registered");
        assertEquals("test-command", command.getCommandName());
        assertEquals("test-service", command.getServiceName());
    }
    
    @Test
    void testCommandExecution() throws ExternalServiceException {
        // Verify command can be retrieved and executed
        ExternalServiceCommand<String, String> command = registry.getCommand("test-service", "test-command");
        assertNotNull(command);
        
        String result = command.execute("test");
        assertEquals("EXECUTED: test", result);
    }
    
    @Test
    void testServiceNotFound() {
        ExternalService service = registry.getService("non-existent");
        assertNull(service, "Non-existent service should return null");
    }
    
    @Test
    void testCommandNotFound() {
        ExternalServiceCommand<?, ?> command = registry.getCommand("test-service", "non-existent");
        assertNull(command, "Non-existent command should return null");
    }
    
    /**
     * Mock external service for testing.
     */
    static class TestService implements ExternalService {
        @Override
        public String getServiceName() {
            return "test-service";
        }
        
        @Override
        public boolean validateConnection() {
            return true;
        }
        
        @Override
        public boolean isHealthy() {
            return true;
        }
    }
    
    /**
     * Mock command for testing.
     */
    static class TestCommand implements ExternalServiceCommand<String, String> {
        @Override
        public String execute(String request) throws ExternalServiceException {
            return "EXECUTED: " + request;
        }
        
        @Override
        public String getCommandName() {
            return "test-command";
        }
        
        @Override
        public String getServiceName() {
            return "test-service";
        }
    }
}
