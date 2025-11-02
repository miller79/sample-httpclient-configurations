package miller79;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {
    // Sets the global defaults for RestTemplates
    @Bean
    RestTemplateBuilder restTemplateBuilder(
            RestTemplateBuilderConfigurer configurer,
            CloseableHttpClient pooledHttpClient) {
        return configurer
                .configure(new RestTemplateBuilder())
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(pooledHttpClient));
    }

    @Bean
    RestTemplate sampleRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
}
