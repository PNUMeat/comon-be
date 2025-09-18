package site.codemonster.comon.domain.teamRecruit.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "team_recruit_image")
public class TeamRecruitImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamRecruitImageId;

    private  String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_recruit_id")
    private TeamRecruit teamRecruit;

    protected TeamRecruitImage(){}

    public TeamRecruitImage(String imageUrl, TeamRecruit teamRecruit){
        this.imageUrl = imageUrl;
        this.teamRecruit = teamRecruit;
    }

    public void updateTeamRecruit(TeamRecruit teamRecruit){
        this.teamRecruit = teamRecruit;
    }

    public void updateImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
}
