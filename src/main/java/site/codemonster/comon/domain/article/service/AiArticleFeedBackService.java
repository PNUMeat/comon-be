package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.ArticleFeedback.AIFeedbackGenerationException;
import site.codemonster.comon.global.globalConfig.FeedbackPromptConfig;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiArticleFeedBackService {

    private final ArticleService articleService;
    private final ArticleFeedbackLowService articleFeedbackLowService;
    private final TransactionTemplate transactionTemplate;
    private final ChatClient chatClient;
    private final FeedbackPromptConfig promptProperties;

    public Flux<String> generateFeedback(Long articleId, Member member) {
        Article article = articleService.validateAndGetArticle(articleId, member);

        return createStreamFeedback(article);
    }

    public Flux<String> createStreamFeedback(Article article) {

        List<Message> messages = new ArrayList<>();

        SystemMessage systemMessage = new SystemMessage(promptProperties.getSystem());
        UserMessage userPrompt = new UserMessage(promptProperties.getUserPrompt(article.getArticleTitle(), article.getArticleBody()));

        boolean hasPrevFeedBack = articleFeedbackLowService.existByArticle(article);
        messages.addAll(List.of(userPrompt,systemMessage));

        if (hasPrevFeedBack) {
            ArticleFeedback prevFeedback = articleFeedbackLowService.findByArticleId(article.getArticleId());
            messages.add(new AssistantMessage(prevFeedback.getFeedbackBody()));
        }

        Prompt prompt = new Prompt(messages);


        StringBuffer messageBuffer = new StringBuffer();

        return chatClient.prompt(prompt)
                .stream()
                .content()
                .mapNotNull(token -> {
                    messageBuffer.append(token);
                    return token;
                })
                .onErrorMap(e-> new AIFeedbackGenerationException())
                .doOnComplete(() -> {
                    String finalText = messageBuffer.toString();
                    transactionTemplate.execute(status -> {
                        ArticleFeedback feedback = new ArticleFeedback(article, finalText);
                        articleFeedbackLowService.deleteByArticleId(feedback.getArticle().getArticleId());
                        return articleFeedbackLowService.save(feedback);
                    });
                });
    }


}
