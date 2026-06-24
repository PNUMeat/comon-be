package site.codemonster.comon.domain.team.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.repository.RecommendationHistoryRepository;
import site.codemonster.comon.domain.team.dto.request.TeamCreateRequest;
import site.codemonster.comon.domain.team.dto.response.TeamCreateResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.enums.Topic;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.util.TestSecurityContextInjector;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.error.response.ErrorValidationResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class TeamControllerTest {

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
    private ProblemRepository problemRepository;

    @Autowired
    private RecommendationHistoryRepository recommendationHistoryRepository;


    @ParameterizedTest
    @ValueSource(ints = {1,25,50})
    @DisplayName("팀 생성 성공")
    void teamCreateSuccess(int memberLimit) throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);

        TeamCreateRequest teamCreateRequest = new TeamCreateRequest("팀이름", "팀설명", Topic.CODINGTEST.getName(), memberLimit, "1234", null, null, null);
        String requestBody = objectMapper.writeValueAsString(teamCreateRequest);


        String response = mockMvc.perform(post("/api/v1/teams")
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        ApiResponse<TeamCreateResponse> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<TeamCreateResponse>>() {
        });

        TeamCreateResponse teamCreateResponse = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(teamCreateResponse.teamId()).isNotNull();
        });
    }

    @Test
    @DisplayName("팀 생성 실패 - 팀원 수는 1이상")
    void teamCreateFail() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());

        TestSecurityContextInjector.inject(member);

        TeamCreateRequest teamCreateRequest = new TeamCreateRequest("팀이름", "팀설명", Topic.CODINGTEST.getName(), 0, "1234", null, null, null);
        String requestBody = objectMapper.writeValueAsString(teamCreateRequest);


        String response = mockMvc.perform(post("/api/v1/teams")
                .with(securityContext(SecurityContextHolder.getContext()))
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);


        ApiResponse<Map<String,String>> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Map<String,String>>>() {
        });


        Map<String, String> data = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.FAIL);
            softly.assertThat(data.get("memberLimit")).isEqualTo("팀 인원은 최소 1명 이상이어야합니다.");
            softly.assertThat(apiResponse.getCode()).isEqualTo(ErrorValidationResult.ERROR_STATUS_CODE);
            softly.assertThat(apiResponse.getMessage()).isEqualTo("팀 인원은 최소 1명 이상이어야합니다.");
        });
    }

    @Test
    @DisplayName("추천 문제 조회 성공 - 멤버가 팀+날짜로 STEP 오름차순 조회")
    void getRecommendationsSuccess() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        teamMemberRepository.save(TestUtil.createTeamMember(team, member));
        TestSecurityContextInjector.inject(member);

        LocalDate date = LocalDate.of(2026, 5, 4);
        Problem step1 = problemRepository.save(new Problem(Platform.BAEKJOON, "1001", "STEP1 문제", ProblemStep.STEP1, "https://step1"));
        Problem step2 = problemRepository.save(new Problem(Platform.BAEKJOON, "1002", "STEP2 문제", ProblemStep.STEP2, "https://step2"));
        // 저장 순서를 STEP2 → STEP1 로 섞어 정렬 검증
        recommendationHistoryRepository.save(new RecommendationHistory(team, step2, date));
        recommendationHistoryRepository.save(new RecommendationHistory(team, step1, date));

        mockMvc.perform(get("/api/v1/teams/{teamId}/recommendations", team.getTeamId())
                        .param("date", "2026-05-04")
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].step").value("STEP1"))
                .andExpect(jsonPath("$.data[0].title").value("STEP1 문제"))
                .andExpect(jsonPath("$.data[0].url").value("https://step1"))
                .andExpect(jsonPath("$.data[1].step").value("STEP2"));
    }

    @Test
    @DisplayName("추천 문제 조회 - 추천이 없는 날짜는 빈 배열")
    void getRecommendationsEmpty() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam());
        teamMemberRepository.save(TestUtil.createTeamMember(team, member));
        TestSecurityContextInjector.inject(member);

        mockMvc.perform(get("/api/v1/teams/{teamId}/recommendations", team.getTeamId())
                        .param("date", "1999-01-01")
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("추천 문제 조회 실패 - 팀 비멤버는 400")
    void getRecommendationsNotMember() throws Exception {

        Member member = memberRepository.save(TestUtil.createMember());
        Team team = teamRepository.save(TestUtil.createTeam()); // 팀에 가입시키지 않음
        TestSecurityContextInjector.inject(member);

        mockMvc.perform(get("/api/v1/teams/{teamId}/recommendations", team.getTeamId())
                        .param("date", "2026-05-04")
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isBadRequest());
    }

}
