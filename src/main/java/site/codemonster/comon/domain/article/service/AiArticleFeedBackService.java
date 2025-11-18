package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.ArticleFeedback.AIFeedbackGenerationException;
import site.codemonster.comon.global.globalConfig.FeedbackPromptConfig;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiArticleFeedBackService {

    private final ArticleFeedbackLowService articleFeedbackLowService;
    private final ArticleService articleService;
    private final ArticleFeedbackService articleFeedbackService;
    private final TransactionTemplate transactionTemplate;
    private final ChatModel chatModel;
    private final FeedbackPromptConfig promptProperties;

    public Flux<String> generateFeedback(Long articleId, Member member) {
        Article article = articleService.validateAndGetArticle(articleId, member);
        StringBuffer builder = new StringBuffer();

        return createStreamFeedback(article)
                .doOnNext(builder::append)
                .doOnComplete(() -> {
                    String finalText = builder.toString();
                    transactionTemplate.execute(status -> {
                        ArticleFeedback feedback = new ArticleFeedback(article, finalText);
                        articleFeedbackService.safeSaveArticleFeedback(feedback);
                        return null;
                    });
                })
                .onErrorResume(e -> {
                    log.error("AI Feedback generation failed", e);

                    return Mono.error(new AIFeedbackGenerationException());
                });
    }

    public Flux<String> createStreamFeedback(Article article) {
        String systemPrompt = promptProperties.getSystem();
        String userPrompt = promptProperties.getUserPrompt(article.getArticleTitle(), articleService.getPlainArticleBody(article.getArticleBody()));


        Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));


        return chatModel.stream(prompt)
                .mapNotNull(response -> response.getResult().getOutput().getText()); // 묶은 chunk을 하나로 합침
    }


}
