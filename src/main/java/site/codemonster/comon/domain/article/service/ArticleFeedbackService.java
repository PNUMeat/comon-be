package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;
import site.codemonster.comon.domain.article.repository.ArticleFeedbackRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.ArticleFeedback.*;

import java.util.List;
import site.codemonster.comon.global.globalConfig.FeedbackPromptConfig;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleFeedbackService {

    private final ArticleService articleService;
    private final ArticleFeedbackRepository articleFeedbackRepository;
    private final ChatModel chatModel;
    private final FeedbackPromptConfig promptProperties;

    @Transactional
    public ArticleFeedbackResponse generateFeedback(Long articleId, Member member) {
        Article article = articleService.validateAndGetArticle(articleId, member);

        articleFeedbackRepository.findByArticle_ArticleId(articleId)
                .ifPresent(feedback -> {throw new ArticleFeedbackAlreadyExistsException();});

        ArticleFeedback feedback = createFeedback(article);
        ArticleFeedback savedFeedback = articleFeedbackRepository.save(feedback);

        return new ArticleFeedbackResponse(savedFeedback);
    }

    @Transactional
    public ArticleFeedbackResponse regenerateFeedback(Long articleId, Member member) {
        Article article = articleService.validateAndGetArticle(articleId, member);

        ArticleFeedback existingFeedback = articleFeedbackRepository.findByArticle_ArticleId(articleId)
                .orElseThrow(ArticleFeedbackNotFoundException::new);

        ArticleFeedback newFeedbackContent = createFeedback(article);

        existingFeedback.updateFeedbackContent(
                newFeedbackContent.getKeyPoint(),
                newFeedbackContent.getStrengths(),
                newFeedbackContent.getImprovements(),
                newFeedbackContent.getLearningPoint()
        );

        return new ArticleFeedbackResponse(existingFeedback);
    }

    public ArticleFeedbackResponse getFeedback(Long articleId) {
        ArticleFeedback feedback = articleFeedbackRepository
                .findByArticle_ArticleId(articleId)
                .orElseThrow(ArticleFeedbackNotFoundException::new);

        return new ArticleFeedbackResponse(feedback);
    }

    public ArticleFeedback createFeedback(Article article) {
        try {
            String systemPrompt = promptProperties.getSystem();
            String userPrompt = promptProperties.getUserPrompt(article.getArticleTitle(), articleService.getPlainArticleBody(article.getArticleBody()));
            Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));

            String aiResponse = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getContent();

            log.info(aiResponse);

            return parseFeedback(aiResponse, article);
        } catch (Exception e) {
            throw new AIFeedbackGenerationException();
        }
    }

    private ArticleFeedback parseFeedback(String feedbackText, Article article) {
        return ArticleFeedback.builder()
                .article(article)
                .keyPoint(extractResponseSection(feedbackText, "1. 문제 핵심 포인트"))
                .strengths(extractResponseSection(feedbackText, "2. 잘한 부분"))
                .improvements(extractResponseSection(feedbackText, "3. 개선 제안"))
                .learningPoint(extractResponseSection(feedbackText, "4. 학습 포인트"))
                .build();
    }

    private String extractResponseSection(String text, String header) {
        int start = text.indexOf(header);
        if (start == -1) return "";

        start = text.indexOf("\n", start) + 1;
        int end = text.indexOf("###", start);
        if (end == -1) end = text.length();

        String content = text.substring(start, end).trim();
        return content.contains("- ") ? joinListItems(content) : content;
    }

    private String joinListItems(String content) {
        return String.join("|||", content.split("\n- "))
                    .replace("- ", "");
    }
}
