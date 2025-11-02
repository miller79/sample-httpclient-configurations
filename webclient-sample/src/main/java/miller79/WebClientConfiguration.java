package miller79;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
    // Sets the global defaults for RestClients
    @Bean
    WebClientCustomizer webClientCustomizer(ReactorClientHttpConnector reactorClientHttpConnector) {
        return builder -> builder.clientConnector(reactorClientHttpConnector);
    }

    @Bean
    WebClient sampleWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }
}
