package miller79;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for Spring's {@link RestTemplate}.
 * 
 * <p>
 * RestTemplate is Spring Framework's classic synchronous HTTP client. While
 * still widely used, it is in maintenance mode and
 * {@link org.springframework.web.client.RestClient} is recommended for new
 * applications.
 * 
 * <p>
 * This configuration creates a RestTemplate instance that uses the custom
 * Apache HttpClient configured in {@link ApacheHttpClientConfiguration}. Spring
 * Boot 3.4+ automatically applies the
 * {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
 * to all RestTemplateBuilder instances, so all HTTP requests made through this
 * RestTemplate will benefit from:
 * <ul>
 * <li>Connection pooling for improved performance and reduced latency</li>
 * <li>Configured timeout settings to prevent indefinite hangs</li>
 * <li>TCP keep-alive for detecting broken connections</li>
 * <li>Automatic eviction of idle and expired connections</li>
 * </ul>
 * 
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * @Autowired
 * private RestTemplate sampleRestTemplate;
 * 
 * public void makeRequest() {
 *     String result = sampleRestTemplate.getForObject("https://api.example.com/data", String.class);
 * }
 * }</pre>
 * 
 * @see ApacheHttpClientConfiguration
 * @see RestTemplate
 */
@Configuration
class RestTemplateConfiguration {

    /**
     * Creates a {@link RestTemplate} bean using the auto-configured
     * RestTemplateBuilder.
     * 
     * <p>
     * The RestTemplateBuilder is automatically configured by Spring Boot 3.4+ using
     * the
     * {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
     * bean from {@link ApacheHttpClientConfiguration}. This means the RestTemplate
     * will use the Apache HttpClient with all configured timeout, connection pool,
     * and keep-alive settings.
     * 
     * <p>
     * This bean can be injected anywhere in the application to make HTTP requests
     * with the configured client settings. This variant does not include OAuth2
     * authentication.
     * 
     * @param restTemplateBuilder the auto-configured RestTemplateBuilder with
     *                            custom HTTP client
     * @return a fully configured RestTemplate instance without authentication
     * @see #sampleRestTemplateWithAuth(RestTemplateBuilder, OAuth2ClientHttpRequestInterceptor)
     */
    @Bean
    RestTemplate sampleRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    /**
     * Creates a {@link RestTemplate} bean with OAuth2 authentication support.
     * 
     * <p>
     * This RestTemplate is configured identically to {@link #sampleRestTemplate} but
     * additionally includes an OAuth2 client credentials interceptor for automatic
     * token management. The interceptor will:
     * <ul>
     * <li>Automatically obtain OAuth2 access tokens using client credentials flow</li>
     * <li>Add the access token to the Authorization header of each request</li>
     * <li>Handle token refresh when tokens expire</li>
     * </ul>
     * 
     * <p>
     * The RestTemplateBuilder is automatically configured by Spring Boot 3.4+ using
     * the
     * {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
     * bean from {@link ApacheHttpClientConfiguration}, so this client inherits all
     * the custom timeout, connection pool, and keep-alive settings.
     * 
     * <p>
     * <b>Usage Example:</b>
     * 
     * <pre>{@code
     * @Autowired
     * private RestTemplate sampleRestTemplateWithAuth;
     * 
     * public void makeAuthenticatedRequest() {
     *     String result = sampleRestTemplateWithAuth
     *             .getForObject("https://api.example.com/protected-resource", String.class);
     *     // OAuth2 token is automatically added to Authorization header
     * }
     * }</pre>
     * 
     * @param restTemplateBuilder                the auto-configured RestTemplateBuilder
     *                                           with custom HTTP client
     * @param oauth2ClientHttpRequestInterceptor OAuth2 interceptor for token management
     * @return a fully configured RestTemplate instance with OAuth2 authentication
     * @see OAuth2ClientHttpRequestInterceptor
     * @see org.springframework.security.oauth2.client.registration.ClientRegistration
     */
    @Bean
    RestTemplate sampleRestTemplateWithAuth(
            RestTemplateBuilder restTemplateBuilder,
            OAuth2ClientHttpRequestInterceptor oauth2ClientHttpRequestInterceptor) {
        return restTemplateBuilder.interceptors(oauth2ClientHttpRequestInterceptor).build();
    }
}
