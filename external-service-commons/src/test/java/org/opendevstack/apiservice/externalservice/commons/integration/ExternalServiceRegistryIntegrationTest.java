package org.opendevstack.apiservice.externalservice.commons.integration;

import org.junit.jupiter.api.Test;
import org.opendevstack.apiservice.externalservice.commons.ExternalService;
import org.opendevstack.apiservice.externalservice.commons.command.ExternalServiceCommand;
import org.opendevstack.apiservice.externalservice.commons.registry.ExternalServiceRegistry;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify auto-registration works correctly.
 * This test simulates the Spring Boot auto-configuration behavior.
 */
class ExternalServiceRegistryIntegrationTest {

    @Test
    void testManualRegistrationAndRetrieval() {
        // Arrange
        ExternalServiceRegistry registry = new ExternalServiceRegistry();
        TestService testService = new TestService();
        TestCommand testCommand = new TestCommand();

        // Act - Manual registration (simulates what auto-configuration does)
        registry.registerService(testService);
        registry.registerCommand(testCommand);

        // Assert - Verify retrieval
        ExternalService retrievedService = registry.getService("test-service");
        assertNotNull(retrievedService);
        assertEquals("test-service", retrievedService.getServiceName());

        ExternalServiceCommand<?, ?> retrievedCommand = registry.getCommand("test-service", "test-command");
        assertNotNull(retrievedCommand);
        assertEquals("test-command", retrievedCommand.getCommandName());
        assertEquals("test-service", retrievedCommand.getServiceName());
    }

    @Test
    void testMultipleServicesAndCommands() {
        // Arrange
        ExternalServiceRegistry registry = new ExternalServiceRegistry();
        
        TestService service1 = new TestService();
        TestService2 service2 = new TestService2();
        TestCommand command1 = new TestCommand();
        TestCommand2 command2 = new TestCommand2();

        // Act
        registry.registerService(service1);
        registry.registerService(service2);
        registry.registerCommand(command1);
        registry.registerCommand(command2);

        // Assert
        assertEquals(2, registry.getAllServices().size());
        assertNotNull(registry.getService("test-service"));
        assertNotNull(registry.getService("test-service-2"));
        assertNotNull(registry.getCommand("test-service", "test-command"));
        assertNotNull(registry.getCommand("test-service-2", "test-command-2"));
    }

    @Test
    void testMultipleCommandsForSameService() {
        // Arrange
        ExternalServiceRegistry registry = new ExternalServiceRegistry();
        TestService testService = new TestService();
        TestCommand command1 = new TestCommand();
        TestCommand command2 = new TestCommand() {
            @Override
            public String getCommandName() {
                return "test-command-alternate";
            }
        };

        // Act
        registry.registerService(testService);
        registry.registerCommand(command1);
        registry.registerCommand(command2);

        // Assert - Verify both commands can be retrieved
        ExternalServiceCommand<?, ?> cmd1 = registry.getCommand("test-service", "test-command");
        ExternalServiceCommand<?, ?> cmd2 = registry.getCommand("test-service", "test-command-alternate");
        
        assertNotNull(cmd1);
        assertNotNull(cmd2);
        assertEquals("test-command", cmd1.getCommandName());
        assertEquals("test-command-alternate", cmd2.getCommandName());
    }

    // Test implementations
    @Component
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

    @Component
    static class TestService2 implements ExternalService {
        @Override
        public String getServiceName() {
            return "test-service-2";
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

    @Component
    static class TestCommand implements ExternalServiceCommand<String, String> {
        @Override
        public String execute(String request) {
            return "result";
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

    @Component
    static class TestCommand2 implements ExternalServiceCommand<String, String> {
        @Override
        public String execute(String request) {
            return "result-2";
        }

        @Override
        public String getCommandName() {
            return "test-command-2";
        }

        @Override
        public String getServiceName() {
            return "test-service-2";
        }
    }
}
