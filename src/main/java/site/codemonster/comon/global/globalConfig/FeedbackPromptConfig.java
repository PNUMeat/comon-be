package site.codemonster.comon.global.globalConfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "feedback.prompt")
public class FeedbackPromptConfig {
    private String system;
    private String user;

    public String getUserPrompt(String title, String body) {
        return user
                .replace("{title}", title)
                .replace("{body}", body);
    }
}
