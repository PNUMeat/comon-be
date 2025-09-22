package site.codemonster.comon.domain.recommendation.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.adminAuth.repository.AdminMemberRepository;
import site.codemonster.comon.domain.adminAuth.util.SessionConst;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.repository.ProblemRepository;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.PlatformRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationResponse;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendationDay;
import site.codemonster.comon.domain.recommendation.repository.PlatformRecommendationRepository;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationDayRepository;
import site.codemonster.comon.domain.recommendation.repository.TeamRecommendationRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.dto.response.ApiResponse;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminRecommendationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRecommendationRepository teamRecommendationRepository;

    @Autowired
    private TeamRecommendationDayRepository teamRecommendationDayRepository;

    @Autowired
    private PlatformRecommendationRepository platformRecommendationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private AdminMemberRepository adminMemberRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        AdminMember adminMember = adminMemberRepository.save(TestUtil.createAdminMember());

        session = new MockHttpSession();
        session.setAttribute(SessionConst.ADMIN_SESSION_KEY, adminMember);
    }

    @Test
    @DisplayName("추천 저장 성공")
    void saveTeamRecommendationSuccess() throws Exception {

        // given
        Team team = teamRepository.save(TestUtil.createTeam());

        TeamRecommendationRequest teamRecommendationRequest = new TeamRecommendationRequest(
                team.getTeamId(),
                List.of(new PlatformRecommendationRequest(Platform.BAEKJOON, ProblemStep.STEP1, 1)),
                9, Set.of(DayOfWeek.MONDAY));

        String content = objectMapper.writeValueAsString(teamRecommendationRequest);

        mockMvc.perform(post("/admin/recommendations/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .session(session)
                .content(content)
        ).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("추천 저장 실패 - 중복된 문제 추천 조합 존재")
    void saveTeamRecommendationFail() throws Exception {

        // given
        Team team = teamRepository.save(TestUtil.createTeam());

        TeamRecommendationRequest teamRecommendationRequest = new TeamRecommendationRequest(
                team.getTeamId(),
                List.of(
                        new PlatformRecommendationRequest(Platform.BAEKJOON, ProblemStep.STEP1, 1),
                        new PlatformRecommendationRequest(Platform.BAEKJOON, ProblemStep.STEP1, 2)),
                9, Set.of(DayOfWeek.MONDAY));

        String content = objectMapper.writeValueAsString(teamRecommendationRequest);

        String response = mockMvc.perform(post("/admin/recommendations/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .content(content)
                ).andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<Map<String,String>> apiResponse = objectMapper.readValue(response, new TypeReference<ApiResponse<Map<String,String>>>() {});

        Map<String, String> data = apiResponse.getData();

        assertThat(data.get("noDuplicateRecommendation")).isEqualTo("중복된 Platform + ProblemStep 조합입니다.");
    }

    @Test
    @DisplayName("추천 조회 성공")
    void getTeamRecommendationSuccess() throws Exception {
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamRecommendation teamRecommendation = teamRecommendationRepository.save(TestUtil.createTeamRecommendation(team));
        TeamRecommendationDay teamRecommendationDay = teamRecommendationDayRepository.save(TestUtil.createTeamRecommendationDay(teamRecommendation));
        PlatformRecommendation platformRecommendation = platformRecommendationRepository.save(TestUtil.createPlatformRecommendation(teamRecommendation));

        // 연관관계를 연결하기 위해서 호출
        flushAndClear();

        String content = mockMvc.perform(get("/admin/recommendations/settings/{teamId}", team.getTeamId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<TeamRecommendationResponse> apiResponse =
                objectMapper.readValue(content, new TypeReference<ApiResponse<TeamRecommendationResponse>>() {});

        TeamRecommendationResponse response = apiResponse.getData();

        assertSoftly(softly-> {
            softly.assertThat(response.recommendationAt()).isEqualTo(teamRecommendation.getRecommendationAt());
            softly.assertThat(response.recommendDays().size()).isEqualTo(1);
            softly.assertThat(response.platformRecommendationResponses().size()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("추천 삭제 성공")
    void deleteTeamRecommendationSuccess() throws Exception {
        Team team = teamRepository.save(TestUtil.createTeam());
        TeamRecommendation teamRecommendation = teamRecommendationRepository.save(TestUtil.createTeamRecommendation(team));
        TeamRecommendationDay teamRecommendationDay = teamRecommendationDayRepository.save(TestUtil.createTeamRecommendationDay(teamRecommendation));
        PlatformRecommendation platformRecommendation = platformRecommendationRepository.save(TestUtil.createPlatformRecommendation(teamRecommendation));

        // 연관관계를 연결하기 위해서 호출
        flushAndClear();

        mockMvc.perform(delete("/admin/recommendations/settings/{teamId}", team.getTeamId())
                        .session(session))
                .andExpect(status().isNoContent());

        boolean present = teamRecommendationRepository.findById(teamRecommendationDay.getId()).isPresent();
        boolean present1 = teamRecommendationDayRepository.findById(teamRecommendationDay.getId()).isPresent();
        boolean present2 = platformRecommendationRepository.findById(platformRecommendation.getId()).isPresent();

        assertSoftly(softly -> {
            softly.assertThat(present).isFalse();
            softly.assertThat(present1).isFalse();
            softly.assertThat(present2).isFalse();
        });
    }

    @Test
    @DisplayName("수동 추천 실행 성공")
    void executeManaulRecommendationSuccess() throws Exception {
        Team team = teamRepository.save(TestUtil.createTeam());
        Member member = memberRepository.save(TestUtil.createMember());
        TeamMember teamManager = teamMemberRepository.save(TestUtil.createTeamManager(team, member));
        TeamRecommendation teamRecommendation = teamRecommendationRepository.save(TestUtil.createTeamRecommendation(team));
        TeamRecommendationDay teamRecommendationDay = teamRecommendationDayRepository.save(TestUtil.createTeamRecommendationDay(teamRecommendation));
        PlatformRecommendation platformRecommendation = platformRecommendationRepository.save(TestUtil.createPlatformRecommendation(teamRecommendation));
        Problem problem = problemRepository.save(TestUtil.createProblem());

        flushAndClear();

        ManualRecommendationRequest request = new ManualRecommendationRequest(team.getTeamId(), Set.of(LocalDate.now()));

        String content = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/admin/recommendations/manual")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ApiResponse<ManualRecommendationResponse> apiResponse =
                objectMapper.readValue(response, new TypeReference<ApiResponse<ManualRecommendationResponse>>() {});

        ManualRecommendationResponse data = apiResponse.getData();

        assertSoftly(softly -> {
            softly.assertThat(apiResponse.getStatus()).isEqualTo(ApiResponse.SUCCESS);
            softly.assertThat(data.totalRecommended()).isEqualTo(1);
        });
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
