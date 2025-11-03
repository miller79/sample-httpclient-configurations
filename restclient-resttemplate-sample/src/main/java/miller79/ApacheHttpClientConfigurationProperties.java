package miller79;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for Apache HttpClient 5 used by RestTemplate and RestClient.
 * 
 * <p>This class externalizes all HTTP client configuration settings, allowing them to be
 * configured via application.yml, application.properties, or environment variables.
 * 
 * <p><b>Example Configuration (application.yml):</b>
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
 * <p><b>Property Descriptions:</b>
 * <ul>
 *   <li><b>name:</b> Identifier for the connection pool (useful for monitoring/debugging)</li>
 *   <li><b>maxConnections:</b> Maximum total connections in the pool across all routes</li>
 *   <li><b>maxIdleTime:</b> How long idle connections remain in pool before eviction</li>
 *   <li><b>maxLifeTime:</b> Maximum lifetime of any connection (should be less than server timeout)</li>
 *   <li><b>soKeepAlive:</b> Enable TCP keep-alive at socket level</li>
 *   <li><b>tcpKeepIdle:</b> Time before first keep-alive probe is sent</li>
 *   <li><b>tcpKeepInterval:</b> Time between subsequent keep-alive probes</li>
 *   <li><b>tcpKeepCount:</b> Number of failed probes before connection is considered dead</li>
 *   <li><b>responseTimeout:</b> Maximum time to wait for server response</li>
 * </ul>
 * 
 * <p><b>Best Practices:</b>
 * <ul>
 *   <li>Set {@code maxLifeTime} less than server's idle timeout to prevent server-side connection closures</li>
 *   <li>Set {@code tcpKeepIdle} to detect broken connections before they're reused</li>
 *   <li>Size {@code maxConnections} based on expected concurrent requests and target server capacity</li>
 *   <li>Set {@code responseTimeout} based on expected API response times plus buffer for network latency</li>
 * </ul>
 * 
 * @see ApacheHttpClientConfiguration
 */
@ConfigurationProperties("miller79.apache")
@Data
public class ApacheHttpClientConfigurationProperties {
    
    /**
     * Name identifier for the connection pool.
     * Useful for monitoring and debugging multiple HTTP clients in the same application.
     */
    private String name = "connection";
    
    /**
     * Maximum number of total connections in the pool across all routes (host:port combinations).
     * 
     * <p>Size this based on:
     * <ul>
     *   <li>Expected concurrent HTTP requests from your application</li>
     *   <li>Target server capacity and rate limits</li>
     *   <li>Available system resources (memory, file descriptors)</li>
     * </ul>
     * 
     * <p>Default: 64 connections
     */
    private int maxConnections = 64;
    
    /**
     * Maximum time a connection can remain idle in the pool before being evicted.
     * 
     * <p>Idle connections are automatically removed after this duration to free resources.
     * Should be less than the server's idle timeout to prevent server-side connection closures.
     * 
     * <p>Default: 60 seconds
     */
    private Duration maxIdleTime = Duration.ofSeconds(60);
    
    /**
     * Maximum lifetime of a connection in the pool, regardless of activity.
     * 
     * <p>Connections are closed and recreated after this duration to prevent issues with
     * long-lived connections (e.g., load balancer timeouts, DNS changes).
     * Should be less than the server's connection timeout.
     * 
     * <p>Default: 60 seconds
     */
    private Duration maxLifeTime = Duration.ofSeconds(60);
    
    /**
     * Enable TCP keep-alive at the socket level.
     * 
     * <p>When enabled, the kernel sends periodic probes on idle connections to detect
     * broken connections (e.g., due to network failures, firewall timeouts).
     * 
     * <p>Default: true (enabled)
     */
    private boolean soKeepAlive = true;
    
    /**
     * Time of inactivity before the first TCP keep-alive probe is sent.
     * 
     * <p>After a connection has been idle for this duration, the kernel starts sending
     * keep-alive probes to verify the connection is still alive.
     * 
     * <p>Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>Default: 30 seconds
     */
    private Duration tcpKeepIdle = Duration.ofSeconds(30);
    
    /**
     * Time interval between subsequent TCP keep-alive probes.
     * 
     * <p>If the first probe fails, additional probes are sent at this interval
     * until either a response is received or {@code tcpKeepCount} probes have failed.
     * 
     * <p>Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>Default: 5 seconds
     */
    private Duration tcpKeepInterval = Duration.ofSeconds(5);
    
    /**
     * Number of failed TCP keep-alive probes before the connection is considered dead.
     * 
     * <p>If this many consecutive probes fail without receiving a response, the kernel
     * closes the connection and marks it as broken.
     * 
     * <p>Total time before connection is closed = {@code tcpKeepIdle} + ({@code tcpKeepCount} × {@code tcpKeepInterval})
     * 
     * <p>Only applicable when {@code soKeepAlive} is true.
     * 
     * <p>Default: 3 probes
     */
    private int tcpKeepCount = 3;
    
    /**
     * Maximum time to wait for a server response.
     * 
     * <p>This timeout applies to:
     * <ul>
     *   <li>Time waiting for response data to start arriving</li>
     *   <li>Time between consecutive data packets</li>
     * </ul>
     * 
     * <p>Set this based on:
     * <ul>
     *   <li>Expected API response times</li>
     *   <li>Network latency</li>
     *   <li>Server processing time</li>
     * </ul>
     * 
     * <p>If the server doesn't respond within this time, a timeout exception is thrown.
     * 
     * <p>Default: 5 seconds
     */
    private Duration responseTimeout = Duration.ofSeconds(5);
}
