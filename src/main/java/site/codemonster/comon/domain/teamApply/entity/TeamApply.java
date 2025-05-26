package site.codemonster.comon.domain.teamApply.entity;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class TeamApply extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamApplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_recruit_id")
    private TeamRecruit teamRecruit;

    private String teamApplyBody;

    private boolean isRead;

    protected TeamApply(){}

    @Builder
    public TeamApply(Member member, TeamRecruit teamRecruit, String teamApplyBody){
        this.member = member;
        this.teamRecruit = teamRecruit;
        this.teamApplyBody = teamApplyBody;
        this.isRead = false;
    }

    public boolean isTeamApplyOwner(Member member) {
        return this.member.equals(member);
    }

    public void updateTeamApplyBody(String teamApplyBody) {
        this.teamApplyBody = teamApplyBody;
    }

    public boolean isAuthor(Member member){
        return this.member.getId().equals(member.getId());
    }
}
