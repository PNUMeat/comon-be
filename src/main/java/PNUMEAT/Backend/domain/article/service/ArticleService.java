
package PNUMEAT.Backend.domain.article.service;


import PNUMEAT.Backend.domain.article.dto.request.ArticleRequest;
import PNUMEAT.Backend.domain.article.dto.request.CalenderSubjectRequest;
import PNUMEAT.Backend.domain.article.dto.request.TeamSubjectRequest;
import PNUMEAT.Backend.domain.article.dto.response.CalenderSubjectResponse;
import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.article.entity.ArticleImage;
import PNUMEAT.Backend.domain.article.enums.ArticleCategory;
import PNUMEAT.Backend.domain.article.repository.ArticleImageRepository;
import PNUMEAT.Backend.domain.article.repository.ArticleRepository;
import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.auth.repository.MemberRepository;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.repository.TeamRepository;
import PNUMEAT.Backend.domain.teamMember.repository.TeamMemberRepository;
import PNUMEAT.Backend.global.error.Member.MemberNotFoundException;
import PNUMEAT.Backend.global.error.Team.TeamManagerInvalidException;
import PNUMEAT.Backend.global.error.Team.TeamMemberInvalidException;
import PNUMEAT.Backend.global.error.Team.TeamNotFoundException;
import PNUMEAT.Backend.global.error.articles.*;
import PNUMEAT.Backend.global.images.ImageService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static PNUMEAT.Backend.domain.article.enums.ArticleCategory.fromName;
import static PNUMEAT.Backend.domain.article.enums.ArticleCategory.getSubjectCategories;


@Service
@Slf4j
public class ArticleService {
    private final ImageService imageService;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final ArticleImageRepository articleImageRepository;

    private final TeamMemberRepository teamMemberRepository;

    public ArticleService(ImageService imageService, ArticleRepository articleRepository,
                          MemberRepository memberRepository, TeamRepository teamRepository,
                          ArticleImageRepository articleImageRepository, TeamMemberRepository teamMemberRepository) {
        this.imageService = imageService;
        this.articleRepository = articleRepository;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
        this.articleImageRepository = articleImageRepository;
        this.teamMemberRepository = teamMemberRepository;
    }


    @Transactional
    public void save(Long memberId, ArticleRequest articleRequest, MultipartFile images){

        Member member = memberRepository.findById(memberId).orElseThrow(
                MemberNotFoundException::new
        );

        Team team = teamRepository.findById(articleRequest.teamId()).orElseThrow(
                TeamNotFoundException::new
        );

        Article article = new Article(team,member,articleRequest.articleTitle()
                ,articleRequest.articleBody(), ArticleCategory.NORMAL, new ArrayList<>());

        articleRepository.save(article);

        handleImageUpload(article, images);
    }

    @Transactional
    public List<Article> getMyArticles(Long userId){
        return articleRepository.findByMemberIdWithImages(userId);
    }

    @Transactional
    public List<Article> getArticlesByTeam(Long teamId){
        return articleRepository.findByTeamTeamIdWithImages(teamId);
    }

    @Transactional
    public Article getArticleById(Long articleId){
        return articleRepository.findByIdWithImages(articleId).orElseThrow(
                ArticleNotFoundException::new
        );
    }

    @Transactional
    public void deleteArticle(Long articleId,Long memberId) {
        Article article = articleRepository.findById(articleId).orElseThrow(
                ArticleNotFoundException::new
        );

        if (!article.getMember().getId().equals(memberId)) {
            throw new UnauthorizedActionException();
        }

        articleRepository.delete(article);
    }

    @Transactional
    public void updateArticle(Long articleId, ArticleRequest articleRequest, MultipartFile image, Long memberId) {
        Article article = articleRepository.findByIdWithImages(articleId).orElseThrow(
                ArticleNotFoundException::new
        );

        if (!article.getMember().getId().equals(memberId)) {
            throw new UnauthorizedActionException();
        }

        updateArticleFields(article, articleRequest);

        handleImageUpdate(article, image);
    }

    @Transactional(readOnly = true)
    public Page<Article> getArticlesByTeamAndDate(Long teamId, LocalDate date, Pageable pageable) {
        return articleRepository.findByTeamIdAndDate(teamId, date, pageable);
    }


    @Transactional(readOnly = true)
    public void isMemberInTeam(Long memberId, Long teamId) {
        if(!teamMemberRepository.existsByTeamTeamIdAndMemberId(teamId, memberId)){
            throw new MemberNotInTeamException();
        }
    }




    private void handleImageUpload(Article article, MultipartFile images) {
        if (images != null && !images.isEmpty()) {
            String imageUrl = imageService.articleImageUpload(images);

            ArticleImage articleImage = new ArticleImage(imageUrl, article);

            article.addImage(articleImage);

            articleImageRepository.save(articleImage);
        }
    }


    private void updateArticleFields(Article article, ArticleRequest articleRequest) {
        if (articleRequest.articleTitle() != null) {
            article.updateTitle(articleRequest.articleTitle());
        }
        if (articleRequest.articleBody() != null) {
            article.updateBody(articleRequest.articleBody());
        }

    }

    private void handleImageUpdate(Article article, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            ArticleImage existingImage = article.getImages().stream()
                    .findFirst()
                    .orElse(null);

            if (existingImage != null) {
                imageService.deleteImageByUrl(existingImage.getImageUrl());
                article.getImages().remove(existingImage);
                articleImageRepository.delete(existingImage);
            }

            // 2. 새 이미지 추가
            String newImageUrl = imageService.articleImageUpload(image);

            ArticleImage articleImage = new ArticleImage(newImageUrl, article);

            article.addImage(articleImage);
            articleImageRepository.save(articleImage);
        }
    }

    @Transactional
    public Article saveTeamSubject(Member member,
                                   Long teamId,
                                   TeamSubjectRequest teamSubjectRequest,
                                   MultipartFile images){

        Team team = getTeamById(teamId);

        validateTeamManager(member, team);

        LocalDate selectedDate = LocalDate.parse(teamSubjectRequest.selectedDate());
        validateSubjectNotDuplicated(team, selectedDate);

        Article subject = createArticle(member, team, teamSubjectRequest, selectedDate);

        Article savedSubject = articleRepository.save(subject);

        handleImageUpload(savedSubject, images);

        return savedSubject;
    }

    @Transactional(readOnly = true)
    public Article getTeamSubjectByDate(Member member, Long teamId, LocalDate date){
        Team team = getTeamById(teamId);

        if(!teamMemberRepository.existsByTeamAndMember(team, member)){
            throw new TeamMemberInvalidException();
        }

        return articleRepository.findTeamSubjectByTeamAndSelectedDate(teamId, date, getSubjectCategories())
                .orElse(null);
    }

    @Transactional
    public void deleteTeamSubjectByArticleId(Member member, Long teamId, Long articleId){
        Team team = getTeamById(teamId);

        validateTeamManager(member, team);

        validateArticleExists(articleId);

        articleRepository.deleteById(articleId);
    }

    @Transactional(readOnly = true)
    public CalenderSubjectResponse getSubjectArticlesUsingCalender(Member member, Long teamId, CalenderSubjectRequest calenderSubjectRequest){
        Team team = getTeamById(teamId);

        if(!teamMemberRepository.existsByTeamAndMember(team, member)){
            throw new TeamMemberInvalidException();
        }

        List<Article> subjectArticles = articleRepository.findSubjectArticlesByTeamIdAndYearAndMonth(teamId, calenderSubjectRequest.year(), calenderSubjectRequest.month(), getSubjectCategories());

        return CalenderSubjectResponse.of(team.getTeamAnnouncement(), subjectArticles);
    }

    private Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    private void validateTeamManager(Member member, Team team) {
        if(!team.isTeamManger(member)){
            throw new TeamManagerInvalidException();
        }
    }

    private void validateSubjectNotDuplicated(Team team, LocalDate selectedDate) {
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

    private void validateArticleExists(Long articleId) {
        if(articleRepository.existsById(articleId)){
            throw new ArticleNotFoundException();
        }
    }
}
