# OpenShift External Service

This module provides a service for interacting with multiple OpenShift Container Platform (OCP) instances. It uses a factory pattern to create configured API clients and supports operations like retrieving secrets from different clusters.

## Features

- **Multiple Instance Support**: Configure and interact with multiple OpenShift clusters
- **Factory Pattern**: Efficient client creation and caching using the factory pattern
- **Secret Management**: Retrieve and decode secrets from OpenShift clusters
- **Namespace Support**: Work with secrets across different namespaces
- **Spring Configuration**: Load all configuration from Spring application properties
- **SSL/TLS Configuration**: Flexible SSL certificate handling for different environments

## Architecture & Design Patterns

The module follows the architecture guidelines using multiple design patterns:

### Factory Pattern
- `OpenshiftApiClientFactory` creates and caches API clients for different instances
- Ensures efficient resource management and connection pooling

### Adapter Pattern
- `OpenshiftServiceAdapter` adapts `OpenshiftService` to implement the `ExternalService` interface
- Enables multi-instance support with unified health checking

### Command Pattern
- All secret operations are wrapped in command classes:
  - `GetSecretCommand`: Retrieve complete secrets
  - `GetSecretValueCommand`: Retrieve specific secret values
  - `SecretExistsCommand`: Check secret existence
- Commands are automatically registered with `ExternalServiceRegistry`
- Provides type-safety, decoupling, and extensibility

### Registry Pattern
- `ExternalServiceRegistry` manages all available commands and services
- Commands are auto-discovered via Spring's component scanning

## Command Pattern Benefits

1. **Type Safety**: Request/Response objects provide compile-time type checking
2. **Decoupling**: Business logic doesn't depend on specific service implementations
3. **Extensibility**: New commands can be added without modifying existing code
4. **Testability**: Commands can be mocked and tested independently
5. **Async Support**: Commands can be executed asynchronously via the executor
6. **Consistency**: All external service operations follow the same pattern



## Configuration

### Spring Configuration

Add the following configuration to your `application.yaml` or `application-{profile}.yaml`:

```yaml
openshift:
  instances:
    # First OpenShift instance
    dev:
      api-url: https://api.dev.ocp.example.com:6443
      token: your-authentication-token
      namespace: default-namespace
      connection-timeout: 30000
      read-timeout: 30000
      trust-all-certificates: false
    
    # Second OpenShift instance
    prod:
      api-url: https://api.prod.ocp.example.com:6443
      token: your-prod-token
      namespace: production
      connection-timeout: 30000
      read-timeout: 30000
      trust-all-certificates: false
```

### Configuration Properties

For each OpenShift instance:

| Property | Description | Required | Default |
|----------|-------------|----------|---------|
| `api-url` | The OpenShift API URL | Yes | - |
| `token` | Authentication token | Yes | - |
| `namespace` | Default namespace | Yes | - |
| `connection-timeout` | Connection timeout in milliseconds | No | 30000 |
| `read-timeout` | Read timeout in milliseconds | No | 30000 |
| `trust-all-certificates` | Trust all SSL certificates (DEV ONLY!) | No | false |

### Environment Variables

You can use environment variables in your configuration:

```yaml
openshift:
  instances:
    dev:
      api-url: ${OPENSHIFT_DEV_API_URL}
      token: ${OPENSHIFT_DEV_TOKEN}
      namespace: ${OPENSHIFT_DEV_NAMESPACE:devstack-dev}
```

## Usage

### Using Commands (Recommended - Command Pattern)

The module uses the **Command Pattern** for all external service operations, providing type-safe, decoupled operations. Commands are automatically registered with the ExternalServiceRegistry.

#### Get Secret Command

```java
@Service
public class ConfigurationService {
    
    private final ExternalServiceFacade externalServiceFacade;
    
    public void loadSecret() throws ExternalServiceException {
        GetSecretRequest request = GetSecretRequest.builder()
                .instanceName("dev")
                .secretName("my-secret")
                .build();
        
        CommandResult<Map<String, String>> result = externalServiceFacade.executeCommand(request);
        Map<String, String> secretData = result.getResult();
    }
}
```

#### Get Secret Value Command

```java
@Service
public class DatabaseService {
    
    private final ExternalServiceFacade externalServiceFacade;
    
    public String getDatabasePassword() throws ExternalServiceException {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .instanceName("prod")
                .secretName("database-credentials")
                .key("password")
                .namespace("production")
                .build();
        
        CommandResult<String> result = externalServiceFacade.executeCommand(request);
        return result.getResult();
    }
}
```

#### Check Secret Exists Command

```java
@Service
public class SecretValidationService {
    
    private final ExternalServiceFacade externalServiceFacade;
    
    public boolean validateSecretExists() throws ExternalServiceException {
        SecretExistsRequest request = SecretExistsRequest.builder()
                .instanceName("dev")
                .secretName("my-secret")
                .build();
        
        CommandResult<Boolean> result = externalServiceFacade.executeCommand(request);
        return result.getResult();
    }
}
```

### Available Commands

| Command | Request Class | Response Type | Description |
|---------|--------------|---------------|-------------|
| `get-secret` | `GetSecretRequest` | `Map<String, String>` | Retrieve entire secret from OpenShift |
| `get-secret-value` | `GetSecretValueRequest` | `String` | Retrieve specific value from secret |
| `secret-exists` | `SecretExistsRequest` | `Boolean` | Check if secret exists in OpenShift |

### Direct Service Usage (Legacy)

While the Command Pattern is recommended, you can still use the service directly:

```java
@Service
public class MyService {
    
    private final OpenshiftService openshiftService;
    
    public MyService(OpenshiftService openshiftService) {
        this.openshiftService = openshiftService;
    }
    
    public void retrieveSecret() throws OpenshiftException {
        // Get entire secret from 'dev' instance
        Map<String, String> secretData = openshiftService.getSecret("dev", "my-secret");
        
        // Get specific value from secret
        String password = openshiftService.getSecretValue("dev", "my-secret", "password");
    }
}
```

### Working with Different Instances

```java
// Get secret from development instance
Map<String, String> devSecret = openshiftService.getSecret("dev", "database-credentials");

// Get secret from production instance
Map<String, String> prodSecret = openshiftService.getSecret("prod", "database-credentials");
```

### Working with Different Namespaces

```java
// Use default namespace (from configuration)
String apiKey = openshiftService.getSecretValue("dev", "api-keys", "github-token");

// Use specific namespace
String dbPassword = openshiftService.getSecretValue("dev", "db-creds", "password", "custom-namespace");
```

### Checking Secret Existence

```java
if (openshiftService.secretExists("dev", "my-secret")) {
    // Secret exists, proceed with retrieval
    Map<String, String> secret = openshiftService.getSecret("dev", "my-secret");
}
```

### Listing Available Instances

```java
Set<String> instances = openshiftService.getAvailableInstances();
System.out.println("Available OpenShift instances: " + instances);

// Check if specific instance is configured
if (openshiftService.hasInstance("prod")) {
    // Use production instance
}
```

## API Methods

### OpenshiftService Interface

| Method | Description |
|--------|-------------|
| `getSecret(instanceName, secretName)` | Get all data from a secret |
| `getSecret(instanceName, secretName, namespace)` | Get all data from a secret in specific namespace |
| `getSecretValue(instanceName, secretName, key)` | Get specific value from a secret |
| `getSecretValue(instanceName, secretName, key, namespace)` | Get specific value from a secret in specific namespace |
| `secretExists(instanceName, secretName)` | Check if secret exists |
| `secretExists(instanceName, secretName, namespace)` | Check if secret exists in specific namespace |
| `getAvailableInstances()` | Get all configured instance names |
| `hasInstance(instanceName)` | Check if instance is configured |

## Security Considerations

### Authentication

The service uses bearer token authentication. Ensure your tokens have appropriate permissions:

```bash
# Get token from OpenShift
oc whoami -t
```

### SSL/TLS Configuration

#### Production Environments

In production, always use proper SSL certificate validation:

```yaml
openshift:
  instances:
    prod:
      trust-all-certificates: false  # Default and recommended
```

#### Development Environments

For development with self-signed certificates:

```yaml
openshift:
  instances:
    dev:
      trust-all-certificates: true  # Use only in development!
```

⚠️ **WARNING**: Never use `trust-all-certificates: true` in production!

### Token Management

- Store tokens securely (use environment variables or secret management systems)
- Use service accounts with minimal required permissions
- Rotate tokens regularly
- Never commit tokens to version control

## Error Handling

The service throws `OpenshiftException` for various error scenarios:

```java
try {
    String secret = openshiftService.getSecretValue("dev", "my-secret", "password");
} catch (OpenshiftException e) {
    // Handle error (instance not configured, secret not found, network error, etc.)
    log.error("Failed to retrieve secret", e);
}
```

Common error scenarios:
- Instance not configured
- Invalid authentication token
- Secret not found
- Network connectivity issues
- Invalid namespace
- Key not found in secret

## Testing

### Unit Tests

Unit tests are provided for all commands using Mockito for dependency mocking:

```java
@ExtendWith(MockitoExtension.class)
class GetSecretCommandTest {
    
    @Mock
    private OpenshiftService openshiftService;
    
    @InjectMocks
    private GetSecretCommand command;
    
    @Test
    void execute_shouldReturnSecretData_whenSuccessful() throws Exception {
        // Given
        when(openshiftService.hasInstance("dev")).thenReturn(true);
        when(openshiftService.getSecret("dev", "my-secret")).thenReturn(sampleSecretData);
        
        // When
        Map<String, String> result = command.execute(request);
        
        // Then
        assertThat(result).isEqualTo(sampleSecretData);
    }
}
```

**Available Unit Tests:**
- `GetSecretCommandTest` - Tests for retrieving complete secrets
- `GetSecretValueCommandTest` - Tests for retrieving specific secret values
- `SecretExistsCommandTest` - Tests for checking secret existence

Each test covers:
- Successful execution with and without namespace
- Exception handling
- Request validation
- Edge cases and error scenarios

### Integration Tests

Integration tests are provided to test against a real OpenShift cluster:

```bash
# Set up environment variables
export OPENSHIFT_INTEGRATION_TEST_ENABLED=true
export OPENSHIFT_TEST_INSTANCE=dev
export OPENSHIFT_TEST_SECRET_NAME=test-credentials
export OPENSHIFT_TEST_NAMESPACE=default
export OPENSHIFT_TEST_SECRET_KEY=password

# Run integration tests
mvn test -Dtest=OpenshiftCommandIntegrationTest
```

**Integration Test Coverage:**
- Connection validation to OpenShift instances
- Health checks for configured instances
- Actual secret retrieval from OpenShift
- Namespace-specific secret operations
- Error handling for missing secrets/keys
- Invalid instance handling

**Requirements for Integration Tests:**
1. Valid `application-local.yaml` with OpenShift configuration
2. Real OpenShift cluster connectivity
3. Valid authentication tokens
4. Pre-existing test secrets in the cluster

**Important: Tests Will Fail on Connection/Auth Errors**
- Integration tests are designed to **fail** (not skip) when there are actual connectivity or authentication problems
- Tests validate the connection before executing operations
- If you see test failures with `401 Unauthorized` or connection errors, check your OpenShift credentials and connectivity
- Tests only skip when optional environment variables (like `OPENSHIFT_TEST_NAMESPACE`) are not set

**Sample Test Configuration:**
```yaml
# application-local.yaml
openshift:
  instances:
    dev:
      api-url: https://api.dev.example.com:6443
      token: ${OPENSHIFT_DEV_TOKEN}
      namespace: default
      connection-timeout: 30000
      read-timeout: 30000
      trust-all-certificates: false
```

### Running Tests

```bash
# Run all tests (unit tests only, no integration tests by default)
mvn test

# Run unit tests for a specific class
mvn test -Dtest=GetSecretCommandTest

# Run integration tests (when OPENSHIFT_INTEGRATION_TEST_ENABLED=true)
mvn test -Dtest=OpenshiftCommandIntegrationTest

# Run all tests including integration
mvn test -Dtest=**/*Test.java
```

### Example Test using the service

```java
@SpringBootTest
class OpenshiftServiceTest {
    
    @Autowired
    private OpenshiftService openshiftService;
    
    @Test
    void testGetSecret() throws OpenshiftException {
        // Ensure test instance is configured
        assumeTrue(openshiftService.hasInstance("test"));
        
        // Test retrieving secret
        Map<String, String> secret = openshiftService.getSecret("test", "test-secret");
        assertNotNull(secret);
    }
}
```
```

## Dependencies

Required dependencies (already included in `pom.xml`):

- Spring Boot Starter
- Spring Boot Starter Web
- Jackson (for JSON processing)
- Lombok (for code generation)

## Examples

### Example 1: Database Credentials

```java
@Service
public class DatabaseService {
    
    private final OpenshiftService openshiftService;
    
    public DataSource createDataSource(String environment) throws OpenshiftException {
        String username = openshiftService.getSecretValue(environment, "database-creds", "username");
        String password = openshiftService.getSecretValue(environment, "database-creds", "password");
        String url = openshiftService.getSecretValue(environment, "database-creds", "url");
        
        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
}
```

### Example 2: API Keys

```java
@Service
public class ExternalApiService {
    
    private final OpenshiftService openshiftService;
    private final RestTemplate restTemplate;
    
    public String callExternalApi() throws OpenshiftException {
        String apiKey = openshiftService.getSecretValue("prod", "api-keys", "external-api-key");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        
        return restTemplate.exchange(
            "https://api.example.com/data",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        ).getBody();
    }
}
```

### Example 3: Multi-Environment Configuration

```java
@Service
public class ConfigurationService {
    
    private final OpenshiftService openshiftService;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    public Map<String, String> getEnvironmentConfig() throws OpenshiftException {
        // Use different OpenShift instances based on active profile
        String instanceName = switch (activeProfile) {
            case "dev" -> "dev";
            case "test" -> "test";
            case "prod" -> "prod";
            default -> "dev";
        };
        
        return openshiftService.getSecret(instanceName, "app-config");
    }
}
```

## Troubleshooting

### Common Issues

1. **Connection Timeout**
   - Increase `connection-timeout` value
   - Check network connectivity to OpenShift cluster
   - Verify API URL is correct

2. **Authentication Failed**
   - Verify token is valid and not expired
   - Check token has necessary permissions
   - Generate new token: `oc whoami -t`

3. **Secret Not Found**
   - Verify secret exists: `oc get secret <name> -n <namespace>`
   - Check namespace is correct
   - Ensure service account has read permissions

4. **SSL Certificate Issues**
   - For development, use `trust-all-certificates: true`
   - For production, ensure proper certificate trust chain
   - Import certificates into Java truststore if needed

## Contributing

When contributing to this module:

1. **Maintain Design Patterns**:
   - Use Command Pattern for new operations
   - Keep Factory Pattern for API client creation
   - Use Adapter Pattern for multi-instance support

2. **Create Command Implementation**:
   - Create request DTO in `org.opendevstack.apiservice.externalservice.ocp.command.secret` package
   - Create command class implementing `ExternalServiceCommand<Request, Response>`
   - Mark command with `@Component` for auto-registration
   - Implement proper validation in `validateRequest()`
   - Add appropriate logging

3. **Add Comprehensive Tests**:
   - Create unit tests with mocks in `src/test/java/.../command/secret/*CommandTest.java`
   - Test successful execution
   - Test error scenarios
   - Test request validation
   - Test with and without optional fields (namespace)

4. **Add Integration Tests**:
   - Add integration test cases in `OpenshiftCommandIntegrationTest`
   - Test against real OpenShift cluster
   - Test with actual secrets and namespaces
   - Verify error handling with real infrastructure

5. **Update Documentation**:
   - Add command documentation to README.md
   - Include usage examples with command pattern
   - Document required configuration changes
   - Update test instructions if needed

6. **Code Quality**:
   - Follow existing code style
   - Use Lombok for boilerplate (getters, setters, builders)
   - Add comprehensive Javadoc
   - Keep logging consistent with existing patterns
   - Handle exceptions properly with ExternalServiceException

## License

This module is part of the DevStack API Service project.
