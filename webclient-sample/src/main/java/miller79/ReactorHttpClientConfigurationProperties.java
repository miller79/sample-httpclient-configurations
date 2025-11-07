package miller79;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for Reactor Netty HTTP client used by Spring
 * WebFlux's WebClient.
 * 
 * <p>
 * This class externalizes all Reactor Netty HTTP client configuration settings,
 * allowing them to be configured via application.yml, application.properties,
 * or environment variables.
 * 
 * <p>
 * <b>⚠️ Why These Settings Matter:</b><br>
 * Without proper configuration, your reactive application may experience:
 * <ul>
 * <li><b>Memory exhaustion</b> - Pending reactive streams accumulate without
 * bounds</li>
 * <li><b>Resource leaks</b> - Stale connections never cleaned up</li>
 * <li><b>Backpressure issues</b> - Slow downstream services blocking reactive
 * pipelines</li>
 * <li><b>Cascading failures</b> - Unbound timeouts propagating through reactive
 * chains</li>
 * </ul>
 * 
 * <p>
 * <b>⭐ Recommended Configuration for Production (application.yml):</b>
 * 
 * <pre>{@code
 * miller79:
 *   reactor:
 *     # Connection lifecycle - These two settings are essential!
 *     max-idle-time: 3m        # 3-4 minutes recommended
 *     max-life-time: 30m       # 5-30 minutes based on environment
 *     
 *     # Optional settings (only configure if needed):
 *     # name: my-webclient     # For monitoring/debugging
 *     # max-connections: 50    # Reactive apps need FEWER connections
 *     # TCP keep-alive NOT needed with proper lifecycle settings
 * }</pre>
 * 
 * <p>
 * <b>Property Descriptions:</b>
 * <ul>
 * <li><b>name:</b> Identifier for the connection provider (useful for
 * monitoring/debugging)</li>
 * <li><b>maxConnections:</b> Maximum number of connections in the pool</li>
 * <li><b>maxIdleTime:</b> How long idle connections remain before eviction
 * (⭐ 3-4 minutes recommended)</li>
 * <li><b>maxLifeTime:</b> Maximum lifetime of any connection
 * (⭐ 5-30 minutes recommended)</li>
 * <li><b>soKeepAlive:</b> Enable TCP keep-alive (NOT needed with proper lifecycle settings)</li>
 * <li><b>tcpKeepIdle:</b> Time before first keep-alive probe (Linux/Epoll only, special cases)</li>
 * <li><b>tcpKeepInterval:</b> Time between keep-alive probes (Linux/Epoll only, special cases)</li>
 * <li><b>tcpKeepCount:</b> Failed probes before connection is dead (Linux/Epoll only, special cases)</li>
 * </ul>
 * 
 * <p>
 * <b>🔑 Key Best Practices:</b>
 * <ul>
 * <li><b>max-idle-time: 3-4 minutes</b> - Prevents stale connections while allowing pooling benefits</li>
 * <li><b>max-life-time: 5-30 minutes</b> - Use 5-10 min for dynamic environments (Kubernetes),
 * 15-30 min for stable infrastructure</li>
 * <li><b>TCP keep-alive NOT necessary</b> - With proper lifecycle settings, keep-alive adds
 * no value for typical REST APIs. Only needed for WebSockets, SSE, or long-lived streams.</li>
 * <li>Size {@code maxConnections} smaller than blocking clients (20-50 is often sufficient)</li>
 * </ul>
 * 
 * <p>
 * <b>Reactive Considerations:</b><br>
 * Unlike blocking HTTP clients, Reactor Netty can handle many concurrent
 * requests with a smaller connection pool due to non-blocking I/O. A pool of 50
 * connections can handle thousands of concurrent reactive streams.
 * 
 * <p>
 * <b>When to Tune These Settings:</b>
 * <ul>
 * <li><b>High-concurrency reactive apps:</b> Use small pools (20-50) - non-blocking I/O handles it</li>
 * <li><b>Dynamic environments:</b> Use shorter maxLifeTime (5-10 minutes)</li>
 * <li><b>Stable infrastructure:</b> Use longer maxLifeTime (15-30 minutes)</li>
 * <li><b>Special cases only:</b> Enable TCP keep-alive for WebSockets, SSE, or long-lived streams</li>
 * <li><b>Linux/Epoll:</b> TCP keep-alive options only work with Epoll transport (not NIO)</li>
 * </ul>
 * 
 * @see ReactorHttpClientConfiguration
 */
@ConfigurationProperties("miller79.reactor")
@Data
class ReactorHttpClientConfigurationProperties {

    /**
     * Name identifier for the connection provider. Useful for monitoring and
     * debugging multiple HTTP clients in reactive applications.
     */
    private String name = "connection";

    /**
     * Maximum number of connections in the pool.
     * 
     * <p>
     * In reactive applications, this can be smaller than blocking clients because
     * connections are used asynchronously and can handle multiple concurrent
     * reactive streams without blocking threads.
     * 
     * <p>
     * Size this based on:
     * <ul>
     * <li>Expected concurrent outbound HTTP requests</li>
     * <li>Target server capacity and rate limits</li>
     * <li>Nature of workload (short-lived requests vs long-lived
     * SSE/WebSocket)</li>
     * </ul>
     * 
     * <p>
     * <b>🔑 Key Difference from Blocking Clients:</b><br>
     * A blocking client needs one connection per concurrent request. A reactive
     * client can multiplex many concurrent reactive streams over a single
     * connection using non-blocking I/O. This means you typically need 5-10x fewer
     * connections.
     * 
     * <p>
     * <b>What happens if this is too low:</b>
     * <ul>
     * <li>Reactive publishers wait for available connections (backpressure)</li>
     * <li>Increased latency as requests queue behind connection acquisition</li>
     * <li>"Pending acquire queue has reached its maximum size" errors</li>
     * <li>Throughput limited by connection availability</li>
     * </ul>
     * 
     * <p>
     * <b>What happens if this is too high:</b>
     * <ul>
     * <li>Memory overhead (Netty buffers + connection state)</li>
     * <li>Excessive concurrent load on downstream services</li>
     * <li>File descriptor exhaustion (each connection = OS handle)</li>
     * <li>Unused connections waste resources (unlike blocking where threads are
     * saved)</li>
     * </ul>
     * 
     * <p>
     * <b>How to size this for reactive applications:</b>
     * <ol>
     * <li><b>Start small:</b> Begin with 10-20 connections for most services</li>
     * <li><b>Monitor actual usage:</b> Track "active connections" vs "pending
     * acquisitions" metrics</li>
     * <li><b>Scale based on patterns:</b>
     * <ul>
     * <li>Short requests (REST APIs): 10-50 connections handles thousands of
     * req/s</li>
     * <li>Streaming (SSE/WebSocket): 1 connection per stream; size accordingly</li>
     * <li>Mixed workload: 50-100 connections for high-throughput services</li>
     * </ul>
     * </li>
     * <li><b>Consider target capacity:</b> Don't exceed what the downstream service
     * can handle</li>
     * </ol>
     * 
     * <p>
     * <b>Example sizing scenarios:</b>
     * <ul>
     * <li><b>Low-volume microservice:</b> 5-10 connections</li>
     * <li><b>Typical REST API client:</b> 20-50 connections</li>
     * <li><b>High-throughput gateway:</b> 100-200 connections</li>
     * <li><b>Long-lived SSE streams:</b> 50-500 (one per concurrent stream)</li>
     * </ul>
     * 
     * <p>
     * Default: 500 connections (if not set, underlying client default is used)
     */
    private Integer maxConnections = 500;

    /**
     * Maximum time a connection can remain idle before being evicted.
     * 
     * <p>
     * Idle connections are automatically removed to free resources. In reactive
     * applications, this helps manage connection churn when load varies over time.
     * 
     * <p>
     * Should be less than the server's idle timeout to prevent server-side
     * closures.
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
     * Maximum lifetime of a connection, regardless of activity.
     * 
     * <p>
     * Connections are closed and recreated after this duration to prevent issues
     * with long-lived connections (e.g., load balancer timeouts, DNS changes,
     * resource leaks).
     * 
     * <p>
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
     * Default: Not set (if not configured, connections may live indefinitely)
     */
    private Duration maxLifeTime;

    /**
     * Enable TCP keep-alive at the socket level.
     * 
     * <p>
     * When enabled, the kernel sends periodic probes on idle connections to detect
     * broken connections (e.g., due to network failures, firewall timeouts, server
     * crashes).
     * 
     * <p>
     * This is particularly important for reactive applications that may hold
     * connections open for extended periods waiting for asynchronous events.
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
     * <b>Note:</b> This option only works on Linux when using Netty's Epoll
     * transport. On other platforms, the system default is used.
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
     * <b>Note:</b> This option only works on Linux when using Netty's Epoll
     * transport. On other platforms, the system default is used.
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
     * <b>Note:</b> This option only works on Linux when using Netty's Epoll
     * transport. On other platforms, the system default is used.
     * 
     * <p>
     * Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>
     * Default: 3 probes (if not set, underlying client default is used)
     */
    private Integer tcpKeepCount;
}
