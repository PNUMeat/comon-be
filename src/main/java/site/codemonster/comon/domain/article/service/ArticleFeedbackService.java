package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.ArticleFeedback.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import site.codemonster.comon.global.globalConfig.FeedbackPromptConfig;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleFeedbackService {

    private final ArticleService articleService;
    private final ArticleFeedbackLowService articleFeedbackLowService;
    private final ChatModel chatModel;
    private final FeedbackPromptConfig promptProperties;

    @Transactional
    public Flux<String> generateFeedback(Long articleId, Member member) {
        Article article = articleService.validateAndGetArticle(articleId, member);

        return createStreamFeedback(article);
    }

    public ArticleFeedbackResponse getFeedback(Long articleId) {
        ArticleFeedback feedback = articleFeedbackLowService.findByArticleId(articleId);

        return new ArticleFeedbackResponse(feedback);
    }

    public Flux<String> createStreamFeedback(Article article) {
        String systemPrompt = promptProperties.getSystem();
        String userPrompt = promptProperties.getUserPrompt(article.getArticleTitle(), articleService.getPlainArticleBody(article.getArticleBody()));


        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));


        return chatModel.stream(prompt)
                .mapNotNull(response -> response.getResult().getOutput().getText()); // 묶은 chunk을 하나로 합침
    }

    public ArticleFeedback createFeedback(Article article) {
        try {
            String systemPrompt = promptProperties.getSystem();
            String userPrompt = promptProperties.getUserPrompt(article.getArticleTitle(), articleService.getPlainArticleBody(article.getArticleBody()));
            Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));

            String aiResponse = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText();

            log.info(aiResponse);

            return ArticleFeedback.fromAiResponse(aiResponse, article);
        } catch (Exception e) {
            throw new AIFeedbackGenerationException();
        }
    }
}
