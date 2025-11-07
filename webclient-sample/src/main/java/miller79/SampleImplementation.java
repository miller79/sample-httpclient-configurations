package miller79;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample implementation demonstrating the use of configured reactive HTTP
 * client.
 * 
 * <p>
 * This component runs at application startup and demonstrates how to use
 * WebClient with the custom Reactor Netty HTTP client configuration. It shows
 * that the configured client works correctly for making reactive HTTP requests.
 * 
 * <p>
 * <b>Reactive HTTP Clients Available:</b>
 * <ul>
 * <li>{@link #sampleWebClient} - WebClient without authentication
 * (demonstrated)</li>
 * <li>{@link #sampleWebClientWithAuth} - WebClient with OAuth2 authentication
 * (available for use)</li>
 * </ul>
 * 
 * <p>
 * Both clients use non-blocking I/O via Reactor Netty, which means they don't
 * tie up threads waiting for HTTP responses. This allows handling many
 * concurrent requests efficiently.
 * 
 * <p>
 * The clients inherit custom Reactor Netty configuration from
 * {@link ReactorHttpClientConfiguration}, including:
 * <ul>
 * <li>Connection pooling (configurable via max-connections)</li>
 * <li>Connection lifecycle management (max-idle-time, max-life-time)</li>
 * <li>TCP keep-alive settings (optional, Linux/Epoll only)</li>
 * <li>Background connection eviction</li>
 * </ul>
 * 
 * <p>
 * The authenticated variant also includes OAuth2 token management from
 * {@link SecurityConfiguration}.
 * 
 * <p>
 * <b>Blocking Note:</b><br>
 * This example uses {@code .block()} to wait for the response, which is
 * acceptable for this ApplicationRunner demonstration. In a real reactive
 * application, you would subscribe to the Mono/Flux and process results
 * asynchronously without blocking.
 * 
 * @see WebClientConfiguration
 * @see ReactorHttpClientConfiguration
 * @see SecurityConfiguration
 * @see reactor.core.publisher.Mono
 */
@Component
@RequiredArgsConstructor
@Slf4j
class SampleImplementation implements ApplicationRunner {
    private static final String MAIN_URL = "https://www.google.com";
    private final WebClient sampleWebClient;
    private final WebClient sampleWebClientWithAuth;

    /**
     * Executes a sample HTTP request on application startup to demonstrate the
     * configured WebClient.
     * 
     * <p>
     * This method makes a simple GET request to a public URL to verify that the
     * WebClient is configured correctly and can successfully make reactive requests
     * with the custom connection pool, keep-alive, and lifecycle settings.
     * 
     * <p>
     * The {@code .block()} call is used here for demonstration purposes in an
     * ApplicationRunner context. In production reactive code, you should avoid
     * blocking and instead use reactive operators to compose asynchronous
     * operations.
     * 
     * @param args command line arguments (not used)
     * @throws Exception if HTTP request fails
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log
                .info("SampleWebClient response: {}",
                        sampleWebClient.get().uri(MAIN_URL).retrieve().bodyToMono(String.class).block());
    }

}
