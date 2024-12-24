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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
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

    @BeforeEach
    void setup() {
        testMember = memberRepository.save(new Member("test@example.com", "testuser", "USER"));

        testTeam = teamRepository.save(
            new Team("Test Team", Topic.STUDY, "This is a test team.", 10, "password", testMember)
        );

        for (int i = 1; i <= 7; i++) {
            Article article = new Article(
                testTeam,
                testMember,
                "Test Title" + i,
                "Test Body" + i,
                ArticleCategory.NORMAL,
                new ArrayList<>()
            );
            if (i == 1) {
                ArticleImage testImage = new ArticleImage("http://test-image-url.com", article);
                article.addImage(testImage);
            }
            articleRepository.save(article);
        }

        Article subjectArticle1 = new Article(
            testTeam,
            testMember,
            "Subject Title 1",
            "Subject Body 1",
            ArticleCategory.STUDY_PREVIEW,
            LocalDate.parse("2024-12-11")
        );

        articleRepository.save(subjectArticle1);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("회원 ID로 게시글 및 이미지 조회")
    void findByMemberIdWithImages_shouldReturnArticlesWithImages() {
        List<Article> articles = articleRepository.findByMemberIdWithImages(testMember.getId());

        assertThat(articles).hasSize(7);
        assertThat(articles.get(0).getImages()).hasSize(1);
        assertThat(articles.get(0).getImages().get(0).getImageUrl()).isEqualTo("http://test-image-url.com");
    }

    @Test
    @DisplayName("팀 ID로 게시글 및 이미지 조회")
    void findByTeamTeamIdWithImages_shouldReturnArticlesWithImages() {
        List<Article> articles = articleRepository.findByTeamTeamIdWithImages(testTeam.getTeamId());

        assertThat(articles).hasSize(7);
    }

    @Test
    @DisplayName("팀 ID와 날짜로 게시글 페이징 조회")
    void findByTeamIdAndDate_shouldReturnPagedArticles() {
        LocalDate today = LocalDate.now();
        Page<Article> articles = articleRepository.findByTeamIdAndDateWithMember(
            testTeam.getTeamId(), today, PageRequest.of(0, 5)
        );

        assertThat(articles.getContent()).hasSize(5);
        assertThat(articles.getTotalElements()).isEqualTo(7);
        assertThat(articles.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("팀과 설정 날짜로 팀 주제 찾기 - 주제가 있는 경우")
    void findTeamSubjectByTeamAndSelectedDate_withSubject() {
        LocalDate selectedDate = LocalDate.parse("2024-12-11");

        Optional<Article> subjectArticle = articleRepository.findTeamSubjectByTeamAndSelectedDate(
            testTeam.getTeamId(),
            selectedDate,
            ArticleCategory.getSubjectCategories()
        );

        assertThat(subjectArticle).isPresent();
        assertThat(subjectArticle.get().getArticleTitle()).isEqualTo("Subject Title 1");
    }

    @Test
    @DisplayName("팀과 설정 날짜로 팀 주제 찾기 - 주제가 없는 경우")
    void findTeamSubjectByTeamAndSelectedDate_withoutSubject() {
        LocalDate selectedDate = LocalDate.parse("2024-12-09");

        Optional<Article> subjectArticle = articleRepository.findTeamSubjectByTeamAndSelectedDate(
            testTeam.getTeamId(),
            selectedDate,
            ArticleCategory.getSubjectCategories()
        );

        assertThat(subjectArticle).isEmpty();
    }
}
