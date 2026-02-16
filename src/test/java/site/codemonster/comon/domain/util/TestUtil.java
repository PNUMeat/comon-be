package site.codemonster.comon.domain.util;

import org.springframework.test.util.ReflectionTestUtils;
import site.codemonster.comon.domain.adminAuth.entity.AdminMember;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.problem.entity.ProblemStep;
import site.codemonster.comon.domain.problem.enums.Platform;
import site.codemonster.comon.domain.recommendation.entity.PlatformRecommendation;
import site.codemonster.comon.domain.recommendation.entity.RecommendationHistory;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.recommendation.entity.TeamRecommendationDay;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.enums.Topic;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TestUtil {

    public static Article createArticle(Team team, Member member) {
        return new Article(team, member, "제목", "내용", ArticleCategory.NORMAL, true);
    }

    public static Team createTeamWithId() {
        Team team = new Team("팀이름", Topic.CODINGTEST, "팀설명", 10, "1234");
        ReflectionTestUtils.setField(team, "teamId", 1L);
        return team;
    }

    public static Team createTeam() {
        Team team = new Team("팀이름", Topic.CODINGTEST, "팀설명", 10, "1234");
        return team;
    }

    public static TeamRecommendation createTeamRecommendationWithId(Team team) {
        TeamRecommendation teamRecommendation = new TeamRecommendation(team, LocalDateTime.now().getHour());
        ReflectionTestUtils.setField(teamRecommendation,"id", 1L);
        return teamRecommendation;
    }

    public static TeamRecommendation createTeamRecommendation(Team team) {
        TeamRecommendation teamRecommendation = new TeamRecommendation(team, LocalDateTime.now().getHour());
        return teamRecommendation;
    }

    public static TeamRecommendationDay createTeamRecommendationDayWithId(TeamRecommendation teamRecommendation) {
        TeamRecommendationDay teamRecommendationDay = new TeamRecommendationDay(DayOfWeek.MONDAY, teamRecommendation);
        ReflectionTestUtils.setField(teamRecommendationDay, "id", 1L);
        return teamRecommendationDay;
    }

    public static TeamRecommendationDay createTeamRecommendationDay(TeamRecommendation teamRecommendation) {
        TeamRecommendationDay teamRecommendationDay = new TeamRecommendationDay(DayOfWeek.MONDAY, teamRecommendation);
        return teamRecommendationDay;
    }

    public static PlatformRecommendation createPlatformRecommendationWithId(TeamRecommendation teamRecommendation) {
        PlatformRecommendation platformRecommendation = new PlatformRecommendation(teamRecommendation, Platform.PROGRAMMERS, ProblemStep.STEP1, 1);
        ReflectionTestUtils.setField(platformRecommendation, "id", 1L);
        return platformRecommendation;
    }

    public static PlatformRecommendation createPlatformRecommendation(TeamRecommendation teamRecommendation) {
        PlatformRecommendation platformRecommendation = new PlatformRecommendation(teamRecommendation, Platform.PROGRAMMERS, ProblemStep.STEP1, 1);
        return platformRecommendation;
    }

    public static RecommendationHistory createRecommendationHistoryWithId(Team team, Problem problem) {
        RecommendationHistory recommendationHistory = new RecommendationHistory(team, problem, LocalDate.now().minusDays(1));
        ReflectionTestUtils.setField(recommendationHistory, "historyId", 1L);
        return recommendationHistory;
    }

    public static RecommendationHistory createRecommendationHistory(Team team, Problem problem) {
        RecommendationHistory recommendationHistory = new RecommendationHistory(team, problem, LocalDate.now().minusDays(1));
        return recommendationHistory;
    }


    public static Problem createProblemWithId() {
        Problem problem = new Problem(Platform.PROGRAMMERS, "123", "문제제목", ProblemStep.STEP1, "url");
        ReflectionTestUtils.setField(problem, "problemId", 1L);
        return problem;
    }

    public static Problem createProblem() {
        Problem problem = new Problem(Platform.PROGRAMMERS, "123", "문제제목", ProblemStep.STEP1, "url");
        return problem;
    }

    public static Member createMemberWithId() {
        Member member = new Member("email", "KAKAO 12345", "ROLE_USER");
        ReflectionTestUtils.setField(member, "id", 1L);
        return member;
    }

    public static Member createMember() {
        return new Member("example@naver.com", "kakao 1234", "USER_ROLE");
    }

    public static Member createOtherMember() {
        return new Member("other@naver.com", "kakao 1234", "USER_ROLE");
    }

    public static TeamMember createTeamManagerWithId(Team team, Member member) {
        TeamMember teamMember = new TeamMember(team, member, true);
        ReflectionTestUtils.setField(member, "id", 1L);
        return teamMember;
    }

    public static TeamMember createTeamManager(Team team, Member member) {
        TeamMember teamMember = new TeamMember(team, member, true);
        return teamMember;
    }

    public static TeamMember createTeamMember(Team team, Member member) {
        TeamMember teamMember = new TeamMember(team, member, false);
        return teamMember;
    }

    public static TeamRecruit createTeamRecruit(Team team, Member member) {

        return new TeamRecruit(team, member, "제목", "바디", "naver.com");
    }

    public static AdminMember createAdminMember() {
        return new AdminMember("admin","admin", "admin");
    }
}
