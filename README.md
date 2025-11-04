# Sample HTTP Client Configurations

A demonstration repository showing how to properly configure internal HTTP client timeouts for various Spring REST implementations using Apache HttpClient and Reactor Netty.

## Overview

This repository provides working examples of configuring low-level HTTP client settings for Spring's REST client implementations:

- **Apache HttpClient 5** - Used by `RestTemplate` and `RestClient`
- **Reactor Netty** - Used by `WebClient`

These samples demonstrate Spring Boot 3.4+ features including:
- `ClientHttpRequestFactoryBuilderCustomizer` for unified HTTP client configuration
- `ClientHttpConnectorBuilderCustomizer` for reactive HTTP client configuration
- Externalized configuration properties for all timeout and connection pool settings
- Fluent builder API for customizing HTTP clients without replacing entire beans
- OAuth2 client credentials with `issuer-uri` auto-discovery

**Current Version:** Spring Boot 3.5.7

## Repository Structure

```
sample-httpclient-configurations/
├── restclient-resttemplate-sample/     # Apache HttpClient configuration
│   └── src/main/
│       ├── java/miller79/
│       │   ├── ApacheHttpClientConfiguration.java
│       │   ├── ApacheHttpClientConfigurationProperties.java
│       │   ├── SecurityConfiguration.java
│       │   ├── RestClientConfiguration.java
│       │   ├── RestTemplateConfiguration.java
│       │   ├── Application.java
│       │   └── SampleImplementation.java
│       └── resources/
│           └── application.yaml
│
└── webclient-sample/                    # Reactor Netty configuration
    └── src/main/
        ├── java/miller79/
        │   ├── ReactorHttpClientConfiguration.java
        │   ├── ReactorHttpClientConfigurationProperties.java
        │   ├── SecurityConfiguration.java
        │   ├── WebClientConfiguration.java
        │   ├── Application.java
        │   └── SampleImplementation.java
        └── resources/
            └── application.yaml
```

## Apache HttpClient Configuration (RestTemplate/RestClient)

The `restclient-resttemplate-sample` demonstrates how to configure:

### Connection Pool Settings
- **Max Total Connections**: Total connections across all routes
- **Max Connections Per Route**: Configured via connection manager

### Timeout Configuration
- **Response Timeout**: Time to wait for response data
- **Socket Timeout**: Socket-level read timeout (set via SocketConfig)
- **Time-to-Live**: Maximum lifetime of a connection
- **Validate After Inactivity**: When to validate connections before reuse

### Keep-Alive Settings
- **TCP Keep-Alive**: Socket-level keep-alive configuration
- **TCP Keep Idle**: Time before first keep-alive probe
- **TCP Keep Interval**: Time between keep-alive probes
- **TCP Keep Count**: Number of failed probes before connection is dead
- **Idle Eviction**: Automatic cleanup of idle connections

### Key Classes
- `ApacheHttpClientConfiguration.java` - Creates `ClientHttpRequestFactoryBuilderCustomizer` bean
- `ApacheHttpClientConfigurationProperties.java` - Externalized configuration properties
- `SecurityConfiguration.java` - OAuth2 client credentials authentication setup
- `RestClientConfiguration.java` - RestClient setup (with and without OAuth2 authentication)
- `RestTemplateConfiguration.java` - RestTemplate setup (with and without OAuth2 authentication)
- `Application.java` - Spring Boot application entry point
- `SampleImplementation.java` - Demonstration of configured clients

## Reactor Netty Configuration (WebClient)

The `webclient-sample` demonstrates how to configure:

### Connection Provider Settings
- **Connection Pool Name**: Identifier for the connection pool
- **Max Connections**: Maximum number of connections
- **Max Idle Time**: How long connections can remain idle
- **Max Life Time**: Maximum lifetime of connections
- **Evict In Background**: Background cleanup interval

### Network Options
- **SO_KEEPALIVE**: Socket keep-alive flag
- **TCP_KEEPIDLE**: Time before sending keep-alive probes
- **TCP_KEEPINTVL**: Interval between keep-alive probes
- **TCP_KEEPCNT**: Number of keep-alive probes
- **Response Timeout**: Maximum time to wait for response

### Key Classes
- `ReactorHttpClientConfiguration.java` - Creates `ClientHttpConnectorBuilderCustomizer` bean
- `ReactorHttpClientConfigurationProperties.java` - Externalized configuration properties
- `SecurityConfiguration.java` - OAuth2 client credentials authentication setup (reactive)
- `WebClientConfiguration.java` - WebClient setup (with and without OAuth2 authentication)
- `Application.java` - Spring Boot application entry point
- `SampleImplementation.java` - Demonstration of configured WebClient

## Configuration Properties

Both samples support externalized configuration via `application.yaml` or `application.properties`.

### Apache HttpClient Example (application.yaml)

The `restclient-resttemplate-sample` includes an `application.yaml` file with example configuration:

```yaml
miller79:
  apache:
    response-timeout: 10s
    # Additional properties can be configured:
    # max-connections: 100
    # max-idle-time: 60s
    # max-life-time: 120s
    # so-keep-alive: true
    # tcp-keep-idle: 30s
    # tcp-keep-interval: 5s
    # tcp-keep-count: 3

spring:
  security:
    oauth2:
      client:
        provider:
          serviceAccount:
            issuer-uri: ${ISSUER_URI:https://accounts.google.com}
        registration:
          serviceAccount:
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET}
            authorization-grant-type: client_credentials
  http:
    client:
      factory: http-components
      read-timeout: 17s
      connect-timeout: 16s
```

**Note:** The example uses environment variables (`${CLIENT_ID}`, `${CLIENT_SECRET}`, `${ISSUER_URI}`) for sensitive OAuth2 credentials. These should be set in your environment or replaced with actual values.

### Reactor Netty Example (application.yaml)

The `webclient-sample` includes an `application.yaml` file with example configuration:

```yaml
miller79:
  reactor:
    name: "my-reactor"
    # Additional properties can be configured:
    # max-connections: 100
    # max-idle-time: 60s
    # max-life-time: 120s
    # so-keep-alive: true
    # tcp-keep-idle: 30s
    # tcp-keep-interval: 5s
    # tcp-keep-count: 3

spring:
  security:
    oauth2:
      client:
        provider:
          serviceAccount:
            issuer-uri: ${ISSUER_URI:https://accounts.google.com}
        registration:
          serviceAccount:
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET}
            authorization-grant-type: client_credentials
  http:
    reactiveclient:
      connector: reactor
      connect-timeout: 16s
      read-timeout: 17s
```

**Note:** The example uses environment variables for OAuth2 credentials. The `issuer-uri` property automatically configures the token endpoint and other OAuth2 details.

### OAuth2 Client Credentials Configuration (Optional)

**For RestClient/RestTemplate (Blocking):**

The `restclient-resttemplate-sample` includes OAuth2 client credentials authentication support in the `application.yaml` file. The OAuth2 configuration uses environment variables for security:

```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          serviceAccount:
            issuer-uri: ${ISSUER_URI:https://accounts.google.com}
        registration:
          serviceAccount:
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET}
            authorization-grant-type: client_credentials
            # Optional: scope: api.read,api.write
```

**Environment Variables:**
- `ISSUER_URI` - OAuth2 provider issuer URI (defaults to Google if not set)
- `CLIENT_ID` - Your OAuth2 client ID
- `CLIENT_SECRET` - Your OAuth2 client secret

This configuration enables:
- **Automatic Token Management**: OAuth2 tokens are automatically obtained and refreshed
- **Token Caching**: Tokens are cached and reused until expiration
- **Configured HTTP Client**: Token requests use the same customized Apache HttpClient settings
- **Service-to-Service Auth**: Ideal for microservice authentication scenarios
- **Issuer URI Discovery**: Automatically discovers token endpoint from issuer URI

The OAuth2 setup is handled by `SecurityConfiguration.java`, which provides:
- `sampleRestClientWithAuth` - RestClient with OAuth2 bearer token authentication
- `sampleRestTemplateWithAuth` - RestTemplate with OAuth2 bearer token authentication

**For WebClient (Reactive):**

The `webclient-sample` also includes reactive OAuth2 client credentials authentication support in the `application.yaml` file. The configuration is identical:

```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          serviceAccount:
            issuer-uri: ${ISSUER_URI:https://accounts.google.com}
        registration:
          serviceAccount:
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET}
            authorization-grant-type: client_credentials
            # Optional: scope: api.read,api.write
```

This configuration enables reactive OAuth2 features:
- **Non-Blocking Token Acquisition**: OAuth2 tokens are obtained asynchronously without blocking threads
- **Reactive Token Management**: Token refresh happens as part of the reactive pipeline
- **Token Caching**: Tokens are cached reactively and reused until expiration
- **Configured HTTP Client**: Token requests use the same customized Reactor Netty settings
- **Issuer URI Discovery**: Automatically discovers token endpoint from issuer URI

The reactive OAuth2 setup is handled by `SecurityConfiguration.java` (in webclient-sample), which provides:
- `sampleWebClientWithAuth` - WebClient with reactive OAuth2 bearer token authentication

**Key Difference:** The RestClient/RestTemplate OAuth2 implementation uses blocking interceptors, while the WebClient implementation uses reactive filter functions that work with Project Reactor's Mono/Flux types.

## Why This Matters

Default HTTP client configurations may not be suitable for production environments. Proper timeout and connection pool configuration is essential for:

- **Resilience**: Preventing resource exhaustion and cascading failures
- **Performance**: Optimizing connection reuse and reducing latency
- **Reliability**: Handling network issues gracefully with appropriate timeouts
- **Resource Management**: Controlling connection pool sizes and lifecycle

## Key Considerations

### Spring Boot 3.4+ Features
This repository uses Spring Boot 3.4+ features:
- **ClientHttpRequestFactoryBuilderCustomizer**: Customizes HTTP client factory builders for blocking clients (Apache, JDK, etc.)
- **ClientHttpConnectorBuilderCustomizer**: Customizes HTTP connector builders for reactive clients (Reactor Netty, Jetty, etc.)
- **Fluent Builder API**: Each customizer receives a builder with methods like `withSocketConfigCustomizer()`, `withHttpClientCustomizer()`, etc.
- **Auto-Configuration**: Customizers are automatically applied to all RestClient, RestTemplate, and WebClient instances

### Timeout Hierarchy
Timeouts should be configured in a cascading manner:
1. **Response Timeout** - Maximum time for complete request/response
2. **Connection TTL** < **Server Idle Timeout**
3. **Validate After Inactivity** - Check connection health before reuse

### Connection Pool Sizing
- Base pool size on expected concurrent requests
- Consider target server capacity and rate limits
- Monitor pool exhaustion and adjust accordingly
- Reactive applications need smaller pools than blocking applications

### Keep-Alive Strategy
- Set client keep-alive shorter than server timeout
- Validate connections after periods of inactivity
- Evict idle connections to free resources
- TCP keep-alive options are platform-specific (Linux only for some settings)

## Running the Samples

Each sample is a standalone Spring Boot application:

```bash
# Run Apache HttpClient sample (Windows PowerShell)
cd restclient-resttemplate-sample
.\mvnw.cmd spring-boot:run

# Run Reactor Netty sample (Windows PowerShell)
cd webclient-sample
.\mvnw.cmd spring-boot:run
```

```bash
# Run Apache HttpClient sample (Linux/Mac)
cd restclient-resttemplate-sample
./mvnw spring-boot:run

# Run Reactor Netty sample (Linux/Mac)
cd webclient-sample
./mvnw spring-boot:run
```

## Additional Resources

- [Spring Boot 3.4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
- [Apache HttpClient 5 Documentation](https://hc.apache.org/httpcomponents-client-5.3.x/)
- [Reactor Netty Reference Guide](https://projectreactor.io/docs/netty/release/reference/)
- [Spring WebClient Documentation](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Spring RestClient Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [ClientHttpRequestFactoryBuilderCustomizer JavaDoc](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/http/client/ClientHttpRequestFactoryBuilderCustomizer.html)
- [ClientHttpConnectorBuilderCustomizer JavaDoc](https://docs.spring.io/spring-boot/api/java/org/springframework/boot/autoconfigure/http/client/reactive/ClientHttpConnectorBuilderCustomizer.html)

## Requirements

- Java 21 or later
- Spring Boot 3.4 or later (tested with 3.5.7)
- Maven 3.6 or later

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Copyright (c) 2024 Anthony Lofton