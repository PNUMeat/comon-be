package PNUMEAT.Backend.domain.teamMember.repository;

import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.auth.repository.MemberRepository;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.enums.Topic;
import PNUMEAT.Backend.domain.team.repository.TeamRepository;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class TeamMemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private EntityManager entityManager;

    private Member teamManager;
    private Member memberHasTeam;
    private Member memberHasNotTeam;
    private Team team;
    private Team team2;
    private TeamMember teamMember;
    private TeamMember teamMember1;
    private TeamMember teamMember2;


    @BeforeEach
    void setUp(){
        teamManager = new Member("test1@naver.com", "1", "ROLE_USER");
        memberHasTeam = new Member("test2@naver.com", "2", "ROLE_USER");
        memberHasNotTeam = new Member("test3@naver.com", "3", "ROLE_USER");

        memberRepository.save(teamManager);
        memberRepository.save(memberHasTeam);
        memberRepository.save(memberHasNotTeam);

        team = new Team("TEST", Topic.CODINGTEST, "TEST", 10, "1111", teamManager);
        team2 = new Team("TEST2", Topic.CODINGTEST, "TEST2", 2, "2222", teamManager);

        teamRepository.save(team);
        teamRepository.save(team2);

        teamMember = new TeamMember(team, teamManager);
        teamMember1 = new TeamMember(team, memberHasTeam);
        teamMember2 = new TeamMember(team2, memberHasTeam);

        teamMemberRepository.save(teamMember);
        teamMemberRepository.save(teamMember1);
        teamMemberRepository.save(teamMember2);

        entityManager.clear();
    }

    @Test
    @DisplayName("멤버 아이디로 최신순으로 정렬된 TeamMember 반환하기")
    void findByMemberIdOrderByTeamInfoIdDesc_테스트() {
        // given
        Long memberId = memberHasTeam.getId();

        // when
        List<TeamMember> teamMembers = teamMemberRepository.findByMemberIdOrderByTeamInfoIdDesc(memberId);

        // then
        assertThat(teamMembers.size()).isEqualTo(2);
        assertThat(teamMembers.get(0).getTeam().getTeamMembers().size()).isEqualTo(1);
    }
}