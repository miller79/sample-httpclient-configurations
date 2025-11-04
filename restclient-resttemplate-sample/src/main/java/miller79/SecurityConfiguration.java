package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
class SecurityConfiguration {
    @Bean
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> accessTokenResponseClient(
            RestClient.Builder restClientBuilder) {
        var restClientClientCredentialsTokenResponseClient = new RestClientClientCredentialsTokenResponseClient();
        restClientClientCredentialsTokenResponseClient.setRestClient(restClientBuilder.build());
        return restClientClientCredentialsTokenResponseClient;
    }

    @Bean
    OAuth2ClientHttpRequestInterceptor oauth2ClientHttpRequestInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) {
        OAuth2ClientHttpRequestInterceptor oauth2ClientHttpRequestInterceptor = new OAuth2ClientHttpRequestInterceptor(
                authorizedClientManager);
        oauth2ClientHttpRequestInterceptor.setClientRegistrationIdResolver(request -> "serviceAccount");
        return oauth2ClientHttpRequestInterceptor;
    }

}
