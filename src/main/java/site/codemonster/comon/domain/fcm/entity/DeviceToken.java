package site.codemonster.comon.domain.fcm.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.entityListeners.TimeStamp;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "device_token",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_id_token",
                        columnNames = {"member_id", "token"}
                )
        }
)
public class DeviceToken extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String token;

    public DeviceToken(Member member, String token) {
        this.member = member;
        this.token = token;
    }


}