# Bitbucket Service - Command Pattern Implementation

## Overview

The `external-service-bitbucket` module provides integration with Atlassian Bitbucket Server through the standardized command pattern. This module wraps the Bitbucket REST API and exposes operations through a consistent, type-safe command interface.

## Architecture

### Multi-Instance Support

The Bitbucket service supports **multiple Bitbucket instances** configured in the application. Each command request must specify the `instanceName` to identify which Bitbucket server to use.

### Components

```
external-service-bitbucket/
├── command/                    # Command pattern implementation
│   └── branch/                 # Branch-related commands
│       ├── GetDefaultBranchCommand.java
│       ├── GetDefaultBranchRequest.java
│       ├── BranchExistsCommand.java
│       └── BranchExistsRequest.java
├── service/                    # Core service layer
│   ├── BitbucketService.java   # Service interface
│   └── impl/
│       └── BitbucketServiceImpl.java
├── client/                     # API client management
├── config/                     # Service configuration
├── exception/                  # Custom exceptions
└── BitbucketServiceAdapter.java  # Adapter for ExternalService interface
```

### Design Pattern

The service follows the **Adapter Pattern** for multi-instance support:
- `BitbucketService`: Core interface defining Bitbucket operations
- `BitbucketServiceImpl`: Implementation managing multiple instances
- `BitbucketServiceAdapter`: Adapter implementing `ExternalService` interface
- **Commands**: Wrap service operations with request validation and error handling

## Available Commands

### Branch Operations

#### 1. Get Default Branch

Retrieves the default branch name for a repository.

**Command Name**: `get-default-branch`

**Request**:
```java
GetDefaultBranchRequest request = GetDefaultBranchRequest.builder()
    .instanceName("dev")           // Required: Bitbucket instance name
    .projectKey("PROJ")            // Required: Project key
    .repositorySlug("my-repo")     // Required: Repository slug
    .build();
```

**Response**: `String` - The default branch name (e.g., "main", "master")

**Usage Example**:
```java
@Autowired
private ExternalServiceFacade externalServiceFacade;

// Execute command
CommandResult<String> result = externalServiceFacade.executeCommand(
    "bitbucket",
    "get-default-branch",
    request
);

if (result.isSuccess()) {
    String defaultBranch = result.getResult();
    log.info("Default branch: {}", defaultBranch);
} else {
    log.error("Failed: {}", result.getErrorMessage());
}
```

**Validation Rules**:
- `instanceName` must not be null or empty
- Instance must exist in configuration
- `projectKey` must not be null or empty
- `repositorySlug` must not be null or empty

**Error Codes**:
- `GET_DEFAULT_BRANCH_FAILED`: Failed to retrieve default branch

---

#### 2. Branch Exists

Checks if a specific branch exists in a repository.

**Command Name**: `branch-exists`

**Request**:
```java
BranchExistsRequest request = BranchExistsRequest.builder()
    .instanceName("dev")           // Required: Bitbucket instance name
    .projectKey("PROJ")            // Required: Project key
    .repositorySlug("my-repo")     // Required: Repository slug
    .branchName("feature/new")     // Required: Branch name to check
    .build();
```

**Response**: `Boolean` - `true` if branch exists, `false` otherwise

**Usage Example**:
```java
@Autowired
private ExternalServiceFacade externalServiceFacade;

// Execute command
CommandResult<Boolean> result = externalServiceFacade.executeCommand(
    "bitbucket",
    "branch-exists",
    request
);

if (result.isSuccess()) {
    Boolean exists = result.getResult();
    log.info("Branch exists: {}", exists);
}
```

**Validation Rules**:
- All rules from Get Default Branch, plus:
- `branchName` must not be null or empty

**Error Codes**:
- `BRANCH_EXISTS_CHECK_FAILED`: Failed to check branch existence

---

## Configuration

### Multi-Instance Setup

The Bitbucket service supports multiple instances configured in `application.yaml` or `application-local.yaml`. This allows connecting to different Bitbucket servers (e.g., dev, test, production).

#### Configuration Format

Add the following to your `core/src/main/resources/application.yaml`:

```yaml
externalservices:
  bitbucket:
    instances:
      # Development Bitbucket instance
      dev:
        # Base URL of your Bitbucket server (no trailing slash)
        # Example: https://bitbucket.mycompany.com or http://localhost:7990
        base-url: ${BITBUCKET_DEV_BASE_REST_URL:https://bitbucket.dev.example.com}
        
        # OPTION 1: Bearer Token (Personal Access Token) - RECOMMENDED
        bearer-token: ${BITBUCKET_DEV_BEARER_TOKEN:}
        
        # OPTION 2: Basic Authentication (only if bearer-token is not set)
        # Uncomment and configure if not using bearer token:
        # username: ${BITBUCKET_DEV_USERNAME:your-username}
        # password: ${BITBUCKET_DEV_PASSWORD:your-password-or-token}
        
        # Connection settings
        connection-timeout: 30000  # 30 seconds
        read-timeout: 30000        # 30 seconds
        
        # SSL/TLS certificate verification
        # WARNING: Only set to true in development environments!
        trust-all-certificates: ${BITBUCKET_DEV_TRUST_ALL:true}
      
      # Production Bitbucket instance (example)
      prod:
        base-url: ${BITBUCKET_PROD_BASE_REST_URL:https://bitbucket.prod.example.com}
        bearer-token: ${BITBUCKET_PROD_BEARER_TOKEN:}
        # username: ${BITBUCKET_PROD_USERNAME:your-username}
        # password: ${BITBUCKET_PROD_PASSWORD:your-password-or-token}
        connection-timeout: 30000
        read-timeout: 30000
        trust-all-certificates: ${BITBUCKET_PROD_TRUST_ALL:false}
```

### Authentication Options

#### Option 1: Bearer Token (Recommended)

Personal Access Tokens provide more security and fine-grained permissions:

1. **Create a Personal Access Token in Bitbucket**:
   - Go to **Bitbucket** → **User Settings** → **Personal Access Tokens**
   - Click **Create token**
   - Grant **READ** permissions for:
     - Projects
     - Repositories
   - Copy the generated token

2. **Configure the token**:
   ```yaml
   bearer-token: ${BITBUCKET_DEV_BEARER_TOKEN:}
   ```

3. **Set the environment variable**:
   ```bash
   export BITBUCKET_DEV_BEARER_TOKEN=your-personal-access-token-here
   ```

#### Option 2: Basic Authentication

Use username and password (or username and personal access token):

```yaml
username: ${BITBUCKET_DEV_USERNAME:admin}
password: ${BITBUCKET_DEV_PASSWORD:your-password-here}
```

Set environment variables:
```bash
export BITBUCKET_DEV_USERNAME=your-username
export BITBUCKET_DEV_PASSWORD=your-password-or-token
```

**Note**: If `bearer-token` is set, it takes precedence over username/password.

### Configuration Properties

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `base-url` | Yes | - | Base URL of Bitbucket server (no trailing slash) |
| `bearer-token` | No | - | Personal Access Token (preferred auth method) |
| `username` | No | - | Username for basic auth (if bearer-token not set) |
| `password` | No | - | Password or PAT for basic auth |
| `connection-timeout` | No | 30000 | Connection timeout in milliseconds |
| `read-timeout` | No | 30000 | Read timeout in milliseconds |
| `trust-all-certificates` | No | false | Disable SSL verification (DEV ONLY!) |

### Environment Variables

Instead of hardcoding values, use environment variables:

```bash
# Bitbucket URL
export BITBUCKET_DEV_BASE_REST_URL=https://bitbucket.mycompany.com

# Authentication (choose one method)
export BITBUCKET_DEV_BEARER_TOKEN=your-token-here
# OR
export BITBUCKET_DEV_USERNAME=your-username
export BITBUCKET_DEV_PASSWORD=your-password

# Optional settings
export BITBUCKET_DEV_TRUST_ALL=true
```

### Getting Available Instances

```java
@Autowired
private BitbucketService bitbucketService;

Set<String> instances = bitbucketService.getAvailableInstances();
// Returns: ["dev", "prod"]
```

### Checking Instance Availability

```java
boolean hasInstance = bitbucketService.hasInstance("dev");
// Returns: true if instance exists in configuration
```

---

## Command Registration

Commands are **auto-registered** on application startup:
1. Commands are annotated with `@Component`
2. They implement `ExternalServiceCommand<Q, R>`
3. `ExternalServiceAutoConfiguration` scans for all command beans
4. Commands are registered in `ExternalServiceRegistry`
5. Available via `ExternalServiceFacade` using command name

**No manual configuration required.**

---

## Error Handling

### Exception Hierarchy

All commands convert exceptions to `ExternalServiceException`:

```java
try {
    return bitbucketService.getDefaultBranch(...);
} catch (Exception e) {
    throw new ExternalServiceException(
        "Failed to get default branch: " + e.getMessage(),
        e,                              // Cause
        "GET_DEFAULT_BRANCH_FAILED",    // Error code
        "bitbucket",                    // Service name
        "getDefaultBranch"              // Operation
    );
}
```

### Common Error Scenarios

1. **Invalid Instance**: `IllegalArgumentException` during validation
2. **Connection Failed**: `ExternalServiceException` with network errors
3. **Authentication Failed**: `ExternalServiceException` with 401/403 errors
4. **Resource Not Found**: `ExternalServiceException` with 404 errors
5. **Validation Errors**: `IllegalArgumentException` for invalid requests

---

## Testing

### Unit Tests

Each command has comprehensive unit tests covering:
- Successful execution
- Exception handling and conversion
- Request validation (null checks, empty checks, instance existence)
- Command metadata (name, service name)

**Test Classes**:
- `GetDefaultBranchCommandTest` - 11 test cases
- `BranchExistsCommandTest` - 13 test cases
- `BitbucketServiceTest` - Service layer unit tests with mocks

**Run Unit Tests**:
```bash
# Run all unit tests for Bitbucket module
mvn test -pl external-service-bitbucket

# Run specific test class
mvn test -pl external-service-bitbucket -Dtest=BitbucketServiceTest

# Run specific test method
mvn test -pl external-service-bitbucket -Dtest=GetDefaultBranchCommandTest#testExecute_Success
```

### Integration Tests

Integration tests run against a **real Bitbucket instance** and are disabled by default. They verify the service can successfully communicate with Bitbucket and perform actual operations.

#### Prerequisites

1. **Access to a Bitbucket instance** (Server or Data Center)
2. **Valid credentials** (Personal Access Token or username/password)
3. **Test repository** with known project key and repository slug
4. **Network connectivity** to the Bitbucket server

#### Configuration Steps

**Step 1: Create Test Configuration**

Create or update `external-service-bitbucket/src/test/resources/application-local.yaml`:

```yaml
logging:
  level:
    org.opendevstack.apiservice.externalservice.bitbucket: DEBUG
    org.springframework.web.client.RestTemplate: DEBUG

externalservices:
  bitbucket:
    instances:
      dev:
        base-url: ${BITBUCKET_DEV_BASE_REST_URL:https://your-bitbucket-server.com}
        bearer-token: ${BITBUCKET_DEV_BEARER_TOKEN:}
        # OR use basic auth:
        # username: ${BITBUCKET_DEV_USERNAME:}
        # password: ${BITBUCKET_DEV_PASSWORD:}
        connection-timeout: 30000
        read-timeout: 30000
        trust-all-certificates: ${BITBUCKET_DEV_TRUST_ALL:true}
```

**Step 2: Set Environment Variables**

```bash
# Enable integration tests
export BITBUCKET_INTEGRATION_TEST_ENABLED=true

# Bitbucket connection details
export BITBUCKET_DEV_BASE_REST_URL=https://bitbucket.mycompany.com
export BITBUCKET_DEV_BEARER_TOKEN=your-personal-access-token

# OR use basic auth:
# export BITBUCKET_DEV_USERNAME=your-username
# export BITBUCKET_DEV_PASSWORD=your-password

# Test repository configuration
export BITBUCKET_TEST_INSTANCE=dev                          # Instance name from config
export BITBUCKET_TEST_PROJECT_KEY=MYPROJECT                 # Existing project key
export BITBUCKET_TEST_REPOSITORY_SLUG=my-test-repo          # Existing repository
export BITBUCKET_TEST_EXISTING_BRANCH=develop               # Known branch name

# Optional: SSL settings for self-signed certificates
export BITBUCKET_DEV_TRUST_ALL=true
```

**Step 3: Run Integration Tests**

```bash
# Run all integration tests
mvn test -pl external-service-bitbucket -Dtest=BitbucketServiceIntegrationTest

# Run specific integration test method
mvn test -pl external-service-bitbucket -Dtest=BitbucketServiceIntegrationTest#testGetDefaultBranch_Success
```

#### Integration Test Coverage

The `BitbucketServiceIntegrationTest` class includes the following tests:

| Test Method | Description |
|-------------|-------------|
| `testGetAvailableInstances` | Verifies configured instances are available |
| `testHasInstance` | Checks if a specific instance is configured |
| `testHasInstance_NonExistent` | Validates behavior for non-existent instances |
| `testGetDefaultBranch_Success` | Retrieves default branch from real repository |
| `testGetDefaultBranch_RepositoryNotFound` | Tests error handling for missing repositories |
| `testGetDefaultBranch_InvalidInstance` | Tests error for unconfigured instance |
| `testBranchExists_ExistingBranch` | Checks if default branch exists |
| `testBranchExists_NonExistentBranch` | Verifies non-existent branch returns false |
| `testBranchExists_ConfiguredTestBranch` | Tests configured test branch existence |
| `testBranchExists_RepositoryNotFound` | Tests branch check on missing repository |
| `testGetDefaultBranch_ConsistentResults` | Verifies multiple calls return same result |
| `testBranchExists_WithRefPrefix` | Tests branch lookup with/without refs/heads/ |

#### Example: Complete Integration Test Setup

```bash
#!/bin/bash
# integration-test.sh - Run Bitbucket integration tests

# 1. Configure Bitbucket connection
export BITBUCKET_DEV_BASE_REST_URL=https://bitbucket.example.com
export BITBUCKET_DEV_BEARER_TOKEN=MTIzNDU2Nzg5MDEyMzQ1Njc4OTA=

# 2. Enable integration tests
export BITBUCKET_INTEGRATION_TEST_ENABLED=true

# 3. Configure test repository details
export BITBUCKET_TEST_INSTANCE=dev
export BITBUCKET_TEST_PROJECT_KEY=DEVSTACK
export BITBUCKET_TEST_REPOSITORY_SLUG=devstack-api-service
export BITBUCKET_TEST_EXISTING_BRANCH=develop

# 4. Run integration tests
mvn test -pl external-service-bitbucket -Dtest=BitbucketServiceIntegrationTest
```

#### Troubleshooting Integration Tests

**Tests are skipped:**
```
Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
```

**Solution**: Set `BITBUCKET_INTEGRATION_TEST_ENABLED=true`

---

**Connection refused / timeout:**
```
ExternalServiceException: Failed to get default branch: Connection refused
```

**Solutions**:
1. Verify `BITBUCKET_DEV_BASE_REST_URL` is correct
2. Check network connectivity: `curl -I https://your-bitbucket-server.com`
3. Verify firewall rules allow outbound HTTPS
4. Check Bitbucket server is running

---

**Authentication failed (401/403):**
```
ExternalServiceException: Failed to get default branch: 401 Unauthorized
```

**Solutions**:
1. Verify bearer token is valid and not expired
2. Check token has READ permissions for Projects and Repositories
3. Test authentication manually:
   ```bash
   curl -H "Authorization: Bearer YOUR_TOKEN" \
        https://bitbucket.example.com/rest/api/1.0/projects
   ```

---

**Repository not found (404):**
```
BitbucketException: Repository not found
```

**Solutions**:
1. Verify `BITBUCKET_TEST_PROJECT_KEY` exists
2. Check `BITBUCKET_TEST_REPOSITORY_SLUG` is correct
3. Ensure your user has access to the repository
4. List repositories to verify:
   ```bash
   curl -H "Authorization: Bearer YOUR_TOKEN" \
        https://bitbucket.example.com/rest/api/1.0/projects/PROJ/repos
   ```

---

**SSL certificate errors:**
```
javax.net.ssl.SSLHandshakeException: PKIX path building failed
```

**Solutions**:
1. Set `trust-all-certificates: true` in application-local.yaml (DEV ONLY)
2. Or import Bitbucket certificate to Java truststore
3. Set `BITBUCKET_DEV_TRUST_ALL=true`

### Test Best Practices

1. **Use dedicated test repositories** - Don't test against production repositories
2. **Clean up test data** - Remove any test branches/resources created during testing
3. **Use read-only operations** - Integration tests should primarily use GET operations
4. **Set appropriate timeouts** - Adjust `connection-timeout` and `read-timeout` for slow networks
5. **Rotate credentials regularly** - Update Personal Access Tokens periodically
6. **Run integration tests in CI/CD** - Automate testing against staging Bitbucket instances

---

## Usage Patterns

### Direct Service Usage (Legacy)

```java
@Autowired
private BitbucketService bitbucketService;

String branch = bitbucketService.getDefaultBranch("dev", "PROJ", "my-repo");
boolean exists = bitbucketService.branchExists("dev", "PROJ", "my-repo", "feature/test");
```

### Command Pattern Usage (Recommended)

```java
@Autowired
private ExternalServiceFacade facade;

// Get default branch
GetDefaultBranchRequest req = GetDefaultBranchRequest.builder()
    .instanceName("dev")
    .projectKey("PROJ")
    .repositorySlug("my-repo")
    .build();

CommandResult<String> result = facade.executeCommand("bitbucket", "get-default-branch", req);
```

**Benefits of Command Pattern**:
- Type-safe request/response
- Standardized error handling
- Request validation
- Consistent API across all external services
- Easy to add new operations
- Testable in isolation

---

## Future Enhancements

Potential additional commands based on Bitbucket API capabilities:

### Branch Operations (Future)
- `create-branch` - Create new branch
- `delete-branch` - Delete branch
- `list-branches` - List all branches with filtering
- `set-default-branch` - Update default branch

### Repository Operations (Future)
- `get-repository` - Get repository details
- `create-repository` - Create new repository
- `delete-repository` - Delete repository
- `list-repositories` - List repositories in project

### Pull Request Operations (Future)
- `get-pull-request` - Get PR details
- `create-pull-request` - Create new PR
- `merge-pull-request` - Merge PR
- `list-pull-requests` - List PRs

### Webhook Operations (Future)
- `create-webhook` - Create webhook
- `delete-webhook` - Delete webhook
- `list-webhooks` - List webhooks

---

## Dependencies

### Internal Dependencies
- `external-service-commons`: Base command interfaces and exceptions
- `core`: Core application configuration

### External Dependencies
- Bitbucket REST API (v8.9+)
- OpenAPI Generator for API client generation
- Spring Framework for dependency injection
- Lombok for builder pattern

---

## API Client Generation

The module uses OpenAPI Generator to create API clients from the Bitbucket OpenAPI specification:

**OpenAPI Spec**: `openapi/openapi-bitbucket-v89.yaml`

**Generated Clients** (in `target/generated-sources`):
- `BranchApi` - Branch operations
- `RepositoryApi` - Repository operations
- `PullRequestsApi` - Pull request operations
- And 11 more specialized APIs

**Regenerate Clients**:
```bash
mvn clean generate-sources -pl external-service-bitbucket
```

---

## Troubleshooting

### Command Not Found

```java
ExternalServiceException: Command 'get-default-branch' not found for service 'bitbucket'
```

**Solution**: Ensure:
1. Command class is annotated with `@Component`
2. Command is in package scanned by Spring
3. Application has started successfully
4. Check logs for registration errors

### Instance Not Found

```java
IllegalArgumentException: Bitbucket instance 'dev' does not exist
```

**Solution**: 
1. Check `application.yaml` for instance configuration
2. Verify instance name matches configuration
3. Use `getAvailableInstances()` to list configured instances

### Connection Errors

```java
ExternalServiceException: Failed to get default branch: Connection refused
```

**Solution**:
1. Verify Bitbucket URL is accessible
2. Check network connectivity
3. Validate credentials
4. Use `validateConnection("instance")` to test
5. Check Bitbucket server status

---

## References

- [Command Pattern Implementation Summary](../external-service-standardization/IMPLEMENTATION_SUMMARY.md)
- [Creating New External Service](../external-service-standardization/CREATING_NEW_SERVICE.md)
- [Bitbucket REST API Documentation](https://docs.atlassian.com/bitbucket-server/rest/)
