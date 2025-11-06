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
 * client settings including connection pooling, lifecycle management, and
 * optional TCP keep-alive behavior. These settings are critical for production
 * applications to ensure resilience, performance, and proper resource
 * management.
 * 
 * <p>
 * <b>⭐ Production Best Practice Configuration:</b>
 * 
 * <pre>{@code
 * miller79:
 *   apache:
 *     max-idle-time: 3m      # Close idle connections after 3 minutes
 *     max-life-time: 30m     # Replace all connections every 30 minutes
 * }</pre>
 * 
 * <p>
 * <b>🔧 How This Configuration Works:</b>
 * <ul>
 * <li><b>Null-Safe:</b> Only applies settings if explicitly configured (preserves
 * underlying client defaults)</li>
 * <li><b>Connection Lifecycle:</b> Uses {@code maxIdleTime} and {@code maxLifeTime}
 * to prevent stale connections</li>
 * <li><b>Optional Keep-Alive:</b> TCP keep-alive settings available but NOT
 * necessary with proper lifecycle management</li>
 * <li><b>Platform-Agnostic:</b> Works on Windows, macOS, and Linux without
 * platform-specific code</li>
 * </ul>
 * 
 * <p>
 * <b>❌ Common Production Issues (and how this prevents them):</b>
 * 
 * <p>
 * <b>Issue #1: Stale Connections Behind Load Balancers</b>
 * 
 * <pre>
 * Problem: AWS ALB has 60s idle timeout. Client holds connection for 5 minutes.
 *          Load balancer closes connection silently. Next request fails with
 *          "Connection reset by peer" or "Broken pipe".
 * 
 * Solution: Set max-idle-time: 3m (shorter than ALB timeout)
 *           Connections are proactively closed before becoming stale.
 * </pre>
 * 
 * <p>
 * <b>Issue #2: Long-Lived Connections in Dynamic Environments</b>
 * 
 * <pre>
 * Problem: In Kubernetes, pods scale down or restart. Client holds connections
 *          to terminated pods, leading to connection failures.
 * 
 * Solution: Set max-life-time: 10m (short enough for pod churn)
 *           All connections are cycled regularly, discovering new endpoints.
 * </pre>
 * 
 * <p>
 * <b>Issue #3: Connection Pool Exhaustion</b>
 * 
 * <pre>
 * Problem: Default pool size too small. High traffic causes threads to wait
 *          indefinitely for available connections.
 * 
 * Solution: Set max-connections: 100 (based on peak concurrent requests)
 *           Monitor with Micrometer: httpcomponents.httpclient.pool.total.max
 * </pre>
 * 
 * <p>
 * <b>🔑 Why TCP Keep-Alive Is NOT Needed:</b><br>
 * With {@code max-idle-time: 3m} and {@code max-life-time: 30m}, connections are
 * regularly cycled. TCP keep-alive was designed for long-lived idle connections
 * (hours), which don't exist in this configuration. Only enable keep-alive for:
 * <ul>
 * <li>WebSockets or Server-Sent Events (long-lived push connections)</li>
 * <li>Aggressive corporate firewalls that drop idle TCP connections &lt; 3 minutes</li>
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
class ApacheHttpClientConfiguration {
    private final ApacheHttpClientConfigurationProperties apacheHttpClientConfigurationProperties;

    @Bean
    ClientHttpRequestFactoryBuilderCustomizer<HttpComponentsClientHttpRequestFactoryBuilder> apacheHttpClientTuning() {
        // Configure connection lifecycle (time-to-live)
        ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();
        if (apacheHttpClientConfigurationProperties.getMaxLifeTime() != null) {
            connectionConfigBuilder.setTimeToLive(Timeout.of(apacheHttpClientConfigurationProperties.getMaxLifeTime()));
        }

        // Return customizer with fluent builder configuration
        return builder -> builder
                // Configure socket-level TCP keep-alive settings
                .withSocketConfigCustomizer(scb -> {
                    if (apacheHttpClientConfigurationProperties.getSoKeepAlive() != null) {
                        scb.setSoKeepAlive(apacheHttpClientConfigurationProperties.getSoKeepAlive());
                    }
                    if (apacheHttpClientConfigurationProperties.getTcpKeepCount() != null) {
                        scb.setTcpKeepCount(apacheHttpClientConfigurationProperties.getTcpKeepCount());
                    }
                    if (apacheHttpClientConfigurationProperties.getTcpKeepIdle() != null) {
                        scb.setTcpKeepIdle((int) apacheHttpClientConfigurationProperties.getTcpKeepIdle().getSeconds());
                    }
                    if (apacheHttpClientConfigurationProperties.getTcpKeepInterval() != null) {
                        scb
                                .setTcpKeepInterval((int) apacheHttpClientConfigurationProperties
                                        .getTcpKeepInterval()
                                        .getSeconds());
                    }
                })
                // Configure connection pool with max connections and lifecycle
                .withConnectionManagerCustomizer(cmb -> {
                    if (apacheHttpClientConfigurationProperties.getMaxConnections() != null) {
                        cmb.setMaxConnTotal(apacheHttpClientConfigurationProperties.getMaxConnections());
                    }
                    cmb.setDefaultConnectionConfig(connectionConfigBuilder.build());
                })
                // Configure HTTP client to evict expired and idle connections
                .withHttpClientCustomizer(hcb -> {
                    hcb.evictExpiredConnections();
                    if (apacheHttpClientConfigurationProperties.getMaxIdleTime() != null) {
                        hcb
                                .evictIdleConnections(
                                        TimeValue.of(apacheHttpClientConfigurationProperties.getMaxIdleTime()));
                    }
                });
    }
}
