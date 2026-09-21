package site.codemonster.comon.domain.recommendation.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.problem.service.ProblemLowService;
import site.codemonster.comon.domain.recommendation.dto.request.ManualRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.request.TeamRecommendationRequest;
import site.codemonster.comon.domain.recommendation.dto.response.ManualRecommendationResponse;
import site.codemonster.comon.domain.recommendation.dto.response.TeamRecommendationResponse;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendationDay;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamLowService;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.domain.util.TestUtil;
import site.codemonster.comon.global.error.recommendation.TeamRecommendationDuplicateException;
import site.codemonster.comon.global.error.recommendation.TeamRecommendationProblemShortageException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TeamRecommendationServiceTest {

    @InjectMocks
    private TeamRecommendationHighService teamRecommendationService;

    @Mock
    private TeamLowService teamLowService;

    @Mock
    private TeamRecommendationLowService teamRecommendationLowService;

    @Mock
    private TeamRecommendationDayLowService teamRecommendationDayLowService;

    @Mock
    private PlatformRecommendationLowService platformRecommendationLowService;

    @Mock
    private RecommendationHistoryLowService recommendationHistoryLowService;

    @Mock
    private ProblemLowService problemQueryService;

    @Mock
    private ArticleService articleService;

    @Mock
    private TeamMemberLowService teamMemberLowService;


    @Test
    @DisplayName("추천 설정 저장 성공")
    void saveRecommendationSettingsSuccess() {

        // given
        Team team = TestUtil.createTeam();

        given(teamLowService.findById(any()))
                .willReturn(team);

        given(teamRecommendationLowService.isExistByTeam(any()))
                .willReturn(false);

        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);

        given(teamRecommendationLowService.save(any(), any()))
                .willReturn(teamRecommendation);

        // when
        TeamRecommendation saveTeamRecommendation = teamRecommendationService.saveRecommendationSettings(
                new TeamRecommendationRequest(team.getTeamId(), new ArrayList<>(), teamRecommendation.getRecommendationAt(), new HashSet<>(Collections.singleton(DayOfWeek.MONDAY)))
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(saveTeamRecommendation.getRecommendationAt()).isEqualTo(teamRecommendation.getRecommendationAt());
            softly.assertThat(saveTeamRecommendation.getTeam().getTeamId()).isEqualTo(team.getTeamId());
        });

        verify(teamLowService).findById(any());
        verify(teamRecommendationLowService).isExistByTeam(any());
        verify(teamRecommendationLowService).save(any(), any());
        verify(teamRecommendationDayLowService).saveAll(any(), any());
        verify(platformRecommendationLowService).saveAll(any(), any());
        verifyNoMoreInteractions(teamLowService,teamRecommendationLowService,teamRecommendationDayLowService,platformRecommendationLowService);

    }

    @Test
    @DisplayName("추천 저장 실패 - 이미 존재하는 추천")
    void saveRecommendationSettingsFail() {

        // given
        Team team = TestUtil.createTeamWithId();

        given(teamLowService.findById(any()))
                .willReturn(team);

        given(teamRecommendationLowService.isExistByTeam(any()))
                .willReturn(true);

        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);

        // when & then
        assertThatThrownBy(()->teamRecommendationService.saveRecommendationSettings(
                new TeamRecommendationRequest(team.getTeamId(), new ArrayList<>(), teamRecommendation.getRecommendationAt(), new HashSet<>(Collections.singleton(DayOfWeek.MONDAY)))
        )).isInstanceOf(TeamRecommendationDuplicateException.class);

        // then
        verify(teamLowService).findById(any());
        verify(teamRecommendationLowService).isExistByTeam(any());
        verifyNoMoreInteractions(teamLowService,teamRecommendationLowService,teamRecommendationDayLowService,platformRecommendationLowService);
    }

    @Test
    @DisplayName("기존 팀 추천 조회")
    void getRecommendationSettingsSuccess() {

        // given
        Team team = TestUtil.createTeamWithId();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        TeamRecommendationDay teamRecommendationDay = TestUtil.createTeamRecommendationDayWithId(teamRecommendation);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);


        ReflectionTestUtils.setField(teamRecommendation, "teamRecommendationDays", List.of(teamRecommendationDay));
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));


        given(teamLowService.findById(any()))
                .willReturn(team);

        given(teamRecommendationLowService.findByTeam(any()))
                .willReturn(teamRecommendation);

        // when
        TeamRecommendationResponse response = teamRecommendationService.getRecommendationSettings(team.getTeamId());

        // then
        assertSoftly(softly-> {
            softly.assertThat(response.recommendationAt()).isEqualTo(teamRecommendation.getRecommendationAt());
            softly.assertThat(response.recommendDays().size()).isEqualTo(1);
            softly.assertThat(response.platformRecommendationResponses().size()).isEqualTo(1);
        });

        verify(teamLowService).findById(any());
        verify(teamRecommendationLowService).findByTeam(any());
        verifyNoMoreInteractions(teamLowService);
    }

    @Test
    @DisplayName("수동 추천 성공")
    void executeManualRecommendationSuccess() {

        // given
        Team team = TestUtil.createTeamWithId();
        Problem problem = TestUtil.createProblem();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistoryWithId(team, problem);
        Member member = TestUtil.createMemberWithId();
        TeamMember teamMember = TestUtil.createTeamManagerWithId(team, member);

        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        given(teamLowService.findById(any()))
                .willReturn(team);

        given(teamRecommendationLowService.findByTeam(any()))
                .willReturn(teamRecommendation);

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of(recommendationHistory));

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(problem, problem, problem, problem)); // 추천 가능한 문제 4개

        given(teamMemberLowService.getTeamManagerByTeamId(any()))
                .willReturn(teamMember);

        given(articleService.createRecommendationArticle(any(),any(),any(), any()))
                .willReturn("제목");


        // when
        ManualRecommendationResponse response = teamRecommendationService.executeManualRecommendation(new ManualRecommendationRequest(
                team.getTeamId(), Set.of(LocalDate.now().plusDays(1))
        ));

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.createdArticleTitles().get(0)).isEqualTo("제목");
            softly.assertThat(response.totalRecommended()).isEqualTo(1);
        });

        verify(teamLowService).findById(any());
        verify(teamRecommendationLowService).findByTeam(any());
        verify(recommendationHistoryLowService, times(2)).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberLowService).getTeamManagerByTeamId(any());
        verify(articleService).createRecommendationArticle(any(),any(),any(), any());
        verify(recommendationHistoryLowService).saveAll(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberLowService,
                articleService);
    }

    @Test
    @DisplayName("수동 추천 성공 - 이미 추천한 날짜 존재 제외하고 추천")
    void executeManualRecommendationSuccess2() {

        // given
        Team team = TestUtil.createTeamWithId();
        Problem problem = TestUtil.createProblemWithId();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistoryWithId(team, problem);
        Member member = TestUtil.createMemberWithId();
        TeamMember teamMember = TestUtil.createTeamManagerWithId(team, member);

        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        given(teamLowService.findById(any()))
                .willReturn(team);
        given(teamRecommendationLowService.findByTeam(any()))
            .willReturn(teamRecommendation);

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of(recommendationHistory));

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(problem, problem, problem, problem)); // 추천 가능한 문제 4개

        given(teamMemberLowService.getTeamManagerByTeamId(any()))
                .willReturn(teamMember);

        given(articleService.createRecommendationArticle(any(),any(),any(), any()))
                .willReturn("제목");

        // when
        ManualRecommendationResponse response = teamRecommendationService.executeManualRecommendation(new ManualRecommendationRequest(
                team.getTeamId(), Set.of(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))
        ));

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.createdArticleTitles().get(0)).isEqualTo("제목");
            softly.assertThat(response.totalRecommended()).isEqualTo(1);
        });

        verify(teamLowService).findById(any());
        verify(teamRecommendationLowService).findByTeam(any());
        verify(recommendationHistoryLowService, times(2)).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberLowService).getTeamManagerByTeamId(any());
        verify(articleService).createRecommendationArticle(any(),any(),any(), any());
        verify(recommendationHistoryLowService).saveAll(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberLowService,
                articleService);
    }


    @Test
    @DisplayName("추천 실행 성공")
    void executeRecommendationSuccess() {

        // given
        Team team = TestUtil.createTeamWithId();
        Problem problem = TestUtil.createProblemWithId();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistoryWithId(team, problem);
        Member member = TestUtil.createMemberWithId();
        TeamMember teamMember = TestUtil.createTeamManagerWithId(team, member);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of(recommendationHistory));

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(problem, problem,problem)); // 추천 가능한 문제 3개

        given(teamMemberLowService.getTeamManagerByTeamId(any()))
                .willReturn(teamMember);

        given(articleService.createRecommendationArticle(any(),any(),any(), any()))
                .willReturn("제목");

        String title = teamRecommendationService.executeRecommendation(teamRecommendation, LocalDate.now());


        assertThat(title).isEqualTo("제목");
        verify(recommendationHistoryLowService).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberLowService).getTeamManagerByTeamId(any());
        verify(articleService).createRecommendationArticle(any(),any(),any(), any());
        verify(recommendationHistoryLowService).saveAll(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberLowService,
                articleService);
    }

    @Test
    @DisplayName("추천 실행 실패 - 플랫폼/STEP 전체 문제 수 부족 (추천 기록도 없음)")
    void executeRecommendationFail() {

        // given
        Team team = TestUtil.createTeamWithId();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));


        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of()); // 추천 기록 없음 -> 순환할 문제도 없음

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of()); // 추천 가능한 문제 0개

        // when & then
        assertThatThrownBy(()->teamRecommendationService.executeRecommendation(
                teamRecommendation, LocalDate.now())
        ).isInstanceOf(TeamRecommendationProblemShortageException.class);

        verify(recommendationHistoryLowService).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberLowService,
                articleService);
    }

    @Test
    @DisplayName("추천 실행 성공 - STEP 소진 시 가장 오래전에 추천한 문제부터 순환")
    void executeRecommendationRecyclesOldestWhenStepExhausted() {

        // given
        Team team = TestUtil.createTeamWithId();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);
        ReflectionTestUtils.setField(platformRecommendation, "problemCount", 2);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        Problem oldest = problemWithId(10L);
        Problem middle = problemWithId(20L);
        Problem newest = problemWithId(30L);
        List<RecommendationHistory> histories = List.of(
                history(team,newest, LocalDate.of(2026, 3, 1)),
                history(team,oldest, LocalDate.of(2026, 1, 1)),
                history(team,middle, LocalDate.of(2026, 2, 1)),
                history(team,oldest, LocalDate.of(2025, 12, 1)) // 같은 문제의 더 오래된 기록
        );
        Member member = TestUtil.createMemberWithId();
        TeamMember teamMember = TestUtil.createTeamManagerWithId(team, member);

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(histories);

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of()); // 아직 추천 안 한 문제 0개 (STEP 소진)

        given(teamMemberLowService.getTeamManagerByTeamId(any()))
                .willReturn(teamMember);

        given(articleService.createRecommendationArticle(any(),any(),any(), any()))
                .willReturn("제목");

        // when
        String title = teamRecommendationService.executeRecommendation(teamRecommendation, LocalDate.now());

        // then
        assertThat(title).isEqualTo("제목");

        ArgumentCaptor<List<Problem>> problemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(articleService).createRecommendationArticle(any(), any(), problemsCaptor.capture(), any());
        assertThat(problemsCaptor.getValue()).containsExactly(oldest, middle);

        ArgumentCaptor<List<RecommendationHistory>> historyCaptor = ArgumentCaptor.forClass(List.class);
        verify(recommendationHistoryLowService).saveAll(historyCaptor.capture());
        assertThat(historyCaptor.getValue())
                .extracting(RecommendationHistory::getProblem)
                .containsExactly(oldest, middle);

        verify(recommendationHistoryLowService).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberLowService).getTeamManagerByTeamId(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberLowService,
                articleService);
    }

    @Test
    @DisplayName("추천 실행 성공 - 안 한 문제를 먼저 쓰고 부족분만 순환으로 채움")
    void executeRecommendationFillsShortfallWithRecycledProblems() {

        // given
        Team team = TestUtil.createTeamWithId();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);
        ReflectionTestUtils.setField(platformRecommendation, "problemCount", 2);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        Problem unused = problemWithId(40L);
        Problem oldest = problemWithId(10L);
        Problem newest = problemWithId(30L);
        List<RecommendationHistory> histories = List.of(
                history(team,newest, LocalDate.of(2026, 3, 1)),
                history(team,oldest, LocalDate.of(2026, 1, 1))
        );
        Member member = TestUtil.createMemberWithId();
        TeamMember teamMember = TestUtil.createTeamManagerWithId(team, member);

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(histories);

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(unused)); // 아직 추천 안 한 문제 1개, 필요 수는 2개

        given(teamMemberLowService.getTeamManagerByTeamId(any()))
                .willReturn(teamMember);

        given(articleService.createRecommendationArticle(any(),any(),any(), any()))
                .willReturn("제목");

        // when
        teamRecommendationService.executeRecommendation(teamRecommendation, LocalDate.now());

        // then
        ArgumentCaptor<List<Problem>> problemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(articleService).createRecommendationArticle(any(), any(), problemsCaptor.capture(), any());
        assertThat(problemsCaptor.getValue()).containsExactly(unused, oldest);

        verify(recommendationHistoryLowService).findByTeamId(any());
        verify(recommendationHistoryLowService).saveAll(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberLowService).getTeamManagerByTeamId(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberLowService,
                articleService);
    }

    private static Problem problemWithId(Long problemId) {
        Problem problem = new Problem(Platform.PROGRAMMERS, String.valueOf(problemId), "문제" + problemId, ProblemStep.STEP1, "url");
        ReflectionTestUtils.setField(problem, "problemId", problemId);
        return problem;
    }

    /** 평소 흐름처럼 추천 당일에 저장된 기록: createdDate = recommendedAt (순환 순서는 저장 순서 기준) */
    private static RecommendationHistory history(Team team, Problem problem, LocalDate recommendedAt) {
        RecommendationHistory history = new RecommendationHistory(team, problem, recommendedAt);
        ReflectionTestUtils.setField(history, "createdDate", recommendedAt.atStartOfDay());
        return history;
    }


    @Test
    @DisplayName("자동 추천 실행 성공")
    void executeAutoRecommendation() {
        // given
        Team team = TestUtil.createTeamWithId();
        Problem problem = TestUtil.createProblemWithId();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendationWithId(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendationWithId(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistoryWithId(team, problem);
        TeamRecommendationDay teamRecommendationDay = TestUtil.createTeamRecommendationDayWithId(teamRecommendation);

        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));
        ReflectionTestUtils.setField(teamRecommendation, "teamRecommendationDays", List.of(teamRecommendationDay));

        given(teamRecommendationLowService.findAllWithRecommendationDays())
                .willReturn(List.of(teamRecommendation));

        given(recommendationHistoryLowService.findByLocalDate(any()))
                .willReturn(List.of(recommendationHistory));

        teamRecommendationService.executeAutoRecommendation(LocalDateTime.now());

    }
}
