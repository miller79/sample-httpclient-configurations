package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for OAuth2 security integration with HTTP clients.
 * 
 * <p>This configuration sets up OAuth2 client credentials authentication for RestClient
 * and RestTemplate. It provides automatic token management for service-to-service
 * authentication scenarios where the application needs to authenticate with external
 * APIs using OAuth2 client credentials flow.
 * 
 * <p><b>Key Components:</b>
 * <ul>
 *   <li><b>Access Token Response Client:</b> Handles OAuth2 token requests using RestClient</li>
 *   <li><b>Request Interceptor:</b> Automatically adds OAuth2 tokens to HTTP requests</li>
 * </ul>
 * 
 * <p>The OAuth2 token client configured here uses the same customized RestClient
 * from {@link ApacheHttpClientConfiguration}, ensuring that token requests also
 * benefit from connection pooling, timeouts, and keep-alive settings.
 * 
 * <p><b>Required Configuration:</b>
 * OAuth2 client registration must be configured in application.yml:
 * <pre>{@code
 * spring:
 *   security:
 *     oauth2:
 *       client:
 *         registration:
 *           serviceAccount:
 *             client-id: your-client-id
 *             client-secret: your-client-secret
 *             authorization-grant-type: client_credentials
 *         provider:
 *           serviceAccount:
 *             token-uri: https://auth.example.com/oauth/token
 * }</pre>
 * 
 * @see RestClientConfiguration for HTTP clients that use OAuth2 authentication
 * @see RestTemplateConfiguration for RestTemplate with OAuth2 authentication
 * @see OAuth2ClientHttpRequestInterceptor
 * @see RestClientClientCredentialsTokenResponseClient
 */
@Configuration
class SecurityConfiguration {
    
    /**
     * Creates an OAuth2 access token response client that uses RestClient for token requests.
     * 
     * <p>This client is responsible for making HTTP requests to the OAuth2 authorization
     * server to obtain access tokens using the client credentials grant type. By using
     * RestClient.Builder, this token client inherits all the HTTP client customizations
     * from {@link ApacheHttpClientConfiguration}, including:
     * <ul>
     *   <li>Connection pooling for efficient token requests</li>
     *   <li>Configured timeouts to prevent hanging on token endpoint</li>
     *   <li>TCP keep-alive for maintaining connections to auth server</li>
     * </ul>
     * 
     * <p>This is particularly important in high-traffic scenarios where token requests
     * can become a bottleneck if not properly configured.
     * 
     * @param restClientBuilder the auto-configured RestClient.Builder with custom
     *                          HTTP client settings from ApacheHttpClientConfiguration
     * @return an OAuth2AccessTokenResponseClient configured to use RestClient
     * @see RestClientClientCredentialsTokenResponseClient
     * @see ApacheHttpClientConfiguration
     */
    @Bean
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> accessTokenResponseClient(
            RestClient.Builder restClientBuilder) {
        var restClientClientCredentialsTokenResponseClient = new RestClientClientCredentialsTokenResponseClient();
        restClientClientCredentialsTokenResponseClient.setRestClient(restClientBuilder.build());
        return restClientClientCredentialsTokenResponseClient;
    }

    /**
     * Creates an OAuth2 HTTP request interceptor for automatic token management.
     * 
     * <p>This interceptor is used by {@link RestClientConfiguration#sampleRestClientWithAuth}
     * and {@link RestTemplateConfiguration#sampleRestTemplateWithAuth} to automatically:
     * <ul>
     *   <li>Obtain OAuth2 access tokens before making requests</li>
     *   <li>Add tokens to the Authorization header as "Bearer {token}"</li>
     *   <li>Refresh tokens when they expire</li>
     *   <li>Cache tokens to avoid unnecessary token endpoint calls</li>
     * </ul>
     * 
     * <p>The interceptor is configured to always use the "serviceAccount" client
     * registration for all requests. This means all HTTP requests made through
     * clients that use this interceptor will authenticate as the service account
     * defined in the application configuration.
     * 
     * <p><b>Usage:</b> This bean is automatically injected into RestClient and
     * RestTemplate instances that need OAuth2 authentication. Application code
     * doesn't need to handle tokens manually.
     * 
     * @param authorizedClientManager manages OAuth2 authorized clients and token lifecycle
     * @return an OAuth2ClientHttpRequestInterceptor configured for service account authentication
     * @see OAuth2ClientHttpRequestInterceptor
     * @see OAuth2AuthorizedClientManager
     */
    @Bean
    OAuth2ClientHttpRequestInterceptor oauth2ClientHttpRequestInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor oauth2ClientHttpRequestInterceptor = new OAuth2ClientHttpRequestInterceptor(
                authorizedClientManager);
        oauth2ClientHttpRequestInterceptor.setClientRegistrationIdResolver(request -> "serviceAccount");
        return oauth2ClientHttpRequestInterceptor;
    }

}
