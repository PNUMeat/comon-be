package site.codemonster.comon.domain.article.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.codemonster.comon.domain.article.dto.request.ArticleCommentRequest;
import site.codemonster.comon.domain.article.dto.response.ArticleCommentIdResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleCommentResponse;
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
    public ResponseEntity<ApiResponse<ArticleCommentIdResponse>> createComment(
            @AuthenticationPrincipal Member member,
            @PathVariable("articleId") Long articleId,
            @RequestBody @Valid ArticleCommentRequest request
    ) {
        ArticleComment savedComment = articleCommentHighService.createComment(articleId, member, request);

        return ResponseEntity.status(COMMENT_CREATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse(new ArticleCommentIdResponse(savedComment), COMMENT_CREATE_SUCCESS.getMessage()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ArticleCommentResponse>>> getComments(
            @AuthenticationPrincipal Member member,
            @PathVariable("articleId") Long articleId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<ArticleCommentResponse> responses = articleCommentHighService.getComments(articleId, member, pageable);

        return ResponseEntity.status(COMMENT_LIST_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(responses, COMMENT_LIST_SUCCESS.getMessage()));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<ArticleCommentIdResponse>> updateComment(
            @AuthenticationPrincipal Member member,
            @PathVariable("articleId") Long articleId,
            @PathVariable("commentId") Long commentId,
            @RequestBody @Valid ArticleCommentRequest request
    ) {
        ArticleComment updatedComment = articleCommentHighService.updateComment(articleId, commentId, member, request);

        return ResponseEntity.status(COMMENT_UPDATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(new ArticleCommentIdResponse(updatedComment), COMMENT_UPDATE_SUCCESS.getMessage()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal Member member,
            @PathVariable("articleId") Long articleId,
            @PathVariable("commentId") Long commentId
    ) {
        articleCommentHighService.deleteComment(articleId, commentId, member);

        return ResponseEntity.status(COMMENT_DELETE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(COMMENT_DELETE_SUCCESS.getMessage()));
    }
}
