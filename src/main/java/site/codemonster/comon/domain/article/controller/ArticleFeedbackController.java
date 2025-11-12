package site.codemonster.comon.domain.article.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.article.dto.response.ArticleFeedbackResponse;
import site.codemonster.comon.domain.article.service.ArticleFeedbackService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;

import static site.codemonster.comon.domain.article.controller.ArticleFeedbackResponseEnum.*;

@Trace
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleFeedbackController {

    private final ArticleFeedbackService articleFeedbackService;

    @PostMapping("/{articleId}/feedback")
    public ResponseEntity<ApiResponse<ArticleFeedbackResponse>> generateFeedback(
            @PathVariable Long articleId,
            @AuthenticationPrincipal Member member
    ) {
        ArticleFeedbackResponse response =
                articleFeedbackService.generateFeedback(articleId, member);

        return ResponseEntity.status(ARTICLE_FEEDBACK_CREATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, ARTICLE_FEEDBACK_CREATE_SUCCESS.getMessage()));
    }

    @PutMapping("/{articleId}/feedback/regenerate")
    public ResponseEntity<ApiResponse<ArticleFeedbackResponse>> regenerateFeedback(
            @PathVariable Long articleId,
            @AuthenticationPrincipal Member member
    ) {
        ArticleFeedbackResponse response =
                articleFeedbackService.regenerateFeedback(articleId, member);

        return ResponseEntity.status(ARTICLE_FEEDBACK_REGENERATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, ARTICLE_FEEDBACK_REGENERATE_SUCCESS.getMessage()));
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
