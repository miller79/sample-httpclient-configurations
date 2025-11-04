package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for Spring's {@link RestClient}.
 * 
 * <p>RestClient is Spring Framework's modern synchronous HTTP client introduced in Spring 6.1.
 * It provides a fluent API for making HTTP requests and is intended to replace RestTemplate
 * for new applications.
 * 
 * <p>This configuration creates a RestClient instance that uses the custom Apache HttpClient
 * configured in {@link ApacheHttpClientConfiguration}. Spring Boot 3.4+ automatically applies
 * the {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
 * to all RestClient.Builder instances, so all HTTP requests made through this RestClient will benefit from:
 * <ul>
 *   <li>Connection pooling for improved performance</li>
 *   <li>Configured timeout settings for reliability</li>
 *   <li>TCP keep-alive for connection health monitoring</li>
 *   <li>Automatic eviction of idle/expired connections</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * @Autowired
 * private RestClient sampleRestClient;
 * 
 * public void makeRequest() {
 *     String result = sampleRestClient.get()
 *         .uri("https://api.example.com/data")
 *         .retrieve()
 *         .body(String.class);
 * }
 * }</pre>
 * 
 * @see ApacheHttpClientConfiguration
 * @see RestClient
 */
@Configuration
public class RestClientConfiguration {
    
    /**
     * Creates a {@link RestClient} bean using the auto-configured RestClient.Builder.
     * 
     * <p>The RestClient.Builder is automatically configured by Spring Boot 3.4+ using the
     * {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
     * bean from {@link ApacheHttpClientConfiguration}. This means the RestClient will use the
     * Apache HttpClient with all configured timeout, connection pool, and keep-alive settings.
     * 
     * <p>This bean can be injected anywhere in the application to make HTTP requests with
     * the configured client settings.
     * 
     * @param restClientBuilder the auto-configured RestClient.Builder with custom HTTP client
     * @return a fully configured RestClient instance
     */
    @Bean
    RestClient sampleRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }
}
