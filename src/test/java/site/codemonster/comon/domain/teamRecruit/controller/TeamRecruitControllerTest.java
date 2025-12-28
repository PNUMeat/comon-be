package site.codemonster.comon.domain.teamRecruit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitCreateRequest;
import site.codemonster.comon.domain.teamRecruit.dto.response.TeamRecruitCreateResponse;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.domain.teamRecruit.repository.TeamRecruitRepository;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static site.codemonster.comon.domain.teamRecruit.controller.TeamRecruitResponseEnum.TEAM_RECRUIT_CREATE;
import static site.codemonster.comon.global.error.ErrorCode.TEAM_MANAGER_INVALID_ERROR;
import static site.codemonster.comon.global.error.ErrorCode.TEAM_RECRUIT_DUPLICATE_ERROR;


@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class TeamRecruitControllerTest {

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
    private TeamRecruitRepository teamRecruitRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("팀 모집글 생성 성공 - 모집글 페이지에서 생성")
    void createTeamRecruitSuccessInRecruitPage() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);

        TeamRecruitCreateRequest teamRecruitCreateRequest = new TeamRecruitCreateRequest(null, "팀모집", "팀모집내용", "www.naver.com");

        String requestBody = objectMapper.writeValueAsString(teamRecruitCreateRequest);

        String response = mockMvc.perform(post("/api/v1/recruitments")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<TeamRecruitCreateResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<TeamRecruitCreateResponse>>() {
        });

        assertSoftly(softly->{
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(TEAM_RECRUIT_CREATE.getStatusCode());
            softly.assertThat(apiResponse.getData().teamRecruitId()).isNotNull();
        });
    }

    @Test
    @DisplayName("팀 모집글 생성 성공 - 팀 페이지에서 생성")
    void createTeamRecruitSuccessInTeamPage() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(TestUtil.createTeamManager(team, member));

        TestSecurityContextInjector.inject(member);

        TeamRecruitCreateRequest teamRecruitCreateRequest = new TeamRecruitCreateRequest(team.getTeamId(), "팀모집", "팀모집내용", "www.naver.com");

        String requestBody = objectMapper.writeValueAsString(teamRecruitCreateRequest);

        String response = mockMvc.perform(post("/api/v1/recruitments")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<TeamRecruitCreateResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<TeamRecruitCreateResponse>>() {
        });

        assertSoftly(softly->{
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(apiResponse.getCode()).isEqualTo(TEAM_RECRUIT_CREATE.getStatusCode());
            softly.assertThat(apiResponse.getData().teamRecruitId()).isNotNull();
        });
    }

    @Test
    @DisplayName("팀 모집글 생성 실패 - 팀 매니저가 아닌 사람이 팀에 대한 모집글 생성")
    void createTeamRecruitFailInTeamPageCase1() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(TestUtil.createTeamMember(team, member));

        TestSecurityContextInjector.inject(member);

        TeamRecruitCreateRequest teamRecruitCreateRequest = new TeamRecruitCreateRequest(team.getTeamId(), "팀모집", "팀모집내용", "www.naver.com");

        String requestBody = objectMapper.writeValueAsString(teamRecruitCreateRequest);

        String response = mockMvc.perform(post("/api/v1/recruitments")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<TeamRecruitCreateResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<TeamRecruitCreateResponse>>() {
        });

        assertSoftly(softly->{
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.ERROR);
            softly.assertThat(apiResponse.getCode()).isEqualTo(TEAM_MANAGER_INVALID_ERROR.getCustomStatusCode());
            softly.assertThat(apiResponse.getMessage()).isEqualTo(TEAM_MANAGER_INVALID_ERROR.getMessage());
        });
    }

    @Test
    @DisplayName("팀 모집글 생성 실패 - 이미 모집글이 생성된 팀")
    void createTeamRecruitFailInTeamPageCase2() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamMember teamMember = teamMemberRepository.save(TestUtil.createTeamManager(team, member));
        TeamRecruit teamRecruit = teamRecruitRepository.save(TestUtil.createTeamRecruit(team, member));

        flushAndClear();

        TestSecurityContextInjector.inject(member);

        TeamRecruitCreateRequest teamRecruitCreateRequest = new TeamRecruitCreateRequest(team.getTeamId(), "팀모집", "팀모집내용", "www.naver.com");

        String requestBody = objectMapper.writeValueAsString(teamRecruitCreateRequest);

        String response = mockMvc.perform(post("/api/v1/recruitments")
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Void> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Void>>() {
        });

        assertSoftly(softly->{
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.ERROR);
            softly.assertThat(apiResponse.getCode()).isEqualTo(TEAM_RECRUIT_DUPLICATE_ERROR.getCustomStatusCode());
            softly.assertThat(apiResponse.getMessage()).isEqualTo(TEAM_RECRUIT_DUPLICATE_ERROR.getMessage());
        });
    }

    public void flushAndClear() {
        em.flush();
        em.clear();
    }


}
