package site.codemonster.comon.domain.auth.controller;

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
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.article.repository.ArticleCommentRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.dto.request.MemberProfileCreateRequest;
import site.codemonster.comon.domain.auth.dto.request.MemberProfileUpdateRequest;
import site.codemonster.comon.domain.auth.dto.response.MemberInfoResponse;
import site.codemonster.comon.domain.auth.dto.response.MemberProfileResponse;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.team.dto.response.TeamAbstractResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.ErrorCode;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.response.ResponseMessageEnum;
import site.codemonster.comon.global.util.s3.S3ImageUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleCommentRepository articleCommentRepository;

    @Test
    @DisplayName("회원 등록 성공")
    void createMemberProfileSuccess() throws Exception {

        String bucketUrl = S3ImageUtil.getBucketUrl();

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);

        MemberProfileCreateRequest memberProfileCreateRequest = new MemberProfileCreateRequest("testName", "testExplain", bucketUrl + "/imageUrl");

        String requestBody = objectMapper.writeValueAsString(memberProfileCreateRequest);

        String response = mockMvc.perform(post("/api/v1/members")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<String> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<String>>() {
        });

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.CREATED.value());
            softly.assertThat(apiResponse.getData()).isEqualTo("회원을 성공적으로 등록했습니다.");
        });
    }

    @Test
    @DisplayName("회원 정보 업데이트 성공")
    void updateMemberProfileSuccess() throws Exception {

        String bucketUrl = S3ImageUtil.getBucketUrl();

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);

        MemberProfileUpdateRequest memberProfileUpdateRequest = new MemberProfileUpdateRequest("testName", "testExplain", bucketUrl + "/imageUrl");

        String requestBody = objectMapper.writeValueAsString(memberProfileUpdateRequest);

        String response = mockMvc.perform(put("/api/v1/members")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<MemberProfileResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<MemberProfileResponse>>() {
        });

        MemberProfileResponse data = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(data.memberExplain()).isEqualTo(memberProfileUpdateRequest.memberExplain());
            softly.assertThat(data.memberName()).isEqualTo(memberProfileUpdateRequest.memberName());
            softly.assertThat(data.imageUrl()).isEqualTo(memberProfileUpdateRequest.imageUrl());
        });
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberProfileSuccess() throws Exception {
        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);


        String response = mockMvc.perform(get("/api/v1/members/own-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<MemberProfileResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<MemberProfileResponse>>() {
        });

        MemberProfileResponse data = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(data.memberExplain()).isEqualTo(member.getDescription());
            softly.assertThat(data.memberName()).isEqualTo(member.getMemberName());
        });
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 인증되지 않은 사용자")
    void getMemberProfileFail() throws Exception {

        String response = mockMvc.perform(get("/api/v1/members/own-profile")
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });


        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.ERROR);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            softly.assertThat(apiResponse.getMessage()).isEqualTo(ErrorCode.UNAUTHORIZED_MEMBER_ERROR.getMessage());
        });
    }

    @Test
    @DisplayName("UUID로 회원 정보 조회 성공")
    void getMemberProfileByUUIDSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);


        String response = mockMvc.perform(get("/api/v1/members/profile/" + member.getUuid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<MemberProfileResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<MemberProfileResponse>>() {
        });

        MemberProfileResponse data = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(data.memberExplain()).isEqualTo(member.getDescription());
            softly.assertThat(data.memberName()).isEqualTo(member.getMemberName());
        });
    }

    @Test
    @DisplayName("내 정보 + 내 팀 정보 조회")
    void getMemberProfileAndTeamInfoSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(new TeamMember(team, member, false));


        TestSecurityContextInjector.inject(member);

        String response = mockMvc.perform(get("/api/v1/members/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<MemberInfoResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<MemberInfoResponse>>() {
        });

        MemberInfoResponse data = apiResponse.getData();
        List<TeamAbstractResponse> teamAbstractResponses = data.teamAbstractResponses();
        TeamAbstractResponse teamAbstractResponse = teamAbstractResponses.get(0);

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(data.memberName()).isEqualTo(member.getMemberName());
            softly.assertThat(data.teamAbstractResponses().size()).isEqualTo(1);
            softly.assertThat(teamAbstractResponse.teamId()).isEqualTo(team.getTeamId());
            softly.assertThat(teamAbstractResponse.teamName()).isEqualTo(team.getTeamName());
        });
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void getMemberInfoSuccess() throws Exception {
        Member member = memberRepository.save(TestUtil.createMember());
        Member otherMember = memberRepository.save(TestUtil.createOtherMember());
        Long memberId = member.getId();
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember createCommentMember = teamMemberRepository.save(TestUtil.createTeamMember(team, member));
        TeamMember createArticleMember = teamMemberRepository.save(TestUtil.createTeamMember(team, otherMember));
        Article article = articleRepository.save(TestUtil.createArticle(team, otherMember));
        ArticleComment articleComment = articleCommentRepository.save(TestUtil.createArticleComment(article, member));

        TestSecurityContextInjector.inject(member);

        String response = mockMvc.perform(delete("/api/v1/members")
                        .with(securityContext(SecurityContextHolder.getContext()))
                )
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });

        Optional<Member> findMember = memberRepository.findByUuid(member.getUuid());

        ArticleComment deletedComment = articleCommentRepository.findById(articleComment.getCommentId()).get();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(apiResponse.getMessage()).isEqualTo(ResponseMessageEnum.MEMBER_DELETE_SUCCESS.getMessage());
            softly.assertThat(findMember.isPresent()).isFalse();
            softly.assertThat(deletedComment.getIsDeleted()).isTrue();
            softly.assertThat(deletedComment.getMember()).isNull();
        });
    }
}
