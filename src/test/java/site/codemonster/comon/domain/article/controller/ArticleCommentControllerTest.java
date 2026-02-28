package site.codemonster.comon.domain.article.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.article.dto.request.ArticleCommentRequest;
import site.codemonster.comon.domain.article.dto.request.ArticleCreateRequest;
import site.codemonster.comon.domain.article.dto.response.ArticleCommentIdResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleCreateResponse;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.article.repository.ArticleCommentRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class ArticleCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ArticleCommentRepository articleCommentRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("댓글 생성 성공")
    void articleCommentCreateSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));

        Article article = articleRepository.save(TestUtil.createArticle(team, member));

        TestSecurityContextInjector.inject(member);

        ArticleCommentRequest articleCommentRequest = new ArticleCommentRequest("댓글 내용");

        String requestBody = objectMapper.writeValueAsString(articleCommentRequest);

        String response = mockMvc.perform(post("/api/v1/articles/{articleId}/comments", article.getArticleId())
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<ArticleCommentIdResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<ArticleCommentIdResponse>>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.CREATED.value());
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getData().commentId()).isNotNull();
        });
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void articleCommentUpdateSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));

        Article article = articleRepository.save(TestUtil.createArticle(team, member));
        ArticleComment articleComment = articleCommentRepository.save(TestUtil.createArticleComment(article, member));

        TestSecurityContextInjector.inject(member);

        ArticleCommentRequest articleCommentRequest = new ArticleCommentRequest("댓글 내용 수정");

        String requestBody = objectMapper.writeValueAsString(articleCommentRequest);

        String response = mockMvc.perform(patch("/api/v1/articles/{articleId}/comments/{commentId}",
                article.getArticleId(), articleComment.getCommentId())
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<ArticleCommentIdResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<ArticleCommentIdResponse>>() {
        });


        ArticleComment findArticleComment = articleCommentRepository.findById(articleComment.getCommentId()).get();



        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getData().commentId()).isNotNull();
            softly.assertThat(findArticleComment.getDescription()).isEqualTo(articleCommentRequest.description());
        });
    }

    @Test
    @DisplayName("댓글 soft delete 성공")
    void articleCommentDeleteSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));

        Article article = articleRepository.save(TestUtil.createArticle(team, member));

        ArticleComment articleComment = articleCommentRepository.save(TestUtil.createArticleComment(article, member));
        TestSecurityContextInjector.inject(member);


        mockMvc.perform(delete("/api/v1/articles/{articleId}/comments/{commentId}",
                article.getArticleId(), articleComment.getCommentId())
                .with(securityContext(SecurityContextHolder.getContext()))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        Optional<ArticleComment> deleteComment = articleCommentRepository.findById(articleComment.getCommentId());


        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deleteComment.get().getCommentId()).isEqualTo(articleComment.getCommentId());
            softly.assertThat(deleteComment.get().getMember()).isNull();
        });




    }

}
