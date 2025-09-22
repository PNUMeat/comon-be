package site.codemonster.comon.domain.adminAuth.entity;

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

    public AdminMember(String adminId, String password, String name) {
        this.adminId = adminId;
        this.password = password;
        this.name = name;
    }
}
