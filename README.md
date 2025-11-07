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

### Configuration Examples

Both samples include `application.yaml` files. See the actual files for complete configuration with detailed comments.

**Apache HttpClient (restclient-resttemplate-sample):**
```yaml
spring:
  http:
    client:
      factory: http-components
      # read-timeout: 3m     # Uncomment and tune based on your API SLAs
      # connect-timeout: 3m

miller79:
  apache:
    max-idle-time: 3m        # ⭐ Clean up idle connections regularly
    max-life-time: 30m       # ⭐ Force connection rotation for DNS/LB changes
    # max-connections: 50    # Uncomment if needed
```

**Reactor Netty (webclient-sample):**
```yaml
spring:
  http:
    reactiveclient:
      connector: reactor
      # connect-timeout: 3m  # Uncomment if needed

miller79:
  reactor:
    max-idle-time: 3m        # ⭐ Clean up idle connections regularly
    max-life-time: 30m       # ⭐ Force connection rotation for DNS/LB changes
    # max-connections: 20    # Uncomment if needed (reactive needs fewer)
```

**Note:** Both samples include OAuth2 client credentials support. See the actual `application.yaml` files for the complete OAuth2 configuration using environment variables.

### OAuth2 Client Credentials (Optional)

Both samples include OAuth2 client credentials support using `issuer-uri` auto-discovery. The configuration uses environment variables (`CLIENT_ID`, `CLIENT_SECRET`, `ISSUER_URI`) for security. See the `application.yaml` files and `SecurityConfiguration.java` in each sample for details.

**Key features:**
- Automatic token management and caching
- Token requests use the same configured HTTP client
- Blocking implementation (RestClient/RestTemplate) uses interceptors
- Reactive implementation (WebClient) uses filter functions

## Why This Matters

Default HTTP client configurations may not be suitable for production environments. Proper timeout and connection pool configuration is essential for:

- **Resilience**: Preventing resource exhaustion and cascading failures
- **Performance**: Optimizing connection reuse and reducing latency
- **Reliability**: Handling network issues gracefully with appropriate timeouts
- **Resource Management**: Controlling connection pool sizes and lifecycle

## Understanding Timeouts & Defaults

### What Are HTTP Client Timeouts?

When your Spring application makes HTTP requests to other services, several timing-related configurations control how long to wait for responses and how to manage connections. Understanding these is critical for building resilient applications.

### The Default Behavior (No Custom Configuration)

**Without explicit configuration, Spring's HTTP clients use library-specific defaults that may not suit your needs:**

**Apache HttpClient (RestClient/RestTemplate):**
- **No response timeout by default** - Requests can wait indefinitely for a server response
- **No connect timeout by default** - Connection attempts can hang waiting for TCP handshake
- **Default connection pool: ~200 total connections, ~20 per route** - May be too high or too low depending on your use case
- **No connection TTL limit** - Connections can live indefinitely, risking stale connections through load balancers
- **No automatic idle connection eviction** - Idle connections consume resources unnecessarily

**Reactor Netty (WebClient):**
- **No response timeout by default** - Reactive streams can wait indefinitely
- **No connect timeout by default** - TCP connections can hang without failing fast
- **Default connection pool: 500 connections** - Often too large for typical microservice communication
- **Connections live indefinitely by default** - Can accumulate stale connections over time
- **No automatic eviction** - Idle connections remain in pool consuming memory

### Why Defaults Are Dangerous

**1. Indefinite Waits Can Crash Your Application**

Without timeouts, a slow or unresponsive downstream service can cause your application to:
- **Exhaust thread pools** (blocking clients) - All request-handling threads get stuck waiting
- **Run out of memory** (reactive clients) - Pending reactive streams accumulate in memory
- **Trigger cascading failures** - Your slow service then impacts services calling you
- **Appear completely frozen** - The application is running but can't process new requests

**2. Resource Leaks Degrade Performance Over Time**

Without proper connection lifecycle management:
- **Stale connections accumulate** - Connections to old server instances persist after deployments
- **Load balancer timeouts disconnect silently** - Your pool contains "dead" connections that fail on use
- **DNS changes aren't picked up** - Long-lived connections bypass DNS updates
- **File descriptors leak** - Operating system limits can be reached, preventing new connections

**3. Poor Resilience During Failures**

When downstream services have issues:
- **No fast failure** - Your application waits the full (infinite) timeout before failing
- **No circuit breaking opportunity** - Can't detect failure patterns without bounded timeouts
- **Difficult troubleshooting** - Hung requests provide no clear indication of where the problem is

### Recommended Starting Points

**⭐ Production Best Practice:**

```yaml
miller79:
  apache:  # or reactor for WebClient
    max-idle-time: 3m        # Clean up idle connections regularly
    max-life-time: 30m       # Force connection rotation for DNS/LB changes
```

- **max-idle-time: 3-4 minutes** - Prevents stale connections while allowing connection pooling benefits
- **max-life-time: 5-30 minutes** - Forces connection rotation (5-10min for Kubernetes, 15-30min for stable infrastructure)

**🔑 Important:** With proper lifecycle settings, **TCP keep-alive is not needed** for typical REST APIs. Only required for WebSockets, SSE, or aggressive firewalls.

### Key Tuning Principles

1. **Connection Lifecycle** - `max-idle-time: 3-4m` and `max-life-time: 5-30m` prevent stale connections
2. **Response Timeout** - Set based on your API's P99 latency + buffer (typically 2-3x P99)
3. **Pool Size** - Blocking clients: match peak concurrency; Reactive: 5-20 connections handles hundreds of streams
4. **TCP Keep-Alive** - Not needed with proper lifecycle settings (only for WebSockets/SSE)
5. **Fail Fast** - Use short timeouts (5-10s connect, SLA-based read) at every layer

### Common Mistakes

❌ Timeouts too high (defeats the purpose) or missing entirely (indefinite waits)  
❌ Same timeout for all services (fast vs. slow APIs need different values)  
❌ Ignoring connection lifecycle (stale connections accumulate)  
❌ Wrong pool size (too large wastes resources, too small causes exhaustion)  
❌ Not monitoring metrics (can't tune without visibility)

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

## Troubleshooting

### Common Problems & Solutions

| Problem | Symptom | Solution |
|---------|---------|----------|
| **Pool exhausted** | Connection pool errors, requests rejected | Increase `max-connections` or reduce `read-timeout` |
| **Read timeout** | SocketTimeoutException | Increase `read-timeout` or optimize downstream service |
| **Connection reset** | NoHttpResponseException, random failures | Reduce `max-life-time` and `max-idle-time` |
| **Hangs indefinitely** | Threads stuck, app frozen | Set explicit `connect-timeout` and `read-timeout` |
| **Memory/file descriptor leak** | Memory grows, too many open files | Enable `max-idle-time` and `max-life-time` |
| **Stale DNS** | Requests to old servers after changes | Set `max-life-time` < DNS TTL (e.g., 60s) |
| **Intermittent failures** | First request after idle fails | Reduce `max-idle-time` or add retry logic |

### Debugging

**Enable debug logging:**
```yaml
logging:
  level:
    org.apache.hc.client5: DEBUG          # Apache HttpClient
    reactor.netty.http.client: DEBUG       # Reactor Netty
```

**Monitor key metrics** (via Spring Boot Actuator):
- Connection pool: active, idle, pending acquisitions
- Request: timeout rate, failure rate
- Latency: P50, P95, P99 response times

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