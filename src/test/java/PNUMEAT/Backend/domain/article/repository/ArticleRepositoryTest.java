package PNUMEAT.Backend.domain.article.repository;

import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.article.entity.ArticleImage;
import PNUMEAT.Backend.domain.article.enums.ArticleCategory;
import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.auth.repository.MemberRepository;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.enums.Topic;
import PNUMEAT.Backend.domain.team.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static PNUMEAT.Backend.domain.article.enums.ArticleCategory.getSubjectCategories;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EntityManager em;

    private Member testMember;
    private Team testTeam;
    private Article testArticle;
    private  Article subjectArticle1;

    @BeforeEach
    void setup() {
        // Create and save a test Member
        testMember = memberRepository.save(new Member("test@example.com", "testuser", "USER"));

        // Create and save a test Team
        testTeam = teamRepository.save(
                new Team("Test Team", Topic.STUDY, "This is a test team.", 10, "password", testMember)
        );

        // Create and save a test Article
        testArticle = articleRepository.save(
                new Article(testTeam, testMember, "Test Title", "Test Body", ArticleCategory.NORMAL, new ArrayList<>())
        );

        subjectArticle1 = new Article(
                testTeam,
                testMember,
                "test1",
                "test1",
                ArticleCategory.STUDY_PREVIEW,
                LocalDate.parse("2024-12-11")
        );

        Article subjectArticle2 = new Article(
                testTeam,
                testMember,
                "test2",
                "test2",
                ArticleCategory.STUDY,
                LocalDate.parse("2024-12-12")
        );

        Article subjectArticle3 = new Article(
                testTeam,
                testMember,
                "test3",
                "test3",
                ArticleCategory.STUDY_REVIEW,
                LocalDate.parse("2024-12-13")
        );

        ArticleImage testImage = new ArticleImage("http://test-image-url.com", testArticle);
        testArticle.addImage(testImage);
        articleRepository.save(testArticle);
        articleRepository.save(subjectArticle1);
        articleRepository.save(subjectArticle2);
        articleRepository.save(subjectArticle3);

        em.clear();
    }

    @Test
    @DisplayName("게시글 ID로 게시글 조회")
    void findByArticleId_shouldReturnArticle() {
        Optional<Article> article = articleRepository.findByArticleId(testArticle.getArticleId());

        assertThat(article).isPresent();
        assertThat(article.get().getArticleTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("회원 ID로 게시글 및 이미지 조회")
    void findByMemberIdWithImages_shouldReturnArticlesWithImages() {
        List<Article> articles = articleRepository.findByMemberIdWithImages(testMember.getId());

        assertThat(articles).hasSize(1);
        Article retrievedArticle = articles.get(0);
        assertThat(retrievedArticle.getArticleTitle()).isEqualTo("Test Title");
        assertThat(retrievedArticle.getImages()).hasSize(1);
        assertThat(retrievedArticle.getImages().get(0).getImageUrl()).isEqualTo("http://test-image-url.com");
    }

    @Test
    @DisplayName("팀 ID로 게시글 및 이미지 조회")
    void findByTeamTeamIdWithImages_shouldReturnArticlesWithImages() {
        List<Article> articles = articleRepository.findByTeamTeamIdWithImages(testTeam.getTeamId());

        assertThat(articles).hasSize(1);
        Article retrievedArticle = articles.get(0);
        assertThat(retrievedArticle.getArticleTitle()).isEqualTo("Test Title");
        assertThat(retrievedArticle.getImages()).hasSize(1);
        assertThat(retrievedArticle.getImages().get(0).getImageUrl()).isEqualTo("http://test-image-url.com");
    }

    @Test
    @DisplayName("게시글 ID로 게시글 및 이미지 조회")
    void findByIdWithImages_shouldReturnArticleWithImages() {
        Optional<Article> article = articleRepository.findByIdWithImages(testArticle.getArticleId());

        assertThat(article).isPresent();
        assertThat(article.get().getArticleTitle()).isEqualTo("Test Title");
        assertThat(article.get().getImages()).hasSize(1);
        assertThat(article.get().getImages().get(0).getImageUrl()).isEqualTo("http://test-image-url.com");
    }

    @Test
    @DisplayName("팀 ID와 날짜로 게시글 페이징 조회")
    void findByTeamIdAndDate_shouldReturnPagedArticles() {
        LocalDate today = LocalDate.now();
        Page<Article> articles = articleRepository.findByTeamIdAndDate(
                testTeam.getTeamId(), today, PageRequest.of(0, 10)
        );

        assertThat(articles.getContent()).hasSize(1);
        Article retrievedArticle = articles.getContent().get(0);
        assertThat(retrievedArticle.getArticleTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("팀ID와 날짜 정보릃 활용하여 주제 게시글 조회")
    void 팀_ID와_날짜_정보를_활용하여_주제_게시글_조회_테스트(){
        //given
        LocalDate date = LocalDate.parse("2024-12-13");

        //when
        List<Article> subjectArticles = articleRepository.findSubjectArticlesByTeamIdAndYearAndMonth(
                testTeam.getTeamId(),
                date.getYear(),
                date.getMonth().getValue(),
                ArticleCategory.getSubjectCategories()
        );

        //expected
        assertThat(subjectArticles.size()).isEqualTo(3);
        assertThat(subjectArticles.get(0).getArticleCategory()).isEqualTo(ArticleCategory.STUDY_PREVIEW);
        assertThat(subjectArticles.get(1).getArticleCategory()).isEqualTo(ArticleCategory.STUDY);
        assertThat(subjectArticles.get(2).getArticleCategory()).isEqualTo(ArticleCategory.STUDY_REVIEW);
    }

    @Test
    @DisplayName("팀과 설정 날짜로 팀 주제 찾기 - 주제가 있는 경우")
    void findTeamSubjectByTeamAndSelectedDate_주제가_있는_경우() {
        // given
        LocalDate selectedDate = LocalDate.parse("2024-12-11");

        // when
        Optional<Article> subjectArticle = articleRepository.findTeamSubjectByTeamAndSelectedDate(
                testTeam.getTeamId(),
                selectedDate,
                ArticleCategory.getSubjectCategories()
        );

        // then
        assertThat(subjectArticle.isPresent()).isEqualTo(true);
        assertThat(subjectArticle.get().getArticleId()).isEqualTo(subjectArticle1.getArticleId());
        assertThat(subjectArticle.get().getTeam().getTeamId()).isEqualTo(testTeam.getTeamId());
        assertThat(subjectArticle.get().getSelectedDate()).isEqualTo(subjectArticle.get().getSelectedDate());
    }

    @Test
    @DisplayName("팀과 설정 날짜로 팀 주제 찾기 - 주제가 없는 경우")
    void findTeamSubjectByTeamAndSelectedDate_주제가_없는_경우() {
        // given
        LocalDate selectedDate = LocalDate.parse("2024-12-09");

        // when
        Optional<Article> subjectArticle = articleRepository.findTeamSubjectByTeamAndSelectedDate(
                testTeam.getTeamId(),
                selectedDate,
                ArticleCategory.getSubjectCategories()
        );

        // then
        assertThat(subjectArticle.isEmpty()).isEqualTo(true);
    }
}