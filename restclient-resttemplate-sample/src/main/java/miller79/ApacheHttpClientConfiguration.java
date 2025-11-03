package miller79;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

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
     * Creates a configured {@link CloseableHttpClient} with connection pooling and
     * custom timeout settings.
     * 
     * <p>
     * This client is configured with:
     * <ul>
     * <li><b>Request Configuration:</b> Controls response timeout behavior</li>
     * <li><b>Socket Configuration:</b> Manages TCP keep-alive settings at the
     * socket level</li>
     * <li><b>Connection Configuration:</b> Defines connection lifecycle
     * (time-to-live)</li>
     * <li><b>Connection Pool:</b> Manages a pool of reusable HTTP connections</li>
     * <li><b>Automatic Eviction:</b> Removes expired and idle connections
     * automatically</li>
     * </ul>
     * 
     * <p>
     * <b>Timeout Hierarchy:</b>
     * <ol>
     * <li>Response Timeout - Maximum time to wait for server response</li>
     * <li>Socket Timeout (SO_TIMEOUT) - Socket-level read timeout (matches response
     * timeout)</li>
     * <li>Connection Time-to-Live - Maximum lifetime of a connection in the
     * pool</li>
     * <li>Idle Eviction - Connections idle longer than this are removed from
     * pool</li>
     * </ol>
     * 
     * <p>
     * <b>Keep-Alive Strategy:</b><br>
     * TCP keep-alive probes are configured to detect broken connections. The kernel
     * sends probes after {@code tcpKeepIdle} seconds of inactivity, then sends
     * {@code tcpKeepCount} probes every {@code tcpKeepInterval} seconds. If all
     * probes fail, the connection is considered dead.
     * 
     * <p>
     * <b>Connection Pool:</b><br>
     * The pool manages a maximum of {@code maxConnections} total connections across
     * all routes. Connections are reused for multiple requests to improve
     * performance and reduce the overhead of establishing new connections.
     * 
     * @return a fully configured CloseableHttpClient with connection pooling
     * @see RequestConfig for request-level timeout configuration
     * @see SocketConfig for socket-level TCP settings
     * @see ConnectionConfig for connection lifecycle management
     * @see PoolingHttpClientConnectionManagerBuilder for connection pool
     *      configuration
     */
    @Bean
    CloseableHttpClient pooledHttpClient() {
        // Configure request-level timeouts
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setResponseTimeout(Timeout.of(apacheHttpClientConfigurationProperties.getResponseTimeout()))
                .build();

        // Configure socket-level TCP keep-alive settings
        SocketConfig socketConfig = SocketConfig
                .custom()
                .setSoKeepAlive(apacheHttpClientConfigurationProperties.isSoKeepAlive())
                .setTcpKeepCount(apacheHttpClientConfigurationProperties.getTcpKeepCount())
                .setTcpKeepIdle(apacheHttpClientConfigurationProperties.getTcpKeepIdle().toSecondsPart())
                .setTcpKeepInterval(apacheHttpClientConfigurationProperties.getTcpKeepInterval().toSecondsPart())
                .build();

        // Configure connection lifecycle
        ConnectionConfig connectionConfig = ConnectionConfig
                .custom()
                .setTimeToLive(Timeout.of(apacheHttpClientConfigurationProperties.getMaxLifeTime()))
                .setValidateAfterInactivity(Timeout.of(apacheHttpClientConfigurationProperties.getMaxIdleTime()))
                .build();

        // Create connection pool with configurations
        HttpClientConnectionManager httpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setMaxConnTotal(apacheHttpClientConfigurationProperties.getMaxConnections())
                .setDefaultConnectionConfig(connectionConfig)
                .setDefaultSocketConfig(socketConfig)
                .build();

        // Build the HTTP client with all configurations
        return HttpClients
                .custom()
                .setConnectionManager(httpClientConnectionManager)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.of(apacheHttpClientConfigurationProperties.getMaxIdleTime()))
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Creates a {@link RestClientCustomizer} that configures all RestClient.Builder
     * instances to use the custom {@link CloseableHttpClient}.
     * 
     * <p>
     * This customizer is automatically applied to any
     * {@link org.springframework.web.client.RestClient.Builder} bean in the
     * application context. It replaces the default HTTP client with our pooled,
     * timeout-configured client.
     * 
     * <p>
     * <b>Usage:</b> Any RestClient.Builder injected into your beans will
     * automatically use the configured HTTP client with all the custom timeout and
     * pooling settings.
     * 
     * @param pooledHttpClient the configured Apache HttpClient
     * @return a RestClientCustomizer that applies the custom HTTP client
     * @see org.springframework.web.client.RestClient
     * @see HttpComponentsClientHttpRequestFactory
     */
    @Bean
    RestClientCustomizer restClientBuilderCustomizer(CloseableHttpClient pooledHttpClient) {
        return builder -> builder.requestFactory(new HttpComponentsClientHttpRequestFactory(pooledHttpClient));
    }

    /**
     * Creates a {@link RestTemplateBuilder} configured to use the custom
     * {@link CloseableHttpClient}.
     * 
     * <p>
     * This builder is automatically injected with Spring Boot's default
     * configuration via {@link RestTemplateBuilderConfigurer}, and then customized
     * to use our pooled HTTP client instead of the default client.
     * 
     * <p>
     * <b>Usage:</b> Inject this RestTemplateBuilder into your beans to create
     * RestTemplate instances that use the custom HTTP client:
     * 
     * <pre>{@code
     * @Autowired
     * private RestTemplateBuilder restTemplateBuilder;
     * 
     * public void someMethod() {
     *     RestTemplate restTemplate = restTemplateBuilder.build();
     *     // Use restTemplate with custom timeout and pooling settings
     * }
     * }</pre>
     * 
     * @param configurer       Spring Boot's default RestTemplateBuilder configurer
     * @param pooledHttpClient the configured Apache HttpClient
     * @return a RestTemplateBuilder with custom HTTP client configuration
     * @see org.springframework.web.client.RestTemplate
     * @see HttpComponentsClientHttpRequestFactory
     */
    @Bean
    RestTemplateBuilder restTemplateBuilder(
            RestTemplateBuilderConfigurer configurer,
            CloseableHttpClient pooledHttpClient) {
        return configurer
                .configure(new RestTemplateBuilder())
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(pooledHttpClient));
    }
}
