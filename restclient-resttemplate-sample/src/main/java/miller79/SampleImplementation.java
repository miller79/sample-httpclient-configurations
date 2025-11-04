package miller79;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample implementation demonstrating the use of configured HTTP clients.
 * 
 * <p>This component runs at application startup and demonstrates how to use
 * both RestClient and RestTemplate with the custom Apache HttpClient configuration.
 * It shows that the configured clients work correctly for making HTTP requests.
 * 
 * <p><b>HTTP Clients Used:</b>
 * <ul>
 *   <li>{@link #sampleRestClient} - RestClient without authentication</li>
 *   <li>{@link #sampleRestTemplate} - RestTemplate without authentication</li>
 *   <li>{@link #sampleRestClientWithAuth} - RestClient with OAuth2 authentication</li>
 *   <li>{@link #sampleRestTemplateWithAuth} - RestTemplate with OAuth2 authentication</li>
 * </ul>
 * 
 * <p>All clients inherit the custom HTTP client configuration from
 * {@link ApacheHttpClientConfiguration}, including:
 * <ul>
 *   <li>Connection pooling (default 64 connections)</li>
 *   <li>Response timeout (default 5 seconds)</li>
 *   <li>TCP keep-alive settings</li>
 *   <li>Automatic connection eviction</li>
 * </ul>
 * 
 * <p>The authenticated variants also include OAuth2 token management from
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
public class SampleImplementation implements CommandLineRunner {
    private static final String MAIN_URL = "https://www.google.com";
    
    private final RestClient sampleRestClient;
    private final RestTemplate sampleRestTemplate;

    private final RestClient sampleRestClientWithAuth;
    private final RestTemplate sampleRestTemplateWithAuth;

    /**
     * Executes sample HTTP requests on application startup to demonstrate the configured clients.
     * 
     * <p>This method makes simple GET requests to a public URL to verify that the
     * HTTP clients are configured correctly and can successfully make requests with
     * the custom timeout, connection pool, and keep-alive settings.
     * 
     * @param args command line arguments (not used)
     * @throws Exception if HTTP requests fail
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("SampleRestClient response: {}", sampleRestClient.get().uri(MAIN_URL).retrieve().body(String.class));
        log.info("SampleRestTemplate response: {}", sampleRestTemplate.getForObject(MAIN_URL, String.class));
    }

}
