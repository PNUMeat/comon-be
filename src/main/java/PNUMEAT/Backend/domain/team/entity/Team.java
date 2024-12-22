package PNUMEAT.Backend.domain.team.entity;

import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.auth.entity.Member;
import PNUMEAT.Backend.domain.team.enums.Topic;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import PNUMEAT.Backend.global.util.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static PNUMEAT.Backend.global.images.ImageConstant.DEFAULT_TEAM_IMAGE_URL;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member teamManager;

    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMembers = new ArrayList<>();

    protected Team(){
    }

    @Builder
    public Team(String teamName, Topic teamTopic, String teamExplain, int maxParticipant, String teamPassword, Member teamManager) {
        this.teamName = teamName;
        this.teamTopic = teamTopic;
        this.teamExplain = teamExplain;
        this.maxParticipant = maxParticipant;
        this.teamPassword = teamPassword;
        this.teamManager = teamManager;
    }

    public void updateTeamIconUrl(String teamIconUrl){
        this.teamIconUrl = teamIconUrl;
    }

    public void updateTeamAnnouncement(String teamAnnouncement) { this.teamAnnouncement = teamAnnouncement; }

    public boolean isTeamManger(Member member){
        return this.getTeamManager().isMyUuid(member.getUuid());
    }
}
