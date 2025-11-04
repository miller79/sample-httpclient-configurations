package miller79;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for Spring's {@link RestTemplate}.
 * 
 * <p>RestTemplate is Spring Framework's classic synchronous HTTP client. While still widely
 * used, it is in maintenance mode and {@link org.springframework.web.client.RestClient}
 * is recommended for new applications.
 * 
 * <p>This configuration creates a RestTemplate instance that uses the custom Apache HttpClient
 * configured in {@link ApacheHttpClientConfiguration}. Spring Boot 3.4+ automatically applies
 * the {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
 * to all RestTemplateBuilder instances, so all HTTP requests made through this RestTemplate will benefit from:
 * <ul>
 *   <li>Connection pooling for improved performance and reduced latency</li>
 *   <li>Configured timeout settings to prevent indefinite hangs</li>
 *   <li>TCP keep-alive for detecting broken connections</li>
 *   <li>Automatic eviction of idle and expired connections</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * @Autowired
 * private RestTemplate sampleRestTemplate;
 * 
 * public void makeRequest() {
 *     String result = sampleRestTemplate.getForObject(
 *         "https://api.example.com/data", 
 *         String.class
 *     );
 * }
 * }</pre>
 * 
 * @see ApacheHttpClientConfiguration
 * @see RestTemplate
 */
@Configuration
public class RestTemplateConfiguration {
    
    /**
     * Creates a {@link RestTemplate} bean using the auto-configured RestTemplateBuilder.
     * 
     * <p>The RestTemplateBuilder is automatically configured by Spring Boot 3.4+ using the
     * {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
     * bean from {@link ApacheHttpClientConfiguration}. This means the RestTemplate will use the
     * Apache HttpClient with all configured timeout, connection pool, and keep-alive settings.
     * 
     * <p>This bean can be injected anywhere in the application to make HTTP requests with
     * the configured client settings.
     * 
     * @param restTemplateBuilder the auto-configured RestTemplateBuilder with custom HTTP client
     * @return a fully configured RestTemplate instance
     */
    @Bean
    RestTemplate sampleRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
}
