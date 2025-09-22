package site.codemonster.comon.domain.team.entity;

import site.codemonster.comon.domain.recommendation.entity.TeamRecommendation;
import site.codemonster.comon.domain.team.dto.request.TeamInfoEditRequest;
import site.codemonster.comon.domain.team.enums.Topic;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.domain.teamRecruit.entity.TeamRecruit;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static site.codemonster.comon.global.images.enums.ImageConstant.DEFAULT_TEAM_IMAGE_URL;

@Entity
@Getter
public class Team extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    private String teamName;

    private String teamIconUrl = DEFAULT_TEAM_IMAGE_URL;

    private Topic teamTopic;

    private String teamExplain;

    private int maxParticipant;

    private String teamPassword;

    private int streakDays = 0;

    private String teamAnnouncement="";

    @OneToOne(mappedBy = "team", fetch = FetchType.LAZY)
    private TeamRecruit teamRecruit;

    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToOne(mappedBy = "team", fetch = FetchType.LAZY)
    private TeamRecommendation teamRecommendation;

    protected Team(){
    }

    @Builder
    public Team(String teamName, Topic teamTopic, String teamExplain, int maxParticipant, String teamPassword) {
        this.teamName = teamName;
        this.teamTopic = teamTopic;
        this.teamExplain = teamExplain;
        this.maxParticipant = maxParticipant;
        this.teamPassword = teamPassword;
    }

    public void updateTeamIconUrl(String teamIconUrl){
        this.teamIconUrl = teamIconUrl;
    }

    public void updateTeamInfo(TeamInfoEditRequest teamInfoEditRequest){
        if (teamInfoEditRequest.teamName() != null){
            this.teamName = teamInfoEditRequest.teamName();
        }
        if (teamInfoEditRequest.teamExplain() != null){
            this.teamExplain = teamInfoEditRequest.teamExplain();
        }
        if (teamInfoEditRequest.topic() != null){
            this.teamTopic = Topic.fromName(teamInfoEditRequest.topic());
        }
        this.maxParticipant = teamInfoEditRequest.memberLimit();
        if (teamInfoEditRequest.password() != null && !teamInfoEditRequest.password().isEmpty()){
            this.teamPassword = teamInfoEditRequest.password();
        }
    }

    public void updateTeamRecruit(TeamRecruit teamRecruit){
        this.teamRecruit = teamRecruit;
    }

    public void updateTeamAnnouncement(String teamAnnouncement) { this.teamAnnouncement = teamAnnouncement; }

    public boolean checkExceedTeamSize(){
        return this.teamMembers.size() >= maxParticipant;
    }

    public void addTeamRecruit(TeamRecruit teamRecruit){
        this.teamRecruit = teamRecruit;
    }
}
