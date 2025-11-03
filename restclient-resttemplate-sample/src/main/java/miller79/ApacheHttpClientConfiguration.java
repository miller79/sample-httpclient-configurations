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

@Configuration
@RequiredArgsConstructor
public class ApacheHttpClientConfiguration {
    private final ApacheHttpClientConfigurationProperties apacheHttpClientConfigurationProperties;

    @Bean
    CloseableHttpClient pooledHttpClient() {
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setResponseTimeout(Timeout.of(apacheHttpClientConfigurationProperties.getResponseTimeout()))
                .build();

        SocketConfig socketConfig = SocketConfig
                .custom()
                .setSoKeepAlive(apacheHttpClientConfigurationProperties.isSoKeepAlive())
                .setTcpKeepCount(apacheHttpClientConfigurationProperties.getTcpKeepCount())
                .setTcpKeepIdle(apacheHttpClientConfigurationProperties.getTcpKeepIdle().toSecondsPart())
                .setTcpKeepInterval(apacheHttpClientConfigurationProperties.getTcpKeepInterval().toSecondsPart())
                .build();

        ConnectionConfig connectionConfig = ConnectionConfig
                .custom()
                .setTimeToLive(Timeout.of(apacheHttpClientConfigurationProperties.getMaxLifeTime()))
                .build();

        HttpClientConnectionManager httpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setMaxConnTotal(apacheHttpClientConfigurationProperties.getMaxConnections())
                .setDefaultConnectionConfig(connectionConfig)
                .setDefaultSocketConfig(socketConfig)
                .build();

        return HttpClients
                .custom()
                .setConnectionManager(httpClientConnectionManager)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.of(apacheHttpClientConfigurationProperties.getMaxIdleTime()))
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Bean
    RestClientCustomizer restClientBuilderCustomizer(CloseableHttpClient pooledHttpClient) {
        return builder -> builder.requestFactory(new HttpComponentsClientHttpRequestFactory(pooledHttpClient));
    }

    @Bean
    RestTemplateBuilder restTemplateBuilder(
            RestTemplateBuilderConfigurer configurer,
            CloseableHttpClient pooledHttpClient) {
        return configurer
                .configure(new RestTemplateBuilder())
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(pooledHttpClient));
    }
}
