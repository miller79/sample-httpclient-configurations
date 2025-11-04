package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.ReactiveOAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for OAuth2 security integration with reactive HTTP clients.
 * 
 * <p>This configuration sets up OAuth2 client credentials authentication for WebClient
 * in a reactive, non-blocking manner. It provides automatic token management for
 * service-to-service authentication scenarios where the application needs to authenticate
 * with external APIs using OAuth2 client credentials flow.
 * 
 * <p><b>Key Components:</b>
 * <ul>
 *   <li><b>Reactive Access Token Response Client:</b> Handles OAuth2 token requests using WebClient</li>
 *   <li><b>Exchange Filter Function:</b> Automatically adds OAuth2 tokens to HTTP requests reactively</li>
 * </ul>
 * 
 * <p>The OAuth2 token client configured here uses the same customized WebClient
 * from {@link ReactorHttpClientConfiguration}, ensuring that token requests also
 * benefit from connection pooling, keep-alive settings, and lifecycle management.
 * 
 * <p><b>Required Configuration:</b>
 * OAuth2 client registration must be configured in application.yml:
 * <pre>{@code
 * spring:
 *   security:
 *     oauth2:
 *       client:
 *         provider:
 *           serviceAccount:
 *             issuer-uri: https://accounts.google.com
 *         registration:
 *           serviceAccount:
 *             client-id: your-client-id
 *             client-secret: your-client-secret
 *             authorization-grant-type: client_credentials
 * }</pre>
 * 
 * <p><b>Note:</b> The {@code issuer-uri} automatically discovers the token endpoint
 * and other OAuth2 configuration from the provider's well-known configuration endpoint.
 * Alternatively, you can explicitly specify {@code token-uri} instead of {@code issuer-uri}.
 * 
 * <p><b>Reactive vs Blocking:</b><br>
 * Unlike the blocking {@code OAuth2ClientHttpRequestInterceptor} used with RestClient/RestTemplate,
 * this configuration uses reactive OAuth2 components that work with WebFlux's non-blocking model.
 * Token acquisition and HTTP requests are fully asynchronous and don't block threads.
 * 
 * @see WebClientConfiguration for WebClient that uses OAuth2 authentication
 * @see ServerOAuth2AuthorizedClientExchangeFilterFunction
 * @see WebClientReactiveClientCredentialsTokenResponseClient
 */
@Configuration
class SecurityConfiguration {
    
    /**
     * Creates a reactive OAuth2 access token response client that uses WebClient for token requests.
     * 
     * <p>This client is responsible for making reactive HTTP requests to the OAuth2 authorization
     * server to obtain access tokens using the client credentials grant type. By using
     * WebClient.Builder, this token client inherits all the HTTP client customizations
     * from {@link ReactorHttpClientConfiguration}, including:
     * <ul>
     *   <li>Connection pooling for efficient token requests</li>
     *   <li>Connection lifecycle management (max idle time, max lifetime)</li>
     *   <li>TCP keep-alive for maintaining connections to auth server</li>
     *   <li>Non-blocking I/O for asynchronous token acquisition</li>
     * </ul>
     * 
     * <p>This is particularly important in reactive applications where blocking on token
     * requests would defeat the purpose of using non-blocking I/O. Token requests are made
     * asynchronously and don't tie up event loop threads.
     * 
     * @param webClientBuilder the auto-configured WebClient.Builder with custom
     *                         HTTP client settings from ReactorHttpClientConfiguration
     * @return a ReactiveOAuth2AccessTokenResponseClient configured to use WebClient
     * @see WebClientReactiveClientCredentialsTokenResponseClient
     * @see ReactorHttpClientConfiguration
     */
    @Bean
    ReactiveOAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> accessTokenResponseClient(
            WebClient.Builder webClientBuilder) {
        WebClientReactiveClientCredentialsTokenResponseClient webClientReactiveClientCredentialsTokenResponseClient = new WebClientReactiveClientCredentialsTokenResponseClient();
        webClientReactiveClientCredentialsTokenResponseClient.setWebClient(webClientBuilder.build());
        return webClientReactiveClientCredentialsTokenResponseClient;
    }

    /**
     * Creates a reactive OAuth2 exchange filter function for automatic token management.
     * 
     * <p>This filter function is used by {@link WebClientConfiguration#sampleWebClientWithAuth}
     * to automatically and reactively:
     * <ul>
     *   <li>Obtain OAuth2 access tokens before making requests (non-blocking)</li>
     *   <li>Add tokens to the Authorization header as "Bearer {token}"</li>
     *   <li>Refresh tokens when they expire (asynchronously)</li>
     *   <li>Cache tokens to avoid unnecessary token endpoint calls</li>
     * </ul>
     * 
     * <p>The filter is configured to always use the "serviceAccount" client
     * registration for all requests. This means all HTTP requests made through
     * WebClient instances that use this filter will authenticate as the service account
     * defined in the application configuration.
     * 
     * <p><b>Reactive Flow:</b><br>
     * The filter operates within the reactive chain, ensuring that token acquisition
     * happens asynchronously without blocking. If a token needs to be obtained or refreshed,
     * it happens as part of the reactive pipeline using Project Reactor's Mono/Flux.
     * 
     * <p><b>Usage:</b> This bean is automatically injected into WebClient instances that
     * need OAuth2 authentication. Application code doesn't need to handle tokens manually.
     * 
     * @param authorizedClientManager manages OAuth2 authorized clients and token lifecycle reactively
     * @return a ServerOAuth2AuthorizedClientExchangeFilterFunction configured for service account authentication
     * @see ServerOAuth2AuthorizedClientExchangeFilterFunction
     * @see ReactiveOAuth2AuthorizedClientManager
     */
    @Bean
    ServerOAuth2AuthorizedClientExchangeFilterFunction serverOAuth2AuthorizedClientExchangeFilterFunction(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction serverOAuth2AuthorizedClientExchangeFilterFunction = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                authorizedClientManager);
        serverOAuth2AuthorizedClientExchangeFilterFunction.setDefaultClientRegistrationId("serviceAccount");
        return serverOAuth2AuthorizedClientExchangeFilterFunction;
    }
}
