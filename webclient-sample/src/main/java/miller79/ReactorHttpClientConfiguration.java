package miller79;

import org.springframework.boot.autoconfigure.http.client.reactive.ClientHttpConnectorBuilderCustomizer;
import org.springframework.boot.http.client.reactive.ReactorClientHttpConnectorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * Configuration class for Reactor Netty HTTP client used by Spring WebFlux's
 * WebClient.
 * 
 * <p>
 * This configuration demonstrates how to properly configure low-level Reactor
 * Netty settings including connection pooling, lifecycle management, and
 * optional TCP keep-alive behavior. These settings are critical for reactive
 * applications to ensure resilience, performance, and proper resource
 * management.
 * 
 * <p>
 * <b>⭐ Production Best Practice Configuration:</b>
 * 
 * <pre>{@code
 * miller79:
 *   reactor:
 *     max-idle-time: 3m      # Close idle connections after 3 minutes
 *     max-life-time: 30m     # Replace all connections every 30 minutes
 * }</pre>
 * 
 * <p>
 * <b>🔧 How This Configuration Works:</b>
 * <ul>
 * <li><b>Null-Safe:</b> Only applies settings if explicitly configured
 * (preserves Reactor Netty defaults)</li>
 * <li><b>Connection Lifecycle:</b> Uses {@code maxIdleTime} and
 * {@code maxLifeTime} to prevent stale connections</li>
 * <li><b>Platform-Aware:</b> Detects Linux/Epoll vs Windows/NIO for TCP
 * keep-alive (if configured)</li>
 * <li><b>Reactive Efficiency:</b> Non-blocking I/O allows small pool sizes
 * (20-50 connections) to handle thousands of concurrent requests</li>
 * </ul>
 * 
 * <p>
 * <b>❌ Common Production Issues (and how this prevents them):</b>
 * 
 * <p>
 * <b>Issue #1: Stale Connections Behind Load Balancers</b>
 * 
 * <pre>
 * Problem: AWS ALB has 60s idle timeout. WebClient holds connection for 5 minutes.
 *          Load balancer closes connection silently. Next reactive stream fails
 *          with "Connection prematurely closed BEFORE response".
 * 
 * Solution: Set max-idle-time: 3m (shorter than ALB timeout)
 *           Reactor proactively closes idle connections before they become stale.
 * </pre>
 * 
 * <p>
 * <b>Issue #2: Memory Leaks from Indefinite Connection Pools</b>
 * 
 * <pre>
 * Problem: Without max-life-time, connections live forever. In Kubernetes with
 *          pod churn, client holds connections to terminated pods, causing
 *          "No route to host" errors and connection pool bloat.
 * 
 * Solution: Set max-life-time: 10m (short enough for pod lifecycle)
 *           All connections are cycled regularly, preventing memory leaks.
 * </pre>
 * 
 * <p>
 * <b>Issue #3: Over-Provisioning Connection Pools</b>
 * 
 * <pre>
 * Problem: Copying blocking HTTP client settings (max-connections: 200).
 *          Reactive apps don't need large pools due to non-blocking I/O.
 * 
 * Solution: Set max-connections: 50 (reactive apps need FEWER connections)
 *           Monitor with Micrometer: reactor.netty.connection.provider.active.connections
 * </pre>
 * 
 * <p>
 * <b>🔑 Why TCP Keep-Alive Is NOT Needed:</b><br>
 * With {@code max-idle-time: 3m} and {@code max-life-time: 30m}, connections
 * are regularly cycled. TCP keep-alive was designed for long-lived idle
 * connections (hours), which don't exist in this configuration. Only enable
 * keep-alive for:
 * <ul>
 * <li>WebSockets or Server-Sent Events (long-lived bidirectional streams)</li>
 * <li>Aggressive corporate firewalls that drop idle TCP connections &lt; 3
 * minutes</li>
 * <li><b>⚠️ Linux/Epoll only:</b> TCP keep-alive options require Epoll
 * transport (not available on Windows/macOS NIO)</li>
 * </ul>
 * 
 * <p>
 * <b>Reactive vs Blocking:</b><br>
 * Unlike Apache HttpClient (blocking I/O), Reactor Netty uses non-blocking I/O
 * with Netty's event loop model. This allows handling many concurrent
 * connections with fewer threads, making it ideal for high-throughput reactive
 * applications.
 * 
 * <p>
 * All configuration values are externalized via
 * {@link ReactorHttpClientConfigurationProperties} and can be overridden in
 * application.yml or application.properties.
 * 
 * @see ReactorHttpClientConfigurationProperties
 * @see WebClientConfiguration
 * @see ConnectionProvider
 * @see HttpClient
 */
@Configuration
@RequiredArgsConstructor
class ReactorHttpClientConfiguration {
    private final ReactorHttpClientConfigurationProperties reactorHttpClientConfigurationProperties;

    @Bean
    ClientHttpConnectorBuilderCustomizer<ReactorClientHttpConnectorBuilder> reactorHttpClientConnectorBuilderCustomizer(
            ReactorResourceFactory resources) {
        // Create a connection provider with pooling and lifecycle management
        ConnectionProvider.Builder cpBuilder = ConnectionProvider
                .builder(reactorHttpClientConfigurationProperties.getName());

        // Apply connection pool settings only if configured
        if (reactorHttpClientConfigurationProperties.getMaxConnections() != null) {
            cpBuilder.maxConnections(reactorHttpClientConfigurationProperties.getMaxConnections());
        }
        if (reactorHttpClientConfigurationProperties.getMaxIdleTime() != null) {
            cpBuilder.maxIdleTime(reactorHttpClientConfigurationProperties.getMaxIdleTime());
            // use 1/3 of idleTime for evictInBackground to proactively close idle
            // connections
            cpBuilder.evictInBackground(reactorHttpClientConfigurationProperties.getMaxIdleTime().dividedBy(3));
        }
        if (reactorHttpClientConfigurationProperties.getMaxLifeTime() != null) {
            cpBuilder.maxLifeTime(reactorHttpClientConfigurationProperties.getMaxLifeTime());
        }

        ConnectionProvider connectionProvider = cpBuilder.build();

        // Return customizer with fluent builder configuration
        return builder -> builder
                // Configure shared reactor resources (event loops, etc.)
                .withReactorResourceFactory(resources)
                // Configure HTTP client with connection provider and channel options
                .withHttpClientFactory(() -> createConfiguredHttpClient(connectionProvider));
    }

    /**
     * Creates an HttpClient with custom connection provider and TCP keep-alive
     * settings.
     * 
     * <p>
     * Applies TCP keep-alive options using platform-specific channel options:
     * <ul>
     * <li><b>Linux (Epoll):</b> Uses EpollChannelOption for optimal native
     * performance</li>
     * <li><b>Other platforms:</b> Uses standard ChannelOption where available</li>
     * </ul>
     * 
     * @param connectionProvider the connection provider with pooling configuration
     * @return configured HttpClient instance
     */
    private HttpClient createConfiguredHttpClient(ConnectionProvider connectionProvider) {
        // Start with base HttpClient
        HttpClient baseClient = HttpClient.create(connectionProvider);

        // Apply SO_KEEPALIVE if configured
        HttpClient clientWithKeepAlive = baseClient;
        if (reactorHttpClientConfigurationProperties.getSoKeepAlive() != null) {
            clientWithKeepAlive = baseClient
                    .option(ChannelOption.SO_KEEPALIVE, reactorHttpClientConfigurationProperties.getSoKeepAlive());
        }

        // Apply platform-specific TCP keep-alive options
        if (io.netty.channel.epoll.Epoll.isAvailable()) {
            return applyEpollKeepAliveOptions(clientWithKeepAlive);
        } else {
            return applyNioKeepAliveOptions(clientWithKeepAlive);
        }
    }

    /**
     * Applies Linux Epoll-specific TCP keep-alive options to the HttpClient.
     * 
     * @param client the base HttpClient
     * @return HttpClient with Epoll keep-alive options applied
     */
    private HttpClient applyEpollKeepAliveOptions(HttpClient client) {
        HttpClient configuredClient = client;

        // Apply TCP_KEEPIDLE: time before first probe
        if (reactorHttpClientConfigurationProperties.getTcpKeepIdle() != null) {
            int keepIdleSeconds = (int) reactorHttpClientConfigurationProperties.getTcpKeepIdle().getSeconds();
            configuredClient = configuredClient.option(EpollChannelOption.TCP_KEEPIDLE, keepIdleSeconds);
        }

        // Apply TCP_KEEPINTVL: interval between probes
        if (reactorHttpClientConfigurationProperties.getTcpKeepInterval() != null) {
            int keepIntervalSeconds = (int) reactorHttpClientConfigurationProperties.getTcpKeepInterval().getSeconds();
            configuredClient = configuredClient.option(EpollChannelOption.TCP_KEEPINTVL, keepIntervalSeconds);
        }

        // Apply TCP_KEEPCNT: number of failed probes before giving up
        if (reactorHttpClientConfigurationProperties.getTcpKeepCount() != null) {
            configuredClient = configuredClient
                    .option(EpollChannelOption.TCP_KEEPCNT, reactorHttpClientConfigurationProperties.getTcpKeepCount());
        }

        return configuredClient;
    }

    /**
     * Applies NIO-compatible TCP keep-alive options to the HttpClient.
     * 
     * <p>
     * On non-Linux platforms (Windows, macOS), SO_KEEPALIVE is enabled but
     * fine-grained control of keep-alive timing (idle, interval, count) is
     * typically not available through standard Java NIO. These platforms use
     * OS-level defaults.
     * 
     * <p>
     * Note: While some platforms may support custom keep-alive timing through
     * native extensions, this implementation uses the standard cross-platform
     * approach.
     * 
     * @param client the base HttpClient
     * @return HttpClient (unchanged on most platforms, as NIO doesn't support
     *         fine-grained keep-alive tuning)
     */
    private HttpClient applyNioKeepAliveOptions(HttpClient client) {
        // NIO on Windows/macOS doesn't expose TCP keep-alive timing parameters
        // SO_KEEPALIVE is already set, which enables keep-alive with OS defaults
        // OS defaults are typically:
        // - Windows: KeepAliveTime=2hrs, KeepAliveInterval=1sec (configurable via
        // registry)
        // - macOS: TCP_KEEPALIVE=2hrs (configurable via sysctl)

        return client;
    }
}
