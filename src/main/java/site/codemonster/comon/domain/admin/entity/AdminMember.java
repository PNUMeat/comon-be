package site.codemonster.comon.domain.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.codemonster.comon.global.entityListeners.TimeStamp;

@Entity
@Table(name = "admin_member")
@Getter
@NoArgsConstructor
public class AdminMember extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String adminId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isActive = true;

    public AdminMember(String adminId, String password, String name) {
        this.adminId = adminId;
        this.password = password;
        this.name = name;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
