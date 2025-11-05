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

@Trace
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleFeedbackController {

    private final ArticleFeedbackService articleFeedbackService;

    @PostMapping("/{articleId}/feedback")
    public ResponseEntity<?> generateFeedback(
            @PathVariable Long articleId,
            @AuthenticationPrincipal Member member
    ) {
        ArticleFeedbackResponse response =
                articleFeedbackService.generateFeedback(articleId, member);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, "AI 피드백이 생성되었습니다."));
    }

    @PostMapping("/{articleId}/feedback/regenerate")
    public ResponseEntity<?> regenerateFeedback(
            @PathVariable Long articleId,
            @AuthenticationPrincipal Member member
    ) {
        ArticleFeedbackResponse response =
                articleFeedbackService.regenerateFeedback(articleId, member);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, "AI 피드백이 재생성되었습니다."));
    }

    @GetMapping("/{articleId}/feedback")
    public ResponseEntity<?> getFeedback(
            @PathVariable Long articleId,
            @AuthenticationPrincipal Member member
    ) {
        ArticleFeedbackResponse response =
                articleFeedbackService.getFeedback(articleId, member);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(response, "AI 피드백을 조회했습니다."));
    }
}
