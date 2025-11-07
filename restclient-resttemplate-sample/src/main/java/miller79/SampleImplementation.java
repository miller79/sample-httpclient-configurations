package miller79;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample implementation demonstrating the use of configured HTTP clients.
 * 
 * <p>
 * This component runs at application startup and demonstrates how to use both
 * RestClient and RestTemplate with the custom Apache HttpClient configuration.
 * It shows that the configured clients work correctly for making HTTP requests.
 * 
 * <p>
 * <b>HTTP Clients Available:</b>
 * <ul>
 * <li>{@link #sampleRestClient} - RestClient without authentication
 * (demonstrated)</li>
 * <li>{@link #sampleRestTemplate} - RestTemplate without authentication
 * (demonstrated)</li>
 * <li>{@link #sampleRestClientWithAuth} - RestClient with OAuth2 authentication
 * (available for use)</li>
 * <li>{@link #sampleRestTemplateWithAuth} - RestTemplate with OAuth2
 * authentication (available for use)</li>
 * </ul>
 * 
 * <p>
 * All clients inherit the custom HTTP client configuration from
 * {@link ApacheHttpClientConfiguration}, including:
 * <ul>
 * <li>Connection pooling (configurable via max-connections)</li>
 * <li>Connection lifecycle management (max-idle-time, max-life-time)</li>
 * <li>TCP keep-alive settings (optional, for special cases)</li>
 * <li>Automatic connection eviction</li>
 * </ul>
 * 
 * <p>
 * The authenticated variants also include OAuth2 token management from
 * {@link SecurityConfiguration}.
 * 
 * @see RestClientConfiguration
 * @see RestTemplateConfiguration
 * @see ApacheHttpClientConfiguration
 * @see SecurityConfiguration
 */
@Component
@RequiredArgsConstructor
@Slf4j
class SampleImplementation implements ApplicationRunner {
    private static final String MAIN_URL = "https://www.google.com";

    private final RestClient sampleRestClient;
    private final RestTemplate sampleRestTemplate;

    private final RestClient sampleRestClientWithAuth;
    private final RestTemplate sampleRestTemplateWithAuth;

    /**
     * Executes sample HTTP requests on application startup to demonstrate the
     * configured clients.
     * 
     * <p>
     * This method makes simple GET requests to a public URL to verify that the HTTP
     * clients are configured correctly and can successfully make requests with the
     * custom timeout, connection pool, and keep-alive settings.
     * 
     * @param args command line arguments (not used)
     * @throws Exception if HTTP requests fail
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("SampleRestClient response: {}", sampleRestClient.get().uri(MAIN_URL).retrieve().body(String.class));
        log.info("SampleRestTemplate response: {}", sampleRestTemplate.getForObject(MAIN_URL, String.class));
    }

}
