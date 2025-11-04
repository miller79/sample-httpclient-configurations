package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for Spring WebFlux's {@link WebClient}.
 * 
 * <p>
 * WebClient is Spring Framework's modern reactive HTTP client introduced in
 * Spring 5. It provides a fluent API for making non-blocking HTTP requests and
 * is designed for reactive applications using Project Reactor.
 * 
 * <p>
 * This configuration creates a WebClient instance that uses the custom Reactor
 * Netty HTTP client configured in {@link ReactorHttpClientConfiguration}.
 * Spring Boot 3.4+ automatically applies the
 * {@link org.springframework.boot.autoconfigure.http.client.reactive.ClientHttpConnectorBuilderCustomizer}
 * to all WebClient.Builder instances, so all HTTP requests made through this
 * WebClient will benefit from:
 * <ul>
 * <li>Non-blocking I/O for high throughput with minimal threads</li>
 * <li>Connection pooling optimized for reactive workloads</li>
 * <li>TCP keep-alive for connection health monitoring</li>
 * <li>Automatic eviction of idle/expired connections</li>
 * </ul>
 * 
 * <p>
 * <b>Reactive vs Blocking:</b><br>
 * Unlike RestTemplate and RestClient (blocking), WebClient returns reactive
 * types (Mono/Flux) that represent asynchronous computations. This allows:
 * <ul>
 * <li>Non-blocking I/O that doesn't tie up threads</li>
 * <li>Backpressure handling for streaming data</li>
 * <li>Composable async operations</li>
 * <li>Better resource utilization under high load</li>
 * </ul>
 * 
 * <p>
 * <b>Usage Example (Mono):</b>
 * 
 * <pre>{@code
 * @Autowired
 * private WebClient sampleWebClient;
 * 
 * public Mono<String> makeRequest() {
 *     return sampleWebClient.get().uri("https://api.example.com/data").retrieve().bodyToMono(String.class);
 * }
 * }</pre>
 * 
 * <p>
 * <b>Usage Example (Flux):</b>
 * 
 * <pre>{@code
 * public Flux<Event> streamEvents() {
 *     return sampleWebClient.get().uri("https://api.example.com/events").retrieve().bodyToFlux(Event.class);
 * }
 * }</pre>
 * 
 * @see ReactorHttpClientConfiguration
 * @see WebClient
 * @see reactor.core.publisher.Mono
 * @see reactor.core.publisher.Flux
 */
@Configuration
class WebClientConfiguration {

    /**
     * Creates a {@link WebClient} bean using the auto-configured WebClient.Builder.
     * 
     * <p>
     * The WebClient.Builder is automatically configured by Spring Boot 3.4+ using
     * the
     * {@link org.springframework.boot.autoconfigure.http.client.reactive.ClientHttpConnectorBuilderCustomizer}
     * bean from {@link ReactorHttpClientConfiguration}. This means the WebClient
     * will use the configured Reactor Netty HTTP client with all timeout,
     * keep-alive, and connection pooling settings.
     * 
     * <p>
     * This bean can be injected anywhere in the application to make reactive HTTP
     * requests with the configured client settings. This variant does not include
     * OAuth2 authentication.
     * 
     * @param webClientBuilder the auto-configured WebClient.Builder with custom
     *                         Reactor Netty client
     * @return a fully configured WebClient instance without authentication
     * @see #sampleWebClientWithAuth(WebClient.Builder, ServerOAuth2AuthorizedClientExchangeFilterFunction)
     */
    @Bean
    WebClient sampleWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }

    /**
     * Creates a {@link WebClient} bean with OAuth2 authentication support.
     * 
     * <p>
     * This WebClient is configured identically to {@link #sampleWebClient} but
     * additionally includes an OAuth2 exchange filter function for automatic
     * reactive token management. The filter will:
     * <ul>
     * <li>Automatically obtain OAuth2 access tokens using client credentials flow (non-blocking)</li>
     * <li>Add the access token to the Authorization header of each request</li>
     * <li>Handle token refresh when tokens expire (asynchronously)</li>
     * <li>Cache tokens to avoid unnecessary token endpoint calls</li>
     * </ul>
     * 
     * <p>
     * The WebClient.Builder is automatically configured by Spring Boot 3.4+ using
     * the
     * {@link org.springframework.boot.autoconfigure.http.client.reactive.ClientHttpConnectorBuilderCustomizer}
     * bean from {@link ReactorHttpClientConfiguration}, so this client inherits all
     * the custom connection pool, keep-alive, and lifecycle settings.
     * 
     * <p>
     * <b>Reactive OAuth2 Flow:</b><br>
     * Token acquisition happens reactively as part of the request pipeline using Project Reactor.
     * This ensures that token requests are non-blocking and don't tie up event loop threads,
     * maintaining the fully reactive nature of the application.
     * 
     * <p>
     * <b>Usage Example:</b>
     * 
     * <pre>{@code
     * @Autowired
     * private WebClient sampleWebClientWithAuth;
     * 
     * public Mono<String> makeAuthenticatedRequest() {
     *     return sampleWebClientWithAuth
     *             .get()
     *             .uri("https://api.example.com/protected-resource")
     *             .retrieve()
     *             .bodyToMono(String.class);
     *     // OAuth2 token is automatically added to Authorization header (reactively)
     * }
     * }</pre>
     * 
     * @param webClientBuilder                                   the auto-configured
     *                                                           WebClient.Builder with
     *                                                           custom Reactor Netty
     *                                                           client
     * @param serverOAuth2AuthorizedClientExchangeFilterFunction OAuth2 filter for
     *                                                           reactive token management
     * @return a fully configured WebClient instance with OAuth2 authentication
     * @see ServerOAuth2AuthorizedClientExchangeFilterFunction
     * @see org.springframework.security.oauth2.client.registration.ClientRegistration
     * @see SecurityConfiguration
     */
    @Bean
    WebClient sampleWebClientWithAuth(
            WebClient.Builder webClientBuilder,
            ServerOAuth2AuthorizedClientExchangeFilterFunction serverOAuth2AuthorizedClientExchangeFilterFunction) {
        return webClientBuilder.filter(serverOAuth2AuthorizedClientExchangeFilterFunction).build();
    }
}
