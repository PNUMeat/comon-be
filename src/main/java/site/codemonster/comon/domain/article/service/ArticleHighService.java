package site.codemonster.comon.domain.article.service;


import site.codemonster.comon.domain.article.factory.RecommendationArticleFactory;
import site.codemonster.comon.domain.article.dto.request.*;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.team.dto.response.MyTeamResponse;
import site.codemonster.comon.domain.team.dto.response.TeamPageResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamService;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
import site.codemonster.comon.global.error.Team.TeamManagerInvalidException;
import site.codemonster.comon.global.error.articles.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static site.codemonster.comon.domain.article.enums.ArticleCategory.fromName;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleHighService {

    private final ArticleLowService articleLowService;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;

    @Transactional
    public Article articleCreate(Member member, ArticleCreateRequest articleCreateRequest) {
        Team team = teamService.getTeamByTeamId(articleCreateRequest.teamId());

        Article article = new Article(
                team,
                member,
                articleCreateRequest.articleTitle(),
                articleCreateRequest.articleBody(),
                ArticleCategory.NORMAL
        );

        return articleLowService.saveArticle(article);
    }

    @Transactional
    public void deleteArticle(Long articleId, Member member) {
        Article article = articleLowService.getArticleById(articleId);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        articleLowService.deleteArticle(article);
    }

    @Transactional
    public void updateArticle(Long articleId, ArticleUpdateRequest articleUpdateRequest, Member member) {
        Article article = articleLowService.getArticleById(articleId);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        article.updateArticle(articleUpdateRequest.articleTitle(), articleUpdateRequest.articleBody());
    }

    @Transactional
    public Article saveTeamSubject(
            Member member,
            Long teamId,
            TeamSubjectRequest teamSubjectRequest
    ){
        Team team = teamService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        LocalDate selectedDate = LocalDate.parse(teamSubjectRequest.selectedDate());
        checkSubjectExists(team, selectedDate);

        Article subject = createArticle(member, team, teamSubjectRequest, selectedDate);
        return articleLowService.saveArticle(subject);
    }

    public Article getTeamSubjectByDate(Long teamId, LocalDate date){
        teamService.getTeamByTeamId(teamId);
        return articleLowService.getTeamSubject(teamId, date);
    }

    @Transactional
    public void deleteTeamSubjectByArticleId(Member member, Long teamId, Long articleId){
        Team team = teamService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        if(!articleLowService.isArticleExists(articleId)){
            throw new ArticleNotFoundException();
        }

        articleLowService.deleteArticleById(articleId);
    }

    @Transactional
    public Article updateTeamSubjectByArticleId(Member member, Long teamId, Long articleId, TeamSubjectUpdateRequest teamSubjectUpdateRequest){
        Team team = teamService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        Article article = articleLowService.getArticleById(articleId);

        article.updateSubject(teamSubjectUpdateRequest.articleTitle(), teamSubjectUpdateRequest.articleBody(), teamSubjectUpdateRequest.articleCategory());

        return article;
    }

    public TeamPageResponse getSubjectArticlesUsingCalender(Member member, Long teamId, CalenderSubjectRequest calenderSubjectRequest){
        Team team = teamService.getTeamByTeamId(teamId);

        List<Article> subjectArticles = articleLowService.getSubjectArticlesByDate(teamId, calenderSubjectRequest.year(), calenderSubjectRequest.month());
        boolean isTeamManager = teamMemberService.checkMemberIsTeamManager(teamId, member);
        return TeamPageResponse.from(MyTeamResponse.of(team), isTeamManager, subjectArticles);
    }

    private void validateTeamManager(Member member, Team team) {
        if(!teamMemberService.checkMemberIsTeamManager(team.getTeamId(), member)){
            throw new TeamManagerInvalidException();
        }
    }

    private void checkSubjectExists(Team team, LocalDate selectedDate) {
        if(articleLowService.isSubjectExists(team, selectedDate)){
            throw new SubjectDuplicatedException();
        }
    }

    private Article createArticle(Member member, Team team, TeamSubjectRequest teamSubjectRequest, LocalDate selectedDate) {
        return Article.builder()
                .team(team)
                .member(member)
                .articleTitle(teamSubjectRequest.articleTitle())
                .articleBody(teamSubjectRequest.articleBody())
                .articleCategory(fromName(teamSubjectRequest.articleCategory()))
                .selectedDate(selectedDate)
                .build();
    }

    public String createRecommendationArticle(Team team, Member systemAdmin, List<Problem> problems, LocalDate date) {
        RecommendationArticleFactory.RecommendationArticleContent content =
                RecommendationArticleFactory.createContent(problems, date);

        Article article = Article.builder()
                .team(team)
                .member(systemAdmin)
                .articleTitle(content.title())
                .articleBody(content.body())
                .articleCategory(ArticleCategory.CODING_TEST)
                .selectedDate(date)
                .build();

        articleLowService.saveArticle(article);
        return content.title();
    }


}
