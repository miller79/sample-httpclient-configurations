package miller79;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {
    @Bean
    RestClient sampleRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }
}
