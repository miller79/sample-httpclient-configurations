package miller79;

import org.springframework.boot.http.client.reactive.ClientHttpConnectorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@RequiredArgsConstructor
class ReactorHttpClientConfiguration {
    private final ReactorHttpClientConfigurationProperties reactorHttpClientConfigurationProperties;

    @Bean
    ClientHttpConnectorBuilder<?> nettyConnectorCustomizer(ReactorResourceFactory resources) {
        ConnectionProvider connectionProvider = ConnectionProvider
                .builder(reactorHttpClientConfigurationProperties.getName())
                .maxConnections(reactorHttpClientConfigurationProperties.getMaxConnections())
                .maxIdleTime(reactorHttpClientConfigurationProperties.getMaxIdleTime())
                .maxLifeTime(reactorHttpClientConfigurationProperties.getMaxLifeTime())
                .evictInBackground(reactorHttpClientConfigurationProperties.getMaxIdleTime())
                .build();

        return ClientHttpConnectorBuilder
                .reactor()
                .withHttpClientFactory(() -> HttpClient
                        .create(connectionProvider)
                        .option(ChannelOption.SO_KEEPALIVE, reactorHttpClientConfigurationProperties.isSoKeepAlive())
                        .option(EpollChannelOption.TCP_KEEPIDLE,
                                reactorHttpClientConfigurationProperties.getTcpKeepIdle().toMillisPart())
                        .option(EpollChannelOption.TCP_KEEPINTVL,
                                reactorHttpClientConfigurationProperties.getTcpKeepInterval().toMillisPart())
                        .option(EpollChannelOption.TCP_KEEPCNT,
                                reactorHttpClientConfigurationProperties.getTcpKeepCount())
                        .responseTimeout(reactorHttpClientConfigurationProperties.getResponseTimeout()));
    }
}
