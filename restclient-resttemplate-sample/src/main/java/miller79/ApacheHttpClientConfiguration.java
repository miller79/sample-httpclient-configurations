package miller79;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer;
import org.springframework.boot.http.client.HttpComponentsClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

/**
 * Configuration class for Apache HttpClient 5 used by RestTemplate and
 * RestClient.
 * 
 * <p>
 * This configuration demonstrates how to properly configure low-level HTTP
 * client settings including connection pooling, timeouts, and keep-alive
 * behavior. These settings are critical for production applications to ensure
 * resilience, performance, and proper resource management.
 * 
 * <p>
 * <b>Key Configuration Areas:</b>
 * <ul>
 * <li><b>Connection Pooling:</b> Controls the maximum number of concurrent
 * connections</li>
 * <li><b>Timeouts:</b> Defines various timeout thresholds for connection and
 * response handling</li>
 * <li><b>Keep-Alive:</b> Manages TCP keep-alive settings to maintain persistent
 * connections</li>
 * <li><b>Connection Lifecycle:</b> Controls how long connections can live and
 * when to evict idle ones</li>
 * </ul>
 * 
 * <p>
 * All configuration values are externalized via
 * {@link ApacheHttpClientConfigurationProperties} and can be overridden in
 * application.yml or application.properties.
 * 
 * @see ApacheHttpClientConfigurationProperties
 * @see RestClientConfiguration
 * @see RestTemplateConfiguration
 */
@Configuration
@RequiredArgsConstructor
public class ApacheHttpClientConfiguration {
    private final ApacheHttpClientConfigurationProperties apacheHttpClientConfigurationProperties;

    /**
     * Creates a {@link ClientHttpRequestFactoryBuilderCustomizer} for Apache HttpClient 5.
     * 
     * <p>This customizer is automatically applied by Spring Boot 3.4+ to configure both RestClient
     * and RestTemplate instances with custom HTTP client settings. It configures:
     * <ul>
     *   <li><b>Request Configuration:</b> Controls response timeout behavior</li>
     *   <li><b>Socket Configuration:</b> Manages TCP keep-alive settings at the socket level</li>
     *   <li><b>Connection Configuration:</b> Defines connection lifecycle (time-to-live and validation)</li>
     *   <li><b>Connection Manager:</b> Manages a pool of reusable HTTP connections</li>
     *   <li><b>HTTP Client Customization:</b> Enables automatic eviction of expired and idle connections</li>
     * </ul>
     * 
     * <p><b>Timeout Configuration:</b>
     * <ul>
     *   <li><b>Response Timeout:</b> Maximum time to wait for server response</li>
     *   <li><b>Connection Time-to-Live:</b> Maximum lifetime of a connection in the pool</li>
     *   <li><b>Validate After Inactivity:</b> Time before a connection is validated before reuse</li>
     *   <li><b>Idle Eviction:</b> Connections idle longer than this are removed from pool</li>
     * </ul>
     * 
     * <p><b>Keep-Alive Strategy:</b><br>
     * TCP keep-alive probes are configured to detect broken connections. The kernel sends probes
     * after {@code tcpKeepIdle} seconds of inactivity, then sends {@code tcpKeepCount} probes
     * every {@code tcpKeepInterval} seconds. If all probes fail, the connection is considered dead.
     * 
     * <p><b>Connection Pool:</b><br>
     * The pool manages a maximum of {@code maxConnections} total connections across all routes.
     * Connections are reused for multiple requests to improve performance and reduce the overhead
     * of establishing new connections.
     * 
     * <p><b>Auto-Configuration:</b><br>
     * Spring Boot automatically applies this customizer to all RestClient.Builder and RestTemplateBuilder
     * instances in the application context. The customizer uses a fluent builder API to configure
     * each aspect of the HTTP client separately.
     * 
     * @return a ClientHttpRequestFactoryBuilderCustomizer for Apache HttpClient 5
     * @see ClientHttpRequestFactoryBuilderCustomizer for Spring Boot 3.4+ customization pattern
     * @see HttpComponentsClientHttpRequestFactoryBuilder for Apache HttpClient 5 builder
     * @see org.apache.hc.client5.http.config.RequestConfig for request-level timeout configuration
     * @see org.apache.hc.core5.http.io.SocketConfig for socket-level TCP settings
     * @see org.apache.hc.client5.http.config.ConnectionConfig for connection lifecycle management
     */
    @Bean
    ClientHttpRequestFactoryBuilderCustomizer<HttpComponentsClientHttpRequestFactoryBuilder> apacheHttpClientTuning() {
        // Configure connection lifecycle (time-to-live and validation)
        ConnectionConfig connectionConfig = ConnectionConfig
                .custom()
                .setTimeToLive(Timeout.of(apacheHttpClientConfigurationProperties.getMaxLifeTime()))
                .setValidateAfterInactivity(Timeout.of(apacheHttpClientConfigurationProperties.getMaxIdleTime()))
                .build();

        // Return customizer with fluent builder configuration
        return builder -> builder
                // Configure request-level timeouts (response timeout)
                .withDefaultRequestConfigCustomizer(rcb -> rcb
                        .setResponseTimeout(Timeout.of(apacheHttpClientConfigurationProperties.getResponseTimeout())))
                // Configure socket-level TCP keep-alive settings
                .withSocketConfigCustomizer(scb -> scb
                        .setSoKeepAlive(apacheHttpClientConfigurationProperties.isSoKeepAlive())
                        .setTcpKeepCount(apacheHttpClientConfigurationProperties.getTcpKeepCount())
                        .setTcpKeepIdle(apacheHttpClientConfigurationProperties.getTcpKeepIdle().toSecondsPart())
                        .setTcpKeepInterval(
                                apacheHttpClientConfigurationProperties.getTcpKeepInterval().toSecondsPart()))
                // Configure connection pool with max connections and lifecycle
                .withConnectionManagerCustomizer(cmb -> cmb
                        .setMaxConnTotal(apacheHttpClientConfigurationProperties.getMaxConnections())
                        .setDefaultConnectionConfig(connectionConfig))
                // Configure HTTP client to evict expired and idle connections
                .withHttpClientCustomizer(hcb -> hcb
                        .evictExpiredConnections()
                        .evictIdleConnections(TimeValue.of(apacheHttpClientConfigurationProperties.getMaxIdleTime())));
    }

}
