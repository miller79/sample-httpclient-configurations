# Sample HTTP Client Configurations

A demonstration repository showing how to properly configure internal HTTP client timeouts for various Spring REST implementations using Apache HttpClient and Reactor Netty.

## Overview

This repository provides working examples of configuring low-level HTTP client settings for Spring's REST client implementations:

- **Apache HttpClient 5** - Used by `RestTemplate` and `RestClient`
- **Reactor Netty** - Used by `WebClient`

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
- **Max Per Route**: Maximum connections per host:port combination

### Timeout Configuration
- **Connect Timeout**: Time to establish TCP connection
- **Connection Request Timeout**: Time to acquire connection from pool
- **Response Timeout**: Time to wait for response data
- **Socket Timeout**: Socket-level read timeout

### Keep-Alive Settings
- **Keep-Alive Strategy**: Determines how long to keep idle connections
- **Time-to-Live**: Maximum lifetime of a connection
- **Validate After Inactivity**: When to validate stale connections
- **Idle Eviction**: Automatic cleanup of idle connections

### Key Classes
- `ApacheHttpClientConfiguration.java` - Bean configuration for Apache HttpClient
- `ApacheHttpClientConfigurationProperties.java` - Externalized configuration properties
- `RestTemplateConfiguration.java` - RestTemplate setup using configured client
- `RestClientConfiguration.java` - RestClient setup using configured client

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
- `ReactorHttpClientConfiguration.java` - Bean configuration for Reactor Netty
- `ReactorHttpClientConfigurationProperties.java` - Externalized configuration properties
- `WebClientConfiguration.java` - WebClient setup using configured connector

## Configuration Properties

Both samples support externalized configuration via `application.yml` or `application.properties`:

### Apache HttpClient Example
```yaml
apache:
  httpclient:
    server-idle-timeout: 5m
    connect-timeout: 10s
    response-timeout: 60s
    connection-request-timeout: 5s
    pool-max-connections: 64
    pool-max-per-route: 32
```

### Reactor Netty Example
```yaml
reactor:
  httpclient:
    name: webclient-connection
    max-connections: 64
    max-idle-time: 60s
    max-life-time: 60s
    response-timeout: 5s
    so-keep-alive: true
    tcp-keep-idle: 30s
    tcp-keep-interval: 5s
    tcp-keep-count: 3
```

## Why This Matters

Default HTTP client configurations may not be suitable for production environments. Proper timeout and connection pool configuration is essential for:

- **Resilience**: Preventing resource exhaustion and cascading failures
- **Performance**: Optimizing connection reuse and reducing latency
- **Reliability**: Handling network issues gracefully with appropriate timeouts
- **Resource Management**: Controlling connection pool sizes and lifecycle

## Key Considerations

### Timeout Hierarchy
Timeouts should be configured in a cascading manner:
1. **Connect Timeout** < **Response Timeout**
2. **Connection TTL** < **Server Idle Timeout**
3. **Keep-Alive Duration** < **Server Idle Timeout**

### Connection Pool Sizing
- Base pool size on expected concurrent requests
- Consider target server capacity and rate limits
- Monitor pool exhaustion and adjust accordingly

### Keep-Alive Strategy
- Set client keep-alive shorter than server timeout
- Validate connections after periods of inactivity
- Evict idle connections to free resources

## Running the Samples

Each sample is a standalone Spring Boot application:

```bash
# Run Apache HttpClient sample
cd restclient-resttemplate-sample
./mvnw spring-boot:run

# Run Reactor Netty sample
cd webclient-sample
./mvnw spring-boot:run
```

## Additional Resources

- [Apache HttpClient 5 Documentation](https://hc.apache.org/httpcomponents-client-5.3.x/)
- [Reactor Netty Reference Guide](https://projectreactor.io/docs/netty/release/reference/)
- [Spring WebClient Documentation](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Spring RestClient Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)

## License

This is a demonstration repository for educational purposes.