package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for Spring's {@link RestClient}.
 * 
 * <p>
 * RestClient is Spring Framework's modern synchronous HTTP client introduced in
 * Spring 6.1. It provides a fluent API for making HTTP requests and is intended
 * to replace RestTemplate for new applications.
 * 
 * <p>
 * This configuration creates a RestClient instance that uses the custom Apache
 * HttpClient configured in {@link ApacheHttpClientConfiguration}. Spring Boot
 * 3.4+ automatically applies the
 * {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
 * to all RestClient.Builder instances, so all HTTP requests made through this
 * RestClient will benefit from:
 * <ul>
 * <li>Connection pooling for improved performance</li>
 * <li>Configured timeout settings for reliability</li>
 * <li>TCP keep-alive for connection health monitoring</li>
 * <li>Automatic eviction of idle/expired connections</li>
 * </ul>
 * 
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * @Autowired
 * private RestClient sampleRestClient;
 * 
 * public void makeRequest() {
 *     String result = sampleRestClient.get().uri("https://api.example.com/data").retrieve().body(String.class);
 * }
 * }</pre>
 * 
 * @see ApacheHttpClientConfiguration
 * @see RestClient
 */
@Configuration
class RestClientConfiguration {

    /**
     * Creates a {@link RestClient} bean using the auto-configured
     * RestClient.Builder.
     * 
     * <p>
     * The RestClient.Builder is automatically configured by Spring Boot 3.4+ using
     * the
     * {@link org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer}
     * bean from {@link ApacheHttpClientConfiguration}. This means the RestClient
     * will use the Apache HttpClient with all configured timeout, connection pool,
     * and keep-alive settings.
     * 
     * <p>
     * This bean can be injected anywhere in the application to make HTTP requests
     * with the configured client settings. This variant does not include OAuth2
     * authentication.
     * 
     * @param restClientBuilder the auto-configured RestClient.Builder with custom
     *                          HTTP client
     * @return a fully configured RestClient instance without authentication
     * @see #sampleRestClientWithAuth(RestClient.Builder, OAuth2ClientHttpRequestInterceptor)
     */
    @Bean
    RestClient sampleRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }

    /**
     * Creates a {@link RestClient} bean with OAuth2 authentication support.
     * 
     * <p>
     * This RestClient is configured identically to {@link #sampleRestClient} but
     * additionally includes an OAuth2 client credentials interceptor for automatic
     * token management. The interceptor will:
     * <ul>
     * <li>Automatically obtain OAuth2 access tokens using client credentials
     * flow</li>
     * <li>Add the access token to the Authorization header of each request</li>
     * <li>Handle token refresh when tokens expire</li>
     * </ul>
     * 
     * <p>
     * The RestClient.Builder is automatically configured by Spring Boot 3.4+ using
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
     * private RestClient sampleRestClientWithAuth;
     * 
     * public void makeAuthenticatedRequest() {
     *     String result = sampleRestClientWithAuth
     *             .get()
     *             .uri("https://api.example.com/protected-resource")
     *             .retrieve()
     *             .body(String.class);
     *     // OAuth2 token is automatically added to Authorization header
     * }
     * }</pre>
     * 
     * @param restClientBuilder                  the auto-configured
     *                                           RestClient.Builder with custom HTTP
     *                                           client
     * @param oauth2ClientHttpRequestInterceptor OAuth2 interceptor for token
     *                                           management
     * @return a fully configured RestClient instance with OAuth2 authentication
     * @see OAuth2ClientHttpRequestInterceptor
     * @see org.springframework.security.oauth2.client.registration.ClientRegistration
     */
    @Bean
    RestClient sampleRestClientWithAuth(
            RestClient.Builder restClientBuilder,
            OAuth2ClientHttpRequestInterceptor oauth2ClientHttpRequestInterceptor) {
        return restClientBuilder.requestInterceptor(oauth2ClientHttpRequestInterceptor).build();
    }
}
