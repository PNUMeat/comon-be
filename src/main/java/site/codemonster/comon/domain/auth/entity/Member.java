package site.codemonster.comon.domain.auth.entity;


import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.teamApply.entity.TeamApply;
import site.codemonster.comon.domain.teamMember.entity.TeamMember;
import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.*;

import site.codemonster.comon.global.images.enums.ImageConstant;

@Entity
@Getter
public class Member extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String memberUniqueId;

    private String uuid;

    private String role;

    private String memberName;

    private String imageUrl = ImageConstant.DEFAULT_MEMBER_PROFILE.getObjectKey();

    private String description;

    @OneToMany(mappedBy = "member")
    private List<Article> articles = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<TeamApply> teamApplies = new ArrayList<>();

    protected Member() {
    }

    public Member(String email, String memberUniqueId, String role) {
        this.email = email;
        this.memberUniqueId = memberUniqueId;
        this.role = role;
        this.uuid = UUID.randomUUID().toString();
    }

    public Member(String anonymousRole) {
        this.id = -1L;
        this.email = "anonymous";
        this.memberUniqueId = "anonymous";
        this.role = anonymousRole;
        this.uuid = UUID.randomUUID().toString();
    }

    public void updateProfile(String memberName, String description, String imageUrl) {
        if (memberName != null) {
            this.memberName = memberName;
        }
        if (description != null) {
            this.description = description;
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isMyUuid(String uuid){
        return this.uuid.equals(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(email, member.email) && Objects.equals(memberUniqueId,
                member.memberUniqueId) && Objects.equals(uuid, member.uuid) && Objects.equals(role,
                member.role) && Objects.equals(memberName, member.memberName) && Objects.equals(
                imageUrl, member.imageUrl) && Objects.equals(description, member.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, memberUniqueId, uuid, role, memberName, imageUrl, description);
    }
}
