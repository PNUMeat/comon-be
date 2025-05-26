package site.codemonster.comon.domain.teamRecruit.entity;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamRecruit.dto.request.TeamRecruitUpdateRequest;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class TeamRecruit extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamRecruitId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String teamRecruitTitle;

    private String teamRecruitBody;

    private String chatUrl;

    private boolean isRecruiting = true;

    @OneToMany(mappedBy = "teamRecruit")
    private List<TeamRecruitImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "teamRecruit")
    private List<TeamApply> teamApplies = new ArrayList<>();

    protected TeamRecruit(){}

    @Builder
    public TeamRecruit(Team team, Member member, String teamRecruitTitle, String teamRecruitBody, String chatUrl){
        this.team = team;
        this.member = member;
        this.teamRecruitTitle = teamRecruitTitle;
        this.teamRecruitBody = teamRecruitBody;
        this.chatUrl = chatUrl;
    }

    public void changeRecruitingStatus(){
        this.isRecruiting = !isRecruiting;
    }

    public void addImage(TeamRecruitImage image){
        images.add(image);
        image.updateTeamRecruit(this);
    }

    public boolean existsTeam(){
        return this.team != null;
    }

    public boolean isTeamRecruitOwner(Member member) {
        return this.member.equals(member);
    }

    public boolean isAuthor(Member member){
        return this.member.getId().equals(member.getId());
    }

    public void updateTeamRecruit(TeamRecruitUpdateRequest teamRecruitUpdateRequest){
        if(teamRecruitUpdateRequest.teamRecruitTitle() != null){
            this.teamRecruitTitle = teamRecruitUpdateRequest.teamRecruitTitle();
        }

        if(teamRecruitUpdateRequest.teamRecruitBody() != null){
            this.teamRecruitBody = teamRecruitUpdateRequest.teamRecruitBody();
        }

        if(teamRecruitUpdateRequest.chatUrl() != null){
            this.chatUrl = teamRecruitUpdateRequest.chatUrl();
        }
    }

    public void addTeam(Team team){
        this.team = team;
        team.addTeamRecruit(this);
    }
}