package site.codemonster.comon.global.globalConfig;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "feedback.prompt")
public class FeedbackPromptConfig {
    private Resource systemPath;
    private Resource userPath;
    private String system;
    private String user;

    @PostConstruct
    void loadPrompts() throws IOException {
        system = new String(systemPath.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        user = new String(userPath.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String getUserPrompt(String title, String body) {
        return user
                .replace("{title}", title)
                .replace("{body}", body);
    }
}
