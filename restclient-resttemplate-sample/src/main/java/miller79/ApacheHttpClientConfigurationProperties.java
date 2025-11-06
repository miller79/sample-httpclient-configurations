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
 * <li><b>Thread pool exhaustion</b> - Threads waiting indefinitely for slow
 * responses</li>
 * <li><b>Resource leaks</b> - Stale connections accumulating over time</li>
 * <li><b>Cascading failures</b> - Slow downstream services impacting your
 * entire application</li>
 * <li><b>Poor performance</b> - Connection reuse not optimized</li>
 * </ul>
 * 
 * <p>
 * <b>⭐ Recommended Configuration for Production (application.yml):</b>
 * 
 * <pre>{@code
 * miller79:
 *   apache:
 *     # Connection lifecycle - These two settings are essential!
 *     max-idle-time: 3m        # 3-4 minutes recommended
 *     max-life-time: 30m       # 5-30 minutes based on environment
 *     
 *     # Optional settings (only configure if needed):
 *     # max-connections: 50    # Based on peak concurrent requests
 *     # TCP keep-alive NOT needed with proper lifecycle settings
 * }</pre>
 * 
 * <p>
 * <b>Property Descriptions:</b>
 * <ul>
 * <li><b>maxConnections:</b> Maximum total connections in the pool across all
 * routes</li>
 * <li><b>maxIdleTime:</b> How long idle connections remain before eviction
 * (⭐ 3-4 minutes recommended)</li>
 * <li><b>maxLifeTime:</b> Maximum lifetime of any connection
 * (⭐ 5-30 minutes recommended)</li>
 * <li><b>soKeepAlive:</b> Enable TCP keep-alive (NOT needed with proper lifecycle settings)</li>
 * <li><b>tcpKeepIdle:</b> Time before first keep-alive probe (only for special cases)</li>
 * <li><b>tcpKeepInterval:</b> Time between keep-alive probes (only for special cases)</li>
 * <li><b>tcpKeepCount:</b> Failed probes before connection is dead (only for special cases)</li>
 * </ul>
 * 
 * <p>
 * <b>🔑 Key Best Practices:</b>
 * <ul>
 * <li><b>max-idle-time: 3-4 minutes</b> - Prevents stale connections while allowing pooling benefits</li>
 * <li><b>max-life-time: 5-30 minutes</b> - Use 5-10 min for dynamic environments (Kubernetes),
 * 15-30 min for stable infrastructure</li>
 * <li><b>TCP keep-alive NOT necessary</b> - With proper lifecycle settings, keep-alive adds
 * no value for typical REST APIs. Only needed for long-lived persistent connections.</li>
 * <li>Size {@code maxConnections} based on peak concurrent requests (50 is a good starting point)</li>
 * </ul>
 * 
 * <p>
 * <b>When to Tune These Settings:</b>
 * <ul>
 * <li><b>High traffic services:</b> Increase maxConnections to handle
 * concurrency</li>
 * <li><b>Dynamic environments:</b> Use shorter maxLifeTime (5-10 minutes)</li>
 * <li><b>Stable infrastructure:</b> Use longer maxLifeTime (15-30 minutes)</li>
 * <li><b>Special cases only:</b> Enable TCP keep-alive for WebSockets, SSE, or aggressive firewalls</li>
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
     * <li>Overwhelming downstream services with too many concurrent
     * connections</li>
     * <li>Longer connection pool cleanup times</li>
     * </ul>
     * 
     * <p>
     * <b>How to size this correctly:</b>
     * <ol>
     * <li>Start with default (64) for most applications</li>
     * <li>Monitor connection pool metrics (active, idle, pending acquisitions)</li>
     * <li>If you see pool exhaustion, increase gradually (e.g., 64 → 100 →
     * 150)</li>
     * <li>For high-throughput services (1000+ req/s), may need 200-500
     * connections</li>
     * <li>For low-volume services, 20-50 connections is often sufficient</li>
     * </ol>
     * 
     * <p>
     * Default: 64 connections (if not set, underlying client default is used)
     */
    private Integer maxConnections;

    /**
     * Maximum time a connection can remain idle in the pool before being evicted.
     * 
     * <p>
     * Idle connections are automatically removed after this duration to free
     * resources. Should be less than the server's idle timeout to prevent
     * server-side connection closures.
     * 
     * <p>
     * <b>⭐ Best Practice: 3-4 minutes</b>
     * <ul>
     * <li>Short enough to prevent stale connection accumulation</li>
     * <li>Long enough to benefit from connection pooling during normal traffic</li>
     * <li>Shorter than typical server/load balancer idle timeouts (5-10 minutes)</li>
     * </ul>
     * 
     * <p>
     * Default: Not set (if not configured, connections may remain idle indefinitely)
     */
    private Duration maxIdleTime;

    /**
     * Maximum lifetime of a connection in the pool, regardless of activity.
     * 
     * <p>
     * Connections are closed and recreated after this duration to prevent issues
     * with long-lived connections (e.g., load balancer timeouts, DNS changes).
     * Should be less than the server's connection timeout.
     * 
     * <p>
     * <b>⭐ Best Practice: 5-30 minutes</b>
     * <ul>
     * <li><b>Dynamic environments (Kubernetes, frequent deployments):</b> 5-10 minutes</li>
     * <li><b>Stable infrastructure:</b> 15-30 minutes</li>
     * <li>Forces periodic connection rotation to pick up DNS changes, load balancer
     * updates, and rolling deployments</li>
     * <li>Always set shorter than your DNS TTL</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if not set or too long:</b>
     * <ul>
     * <li>Connections persist through load balancer timeouts (typically
     * 60-90s)</li>
     * <li>Stale connections accumulate, leading to "connection reset" errors</li>
     * <li>DNS changes not picked up (requests still go to old IPs)</li>
     * <li>Server-side connection closes silently, causing failures on next use</li>
     * <li>Cannot handle rolling deployments cleanly</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if too short:</b>
     * <ul>
     * <li>Excessive connection churn (constantly creating/destroying
     * connections)</li>
     * <li>Increased latency due to frequent TCP handshakes</li>
     * <li>Higher CPU usage for connection establishment</li>
     * <li>Connection pool may appear "busy" more often</li>
     * </ul>
     * 
     * <p>
     * <b>How to set this correctly:</b>
     * <ol>
     * <li>Identify your infrastructure's idle timeout settings:
     * <ul>
     * <li>AWS ALB: 60s default</li>
     * <li>AWS ELB Classic: 60s default</li>
     * <li>Nginx: 75s default (keepalive_timeout)</li>
     * <li>Tomcat: 60s default</li>
     * </ul>
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
     * Default: Not set (if not configured, connections may live indefinitely)
     */
    private Duration maxLifeTime;

    /**
     * Enable TCP keep-alive at the socket level.
     * 
     * <p>
     * When enabled, the kernel sends periodic probes on idle connections to detect
     * broken connections (e.g., due to network failures, firewall timeouts).
     * 
     * <p>
     * <b>🔑 Important:</b> With properly configured {@code maxIdleTime} (3-4 minutes)
     * and {@code maxLifeTime} (5-30 minutes), TCP keep-alive is <b>NOT necessary</b>
     * for most applications. The connection lifecycle settings already prevent stale
     * connections by proactively closing and refreshing them.
     * 
     * <p>
     * <b>Only enable TCP keep-alive for special cases:</b>
     * <ul>
     * <li>Very long-lived idle connections (e.g., persistent WebSockets, SSE)</li>
     * <li>Environments with aggressive intermediate firewalls that close idle
     * connections faster than your lifecycle settings</li>
     * <li>Connections that must remain open for extended periods without data
     * transfer</li>
     * </ul>
     * 
     * <p>
     * Default: Not set (if not configured, OS default is used, typically disabled)
     */
    private Boolean soKeepAlive;

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
     * Default: 30 seconds (if not set, underlying client default is used)
     */
    private Duration tcpKeepIdle;

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
     * Default: 5 seconds (if not set, underlying client default is used)
     */
    private Duration tcpKeepInterval;

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
     * Default: 3 probes (if not set, underlying client default is used)
     */
    private Integer tcpKeepCount;
}
