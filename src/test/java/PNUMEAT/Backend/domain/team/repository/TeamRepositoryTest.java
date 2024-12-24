package PNUMEAT.Backend.domain.team.repository;

import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.auth.repository.MemberRepository;
import PNUMEAT.Backend.domain.team.entity.Team;
import PNUMEAT.Backend.domain.team.enums.Topic;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import PNUMEAT.Backend.domain.teamMember.repository.TeamMemberRepository;
import PNUMEAT.Backend.global.security.configuration.JpaAuditingConfiguration;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@Import(JpaAuditingConfiguration.class)
class TeamRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TeamMemberRepository teamMemberRepository;

    @Autowired
    EntityManager entityManager;

    private int lastRegisteredIndex = 20;
    private Team team1;
    private Team team2;

    @BeforeEach
    void setUp(){
        Member member = new Member("TEST", "TEST", "ROLE_USER");
        memberRepository.save(member);

        for(int i=1; i<=lastRegisteredIndex; i++){
            Team team = Team.builder()
                    .teamName("팀"+i)
                    .teamExplain("팀"+i+" 입니다")
                    .teamTopic(Topic.CODINGTEST)
                    .maxParticipant(i)
                    .teamPassword("1111")
                    .teamManager(member)
                    .build();

            teamRepository.save(team);
        }
    }

    @Test
    @DisplayName("전체 조회 페이지네이션 테스트")
    void findAll_페이지네이션_테스트() {
        // given
        Pageable pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdDate"));

        // when
        Page<Team> teamWithPage = teamRepository.findAll(pageable);
        List<Team> teams = teamWithPage.stream().toList();

        // then
        assertAll(
                () -> assertThat(teamWithPage.getTotalPages()).isEqualTo(4),
                () -> assertThat(teamWithPage.getNumberOfElements()).isEqualTo(6),
                () -> assertThat(teams.getFirst().getTeamName()).isEqualTo("팀"+lastRegisteredIndex),
                () -> assertThat(teams.getFirst().getMaxParticipant()).isEqualTo(lastRegisteredIndex)
        );
    }
}