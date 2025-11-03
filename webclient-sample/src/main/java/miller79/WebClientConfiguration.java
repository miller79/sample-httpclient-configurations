package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for Spring WebFlux's {@link WebClient}.
 * 
 * <p>WebClient is Spring Framework's modern reactive HTTP client introduced in Spring 5.
 * It provides a fluent API for making non-blocking HTTP requests and is designed for
 * reactive applications using Project Reactor.
 * 
 * <p>This configuration creates a WebClient instance that uses the custom Reactor Netty
 * HTTP client configured in {@link ReactorHttpClientConfiguration}. This means all HTTP
 * requests made through this WebClient will benefit from:
 * <ul>
 *   <li>Non-blocking I/O for high throughput with minimal threads</li>
 *   <li>Connection pooling optimized for reactive workloads</li>
 *   <li>Configured timeout settings for reliability</li>
 *   <li>TCP keep-alive for connection health monitoring</li>
 *   <li>Automatic eviction of idle/expired connections</li>
 * </ul>
 * 
 * <p><b>Reactive vs Blocking:</b><br>
 * Unlike RestTemplate and RestClient (blocking), WebClient returns reactive types
 * (Mono/Flux) that represent asynchronous computations. This allows:
 * <ul>
 *   <li>Non-blocking I/O that doesn't tie up threads</li>
 *   <li>Backpressure handling for streaming data</li>
 *   <li>Composable async operations</li>
 *   <li>Better resource utilization under high load</li>
 * </ul>
 * 
 * <p><b>Usage Example (Mono):</b>
 * <pre>{@code
 * @Autowired
 * private WebClient sampleWebClient;
 * 
 * public Mono<String> makeRequest() {
 *     return sampleWebClient.get()
 *         .uri("https://api.example.com/data")
 *         .retrieve()
 *         .bodyToMono(String.class);
 * }
 * }</pre>
 * 
 * <p><b>Usage Example (Flux):</b>
 * <pre>{@code
 * public Flux<Event> streamEvents() {
 *     return sampleWebClient.get()
 *         .uri("https://api.example.com/events")
 *         .retrieve()
 *         .bodyToFlux(Event.class);
 * }
 * }</pre>
 * 
 * @see ReactorHttpClientConfiguration
 * @see WebClient
 * @see reactor.core.publisher.Mono
 * @see reactor.core.publisher.Flux
 */
@Configuration
public class WebClientConfiguration {
    
    /**
     * Creates a {@link WebClient} bean using the custom-configured WebClient.Builder.
     * 
     * <p>The WebClient.Builder is automatically customized by {@link ReactorHttpClientConfiguration}
     * to use the configured Reactor Netty HTTP client with all timeout, keep-alive, and
     * connection pooling settings.
     * 
     * <p>This bean can be injected anywhere in the application to make reactive HTTP requests
     * with the configured client settings.
     * 
     * @param webClientBuilder the auto-configured WebClient.Builder with custom Reactor Netty client
     * @return a fully configured WebClient instance
     */
    @Bean
    WebClient sampleWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
