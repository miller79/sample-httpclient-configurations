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
 * <li><b>Memory exhaustion</b> - Pending reactive streams accumulate without bounds</li>
 * <li><b>Resource leaks</b> - Stale connections never cleaned up</li>
 * <li><b>Backpressure issues</b> - Slow downstream services blocking reactive pipelines</li>
 * <li><b>Cascading failures</b> - Unbound timeouts propagating through reactive chains</li>
 * </ul>
 * 
 * <p>
 * <b>Example Configuration (application.yml):</b>
 * 
 * <pre>{@code
 * miller79:
 *   reactor:
 *     name: my-webclient
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
 * <li><b>name:</b> Identifier for the connection provider (useful for
 * monitoring/debugging)</li>
 * <li><b>maxConnections:</b> Maximum number of connections in the pool</li>
 * <li><b>maxIdleTime:</b> How long idle connections remain before eviction</li>
 * <li><b>maxLifeTime:</b> Maximum lifetime of any connection</li>
 * <li><b>soKeepAlive:</b> Enable TCP keep-alive at socket level</li>
 * <li><b>tcpKeepIdle:</b> Time before first keep-alive probe is sent (Linux
 * only)</li>
 * <li><b>tcpKeepInterval:</b> Time between subsequent keep-alive probes (Linux
 * only)</li>
 * <li><b>tcpKeepCount:</b> Number of failed probes before connection is dead
 * (Linux only)</li>
 * <li><b>responseTimeout:</b> Maximum time to wait for server response</li>
 * </ul>
 * 
 * <p>
 * <b>Best Practices:</b>
 * <ul>
 * <li>Set {@code maxLifeTime} less than server's idle timeout to prevent
 * server-side closures</li>
 * <li>Set {@code maxIdleTime} to clean up unused connections and free
 * resources</li>
 * <li>Size {@code maxConnections} based on expected concurrent reactive
 * streams</li>
 * <li>Set {@code responseTimeout} generously for reactive streams that may
 * produce data over time</li>
 * <li>TCP keep-alive options only work on Linux when using Epoll transport</li>
 * </ul>
 * 
 * <p>
 * <b>Reactive Considerations:</b><br>
 * Unlike blocking HTTP clients, Reactor Netty can handle many concurrent
 * requests with a smaller connection pool due to non-blocking I/O. A pool of 64
 * connections can handle thousands of concurrent reactive streams.
 * 
 * <p>
 * <b>When to Tune These Settings:</b>
 * <ul>
 * <li><b>High-concurrency reactive apps:</b> Still use relatively small pools (20-100)</li>
 * <li><b>Streaming APIs (SSE, WebSocket):</b> Increase maxConnections for long-lived streams</li>
 * <li><b>Behind load balancers:</b> Set maxLifeTime &lt; LB idle timeout</li>
 * <li><b>Microservice mesh:</b> Use shorter timeouts for fast failure detection</li>
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
     * client can multiplex many concurrent reactive streams over a single connection
     * using non-blocking I/O. This means you typically need 5-10x fewer connections.
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
     * <li>Unused connections waste resources (unlike blocking where threads are saved)</li>
     * </ul>
     * 
     * <p>
     * <b>How to size this for reactive applications:</b>
     * <ol>
     * <li><b>Start small:</b> Begin with 10-20 connections for most services</li>
     * <li><b>Monitor actual usage:</b> Track "active connections" vs "pending acquisitions" metrics</li>
     * <li><b>Scale based on patterns:</b>
     *   <ul>
     *     <li>Short requests (REST APIs): 10-50 connections handles thousands of req/s</li>
     *     <li>Streaming (SSE/WebSocket): 1 connection per stream; size accordingly</li>
     *     <li>Mixed workload: 50-100 connections for high-throughput services</li>
     *   </ul>
     * </li>
     * <li><b>Consider target capacity:</b> Don't exceed what the downstream service can handle</li>
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
     * Default: 64 connections
     */
    private int maxConnections = 64;

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
     * Default: 60 seconds
     */
    private Duration maxIdleTime = Duration.ofSeconds(60);

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
     * Default: 60 seconds
     */
    private Duration maxLifeTime = Duration.ofSeconds(60);

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
     * <b>Note:</b> This option only works on Linux when using Netty's Epoll
     * transport. On other platforms, the system default is used.
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
     * <b>Note:</b> This option only works on Linux when using Netty's Epoll
     * transport. On other platforms, the system default is used.
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
     * <b>Note:</b> This option only works on Linux when using Netty's Epoll
     * transport. On other platforms, the system default is used.
     * 
     * <p>
     * Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>
     * Default: 3 probes
     */
    private int tcpKeepCount = 3;
}
