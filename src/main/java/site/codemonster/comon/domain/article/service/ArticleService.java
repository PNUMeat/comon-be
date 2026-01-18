package site.codemonster.comon.domain.article.service;


import site.codemonster.comon.domain.article.dto.response.ArticleParticularDateResponse;
import site.codemonster.comon.domain.article.dto.response.ArticleResponse;
import site.codemonster.comon.domain.article.factory.RecommendationArticleFactory;
import site.codemonster.comon.domain.article.dto.request.*;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.problem.entity.Problem;
import site.codemonster.comon.domain.team.dto.response.MyTeamResponse;
import site.codemonster.comon.domain.team.dto.response.TeamPageResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamLowService;
import site.codemonster.comon.domain.teamMember.service.TeamMemberLowService;
import site.codemonster.comon.global.error.Team.TeamManagerInvalidException;
import site.codemonster.comon.global.error.articles.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static site.codemonster.comon.domain.article.enums.ArticleCategory.fromName;
import static site.codemonster.comon.domain.article.enums.ArticleCategory.getSubjectCategories;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {

    private final TeamMemberLowService teamMemberLowService;
    private final ArticleLowService articleLowService;
    private final ArticleImageLowService articleImageLowService;
    private final TeamLowService teamLowService;
    private final ArticleFeedbackLowService articleFeedbackLowService;

    public Article articleCreate(Member member, ArticleCreateRequest articleCreateRequest) {

        teamMemberLowService.getTeamMemberByTeamIdAndMemberId(articleCreateRequest.teamId(), member);

        Team team = teamLowService.getTeamByTeamId(articleCreateRequest.teamId());

        Article article = new Article(
                team,
                member,
                articleCreateRequest.articleTitle(),
                articleCreateRequest.articleBody(),
                ArticleCategory.NORMAL,
                articleCreateRequest.isVisible()
        );

        return articleLowService.save(article);
    }

    public void deleteArticle(Long articleId, Member member) {
        Article article = articleLowService.findById(articleId);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        articleFeedbackLowService.deleteByArticleId(articleId);
        articleImageLowService.deleteByArticleId(articleId);
        articleLowService.delete(article);
    }

    public void updateArticle(Long articleId, ArticleUpdateRequest articleUpdateRequest, Member member) {
        Article article = articleLowService.findByIdWithImages(articleId);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        article.updateArticle(articleUpdateRequest.articleTitle(), articleUpdateRequest.articleBody(), articleUpdateRequest.isVisible());
    }

    @Transactional(readOnly = true)
    public Page<ArticleParticularDateResponse>  getArticlesByTeamAndDate(Long teamId, LocalDate date, Member member, Pageable pageable) {
        Page<Article> articlePage = articleLowService.findVisibleByTeamIdAndDateWithMember(teamId, date, pageable);

        boolean isMyTeam = teamMemberLowService.existsByTeamIdAndMemberId(teamId, member);

        return articlePage.map(article ->
                ArticleParticularDateResponse.of(article, member, isMyTeam));
    }

    public Article saveTeamSubject(
            Member member,
            Long teamId,
            TeamSubjectRequest teamSubjectRequest
    ){
        Team team = teamLowService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        LocalDate selectedDate = LocalDate.parse(teamSubjectRequest.selectedDate());
        checkSubjectExists(team, selectedDate);

        Article subject = createArticle(member, team, teamSubjectRequest, selectedDate);
        Article savedSubject = articleLowService.save(subject);
        return savedSubject;
    }

    @Transactional(readOnly = true)
    public Article getTeamSubjectByDate(Long teamId, LocalDate date){
        teamLowService.getTeamByTeamId(teamId);
        return articleLowService.findTeamSubjectByTeamAndSelectedDate(teamId, date, getSubjectCategories())
                .orElse(null);
    }

    public void deleteTeamSubjectByArticleId(Member member, Long teamId, Long articleId){
        Team team = teamLowService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        if(!articleLowService.existsById(articleId)){
            throw new ArticleNotFoundException();
        }

        articleLowService.deleteById(articleId);
    }

    public Article updateTeamSubjectByArticleId(Member member, Long teamId, Long articleId, TeamSubjectUpdateRequest teamSubjectUpdateRequest){
        Team team = teamLowService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        Article article = articleLowService.findById(articleId);

        article.updateSubject(teamSubjectUpdateRequest.articleTitle(), teamSubjectUpdateRequest.articleBody(), teamSubjectUpdateRequest.articleCategory());

        return article;
    }

    @Transactional(readOnly = true)
    public TeamPageResponse getSubjectArticlesUsingCalender(Member member, Long teamId, CalenderSubjectRequest calenderSubjectRequest){
        Team team = teamLowService.getTeamByTeamId(teamId);

        List<Article> subjectArticles = articleLowService.findSubjectArticlesByTeamIdAndYearAndMonth(teamId, calenderSubjectRequest);
        boolean isTeamManager = teamMemberLowService.checkMemberIsTeamManager(teamId, member);
        return TeamPageResponse.from(MyTeamResponse.of(team), isTeamManager, subjectArticles);
    }

    private void validateTeamManager(Member member, Team team) {
        if(!teamMemberLowService.checkMemberIsTeamManager(team.getTeamId(), member)){
            throw new TeamManagerInvalidException();
        }
    }

    private void checkSubjectExists(Team team, LocalDate selectedDate) {
        if(articleLowService.existsByTeamAndSelectedDateAndArticleCategoryIn(team, selectedDate, getSubjectCategories())){
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
                .isVisible(true)
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
                .isVisible(true)
                .build();

        articleLowService.save(article);
        return content.title();
    }

    @Transactional(readOnly = true)
    public Article validateAndGetArticle(Long articleId, Member member) {
        Article article = articleLowService.findById(articleId);

        if (!article.isAuthor(member)) throw new UnauthorizedActionException();

        return article;
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> getMyArticleResponseUsingPaging(Long teamId, Member member, Pageable pageable) {
        Page<Article> myArticles = articleLowService.getMyVisibleArticlesUsingPaging(member.getId(), teamId, pageable);

        return myArticles.map(ArticleResponse::new);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getAllArticleResponseByTeam(Long teamId) {
        List<Article> articles = articleLowService.getAllVisibleArticlesByTeam(teamId);
        return articles.stream()
                .map(ArticleResponse::new)
                .toList();
    }
}
