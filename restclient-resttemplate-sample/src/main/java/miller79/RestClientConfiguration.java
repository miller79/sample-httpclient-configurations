package miller79;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {
    // Sets the global defaults for RestClients
    @Bean
    RestClientCustomizer restClientBuilderCustomizer(CloseableHttpClient pooledHttpClient) {
        return builder -> builder.requestFactory(new HttpComponentsClientHttpRequestFactory(pooledHttpClient));
    }

    @Bean
    RestClient sampleRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }
}
