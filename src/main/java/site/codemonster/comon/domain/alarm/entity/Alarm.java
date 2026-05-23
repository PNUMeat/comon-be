package site.codemonster.comon.domain.alarm.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.codemonster.comon.domain.auth.entity.Member;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Column(length = 1000)
    private String deepLink;

    private Long routingId;

    private String alarmType;

    public Alarm(String title, String content, Member member, String deepLink, Long routingId, AlarmType alarmType) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.deepLink = deepLink;
        this.routingId = routingId;
        this.alarmType = alarmType.name();
    }
}
