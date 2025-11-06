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
    # ⭐ BEST PRACTICE: Set these two properties for production
    max-idle-time: 3m        # Close idle connections after 3 minutes
    max-life-time: 30m       # Replace all connections every 30 minutes
    
    # Optional properties (only configure if needed):
    # max-connections: 50    # Based on peak concurrent requests
    # 
    # TCP keep-alive settings (NOT necessary with proper lifecycle settings):
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
    # ⭐ BEST PRACTICE: Set these two properties for production
    max-idle-time: 3m        # Close idle connections after 3 minutes
    max-life-time: 30m       # Replace all connections every 30 minutes
    
    # Optional properties (only configure if needed):
    # name: my-webclient     # For monitoring/debugging
    # max-connections: 50    # Reactive apps need FEWER connections
    # 
    # TCP keep-alive settings (NOT necessary with proper lifecycle settings):
    # so-keep-alive: true
    # tcp-keep-idle: 30s     # ⚠️ Linux/Epoll only
    # tcp-keep-interval: 5s  # ⚠️ Linux/Epoll only
    # tcp-keep-count: 3      # ⚠️ Linux/Epoll only

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

These values are reasonable defaults for most microservice-to-microservice communication. **Always tune based on your actual measured latencies and SLAs.**

**⭐ Best Practice for Production:**

For production environments, use these recommended connection lifecycle settings:

```yaml
miller79:
  apache:  # or reactor for WebClient
    max-idle-time: 3m        # 3-4 minutes - Clean up idle connections regularly
    max-life-time: 30m       # 5-30 minutes - Force connection rotation for DNS/LB changes
```

**Why these values?**
- **max-idle-time: 3-4 minutes** - Balances connection reuse with resource cleanup. Shorter than most server/load balancer timeouts, preventing stale connection issues while still allowing effective pooling during normal traffic.
- **max-life-time: 5-30 minutes** - Forces periodic connection rotation to pick up DNS changes, load balancer updates, and rolling deployments. Use shorter values (5-10min) in dynamic environments like Kubernetes, longer values (20-30min) in stable infrastructure.

**🔑 Important:** With properly configured `max-idle-time` and `max-life-time`, **TCP keep-alive settings are not necessary** for most applications. These connection lifecycle settings already prevent stale connections by proactively closing and refreshing them. TCP keep-alive is only needed for very long-lived idle connections (e.g., persistent websockets, streaming APIs) or environments with aggressive intermediate firewalls that close idle connections faster than your lifecycle settings.

**For Blocking Clients (Apache HttpClient):**
```yaml
spring:
  http:
    client:
      connect-timeout: 5s      # TCP connection establishment (typically very fast)
      read-timeout: 15s        # Time to wait for response data (depends on API speed)

miller79:
  apache:
    max-connections: 50        # Size based on concurrent request needs (start conservative)
    max-idle-time: 3m          # ⭐ Best practice: 3-4 minutes
    max-life-time: 30m         # ⭐ Best practice: 5-30 minutes based on environment
    # TCP keep-alive NOT needed with proper lifecycle settings:
    # so-keep-alive: true      
    # tcp-keep-idle: 30s       
    # tcp-keep-interval: 10s   
    # tcp-keep-count: 3        
```

**For Reactive Clients (Reactor Netty):**
```yaml
spring:
  http:
    reactiveclient:
      connect-timeout: 5s      # TCP connection establishment
      # Note: read-timeout works differently in reactive - often not needed

miller79:
  reactor:
    name: "my-service"         # Helpful for monitoring multiple clients
    max-connections: 20        # Smaller than blocking (non-blocking I/O is efficient)
    max-idle-time: 3m          # ⭐ Best practice: 3-4 minutes
    max-life-time: 30m         # ⭐ Best practice: 5-30 minutes based on environment
    # TCP keep-alive NOT needed with proper lifecycle settings:
    # so-keep-alive: true      
    # tcp-keep-idle: 30s       # Linux/Epoll only
    # tcp-keep-interval: 10s   # Linux/Epoll only  
    # tcp-keep-count: 3        # Linux/Epoll only
```

### Key Principles for Tuning

**1. Connection Lifecycle is Critical for Production Reliability**
- **max-idle-time: 3-4 minutes** is the sweet spot for most applications
  - Short enough to prevent stale connections accumulating
  - Long enough to benefit from connection pooling during normal traffic
  - Shorter than typical server/load balancer idle timeouts (5-10 minutes)
- **max-life-time: 5-30 minutes** ensures regular connection rotation
  - Use 5-10 minutes in dynamic environments (Kubernetes, frequent deployments)
  - Use 15-30 minutes in stable infrastructure
  - Always shorter than your DNS TTL to pick up infrastructure changes
- **With proper lifecycle settings, TCP keep-alive is unnecessary** for typical REST APIs

**2. Response Timeout Should Reflect Your SLA**
- Measure your P99 latency for the target API
- Add buffer for network variability (typically 2-3x P99)
- Never set it lower than your expected normal response time
- Consider different timeouts for different APIs (fast vs. slow endpoints)

**3. Connection Pool Size = Expected Concurrency**
- **Blocking clients**: Size for peak concurrent outbound requests
- **Reactive clients**: Can be much smaller (5-20 connections can handle hundreds of concurrent reactive streams)
- Monitor pool exhaustion metrics and scale up if needed
- Avoid over-sizing: large pools consume memory and file descriptors

**4. TCP Keep-Alive Only for Special Cases**
- **Not needed** when using proper `max-idle-time` and `max-life-time` settings
- **Use only for:** Long-lived persistent connections (websockets, SSE), or aggressive firewall environments
- **Note:** Linux/Epoll only for Reactor Netty - degrades gracefully on other platforms

**5. Fail Fast and Clearly**
- Short connect timeout (5-10s) for faster failure detection
- Response timeout based on your service SLA
- Use bounded timeouts at every layer (client → gateway → service)

### Common Mistakes to Avoid

❌ **Setting timeouts too high** - "Let's set 60s to be safe" defeats the purpose; you'll still exhaust resources  
❌ **No timeout at all** - Relying on defaults means indefinite waits in most cases  
❌ **Same timeout for all services** - Fast APIs and slow batch APIs need different timeouts  
❌ **Ignoring connection lifecycle** - Leads to stale connection accumulation over days/weeks  
❌ **Over-sized connection pools** - Wastes resources and can overwhelm downstream services  
❌ **Under-sized connection pools** - Causes pool exhaustion and request queuing  
❌ **Not monitoring timeout metrics** - Can't tune without visibility into actual behavior

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

## Troubleshooting Common Issues

### Problem: "Connection pool shut down" or "Connection pool exhausted"

**Symptom:** Application logs show connection pool errors, requests are rejected or queued

**Root Cause:** All connections in the pool are in use, and new requests cannot acquire a connection

**Solutions:**
1. **Increase pool size** if you have legitimate high concurrency:
   ```yaml
   miller79:
     apache:
       max-connections: 100  # or higher based on need
   ```
2. **Check for connection leaks** - Ensure you're not holding connections without releasing them
3. **Reduce response timeout** if requests are waiting too long on slow services
4. **Monitor pool metrics** to understand actual usage patterns

---

### Problem: "SocketTimeoutException: Read timed out"

**Symptom:** Requests fail with socket timeout exceptions after a fixed period

**Root Cause:** Server took longer than `read-timeout` or `response-timeout` to send response

**Solutions:**
1. **Increase timeout** if the API legitimately takes longer:
   ```yaml
   spring:
     http:
       client:
         read-timeout: 30s  # Increase based on API behavior
   ```
2. **Optimize the downstream service** if it's slower than your SLA
3. **Use async patterns** (reactive) if you need to handle long-running operations
4. **Implement retry logic** with exponential backoff for transient failures

---

### Problem: "NoHttpResponseException" or "Connection reset"

**Symptom:** Random failures with "connection reset" or "no HTTP response" errors

**Root Cause:** Using a stale connection that the server or load balancer already closed

**Solutions:**
1. **Reduce connection TTL** to refresh connections more frequently:
   ```yaml
   miller79:
     apache:
       max-life-time: 30s  # Force connection refresh
       max-idle-time: 20s  # Evict idle connections sooner
   ```
2. **Enable connection validation** (already enabled in these samples via `validate-after-inactivity`)
3. **Ensure TTL < server timeout** (check your server/LB keep-alive timeout)
4. **Enable TCP keep-alive** to detect broken connections early

---

### Problem: Requests hang indefinitely / Application becomes unresponsive

**Symptom:** Requests never complete, threads are stuck in WAITING state, application appears frozen

**Root Cause:** No timeout configured, waiting forever for a response from an unresponsive service

**Solutions:**
1. **Set explicit timeouts** - Never rely on defaults:
   ```yaml
   spring:
     http:
       client:
         connect-timeout: 5s
         read-timeout: 15s
   miller79:
     apache:
       response-timeout: 15s
   ```
2. **Use thread dumps** to identify which downstream call is hanging
3. **Implement circuit breakers** (Resilience4j) to fail fast when services are down
4. **Add request-level timeouts** in your service layer as a last line of defense

---

### Problem: High memory usage or too many open file descriptors

**Symptom:** Application memory grows over time, OS reports too many open files

**Root Cause:** Connections not being evicted, accumulating idle connections in the pool

**Solutions:**
1. **Enable idle eviction** (already configured in these samples):
   ```yaml
   miller79:
     apache:
       max-idle-time: 30s
   ```
2. **Reduce connection pool size** if it's unnecessarily large
3. **Set max-life-time** to force connection refresh:
   ```yaml
   miller79:
     apache:
       max-life-time: 60s
   ```
4. **Monitor connection metrics** using Spring Boot Actuator or APM tools

---

### Problem: DNS changes not picked up (requests go to old servers)

**Symptom:** After infrastructure changes, some requests still route to decommissioned servers

**Root Cause:** Long-lived connections bypass DNS resolution

**Solutions:**
1. **Set connection max-life-time** to force periodic reconnection:
   ```yaml
   miller79:
     apache:
       max-life-time: 60s  # Force DNS re-resolution every 60s
   ```
2. **Use service mesh** (like Istio) for service discovery instead of DNS
3. **Implement client-side load balancing** (Spring Cloud LoadBalancer)

---

### Problem: Intermittent failures after deployments or during low traffic

**Symptom:** First request after idle period fails, subsequent requests succeed

**Root Cause:** Stale connections in pool were closed by server/LB during idle time

**Solutions:**
1. **Reduce max-idle-time** to evict idle connections sooner:
   ```yaml
   miller79:
     apache:
       max-idle-time: 20s
   ```
2. **Enable connection validation** before reuse (configured via `validate-after-inactivity` in these samples)
3. **Implement retry logic** in your application to handle first-request failures gracefully

---

### Debugging Tips

**Enable HTTP client debug logging:**
```yaml
logging:
  level:
    org.apache.hc.client5: DEBUG          # Apache HttpClient
    reactor.netty.http.client: DEBUG       # Reactor Netty
    org.springframework.web.reactive: DEBUG  # Spring WebClient
```

**Use Spring Boot Actuator metrics:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: metrics,health
  metrics:
    enable:
      http.client.requests: true
```

**Monitor these key metrics:**
- Connection pool active connections
- Connection pool idle connections  
- Connection pool pending acquisitions
- Request timeout rate
- Connection failure rate
- P50, P95, P99 response times

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