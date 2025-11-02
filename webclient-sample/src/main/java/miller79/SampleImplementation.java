package miller79;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleImplementation implements CommandLineRunner {
    private static final String MAIN_URL = "https://www.google.com";
    private final WebClient sampleWebClient;

    @Override
    public void run(String... args) throws Exception {
        log
                .info("SampleWebClient response: {}",
                        sampleWebClient.get().uri(MAIN_URL).retrieve().bodyToMono(String.class).block());
    }

}
