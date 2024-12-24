package PNUMEAT.Backend.domain.team.service;

import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.team.dto.request.TeamRequest;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.enums.Topic;
import PNUMEAT.Backend.domain.team.repository.TeamRepository;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import PNUMEAT.Backend.domain.teamMember.repository.TeamMemberRepository;
import PNUMEAT.Backend.global.error.ComonException;
import PNUMEAT.Backend.global.images.ImageConstant;
import PNUMEAT.Backend.global.images.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static PNUMEAT.Backend.global.error.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamService teamService;

    private Member member;
    private Team team2;
    private Team team3;
    private Team team4;
    private final String PASSWORD = "1111";

    @BeforeEach
    void setUp() {
        member = new Member("TEST", "TEST", "ROLE_USER");

        team2 = createTeam("팀2", "팀2 설명", 10, member);
        team3 = createTeam("팀3", "팀3 설명", 10, member);
        team4 = createTeam("팀4", "팀4 설명", 10, member);
    }

    @Test
    @DisplayName("팀 등록 시 teamIcon을 등록하지 않았다면 기본 이미지가 등록된다.")
    void 팀_등록_시_기본_이미지_등록_테스트() {
        //given
        TeamRequest teamRequest = new TeamRequest(
                "한글Team1",
                "이 팀은 예시 팀입니다!",
                "스터디",
                10,
                "1234"
        );
        Member member = new Member("TEST", "TEST", "ROLE_USER");
        MultipartFile teamIcon = null;

        Team team = Team.builder()
                .teamName(teamRequest.teamName())
                .teamExplain(teamRequest.teamExplain())
                .teamTopic(Topic.fromName(teamRequest.topic()))
                .maxParticipant(teamRequest.memberLimit())
                .teamPassword(teamRequest.password())
                .teamManager(member)
                .build();

        given(teamRepository.save(any(Team.class))).willReturn(team);

        TeamMember teamMember = new TeamMember(team, member);
        given(teamMemberRepository.save(any(TeamMember.class))).willReturn(teamMember);

        //when
        Team createdTeam = teamService.createTeam(teamRequest, teamIcon, member);

        //then
        assertThat(createdTeam.getTeamIconUrl()).isEqualTo(ImageConstant.DEFAULT_TEAM_IMAGE_URL);
        assertThat(createdTeam.getTeamName()).isEqualTo(teamRequest.teamName());
        assertThat(createdTeam.getTeamManager().getMemberName()).isEqualTo(member.getMemberName());
    }

    @Test
    @DisplayName("회원 프로필 등록 시 image를 등록하면 imageUrl이 변경된다.")
    void 프로필_등록_시_이미지_등록_테스트() {
        //given
        TeamRequest teamRequest = new TeamRequest(
                "한글Team1",
                "이 팀은 예시 팀입니다!",
                "스터디",
                10,
                "1234"
        );

        MultipartFile teamIcon = new MockMultipartFile("TEST",
                "TEST.png",
                MediaType.IMAGE_PNG_VALUE,
                "TEST".getBytes());

        Member member = new Member("TEST", "TEST", "ROLE_USER");

        Team team = Team.builder()
                .teamName(teamRequest.teamName())
                .teamExplain(teamRequest.teamExplain())
                .teamTopic(Topic.fromName(teamRequest.topic()))
                .maxParticipant(teamRequest.memberLimit())
                .teamPassword(teamRequest.password())
                .teamManager(member)
                .build();
        team.updateTeamIconUrl("s3://");

        given(imageService.teamImageUpload(teamIcon)).willReturn("s3://");

        given(teamRepository.save(any(Team.class))).willReturn(team);

        TeamMember teamMember = new TeamMember(team, member);
        given(teamMemberRepository.save(any(TeamMember.class))).willReturn(teamMember);

        //when
        Team createdTeam = teamService.createTeam(teamRequest, teamIcon, member);

        //then
        assertThat(createdTeam.getTeamIconUrl()).isEqualTo("s3://");
        assertThat(createdTeam.getTeamName()).isEqualTo(teamRequest.teamName());
        assertThat(createdTeam.getTeamManager().getMemberName()).isEqualTo(member.getMemberName());
    }

    @Test
    @DisplayName("페이지네이션을 적용하여 모든 팀 조회하기")
    void getAllTeams_페이지네이션_적용_시() {
        // given
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdDate"));
        List<Team> teams = Arrays.asList(team4, team3, team2);
        Page<Team> mockPage = new PageImpl<>(teams, pageable, 4);

        given(teamRepository.findAll(pageable)).willReturn(mockPage);

        // when
        Page<Team> result = teamService.getAllTeams(pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().get(0)).isEqualTo(team4);
    }

    @Test
    @DisplayName("팀 가입하기 - 정상 가입")
    void joinTeam_정상가입() {
        // given
        Long teamId = team2.getTeamId();
        TeamMember teamMember = new TeamMember(team2, member);

        given(teamRepository.findById(teamId)).willReturn(Optional.of(team2));
        given(teamMemberRepository.existsByTeamAndMember(team2,member)).willReturn(false);
        given(teamMemberRepository.save(teamMember)).willReturn(teamMember);

        // when
        TeamMember savedTamMember = teamService.joinTeam(member,PASSWORD,teamId);


        // then
        assertThat(savedTamMember.getTeam().getTeamId()).isEqualTo(team2.getTeamId());
        assertThat(savedTamMember.getMember().getUuid()).isEqualTo(member.getUuid());
    }

    @Test
    @DisplayName("팀 가입하기 - 팀이 존재하지 않는 경우")
    void joinTeam_팀이_존재하지_않는_경우() {
        // given
        Long invalidTeamId = 7L;
        given(teamRepository.findById(invalidTeamId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> teamService.joinTeam(member,PASSWORD,invalidTeamId))
                .isInstanceOf(ComonException.class)
                .hasMessage(TEAM_NOT_FOUND_ERROR.getMessage());
    }

    @Test
    @DisplayName("팀 가입하기 - 비밀번호가 틀린 경우")
    void joinTeam_비밀번호가_틀린_경우() {
        // given
        String invalidPassword = "2222";

        Long teamId = team2.getTeamId();
        given(teamRepository.findById(teamId)).willReturn(Optional.of(team2));

        // expected
        assertThatThrownBy(() -> teamService.joinTeam(member,invalidPassword,teamId))
                .isInstanceOf(ComonException.class)
                .hasMessage(TEAM_PASSWORD_INVALID.getMessage());
    }

    @Test
    @DisplayName("팀 가입하기 - 이미 동일한 팀에 가입한 경우")
    void joinTeam_이미_동일한_팀에_가입한_경우() {
        // given
        Long teamId = team2.getTeamId();
        given(teamRepository.findById(teamId)).willReturn(Optional.of(team2));
        given(teamMemberRepository.existsByTeamAndMember(team2,member)).willReturn(true);

        // expected
        assertThatThrownBy(() -> teamService.joinTeam(member,PASSWORD,teamId))
                .isInstanceOf(ComonException.class)
                .hasMessage(TEAM_ALREADY_JOIN.getMessage());
    }

    private Team createTeam(String teamName, String teamExplain, int maxParticipant, Member teamManager) {
        return Team.builder()
                .teamName(teamName)
                .teamExplain(teamExplain)
                .teamTopic(Topic.CODINGTEST)
                .maxParticipant(maxParticipant)
                .teamPassword(PASSWORD)
                .teamManager(teamManager)
                .build();
    }

    @Test
    @DisplayName("팀 공지 업데이트 하기 - 정상 업데이트")
    void updateTeamAnnouncement_정상_업데이트() {
        // given
        Long teamId = team2.getTeamId();
        String teamAnnouncement = "팀 공지 입니다.";

        given(teamRepository.findById(teamId)).willReturn(Optional.of(team2));

        // when
        Team team = teamService.updateTeamAnnouncement(member, teamAnnouncement, teamId);

        // then
        assertThat(team.getTeamId()).isEqualTo(teamId);
        assertThat(team.getTeamManager().getUuid()).isEqualTo(member.getUuid());
        assertThat(team.getTeamAnnouncement()).isEqualTo(teamAnnouncement);

    }

    @Test
    @DisplayName("팀 공지 올리기 - 팀이 존재하지 않을 때")
    void updateTeamAnnouncement_팀이_존재하지_않을_때() {
        // given
        Long teamId = team2.getTeamId();
        String teamAnnouncement = "팀 공지 입니다.";
        given(teamRepository.findById(teamId)).willReturn(Optional.empty());

        // expected
        assertThatThrownBy(() -> teamService.updateTeamAnnouncement(member, teamAnnouncement, teamId))
                .isInstanceOf(ComonException.class)
                .hasMessage(TEAM_NOT_FOUND_ERROR.getMessage());
    }

    @Test
    @DisplayName("팀 공지 올리기 - 팀의 매니저가 아닐 때")
    void updateTeamAnnouncement_팀의_매니저가_아닐_때() {
        // given
        Long teamId = team2.getTeamId();
        String teamAnnouncement = "팀 공지 입니다.";
        Member invalidMember = new Member("INVALID", "INVALID", "ROLE_USER");
        given(teamRepository.findById(teamId)).willReturn(Optional.of(team2));

        // expected
        assertThatThrownBy(() -> teamService.updateTeamAnnouncement(invalidMember, teamAnnouncement, teamId))
                .isInstanceOf(ComonException.class)
                .hasMessage(TEAM_MANAGER_INVALID_ERROR.getMessage());
    }
}