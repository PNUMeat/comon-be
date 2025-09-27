package site.codemonster.comon.global.globalConfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.domain")
@Getter
@Setter
public class DomainProperties {

    private String frontend;
    private String backend;
}
