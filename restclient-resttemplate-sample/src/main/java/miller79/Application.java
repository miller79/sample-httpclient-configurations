package miller79;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main Spring Boot application class for the RestClient/RestTemplate HTTP client configuration sample.
 * 
 * <p>This application demonstrates how to configure Apache HttpClient 5 for use with
 * Spring's {@link org.springframework.web.client.RestClient} and
 * {@link org.springframework.web.client.RestTemplate}. It showcases:
 * <ul>
 *   <li>Custom HTTP client configuration with timeouts and connection pooling</li>
 *   <li>TCP keep-alive settings for connection health monitoring</li>
 *   <li>OAuth2 client credentials authentication integration</li>
 *   <li>Spring Boot 3.4+ HTTP client customizer pattern</li>
 * </ul>
 * 
 * <p><b>Key Configuration Classes:</b>
 * <ul>
 *   <li>{@link ApacheHttpClientConfiguration} - HTTP client customization</li>
 *   <li>{@link ApacheHttpClientConfigurationProperties} - Externalized configuration</li>
 *   <li>{@link SecurityConfiguration} - OAuth2 security setup</li>
 *   <li>{@link RestClientConfiguration} - RestClient beans</li>
 *   <li>{@link RestTemplateConfiguration} - RestTemplate beans</li>
 * </ul>
 * 
 * @see ApacheHttpClientConfiguration
 * @see SecurityConfiguration
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
