package miller79;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main Spring Boot application class for the WebClient HTTP client configuration sample.
 * 
 * <p>This application demonstrates how to configure Reactor Netty for use with
 * Spring WebFlux's {@link org.springframework.web.reactive.function.client.WebClient}.
 * It showcases:
 * <ul>
 *   <li>Custom reactive HTTP client configuration with connection pooling</li>
 *   <li>TCP keep-alive settings for connection health monitoring</li>
 *   <li>Non-blocking I/O for high-throughput reactive applications</li>
 *   <li>Spring Boot 3.4+ HTTP connector customizer pattern</li>
 * </ul>
 * 
 * <p><b>Key Configuration Classes:</b>
 * <ul>
 *   <li>{@link ReactorHttpClientConfiguration} - Reactor Netty HTTP client customization</li>
 *   <li>{@link ReactorHttpClientConfigurationProperties} - Externalized configuration</li>
 *   <li>{@link SecurityConfiguration} - OAuth2 security setup (reactive)</li>
 *   <li>{@link WebClientConfiguration} - WebClient beans</li>
 * </ul>
 * 
 * <p><b>Reactive vs Blocking:</b><br>
 * Unlike the RestClient/RestTemplate sample which uses blocking I/O, this application
 * demonstrates non-blocking reactive HTTP clients using Project Reactor. This approach
 * is ideal for high-throughput scenarios with many concurrent requests.
 * 
 * @see ReactorHttpClientConfiguration
 * @see SecurityConfiguration
 * @see WebClientConfiguration
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
