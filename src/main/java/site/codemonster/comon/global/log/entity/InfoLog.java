package site.codemonster.comon.global.log.entity;

import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "info_log")
public class InfoLog extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long infoLogId;
    private Long memberId;
    private LocalDateTime logCreatedAt;
    private String level;
    private String className;
    private String methodName;
    private long executionTime;

    protected InfoLog(){
    }

    @Builder
    public InfoLog(Long memberId, LocalDateTime logCreatedAt, String level, String className, String methodName, long executionTime) {
        this.memberId = memberId;
        this.logCreatedAt = logCreatedAt;
        this.level = level;
        this.className = className;
        this.methodName = methodName;
        this.executionTime = executionTime;
    }
}
