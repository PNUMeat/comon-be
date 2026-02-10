package site.codemonster.comon.domain.article.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.article.dto.request.ArticleCommentCreateRequest;
import site.codemonster.comon.domain.article.dto.response.ArticleCommentCreateResponse;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.article.service.ArticleCommentHighService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

import static site.codemonster.comon.domain.article.controller.ArticleCommentResponseEnum.*;

@RestController
@RequestMapping("/api/v1/articles/{articleId}/comments")
@RequiredArgsConstructor
public class ArticleCommentController {

    private final ArticleCommentHighService articleCommentHighService;

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleCommentCreateResponse>> createComment(
            @AuthenticationPrincipal Member member,
            @PathVariable("articleId") Long articleId,
            @RequestBody @Valid ArticleCommentCreateRequest request
    ) {
        ArticleComment savedComment = articleCommentHighService.createComment(articleId, member, request);

        return ResponseEntity.status(COMMENT_CREATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse(new ArticleCommentCreateResponse(savedComment), COMMENT_CREATE_SUCCESS.getMessage()));
    }
}
