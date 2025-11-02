package miller79;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class ReactorHttpClientConfiguration {
    private static final int MAX_CONNECTIONS = 64;
    private static final Duration MAX_IDLE = Duration.ofSeconds(60);
    private static final Duration MAX_LIFE = MAX_IDLE;
    private static final Duration PENDING_ACQUIRE_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration EVICT_IN_BACKGROUND_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration KEEP_ALIVE_IDLE = Duration.ofSeconds(30);
    private static final Duration KEEP_ALIVE_INTERVAL = Duration.ofSeconds(5);
    private static final int KEEP_ALIVE_COUNT = 3;
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    ConnectionProvider connectionProvider() {
        return ConnectionProvider
                .builder("webclient-connection")
                .maxConnections(MAX_CONNECTIONS)
                .maxIdleTime(MAX_IDLE)
                .maxLifeTime(MAX_LIFE)
                .pendingAcquireTimeout(PENDING_ACQUIRE_TIMEOUT)
                .evictInBackground(EVICT_IN_BACKGROUND_TIMEOUT)
                .pendingAcquireMaxCount(MAX_CONNECTIONS * 2)
                .build();
    }

    @Bean
    ReactorClientHttpConnector reactorClientHttpConnector(ConnectionProvider connectionProvider) {
        return new ReactorClientHttpConnector(HttpClient
                .create(connectionProvider)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.TCP_KEEPIDLE, KEEP_ALIVE_IDLE.getNano())
                .option(EpollChannelOption.TCP_KEEPINTVL, KEEP_ALIVE_INTERVAL.getNano())
                .option(EpollChannelOption.TCP_KEEPCNT, KEEP_ALIVE_COUNT)
                .responseTimeout(RESPONSE_TIMEOUT));
    }
}
