package site.codemonster.comon.domain.article.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackStreamResponse;
import site.codemonster.comon.domain.article.service.AiArticleFeedBackService;
import site.codemonster.comon.domain.article.service.ArticleFeedbackHighService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;

import static site.codemonster.comon.domain.article.controller.ArticleFeedbackResponseEnum.*;

@Trace
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleFeedbackController {

    private final ArticleFeedbackHighService articleFeedbackService;
    private final AiArticleFeedBackService aiArticleFeedBackService;

    @GetMapping(value = "/{articleId}/feedback/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ArticleFeedbackStreamResponse>> generateFeedback(
            @PathVariable Long articleId,
            @AuthenticationPrincipal Member member
    ) {

        Flux<ArticleFeedbackStreamResponse> response = aiArticleFeedBackService.generateFeedback(articleId, member);

        return ResponseEntity.status(ARTICLE_FEEDBACK_GENERATE_SUCCESS.getStatusCode()).body(response);
    }

    @GetMapping("/{articleId}/feedback")
    public ResponseEntity<ApiResponse<ArticleFeedbackResponse>> getFeedback(
            @PathVariable Long articleId,
            @AuthenticationPrincipal Member member
    ) {
        ArticleFeedbackResponse response =
                articleFeedbackService.getFeedback(articleId);

        return ResponseEntity.status(GET_ARTICLE_FEEDBACK.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, GET_ARTICLE_FEEDBACK.getMessage()));
    }
}
