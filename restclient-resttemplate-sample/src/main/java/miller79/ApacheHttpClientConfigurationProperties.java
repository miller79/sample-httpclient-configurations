package miller79;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties("miller79.apache")
@Data
public class ApacheHttpClientConfigurationProperties {
    private String name = "connection";
    private int maxConnections = 64;
    private Duration maxIdleTime = Duration.ofSeconds(60);
    private Duration maxLifeTime = Duration.ofSeconds(60);
    private boolean soKeepAlive = true;
    private Duration tcpKeepIdle = Duration.ofSeconds(30);
    private Duration tcpKeepInterval = Duration.ofSeconds(5);
    private int tcpKeepCount = 3;
    private Duration responseTimeout = Duration.ofSeconds(5);
}
