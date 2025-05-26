package site.codemonster.comon.global.globalConfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloud.aws")
@Getter
@Setter
public class AwsProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
}
