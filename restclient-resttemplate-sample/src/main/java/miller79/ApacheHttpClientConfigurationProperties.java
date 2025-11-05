package miller79;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for Apache HttpClient 5 used by RestTemplate and
 * RestClient.
 * 
 * <p>
 * This class externalizes all HTTP client configuration settings, allowing them
 * to be configured via application.yml, application.properties, or environment
 * variables.
 * 
 * <p>
 * <b>⚠️ Why These Settings Matter:</b><br>
 * Without proper configuration, your application may experience:
 * <ul>
 * <li><b>Thread pool exhaustion</b> - Threads waiting indefinitely for slow responses</li>
 * <li><b>Resource leaks</b> - Stale connections accumulating over time</li>
 * <li><b>Cascading failures</b> - Slow downstream services impacting your entire application</li>
 * <li><b>Poor performance</b> - Connection reuse not optimized</li>
 * </ul>
 * 
 * <p>
 * <b>Example Configuration (application.yml):</b>
 * 
 * <pre>{@code
 * miller79:
 *   apache:
 *     name: my-http-client
 *     max-connections: 100
 *     max-idle-time: 60s
 *     max-life-time: 120s
 *     so-keep-alive: true
 *     tcp-keep-idle: 30s
 *     tcp-keep-interval: 5s
 *     tcp-keep-count: 3
 *     response-timeout: 10s
 * }</pre>
 * 
 * <p>
 * <b>Property Descriptions:</b>
 * <ul>
 * <li><b>name:</b> Identifier for the connection pool (useful for
 * monitoring/debugging)</li>
 * <li><b>maxConnections:</b> Maximum total connections in the pool across all
 * routes</li>
 * <li><b>maxIdleTime:</b> How long idle connections remain in pool before
 * eviction</li>
 * <li><b>maxLifeTime:</b> Maximum lifetime of any connection (should be less
 * than server timeout)</li>
 * <li><b>soKeepAlive:</b> Enable TCP keep-alive at socket level</li>
 * <li><b>tcpKeepIdle:</b> Time before first keep-alive probe is sent</li>
 * <li><b>tcpKeepInterval:</b> Time between subsequent keep-alive probes</li>
 * <li><b>tcpKeepCount:</b> Number of failed probes before connection is
 * considered dead</li>
 * <li><b>responseTimeout:</b> Maximum time to wait for server response</li>
 * </ul>
 * 
 * <p>
 * <b>Best Practices:</b>
 * <ul>
 * <li>Set {@code maxLifeTime} less than server's idle timeout to prevent
 * server-side connection closures</li>
 * <li>Set {@code tcpKeepIdle} to detect broken connections before they're
 * reused</li>
 * <li>Size {@code maxConnections} based on expected concurrent requests and
 * target server capacity</li>
 * <li>Set {@code responseTimeout} based on expected API response times plus
 * buffer for network latency</li>
 * </ul>
 * 
 * <p>
 * <b>When to Tune These Settings:</b>
 * <ul>
 * <li><b>High traffic services:</b> Increase maxConnections to handle concurrency</li>
 * <li><b>Slow APIs:</b> Increase responseTimeout to match SLA</li>
 * <li><b>Behind load balancers:</b> Set maxLifeTime &lt; LB idle timeout</li>
 * <li><b>Microservice communication:</b> Use shorter timeouts for fail-fast behavior</li>
 * </ul>
 * 
 * @see ApacheHttpClientConfiguration
 */
@ConfigurationProperties("miller79.apache")
@Data
class ApacheHttpClientConfigurationProperties {
    /**
     * Maximum number of total connections in the pool across all routes (host:port
     * combinations).
     * 
     * <p>
     * Size this based on:
     * <ul>
     * <li>Expected concurrent HTTP requests from your application</li>
     * <li>Target server capacity and rate limits</li>
     * <li>Available system resources (memory, file descriptors)</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if this is too low:</b>
     * <ul>
     * <li>Requests queue waiting for available connections</li>
     * <li>Increased latency (P95/P99 response times spike)</li>
     * <li>Potential timeout errors under load</li>
     * <li>"Connection pool timeout" or "pool exhausted" exceptions</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if this is too high:</b>
     * <ul>
     * <li>Excessive memory usage (each connection consumes ~4-8KB)</li>
     * <li>File descriptor exhaustion (OS limits typically 1024-65536)</li>
     * <li>Overwhelming downstream services with too many concurrent connections</li>
     * <li>Longer connection pool cleanup times</li>
     * </ul>
     * 
     * <p>
     * <b>How to size this correctly:</b>
     * <ol>
     * <li>Start with default (64) for most applications</li>
     * <li>Monitor connection pool metrics (active, idle, pending acquisitions)</li>
     * <li>If you see pool exhaustion, increase gradually (e.g., 64 → 100 → 150)</li>
     * <li>For high-throughput services (1000+ req/s), may need 200-500 connections</li>
     * <li>For low-volume services, 20-50 connections is often sufficient</li>
     * </ol>
     * 
     * <p>
     * Default: 64 connections
     */
    private int maxConnections = 64;

    /**
     * Maximum time a connection can remain idle in the pool before being evicted.
     * 
     * <p>
     * Idle connections are automatically removed after this duration to free
     * resources. Should be less than the server's idle timeout to prevent
     * server-side connection closures.
     * 
     * <p>
     * Default: 60 seconds
     */
    private Duration maxIdleTime = Duration.ofSeconds(60);

    /**
     * Maximum lifetime of a connection in the pool, regardless of activity.
     * 
     * <p>
     * Connections are closed and recreated after this duration to prevent issues
     * with long-lived connections (e.g., load balancer timeouts, DNS changes).
     * Should be less than the server's connection timeout.
     * 
     * <p>
     * <b>What happens if not set or too long:</b>
     * <ul>
     * <li>Connections persist through load balancer timeouts (typically 60-90s)</li>
     * <li>Stale connections accumulate, leading to "connection reset" errors</li>
     * <li>DNS changes not picked up (requests still go to old IPs)</li>
     * <li>Server-side connection closes silently, causing failures on next use</li>
     * <li>Cannot handle rolling deployments cleanly</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if too short:</b>
     * <ul>
     * <li>Excessive connection churn (constantly creating/destroying connections)</li>
     * <li>Increased latency due to frequent TCP handshakes</li>
     * <li>Higher CPU usage for connection establishment</li>
     * <li>Connection pool may appear "busy" more often</li>
     * </ul>
     * 
     * <p>
     * <b>How to set this correctly:</b>
     * <ol>
     * <li>Identify your infrastructure's idle timeout settings:
     *   <ul>
     *     <li>AWS ALB: 60s default</li>
     *     <li>AWS ELB Classic: 60s default</li>
     *     <li>Nginx: 75s default (keepalive_timeout)</li>
     *     <li>Tomcat: 60s default</li>
     *   </ul>
     * </li>
     * <li>Set this value 20-30% lower than the shortest timeout in your stack</li>
     * <li>For ALB (60s timeout): set maxLifeTime to 40-50s</li>
     * <li>For standard setups: 30-60s is usually appropriate</li>
     * <li>For frequently changing infrastructure: use shorter values (15-30s)</li>
     * </ol>
     * 
     * <p>
     * <b>Common patterns:</b>
     * <ul>
     * <li><b>Behind AWS ALB:</b> 45-50 seconds</li>
     * <li><b>Behind Nginx:</b> 50-60 seconds</li>
     * <li><b>Direct service-to-service:</b> 60 seconds</li>
     * <li><b>Kubernetes with frequent pod rotation:</b> 30 seconds</li>
     * </ul>
     * 
     * <p>
     * Default: 60 seconds
     */
    private Duration maxLifeTime = Duration.ofSeconds(60);

    /**
     * Enable TCP keep-alive at the socket level.
     * 
     * <p>
     * When enabled, the kernel sends periodic probes on idle connections to detect
     * broken connections (e.g., due to network failures, firewall timeouts).
     * 
     * <p>
     * Default: true (enabled)
     */
    private boolean soKeepAlive = true;

    /**
     * Time of inactivity before the first TCP keep-alive probe is sent.
     * 
     * <p>
     * After a connection has been idle for this duration, the kernel starts sending
     * keep-alive probes to verify the connection is still alive.
     * 
     * <p>
     * Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>
     * Default: 30 seconds
     */
    private Duration tcpKeepIdle = Duration.ofSeconds(30);

    /**
     * Time interval between subsequent TCP keep-alive probes.
     * 
     * <p>
     * If the first probe fails, additional probes are sent at this interval until
     * either a response is received or {@code tcpKeepCount} probes have failed.
     * 
     * <p>
     * Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>
     * Default: 5 seconds
     */
    private Duration tcpKeepInterval = Duration.ofSeconds(5);

    /**
     * Number of failed TCP keep-alive probes before the connection is considered
     * dead.
     * 
     * <p>
     * If this many consecutive probes fail without receiving a response, the kernel
     * closes the connection and marks it as broken.
     * 
     * <p>
     * Total time before connection is closed = {@code tcpKeepIdle} +
     * ({@code tcpKeepCount} × {@code tcpKeepInterval})
     * 
     * <p>
     * Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>
     * Default: 3 probes
     */
    private int tcpKeepCount = 3;

    /**
     * Maximum time to wait for a server response.
     * 
     * <p>
     * This timeout applies to:
     * <ul>
     * <li>Time waiting for response data to start arriving</li>
     * <li>Time between consecutive data packets</li>
     * </ul>
     * 
     * <p>
     * Set this based on:
     * <ul>
     * <li>Expected API response times (measure P95/P99 latency)</li>
     * <li>Network latency between services</li>
     * <li>Server processing time for the operation</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if not set or too high:</b>
     * <ul>
     * <li>Threads wait indefinitely for slow or hung services</li>
     * <li>Thread pool exhaustion under load</li>
     * <li>Application appears frozen or unresponsive</li>
     * <li>Cascading failures to upstream services</li>
     * <li>No fast failure - can't implement circuit breakers effectively</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if too low:</b>
     * <ul>
     * <li>Valid requests fail prematurely</li>
     * <li>Increased error rate and retry storms</li>
     * <li>False positives in monitoring/alerting</li>
     * <li>Poor user experience due to unnecessary failures</li>
     * </ul>
     * 
     * <p>
     * <b>How to set this correctly:</b>
     * <ol>
     * <li>Measure your API's actual response time (P99 latency)</li>
     * <li>Add buffer for network variability (typically 2-3x P99)</li>
     * <li>For most microservices: 5-15 seconds is appropriate</li>
     * <li>For slow batch APIs: may need 30-60 seconds</li>
     * <li>For fast cache/lookup APIs: 1-3 seconds may suffice</li>
     * <li>Never set lower than your expected normal response time</li>
     * </ol>
     * 
     * <p>
     * <b>Example scenarios:</b>
     * <ul>
     * <li><b>Fast API (cache lookup):</b> P99=200ms → set 1-2s timeout</li>
     * <li><b>Normal API (database query):</b> P99=500ms → set 5-10s timeout</li>
     * <li><b>Slow API (complex calculation):</b> P99=5s → set 15-20s timeout</li>
     * <li><b>Batch API (report generation):</b> P99=30s → set 60-90s timeout</li>
     * </ul>
     * 
     * <p>
     * If the server doesn't respond within this time, a timeout exception is
     * thrown.
     * 
     * <p>
     * Default: 5 seconds
     */
    private Duration responseTimeout = Duration.ofSeconds(5);
}
