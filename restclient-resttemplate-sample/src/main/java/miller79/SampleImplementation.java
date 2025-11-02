package miller79;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleImplementation implements CommandLineRunner {
    private static final String MAIN_URL = "https://www.google.com";
    private final RestClient sampleRestClient;
    private final RestTemplate sampleRestTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("SampleRestClient response: {}", sampleRestClient.get().uri(MAIN_URL).retrieve().body(String.class));
        log.info("SampleRestTemplate response: {}", sampleRestTemplate.getForObject(MAIN_URL, String.class));
    }

}
