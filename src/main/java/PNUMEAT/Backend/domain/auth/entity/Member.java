package PNUMEAT.Backend.domain.auth.entity;


import static PNUMEAT.Backend.global.images.ImageConstant.DEFAULT_MEMBER_PROFILE_IMAGE_URL;

import PNUMEAT.Backend.domain.article.entity.Article;
import PNUMEAT.Backend.domain.teamMember.entity.TeamMember;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.Getter;

@Entity
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String memberUniqueId;

    private String uuid;

    private String role;

    private String memberName;

    private String imageUrl = DEFAULT_MEMBER_PROFILE_IMAGE_URL;

    private String description;

    @OneToMany(mappedBy = "member")
    private List<Article> articles = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<TeamMember> teamMembers = new ArrayList<>();

    protected Member() {
    }

    public Member(String email, String memberUniqueId, String role) {
        this.email = email;
        this.memberUniqueId = memberUniqueId;
        this.role = role;
        this.uuid = UUID.randomUUID().toString();
    }

    public void updateProfile(String memberName, String description){
        if(!memberName.isEmpty()){
            this.memberName = memberName;
        }

        if(!description.isEmpty()){
            this.description = description;
        }
    }

    public void updateImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public boolean isMyUuid(String uuid){
        return this.uuid.equals(uuid);
    }
}
