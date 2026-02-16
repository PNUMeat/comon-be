package site.codemonster.comon.domain.article.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import site.codemonster.comon.domain.article.dto.request.ArticleCreateRequest;
import site.codemonster.comon.domain.article.dto.request.ArticleUpdateRequest;
import site.codemonster.comon.domain.article.dto.response.ArticleCreateResponse;
import site.codemonster.comon.domain.article.entity.Article;
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
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class ArticleControllerTest {

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
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("게시글 생성 성공")
    void articleCreateSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));

        TestSecurityContextInjector.inject(member);

        ArticleCreateRequest articleCreateRequest = new ArticleCreateRequest(team.getTeamId(), "게시글 제목", "게시글 내용", true);

        String requestBody = objectMapper.writeValueAsString(articleCreateRequest);

        String response = mockMvc.perform(post("/api/v1/articles")
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<ArticleCreateResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<ArticleCreateResponse>>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.CREATED.value());
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getData()).isNotNull();
        });
    }

    @Test
    @DisplayName("게시글 작성 실패 - empty articleBody")
    void articleCreateFail() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));

        TestSecurityContextInjector.inject(member);

        ArticleCreateRequest articleCreateRequest = new ArticleCreateRequest(team.getTeamId(), "게시글 제목", "<p><br></p>", true);

        String requestBody = objectMapper.writeValueAsString(articleCreateRequest);

        String response = mockMvc.perform(post("/api/v1/articles")
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Map<String,String>> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Map<String,String>>>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.FAIL);
            softly.assertThat(apiResponse.getMessage()).isEqualTo("게시글 내용은 필수요소입니다");
            softly.assertThat(apiResponse.getData().get("articleBody")).isEqualTo("게시글 내용은 필수요소입니다");
        });
    }

    @Test
    @DisplayName("게시글 수정 실패 - articleBody가 빈값")
    void articleUpdateFail() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));

        Article article = articleRepository.save(TestUtil.createArticle(team, member));
        TestSecurityContextInjector.inject(member);


        ArticleUpdateRequest articleUpdateRequest = new ArticleUpdateRequest("게시글 제목", "<p><br></p>", true);

        String requestBody = objectMapper.writeValueAsString(articleUpdateRequest);

        String response = mockMvc.perform(put("/api/v1/articles/{articleId}", article.getArticleId())
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Map<String,String>> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Map<String,String>>>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.FAIL);
            softly.assertThat(apiResponse.getMessage()).isEqualTo("게시글 내용은 필수요소입니다");
            softly.assertThat(apiResponse.getData().get("articleBody")).isEqualTo("게시글 내용은 필수요소입니다");
        });
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void articleUpdateSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));

        Article article = articleRepository.save(TestUtil.createArticle(team, member));

        TestSecurityContextInjector.inject(member);

        ArticleUpdateRequest articleUpdateRequest = new ArticleUpdateRequest("게시글 제목", "<p><br>게시글 본문</p>", true);

        String requestBody = objectMapper.writeValueAsString(articleUpdateRequest);

        String response = mockMvc.perform(put("/api/v1/articles/{articleId}", article.getArticleId())
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
        });
    }
}
