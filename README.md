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

## Repository Structure

```
sample-httpclient-configurations/
├── restclient-resttemplate-sample/     # Apache HttpClient configuration
│   └── src/main/java/miller79/
│       ├── ApacheHttpClientConfiguration.java
│       ├── ApacheHttpClientConfigurationProperties.java
│       ├── RestClientConfiguration.java
│       ├── RestTemplateConfiguration.java
│       └── SampleImplementation.java
│
└── webclient-sample/                    # Reactor Netty configuration
    └── src/main/java/miller79/
        ├── ReactorHttpClientConfiguration.java
        ├── ReactorHttpClientConfigurationProperties.java
        ├── WebClientConfiguration.java
        └── SampleImplementation.java
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
- `RestTemplateConfiguration.java` - RestTemplate setup (auto-configured via customizer)
- `RestClientConfiguration.java` - RestClient setup (auto-configured via customizer)

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
- `WebClientConfiguration.java` - WebClient setup (auto-configured via customizer)

## Configuration Properties

Both samples support externalized configuration via `application.yml` or `application.properties`:

### Apache HttpClient Example
```yaml
miller79:
  apache:
    name: my-http-client
    max-connections: 100
    max-idle-time: 60s
    max-life-time: 120s
    so-keep-alive: true
    tcp-keep-idle: 30s
    tcp-keep-interval: 5s
    tcp-keep-count: 3
    response-timeout: 10s
```

### Reactor Netty Example
```yaml
miller79:
  reactor:
    name: my-webclient
    max-connections: 100
    max-idle-time: 60s
    max-life-time: 120s
    so-keep-alive: true
    tcp-keep-idle: 30s
    tcp-keep-interval: 5s
    tcp-keep-count: 3
    response-timeout: 10s
```

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

- Java 17 or later
- Spring Boot 3.4 or later
- Maven 3.6 or later

## License

This is a demonstration repository for educational purposes.