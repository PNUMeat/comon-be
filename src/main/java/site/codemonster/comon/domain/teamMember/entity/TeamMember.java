package site.codemonster.comon.domain.teamMember.entity;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.Objects;

@Getter
@Entity
public class TeamMember extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamInfoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id")
    private Team team;

    @Column(name = "is_team_manager")
    private Boolean isTeamManager;

    protected TeamMember(){}

    public TeamMember(Team team, Member member, Boolean isTeamManager) {
        this.member = member;
        this.team = team;
        this.isTeamManager = isTeamManager;
    }

    public Boolean updateTeamManagerStatus(Boolean isTeamManager){
        this.isTeamManager = isTeamManager;
        return this.isTeamManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember that = (TeamMember) o;
        return Objects.equals(teamInfoId, that.teamInfoId) && Objects.equals(member, that.member) && Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamInfoId, member, team);
    }
}
