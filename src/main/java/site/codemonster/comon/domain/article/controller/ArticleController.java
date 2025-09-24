package site.codemonster.comon.domain.article.controller;

import site.codemonster.comon.domain.article.dto.request.ArticleCreateRequest;
import site.codemonster.comon.domain.article.dto.request.ArticleUpdateRequest;
import site.codemonster.comon.domain.article.dto.request.TeamSubjectRequest;
import site.codemonster.comon.domain.article.dto.request.TeamSubjectUpdateRequest;
import site.codemonster.comon.domain.article.dto.response.ArticleCreateResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleParticularDateResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleResponse;
import site.codemonster.comon.domain.article.dto.response.TeamSubjectResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.log.annotation.Trace;
import site.codemonster.comon.global.security.annotation.LoginMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static site.codemonster.comon.domain.article.controller.ArticleResponseEnum.*;

@Trace
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final TeamMemberService teamMemberService;

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleCreateResponse>> createArticle(
            @LoginMember Member member,
            @RequestBody @Valid ArticleCreateRequest articleCreateRequest
    ) {
        teamMemberService.getTeamMemberByTeamIdAndMemberId(articleCreateRequest.teamId(), member);

        Article savedArticle = articleService.articleCreate(member, articleCreateRequest);

        ArticleCreateResponse articleCreateResponse = ArticleCreateResponse.of(savedArticle);

        return ResponseEntity.status(ARTICLE_CREATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponse(articleCreateResponse, ARTICLE_CREATE_SUCCESS.getMessage()));
    }

    @GetMapping("/{teamId}/my-page")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getMyArticlesAtTeam(
            @LoginMember Member member,
            @PathVariable("teamId") Long teamId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<Article> myArticles = articleService.getMyArticlesUsingPaging(member.getId(), teamId, pageable);

        Page<ArticleResponse> responses = myArticles.map(ArticleResponse::new);

        return ResponseEntity.status(GET_MY_PAGE_ARTICLE_PARTICULAR_TEAM.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(responses, GET_MY_PAGE_ARTICLE_PARTICULAR_TEAM.getMessage()));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getArticlesByTeam(@PathVariable Long teamId) {
        List<Article> articles = articleService.getAllArticlesByTeam(teamId);
        List<ArticleResponse> responses = articles.stream()
                .map(ArticleResponse::new)
                .toList();

        return ResponseEntity.status(GET_ARTICLE_PARTICULAR_TEAM.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(responses, GET_ARTICLE_PARTICULAR_TEAM.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @PathVariable(name = "id") Long articleId,
            @LoginMember Member member
    ) {
        articleService.deleteArticle(articleId, member);

        return ResponseEntity.status(ARTICLE_DELETE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(ARTICLE_DELETE_SUCCESS.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateArticle(
            @PathVariable(name = "id") Long articleId,
            @RequestBody ArticleUpdateRequest articleUpdateRequest,
            @LoginMember Member member
    ) {
        articleService.updateArticle(articleId, articleUpdateRequest, member);

        return ResponseEntity.status(ARTICLE_PUT_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(ARTICLE_PUT_SUCCESS.getMessage()));
    }

    @GetMapping("/{teamId}/by-date")
    public ResponseEntity<ApiResponse<Page<ArticleParticularDateResponse>>> getArticlesByTeamAndDate(
        @PathVariable("teamId") Long teamId,
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @LoginMember Member member,
        @PageableDefault(size = 6, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        boolean isMyTeam = teamMemberService.existsByTeamIdAndMemberId(teamId, member);
        Page<Article> articlePage = articleService.getArticlesByTeamAndDate(teamId, date, pageable);

        Page<ArticleParticularDateResponse> responsePage = articlePage.map(article ->
                ArticleParticularDateResponse.of(article, member, isMyTeam));

        return ResponseEntity.status(GET_ARTICLE_PARTICULAR_TEAM_AND_DATE.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(ApiResponse.successResponse(responsePage, GET_ARTICLE_PARTICULAR_TEAM_AND_DATE.getMessage()));
    }


    @PostMapping("/teams/{teamId}/subjects")
    public ResponseEntity<ApiResponse<Void>> createTeamSubject(
            @LoginMember Member member,
            @PathVariable("teamId") Long teamId,
            @ModelAttribute @Valid TeamSubjectRequest teamSubjectRequest
    ) {
        articleService.saveTeamSubject(member, teamId, teamSubjectRequest);

        return ResponseEntity.status(SUBJECT_CREATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.createResponseWithMessage(SUBJECT_CREATE_SUCCESS.getMessage()));
    }

    @GetMapping("/teams/{teamId}/subjects")
    public ResponseEntity<ApiResponse<TeamSubjectResponse>> getTeamSubjectsByDate( // 특정 날짜에 팀 공지사항 조회
            @PathVariable("teamId") Long teamId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Article subject = articleService.getTeamSubjectByDate(teamId, date);

        TeamSubjectResponse teamSubjectResponse = null;
        if(subject!= null){
            teamSubjectResponse = TeamSubjectResponse.of(subject);
        }

        return ResponseEntity.status(GET_SUBJECT_PARTICULAR_TEAM_AND_DATE.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponse(teamSubjectResponse, GET_SUBJECT_PARTICULAR_TEAM_AND_DATE.getMessage()));
    }

    @DeleteMapping ("/teams/{teamId}/subjects/{articleId}")
    public ResponseEntity<ApiResponse<Void>> deleteTeamSubjectsByArticleId(
            @LoginMember Member member,
            @PathVariable("teamId") Long teamId,
            @PathVariable("articleId") Long articleId
    ) {
        articleService.deleteTeamSubjectByArticleId(member, teamId, articleId);

        return ResponseEntity.status(SUBJECT_DELETE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(SUBJECT_DELETE_SUCCESS.getMessage()));
    }

    @PutMapping("/teams/{teamId}/subjects/{articleId}")
    public ResponseEntity<ApiResponse<Void>> updateTeamSubjectsByArticleId(
            @LoginMember Member member,
            @PathVariable("teamId") Long teamId,
            @PathVariable("articleId") Long articleId,
            @RequestBody TeamSubjectUpdateRequest teamSubjectUpdateRequest
    ) {
        articleService.updateTeamSubjectByArticleId(member, teamId, articleId, teamSubjectUpdateRequest);

        return ResponseEntity.status(SUBJECT_UPDATE_SUCCESS.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.successResponseWithMessage(SUBJECT_UPDATE_SUCCESS.getMessage()));
    }
}
