package site.codemonster.comon.global.log.entity;

import site.codemonster.comon.global.entityListeners.TimeStamp;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "dangerous_log")
public class DangerousLog extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dangerousLogId;
    private Long memberId;
    private LocalDateTime logCreatedAt;
    private String level;
    private String className;
    private String methodName;
    private long executionTime;
    private String errorClassName;

    protected DangerousLog(){
    }

    @Builder
    public DangerousLog(Long memberId, LocalDateTime logCreatedAt, String level, String className, String methodName, long executionTime, String errorClassName) {
        this.memberId = memberId;
        this.logCreatedAt = logCreatedAt;
        this.level = level;
        this.className = className;
        this.methodName = methodName;
        this.executionTime = executionTime;
        this.errorClassName = errorClassName;
    }
}
