package site.codemonster.comon.domain.article.service;


import site.codemonster.comon.domain.article.dto.request.*;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.enums.ArticleCategory;
import site.codemonster.comon.domain.article.repository.ArticleImageRepository;
import site.codemonster.comon.domain.article.repository.ArticleRepository;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.dto.response.TeamPageResponse;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.service.TeamService;
import site.codemonster.comon.domain.teamMember.service.TeamMemberService;
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
@Transactional(readOnly = true)
public class ArticleService {

    private final TeamService teamService;
    private final TeamMemberService teamMemberService;
    private final ArticleRepository articleRepository;
    private final ArticleImageRepository articleImageRepository;

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

        Article savedArticle = articleRepository.save(article);

        return savedArticle;
    }

    @Transactional
    public Page<Article> getMyArticlesUsingPaging(Long memberId, Long teamId, Pageable pageable){
        return articleRepository.findArticleByMemberIdAndByTeamIdUsingPage(memberId, teamId, pageable);
    }

    @Transactional
    public List<Article> getAllArticlesByTeam(Long teamId){
        return articleRepository.findByTeamTeamIdWithImages(teamId);
    }

    @Transactional
    public void deleteArticle(Long articleId, Member member) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        articleImageRepository.deleteByArticleId(articleId);
        articleRepository.delete(article);
    }

    @Transactional
    public void updateArticle(Long articleId, ArticleUpdateRequest articleUpdateRequest, Member member) {
        Article article = articleRepository.findByIdWithImages(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        if (!article.isAuthor(member)) {
            throw new UnauthorizedActionException();
        }

        article.updateArticle(articleUpdateRequest.articleTitle(), articleUpdateRequest.articleBody());
    }

    public Page<Article> getArticlesByTeamAndDate(Long teamId, LocalDate date, Pageable pageable) {
        return articleRepository.findByTeamIdAndDateWithMember(teamId, date, pageable);
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
        Article savedSubject = articleRepository.save(subject);
        return savedSubject;
    }

    public Article getTeamSubjectByDate(Long teamId, LocalDate date){
        teamService.getTeamByTeamId(teamId);
        return articleRepository.findTeamSubjectByTeamAndSelectedDate(teamId, date, getSubjectCategories())
                .orElse(null);
    }

    @Transactional
    public void deleteTeamSubjectByArticleId(Member member, Long teamId, Long articleId){
        Team team = teamService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        if(!articleRepository.existsById(articleId)){
            throw new ArticleNotFoundException();
        }

        articleRepository.deleteById(articleId);
    }

    @Transactional
    public Article updateTeamSubjectByArticleId(Member member, Long teamId, Long articleId, TeamSubjectUpdateRequest teamSubjectUpdateRequest){
        Team team = teamService.getTeamByTeamId(teamId);
        validateTeamManager(member, team);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(ArticleNotFoundException::new);

        article.updateSubject(teamSubjectUpdateRequest.articleTitle(), teamSubjectUpdateRequest.articleBody(), teamSubjectUpdateRequest.articleCategory());

        return article;
    }

    public TeamPageResponse getSubjectArticlesUsingCalender(Member member, Long teamId, CalenderSubjectRequest calenderSubjectRequest){
        Team team = teamService.getTeamByTeamId(teamId);

        List<Article> subjectArticles = articleRepository.findSubjectArticlesByTeamIdAndYearAndMonth(teamId, calenderSubjectRequest.year(), calenderSubjectRequest.month(), getSubjectCategories());
        boolean isTeamManager = teamMemberService.checkMemberIsTeamManager(teamId, member);
        return TeamPageResponse.from(team, isTeamManager, subjectArticles);
    }

    private void validateTeamManager(Member member, Team team) {
        if(!teamMemberService.checkMemberIsTeamManager(team.getTeamId(), member)){
            throw new TeamManagerInvalidException();
        }
    }

    private void checkSubjectExists(Team team, LocalDate selectedDate) {
        if(articleRepository.existsByTeamAndSelectedDateAndArticleCategoryIn(team, selectedDate, getSubjectCategories())){
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
}
