package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
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
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.ArticleFeedback.*;
import site.codemonster.comon.global.error.articles.ArticleNotFoundException;
import site.codemonster.comon.global.error.articles.UnauthorizedActionException;

import java.util.List;
import site.codemonster.comon.global.globalConfig.FeedbackPromptConfig;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleFeedbackService {

    private final ArticleRepository articleRepository;
    private final ArticleFeedbackRepository articleFeedbackRepository;
    private final ChatModel chatModel;
    private final FeedbackPromptConfig promptProperties;

    @Transactional
    public ArticleFeedbackResponse generateFeedback(Long articleId, Member member) {
        Article article = validateAndGetArticle(articleId, member);

        articleFeedbackRepository.findByArticle_ArticleId(articleId)
                .ifPresent(feedback -> {throw new ArticleFeedbackAlreadyExistsException();});

        ArticleFeedback feedback = createFeedback(article);
        ArticleFeedback savedFeedback = articleFeedbackRepository.save(feedback);

        return new ArticleFeedbackResponse(savedFeedback);
    }

    @Transactional
    public ArticleFeedbackResponse regenerateFeedback(Long articleId, Member member) {
        Article article = validateAndGetArticle(articleId, member);

        articleFeedbackRepository.findByArticle_ArticleId(articleId)
                .ifPresent(articleFeedbackRepository::delete);

        ArticleFeedback feedback = createFeedback(article);
        ArticleFeedback savedFeedback = articleFeedbackRepository.save(feedback);

        return new ArticleFeedbackResponse(savedFeedback);
    }

    public ArticleFeedbackResponse getFeedback(Long articleId) {
        ArticleFeedback feedback = articleFeedbackRepository
                .findByArticle_ArticleId(articleId)
                .orElseThrow(ArticleFeedbackNotFoundException::new);

        return new ArticleFeedbackResponse(feedback);
    }

    private Article validateAndGetArticle(Long articleId, Member member) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        return article;
    }

    private ArticleFeedback createFeedback(Article article) {
        try {
            String systemPrompt = promptProperties.getSystem();
            String userPrompt = promptProperties.getUserPrompt(article.getArticleTitle(), article.getArticleBody());
            Prompt prompt = new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)));

            String aiResponse = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getContent();

            return parseFeedback(aiResponse, article);
        } catch (Exception e) {
            throw new AIFeedbackGenerationException();
        }
    }

    private ArticleFeedback parseFeedback(String feedbackText, Article article) {
        return ArticleFeedback.builder()
                .article(article)
                .keyPoint(extractSection(feedbackText, "1. 문제 핵심 포인트"))
                .strengths(extractSection(feedbackText, "2. 잘한 부분"))
                .improvements(extractSection(feedbackText, "3. 개선 사항"))
                .learningPoint(extractSection(feedbackText, "4. 학습 포인트"))
                .build();
    }

    private String extractSection(String text, String sectionHeader) {
        try {
            int startIdx = text.indexOf(sectionHeader);
            if (startIdx == -1) {
                return "";
            }

            startIdx = text.indexOf("\n", startIdx) + 1;
            int endIdx = text.indexOf("###", startIdx);
            if (endIdx == -1) {
                endIdx = text.length();
            }

            String section = text.substring(startIdx, endIdx).trim();

            if (section.contains("- ")) {
                return parseListItems(section);
            }

            return section;

        } catch (Exception e) {
            return "";
        }
    }

    private String parseListItems(String section) {
        String[] items = section.split("\n- ");
        StringBuilder result = new StringBuilder();

        for (String item : items) {
            String cleanItem = item.replace("- ", "").trim();
            if (!cleanItem.isEmpty()) {
                if (result.length() > 0) {
                    result.append("|||");
                }
                result.append(cleanItem);
            }
        }
        return result.toString();
    }
}
