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
 * Netty settings including connection pooling, timeouts, and TCP keep-alive
 * behavior. These settings are critical for reactive applications to ensure
 * resilience, performance, and proper resource management.
 * 
 * <p>
 * <b>Key Configuration Areas:</b>
 * <ul>
 * <li><b>Connection Provider:</b> Manages connection pooling and lifecycle in
 * reactive context</li>
 * <li><b>Channel Options:</b> Low-level Netty socket configuration</li>
 * <li><b>Keep-Alive:</b> TCP keep-alive settings to maintain persistent
 * connections</li>
 * <li><b>Response Timeout:</b> Maximum time to wait for server response</li>
 * </ul>
 * 
 * <p>
 * All configuration values are externalized via
 * {@link ReactorHttpClientConfigurationProperties} and can be overridden in
 * application.yml or application.properties.
 * 
 * <p>
 * <b>Reactive vs Blocking:</b><br>
 * Unlike Apache HttpClient (blocking I/O), Reactor Netty uses non-blocking I/O
 * with Netty's event loop model. This allows handling many concurrent
 * connections with fewer threads, making it ideal for high-throughput reactive
 * applications.
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

    /**
     * Creates a {@link ClientHttpConnectorBuilderCustomizer} with custom Reactor Netty configuration.
     * 
     * <p>This customizer is automatically applied by Spring Boot 3.4+ to configure all WebClient
     * instances with custom Reactor Netty HTTP client settings. It configures:
     * <ul>
     *   <li><b>Connection Provider:</b> Manages connection pool with configurable size and lifecycle</li>
     *   <li><b>Reactor Resource Factory:</b> Shared event loop and connection provider resources</li>
     *   <li><b>HTTP Client Factory:</b> Creates HttpClient with TCP keep-alive and channel options</li>
     * </ul>
     * 
     * <p><b>Connection Pool Management:</b><br>
     * The {@link ConnectionProvider} manages a pool of connections that can be reused across
     * multiple HTTP requests. Key behaviors:
     * <ul>
     *   <li><b>maxConnections:</b> Maximum number of connections in the pool</li>
     *   <li><b>maxIdleTime:</b> Connections idle longer than this are evicted</li>
     *   <li><b>maxLifeTime:</b> Maximum lifetime of any connection (prevents stale connections)</li>
     *   <li><b>evictInBackground:</b> Periodic cleanup interval for idle connections</li>
     * </ul>
     * 
     * <p><b>TCP Keep-Alive Strategy:</b><br>
     * TCP keep-alive is configured at the Netty channel level using Epoll options (Linux-specific).
     * The kernel sends keep-alive probes after {@code tcpKeepIdle} seconds of inactivity, then
     * sends {@code tcpKeepCount} probes every {@code tcpKeepInterval} seconds. If all probes fail,
     * the connection is closed.
     * 
     * <p><b>Epoll vs NIO:</b><br>
     * This configuration uses {@link EpollChannelOption} which provides native socket options on Linux.
     * For cross-platform support, you may need to conditionally use {@code ChannelOption} instead.
     * 
     * <p><b>Response Timeout:</b><br>
     * Note that this configuration does not set a response timeout. Spring Boot 3.4+ provides
     * separate mechanisms for configuring response timeouts on WebClient instances if needed.
     * Unlike Apache HttpClient's multiple timeout layers, Reactor Netty primarily uses a single
     * response timeout that applies to the entire request/response cycle.
     * 
     * <p><b>Auto-Configuration:</b><br>
     * Spring Boot automatically applies this customizer to all WebClient.Builder instances in
     * the application context using the fluent builder API.
     * 
     * @param resources Spring's ReactorResourceFactory for shared event loop management
     * @return a configured ClientHttpConnectorBuilderCustomizer for Reactor Netty
     * @see ClientHttpConnectorBuilderCustomizer for Spring Boot 3.4+ customization pattern
     * @see ReactorClientHttpConnectorBuilder for Reactor Netty builder
     * @see ConnectionProvider for connection pool configuration
     * @see HttpClient for HTTP client customization
     * @see EpollChannelOption for Linux-specific TCP options
     */
    @Bean
    ClientHttpConnectorBuilderCustomizer<ReactorClientHttpConnectorBuilder> reactorHttpClientConnectorBuilderCustomizer(
            ReactorResourceFactory resources) {
        // Create a connection provider with pooling and lifecycle management
        ConnectionProvider connectionProvider = ConnectionProvider
                .builder(reactorHttpClientConfigurationProperties.getName())
                .maxConnections(reactorHttpClientConfigurationProperties.getMaxConnections())
                .maxIdleTime(reactorHttpClientConfigurationProperties.getMaxIdleTime())
                .maxLifeTime(reactorHttpClientConfigurationProperties.getMaxLifeTime())
                .evictInBackground(reactorHttpClientConfigurationProperties.getMaxIdleTime())
                .build();

        // Return customizer with fluent builder configuration
        return builder -> builder
                // Configure shared reactor resources (event loops, etc.)
                .withReactorResourceFactory(resources)
                // Configure HTTP client with connection provider and channel options
                .withHttpClientFactory(() -> HttpClient
                        .create(connectionProvider)
                        // Enable TCP keep-alive at socket level
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        // Time before first keep-alive probe (Linux/Epoll only)
                        .option(EpollChannelOption.TCP_KEEPIDLE,
                                reactorHttpClientConfigurationProperties.getTcpKeepIdle().toSecondsPart())
                        // Interval between keep-alive probes (Linux/Epoll only)
                        .option(EpollChannelOption.TCP_KEEPINTVL,
                                reactorHttpClientConfigurationProperties.getTcpKeepInterval().toSecondsPart())
                        // Number of failed probes before connection is dead (Linux/Epoll only)
                        .option(EpollChannelOption.TCP_KEEPCNT,
                                reactorHttpClientConfigurationProperties.getTcpKeepCount()));
    }
}
