package site.codemonster.comon.domain.recommendation.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import site.codemonster.comon.domain.article.service.ArticleService;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.problem.entity.Problem;
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
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
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
    private TeamMemberService teamMemberService;


    @Test
    @DisplayName("추천 설정 저장 성공")
    void saveRecommendationSettingsSuccess() {

        // given
        Team team = TestUtil.createTeam();

        given(teamLowService.findById(any()))
                .willReturn(team);

        given(teamRecommendationLowService.isExistByTeam(any()))
                .willReturn(false);

        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);

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
        Team team = TestUtil.createTeam();

        given(teamLowService.findById(any()))
                .willReturn(team);

        given(teamRecommendationLowService.isExistByTeam(any()))
                .willReturn(true);

        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);

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
        Team team = TestUtil.createTeam();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);
        TeamRecommendationDay teamRecommendationDay = TestUtil.createTeamRecommendationDay(teamRecommendation);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendation(teamRecommendation);


        ReflectionTestUtils.setField(team, "teamRecommendation", teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendation, "teamRecommendationDays", List.of(teamRecommendationDay));
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));


        given(teamLowService.findByTeamIdWithTeamRecommendation(any()))
                .willReturn(team);

        // when
        TeamRecommendationResponse response = teamRecommendationService.getRecommendationSettings(team.getTeamId());

        // then
        assertSoftly(softly-> {
            softly.assertThat(response.recommendationAt()).isEqualTo(teamRecommendation.getRecommendationAt());
            softly.assertThat(response.recommendDays().size()).isEqualTo(1);
            softly.assertThat(response.platformRecommendationResponses().size()).isEqualTo(1);
        });

        verify(teamLowService).findByTeamIdWithTeamRecommendation(any());
        verifyNoMoreInteractions(teamLowService);
    }

    @Test
    @DisplayName("수동 추천 성공")
    void executeManualRecommendationSuccess() {

        // given
        Team team = TestUtil.createTeam();
        Problem problem = TestUtil.createProblem();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendation(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistory(team, problem);
        Member member = TestUtil.createMember();
        TeamMember teamMember = TestUtil.createTeamManager(team, member);

        ReflectionTestUtils.setField(team,"teamRecommendation", teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        given(teamLowService.findByTeamIdWithTeamRecommendation(any()))
                .willReturn(team);

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of(recommendationHistory));

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(problem, problem, problem, problem)); // 추천 가능한 문제 4개

        given(teamMemberService.getTeamManagerByTeamId(any()))
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

        verify(teamLowService).findByTeamIdWithTeamRecommendation(any());
        verify(recommendationHistoryLowService, times(2)).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberService).getTeamManagerByTeamId(any());
        verify(articleService).createRecommendationArticle(any(),any(),any(), any());
        verify(recommendationHistoryLowService).saveAll(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberService,
                articleService);
    }

    @Test
    @DisplayName("수동 추천 성공 - 이미 추천한 날짜 존재 제외하고 추천")
    void executeManualRecommendationSuccess2() {

        // given
        Team team = TestUtil.createTeam();
        Problem problem = TestUtil.createProblem();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendation(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistory(team, problem);
        Member member = TestUtil.createMember();
        TeamMember teamMember = TestUtil.createTeamManager(team, member);

        ReflectionTestUtils.setField(team,"teamRecommendation", teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        given(teamLowService.findByTeamIdWithTeamRecommendation(any()))
                .willReturn(team);

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of(recommendationHistory));

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(problem, problem, problem, problem)); // 추천 가능한 문제 4개

        given(teamMemberService.getTeamManagerByTeamId(any()))
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

        verify(teamLowService).findByTeamIdWithTeamRecommendation(any());
        verify(recommendationHistoryLowService, times(2)).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberService).getTeamManagerByTeamId(any());
        verify(articleService).createRecommendationArticle(any(),any(),any(), any());
        verify(recommendationHistoryLowService).saveAll(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberService,
                articleService);
    }


    @Test
    @DisplayName("추천 실행 성공")
    void executeRecommendationSuccess() {

        // given
        Team team = TestUtil.createTeam();
        Problem problem = TestUtil.createProblem();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendation(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistory(team, problem);
        Member member = TestUtil.createMember();
        TeamMember teamMember = TestUtil.createTeamManager(team, member);
        ReflectionTestUtils.setField(team,"teamRecommendation", teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));

        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of(recommendationHistory));

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(problem, problem,problem)); // 추천 가능한 문제 3개

        given(teamMemberService.getTeamManagerByTeamId(any()))
                .willReturn(teamMember);

        given(articleService.createRecommendationArticle(any(),any(),any(), any()))
                .willReturn("제목");

        String title = teamRecommendationService.executeRecommendation(teamRecommendation, LocalDate.now());


        assertThat(title).isEqualTo("제목");
        verify(recommendationHistoryLowService).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verify(teamMemberService).getTeamManagerByTeamId(any());
        verify(articleService).createRecommendationArticle(any(),any(),any(), any());
        verify(recommendationHistoryLowService).saveAll(any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberService,
                articleService);
    }

    @Test
    @DisplayName("추천 실행 실패 - 문제 수 부족")
    void executeRecommendationFail() {

        // given
        Team team = TestUtil.createTeam();
        Problem problem = TestUtil.createProblem();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendation(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistory(team, problem);
        ReflectionTestUtils.setField(team,"teamRecommendation", teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));


        given(recommendationHistoryLowService.findByTeamId(any()))
                .willReturn(List.of(recommendationHistory));

        given(problemQueryService.findRecommendationProblem(any(),any()))
                .willReturn(List.of(problem, problem)); // 추천 가능한 문제 2개

        // when & then
        assertThatThrownBy(()->teamRecommendationService.executeRecommendation(
                teamRecommendation, LocalDate.now())
        ).isInstanceOf(TeamRecommendationProblemShortageException.class);

        verify(recommendationHistoryLowService).findByTeamId(any());
        verify(problemQueryService).findRecommendationProblem(any(),any());
        verifyNoMoreInteractions(
                teamLowService,recommendationHistoryLowService,
                problemQueryService,teamMemberService,
                articleService);
    }


    @Test
    @DisplayName("자동 추천 실행 성공")
    void executeAutoRecommendation() {
        // given
        Team team = TestUtil.createTeam();
        Problem problem = TestUtil.createProblem();
        TeamRecommendation teamRecommendation = TestUtil.createTeamRecommendation(team);
        PlatformRecommendation platformRecommendation = TestUtil.createPlatformRecommendation(teamRecommendation);
        RecommendationHistory recommendationHistory = TestUtil.createRecommendationHistory(team, problem);
        TeamRecommendationDay teamRecommendationDay = TestUtil.createTeamRecommendationDay(teamRecommendation);

        ReflectionTestUtils.setField(team,"teamRecommendation", teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendation, "platformRecommendations", List.of(platformRecommendation));
        ReflectionTestUtils.setField(teamRecommendation, "teamRecommendationDays", List.of(teamRecommendationDay));

        given(teamRecommendationLowService.findAllWithRecommendationDays())
                .willReturn(List.of(teamRecommendation));

        given(recommendationHistoryLowService.findByLocalDate(any()))
                .willReturn(List.of(recommendationHistory));

        teamRecommendationService.executeAutoRecommendation(LocalDateTime.now());

    }
}
