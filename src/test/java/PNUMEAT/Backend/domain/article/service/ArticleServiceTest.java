package PNUMEAT.Backend.domain.article.service;

import PNUMEAT.Backend.domain.article.dto.request.TeamSubjectRequest;
import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.article.repository.ArticleImageRepository;
import PNUMEAT.Backend.domain.article.repository.ArticleRepository;
import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.enums.Topic;
import PNUMEAT.Backend.domain.team.repository.TeamRepository;
import PNUMEAT.Backend.domain.teamMember.repository.TeamMemberRepository;
import PNUMEAT.Backend.global.error.Team.TeamManagerInvalidException;
import PNUMEAT.Backend.global.error.Team.TeamMemberInvalidException;
import PNUMEAT.Backend.global.error.Team.TeamNotFoundException;
import PNUMEAT.Backend.global.error.articles.SubjectDuplicatedException;
import PNUMEAT.Backend.global.images.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static PNUMEAT.Backend.domain.article.enums.ArticleCategory.STUDY;
import static PNUMEAT.Backend.domain.article.enums.ArticleCategory.getSubjectCategories;
import static PNUMEAT.Backend.global.error.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock
    private ImageService imageService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleImageRepository articleImageRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private ArticleService articleService;

    private Member testMember;
    private Team testTeam;
    private Article savedArticle;
    private MockMultipartFile mockImage;
    private String imageUrl;
    private String selectedDateStr;
    private String articleCategoryStr;
    private String articleTitle;
    private String articleBody;

    @BeforeEach
    void setup() {
        testMember = new Member("test@example.com", "testuser", "USER");
        testTeam = new Team("Test Team", Topic.STUDY, "This is a test team.", 10, "password", testMember);
        mockImage = new MockMultipartFile("image", "image.jpg", "image/jpeg", "mock-image-content".getBytes());
        imageUrl = "http://mock-s3-url.com/image.jpg";
        selectedDateStr = "2024-12-02";
        articleCategoryStr = "스터디";
        articleTitle = "글제목";
        articleBody = "글내용";

        LocalDate selectedDate = LocalDate.parse(selectedDateStr);

        savedArticle = Article.builder()
                .team(testTeam)
                .member(testMember)
                .articleTitle(articleTitle)
                .articleBody(articleBody)
                .articleCategory(STUDY)
                .selectedDate(selectedDate)
                .build();
    }

    @Test
    @DisplayName("팀 주제 저장할 때 이미지가 있는 경우 테스트")
    void saveTeamSubject_팀_주제_정상_저장_이미지있음() {
        // given
        Long teamId = testTeam.getTeamId();
        LocalDate selectedDate = LocalDate.parse(selectedDateStr);

        TeamSubjectRequest teamSubjectRequest = new TeamSubjectRequest(articleCategoryStr, selectedDateStr, articleTitle, articleBody);

        given(teamRepository.findById(teamId)).willReturn(Optional.of(testTeam));
        given(articleRepository.existsByTeamAndSelectedDateAndArticleCategoryIn(testTeam, selectedDate, getSubjectCategories())).willReturn(false);
        given(articleRepository.save(any(Article.class))).willReturn(savedArticle);
        given(imageService.articleImageUpload(mockImage)).willReturn(imageUrl);
        given(articleImageRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Article savedTeamSubject = articleService.saveTeamSubject(testMember,teamId,teamSubjectRequest,mockImage);

        // then
        assertThat(savedTeamSubject.getArticleCategory().getName()).isEqualTo(teamSubjectRequest.articleCategory());
        assertThat(savedTeamSubject.getSelectedDate().toString()).isEqualTo(teamSubjectRequest.selectedDate());
        assertThat(savedTeamSubject.getArticleTitle()).isEqualTo(teamSubjectRequest.articleTitle());
        assertThat(savedTeamSubject.getArticleBody()).isEqualTo(teamSubjectRequest.articleBody());
        assertThat(savedTeamSubject.getImages().get(0).getImageUrl()).isEqualTo(imageUrl);
        assertThat(savedTeamSubject.getTeam().getTeamId()).isEqualTo(teamId);
        assertThat(savedTeamSubject.getMember().getUuid()).isEqualTo(testMember.getUuid());
    }

    @Test
    @DisplayName("팀 주제를 저장할 때 이미지가 없는 경우 테스트")
    void saveTeamSubject_팀_주제_정상_저장_이미지없음() {
        // given
        Long teamId = testTeam.getTeamId();
        LocalDate selectedDate = LocalDate.parse(selectedDateStr);

        TeamSubjectRequest teamSubjectRequest = new TeamSubjectRequest(articleCategoryStr, selectedDateStr, articleTitle, articleBody);

        given(teamRepository.findById(teamId)).willReturn(Optional.of(testTeam));
        given(articleRepository.existsByTeamAndSelectedDateAndArticleCategoryIn(testTeam, selectedDate, getSubjectCategories())).willReturn(false);
        given(articleRepository.save(any(Article.class))).willReturn(savedArticle);

        // when
        Article savedTeamSubject = articleService.saveTeamSubject(testMember,teamId,teamSubjectRequest,null);

        // then
        assertThat(savedTeamSubject.getArticleCategory().getName()).isEqualTo(teamSubjectRequest.articleCategory());
        assertThat(savedTeamSubject.getSelectedDate().toString()).isEqualTo(teamSubjectRequest.selectedDate());
        assertThat(savedTeamSubject.getArticleTitle()).isEqualTo(teamSubjectRequest.articleTitle());
        assertThat(savedTeamSubject.getArticleBody()).isEqualTo(teamSubjectRequest.articleBody());
        assertThat(savedTeamSubject.getImages().size()).isEqualTo(0);
        assertThat(savedTeamSubject.getTeam().getTeamId()).isEqualTo(teamId);
        assertThat(savedTeamSubject.getMember().getUuid()).isEqualTo(testMember.getUuid());
    }

    @Test
    @DisplayName("팀 주제 저장할 때 팀이 존재하지 않는 경우, team not found exception 발생")
    void saveTeamSubject_팀_주제_팀이_존재하지_않는_경우() {
        // given
        Long teamId = testTeam.getTeamId();

        TeamSubjectRequest teamSubjectRequest = new TeamSubjectRequest(articleCategoryStr, selectedDateStr, articleTitle, articleBody);

        given(teamRepository.findById(teamId)).willReturn(Optional.empty());

        // expected
       assertThatThrownBy(()->articleService.saveTeamSubject(testMember,teamId,teamSubjectRequest,mockImage))
               .isInstanceOf(TeamNotFoundException.class)
               .hasMessage(TEAM_NOT_FOUND_ERROR.getMessage());
    }

    @Test
    @DisplayName("팀 주제 저장할 때 팀 매니저가 아닌 경우, team manager invalid exception 발생")
    void saveTeamSubject_팀_매니저가_아닌_경우() {
        // given
        Long teamId = testTeam.getTeamId();
        Member invalidMember = new Member("invalid", "invalid", "USER");

        TeamSubjectRequest teamSubjectRequest = new TeamSubjectRequest(articleCategoryStr, selectedDateStr, articleTitle, articleBody);

        given(teamRepository.findById(teamId)).willReturn(Optional.of(testTeam));

        // expected
        assertThatThrownBy(()->articleService.saveTeamSubject(invalidMember,teamId,teamSubjectRequest,mockImage))
                .isInstanceOf(TeamManagerInvalidException.class)
                .hasMessage(TEAM_MANAGER_INVALID_ERROR.getMessage());
    }

    @Test
    @DisplayName("팀 주제를 저장할 때 해당 날에 이미 주제가 등록되어 있는 경우, subject duplicated exception 발생")
    void saveTeamSubject_해당일에_주제가_이미_등록되어있는_경우() {
        // given
        Long teamId = testTeam.getTeamId();
        LocalDate selectedDate = LocalDate.parse(selectedDateStr);

        TeamSubjectRequest teamSubjectRequest = new TeamSubjectRequest(articleCategoryStr, selectedDateStr, articleTitle, articleBody);
        given(teamRepository.findById(teamId)).willReturn(Optional.of(testTeam));
        given(articleRepository.existsByTeamAndSelectedDateAndArticleCategoryIn(testTeam, selectedDate, getSubjectCategories())).willReturn(true);

        // expected
        assertThatThrownBy(()->articleService.saveTeamSubject(testMember,teamId,teamSubjectRequest,mockImage))
                .isInstanceOf(SubjectDuplicatedException.class)
                .hasMessage(SUBJECT_DUPLICATED_ERROR.getMessage());
    }

    @Test
    @DisplayName("특정 날짜로 팀 주제 가져올 때 주제가 있으면, 주제를 반환")
    void getTeamSubjectByDate_정상_주제_있음() {
        // given
        Long teamId = testTeam.getTeamId();
        LocalDate selectedDate = LocalDate.parse(selectedDateStr);

        given(teamRepository.findById(teamId)).willReturn(Optional.of(testTeam));
        given(teamMemberRepository.existsByTeamAndMember(testTeam, testMember)).willReturn(true);
        given(articleRepository.findTeamSubjectByTeamAndSelectedDate(teamId, selectedDate, getSubjectCategories()))
                .willReturn(Optional.of(savedArticle));

        // when
        Article foundSubject = articleService.getTeamSubjectByDate(testMember, teamId, selectedDate);

        // then
        assertThat(foundSubject.getArticleId()).isEqualTo(savedArticle.getArticleId());
        assertThat(foundSubject.getSelectedDate()).isEqualTo(selectedDateStr);
    }

    @Test
    @DisplayName("특정 날짜로 팀 주제 가져올 때 주제가 없으면, null 반환")
    void getTeamSubjectByDate_정상_주제_없음() {
        // given
        Long teamId = testTeam.getTeamId();
        LocalDate selectedDate = LocalDate.parse("2008-05-11");

        given(teamRepository.findById(teamId)).willReturn(Optional.of(testTeam));
        given(teamMemberRepository.existsByTeamAndMember(testTeam, testMember)).willReturn(true);
        given(articleRepository.findTeamSubjectByTeamAndSelectedDate(teamId, selectedDate, getSubjectCategories()))
                .willReturn(Optional.empty());

        // when
        Article foundSubject = articleService.getTeamSubjectByDate(testMember, teamId, selectedDate);

        // then
        assertThat(foundSubject).isEqualTo(null);
    }

    @Test
    @DisplayName("특정 날짜로 팀 주제 가져올 때 팀이 존재하지 않는 경우, team not found exception 발생")
    void getTeamSubjectByDate_팀이_존재하지_않는_경우() {
        // given
        Long teamId = testTeam.getTeamId();
        LocalDate selectedDate = LocalDate.parse(selectedDateStr);

        given(teamRepository.findById(teamId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(()->articleService.getTeamSubjectByDate(testMember, teamId, selectedDate))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage(TEAM_NOT_FOUND_ERROR.getMessage());
    }

    @Test
    @DisplayName("특정 날짜로 팀 주제 가져올 때 팀원이 아닌 경우, team member invalid exception 발생")
    void getTeamSubjectByDate_팀원이_아닌_경우() {
        // given
        Long teamId = testTeam.getTeamId();
        LocalDate selectedDate = LocalDate.parse(selectedDateStr);

        given(teamRepository.findById(teamId)).willReturn(Optional.of(testTeam));
        given(teamMemberRepository.existsByTeamAndMember(testTeam, testMember)).willReturn(false);

        // expected
        assertThatThrownBy(()->articleService.getTeamSubjectByDate(testMember, teamId, selectedDate))
                .isInstanceOf(TeamMemberInvalidException.class)
                .hasMessage(TEAM_MEMBER_INVALID_ERROR.getMessage());
    }
}
