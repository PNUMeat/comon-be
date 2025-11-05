package site.codemonster.comon.global.globalConfig;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public OpenAiService openAiService() {
        // OpenAiService 생성자에 직접 base URL을 설정할 수 없으므로
        // 환경변수나 시스템 프로퍼티로 설정
        System.setProperty("OPENAI_BASE_URL", "https://generativelanguage.googleapis.com/v1beta/openai/");

        return new OpenAiService(apiKey, Duration.ofSeconds(60));
    }
}
