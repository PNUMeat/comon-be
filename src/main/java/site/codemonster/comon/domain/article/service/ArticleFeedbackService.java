package site.codemonster.comon.domain.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleFeedback;
import site.codemonster.comon.domain.article.repository.ArticleFeedbackRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.articles.ArticleNotFoundException;
import site.codemonster.comon.global.error.articles.UnauthorizedActionException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleFeedbackService {

    private final ArticleRepository articleRepository;
    private final ArticleFeedbackRepository articleFeedbackRepository;
    private final ChatModel chatModel;

    private static final String SYSTEM_PROMPT =
            "당신은 알고리즘 문제 풀이를 분석하는 친근한 코딩 멘토입니다.";

    private static final String USER_PROMPT_TEMPLATE = """
        아래 코딩테스트 풀이를 분석하고 정해진 형식으로 피드백을 제공하세요.
        
        ## 풀이 정보
        제목: %s
        본문:
        %s
        
        ## 피드백 형식 (반드시 이 형식을 지켜주세요)
        
        ### 1. 문제 핵심 포인트
        이 문제에서 가장 중요한 핵심 개념이나 함정을 2-3줄로 간단히 설명하세요.
        
        ### 2. 잘한 부분
        - 첫 번째 잘한 점: 구체적으로 어떤 부분이 좋았는지, 왜 그것이 좋은 접근인지
        - 두 번째 잘한 점: 구체적으로 어떤 부분이 좋았는지, 왜 그것이 좋은 접근인지
        - 세 번째 잘한 점: (있다면) 구체적으로 어떤 부분이 좋았는지, 왜 그것이 좋은 접근인지
        
        ### 3. 개선 제안
        - 첫 번째 개선점: 구체적인 개선점과 개선하면 어떤 이점이 있는지
        - 두 번째 개선점: (있다면) 구체적인 개선점과 개선하면 어떤 이점이 있는지
        
        ### 4. 학습 포인트
        이 문제를 통해 얻을 수 있는 핵심 학습 내용을 2-3줄로 요약하세요.
        
        응답 규칙:
        - 반드시 한국어로 작성
        - 친근하지만 전문적인 톤 유지
        - "###" 헤더는 그대로 사용
        - 개선 제안이 없다면 "현재 풀이가 매우 효율적입니다" 같은 긍정적 피드백 제공
        """;

    @Transactional
    public ArticleFeedbackResponse generateFeedback(Long articleId, Member member) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        Optional<ArticleFeedback> existingFeedback =
                articleFeedbackRepository.findByArticle_ArticleId(articleId);

        if (existingFeedback.isPresent()) {
            return new ArticleFeedbackResponse(existingFeedback.get());
        }

        String feedbackText = callAI(article);

        ArticleFeedback feedback = parseFeedback(feedbackText, article);
        ArticleFeedback savedFeedback = articleFeedbackRepository.save(feedback);

        return new ArticleFeedbackResponse(savedFeedback);
    }

    @Transactional
    public ArticleFeedbackResponse regenerateFeedback(Long articleId, Member member) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        articleFeedbackRepository.findByArticle_ArticleId(articleId)
                .ifPresent(articleFeedbackRepository::delete);

        String feedbackText = callAI(article);

        ArticleFeedback feedback = parseFeedback(feedbackText, article);
        ArticleFeedback savedFeedback = articleFeedbackRepository.save(feedback);

        return new ArticleFeedbackResponse(savedFeedback);
    }

    public ArticleFeedbackResponse getFeedback(Long articleId, Member member) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        ArticleFeedback feedback = articleFeedbackRepository.findByArticle_ArticleId(articleId)
                .orElseThrow(() -> new IllegalStateException("피드백이 존재하지 않습니다."));

        return new ArticleFeedbackResponse(feedback);
    }

    private String callAI(Article article) {
        try {
            String userPrompt = String.format(USER_PROMPT_TEMPLATE,
                    article.getArticleTitle(),
                    article.getArticleBody());

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(userPrompt)
            ));

            String response = chatModel.call(prompt).getResult().getOutput().getContent();

            return response;

        } catch (Exception e) {
            throw new RuntimeException("AI 피드백 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private ArticleFeedback parseFeedback(String feedbackText, Article article) {
        String keyPoint = extractSection(feedbackText, "1. 문제 핵심 포인트");
        String strengths = extractSection(feedbackText, "2. 잘한 부분");
        String improvements = extractSection(feedbackText, "3. 개선 제안");
        String learningPoint = extractSection(feedbackText, "4. 학습 포인트");

        return ArticleFeedback.builder()
                .article(article)
                .keyPoint(keyPoint)
                .strengths(strengths)
                .improvements(improvements)
                .learningPoint(learningPoint)
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

            return section;
        } catch (Exception e) {
            return "";
        }
    }
}
