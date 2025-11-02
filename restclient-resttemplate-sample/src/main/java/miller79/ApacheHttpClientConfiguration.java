package miller79;

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ApacheHttpClientConfiguration {
    private static final int SERVER_IDLE_TIMEOUT_MINUTES = 5;
    private static final int APPLICATION_TIMEOUT_SECONDS = 60;
    private static final int NORMAL_WAIT_TIME_SECONDS = 10;
    private static final int POOL_MAX_CONNECTIONS = 64;

    @Bean
    RequestConfig requestConfig() {
        return RequestConfig
                .custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(NORMAL_WAIT_TIME_SECONDS / 2L))
                .setResponseTimeout(Timeout.ofSeconds(APPLICATION_TIMEOUT_SECONDS + 5L))
                .build();
    }

    @Bean
    SocketConfig socketConfig() {
        return SocketConfig
                .custom()
                .setSoKeepAlive(true)
                .setSoTimeout(Timeout.ofSeconds(APPLICATION_TIMEOUT_SECONDS))
                .build();
    }

    @Bean
    ConnectionConfig connectionConfig() {
        return ConnectionConfig
                .custom()
                .setValidateAfterInactivity(TimeValue.ofMinutes(SERVER_IDLE_TIMEOUT_MINUTES - 2L))
                .setTimeToLive(Timeout.ofMinutes((SERVER_IDLE_TIMEOUT_MINUTES - 1L) * 2))
                .setConnectTimeout(Timeout.ofSeconds(NORMAL_WAIT_TIME_SECONDS))
                .build();
    }

    @Bean
    ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        final DefaultConnectionKeepAliveStrategy customizedStrategy = new DefaultConnectionKeepAliveStrategy();
        final TimeValue defaultKeepAlive = TimeValue.ofMinutes(SERVER_IDLE_TIMEOUT_MINUTES - 1L);

        return (response, context) -> {
            TimeValue keepAliveDuration = customizedStrategy.getKeepAliveDuration(response, context);

            if (keepAliveDuration == null || keepAliveDuration.toMilliseconds() <= 0
                    || keepAliveDuration.toMilliseconds() > defaultKeepAlive.toMilliseconds()) {
                return defaultKeepAlive;
            }

            return keepAliveDuration;
        };
    }

    @Bean
    HttpClientConnectionManager httpClientConnectionManager(
            SocketConfig socketConfig,
            ConnectionConfig connectionConfig) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        cm.setMaxTotal(POOL_MAX_CONNECTIONS * 4);
        cm.setDefaultMaxPerRoute(POOL_MAX_CONNECTIONS * 2);
        cm.closeIdle(Timeout.ofMinutes(SERVER_IDLE_TIMEOUT_MINUTES - 1L));
        cm.setDefaultSocketConfig(socketConfig);
        cm.setDefaultConnectionConfig(connectionConfig);

        return cm;
    }

    @Bean
    CloseableHttpClient pooledHttpClient(
            HttpClientConnectionManager httpClientConnectionManager,
            ConnectionKeepAliveStrategy connectionKeepAliveStrategy,
            RequestConfig requestConfig) {
        return HttpClients
                .custom()
                .setConnectionManager(httpClientConnectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofMinutes(SERVER_IDLE_TIMEOUT_MINUTES - 1L))
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}
