package site.codemonster.comon.global.log.aop;

import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.global.log.entity.DangerousLog;
import site.codemonster.comon.global.log.entity.InfoLog;
import site.codemonster.comon.global.log.repository.DangerousLogRepository;
import site.codemonster.comon.global.log.repository.InfoLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@Aspect
public class TraceAspect {

    private final InfoLogRepository infoLogRepository;
    private final DangerousLogRepository dangerousLogRepository;

    public TraceAspect(InfoLogRepository infoLogRepository, DangerousLogRepository dangerousLogRepository) {
        this.infoLogRepository = infoLogRepository;
        this.dangerousLogRepository = dangerousLogRepository;
    }

    @Around("@annotation(site.codemonster.comon.global.log.annotation.Trace) || @within(site.codemonster.comon.global.log.annotation.Trace)")
    public Object doTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String currentTime = now();

        Object[] args = joinPoint.getArgs();
        Long memberId = extractMemberId(args);

        try {
            return handleProceed(joinPoint, startTime, currentTime, memberId);
        } catch (Exception e) {
            handleException(joinPoint, startTime, currentTime, memberId, e);
            throw e;
        }
    }

    private Object handleProceed(ProceedingJoinPoint joinPoint, long startTime, String currentTime, Long memberId) throws Throwable {
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        String className = getSimpleClassName(joinPoint.getSignature().getDeclaringTypeName());
        String methodName = joinPoint.getSignature().getName();

        saveInfoLog(memberId, currentTime, className, methodName, executionTime);

        return result;
    }

    private void handleException(ProceedingJoinPoint joinPoint, long startTime, String currentTime, Long memberId, Exception e) {
        long errorTime = System.currentTimeMillis();
        long executionTime = errorTime - startTime;

        String className = getSimpleClassName(joinPoint.getSignature().getDeclaringTypeName());
        String methodName = joinPoint.getSignature().getName();
        String errorClassName = getSimpleClassName(e.getClass().getName());

        saveDangerousLog(memberId, currentTime, className, methodName, executionTime, errorClassName);
    }

    private void saveInfoLog(Long memberId, String currentTime, String className, String methodName, long executionTime) {
        infoLogRepository.save(
                InfoLog.builder()
                        .memberId(memberId)
                        .logCreatedAt(parseTime(currentTime))
                        .level(LogLevel.INFO.name())
                        .className(className)
                        .methodName(methodName)
                        .executionTime(executionTime)
                        .build()
        );
    }

    private void saveDangerousLog(Long memberId, String currentTime, String className, String methodName, long executionTime, String errorClassName) {
        dangerousLogRepository.save(
                DangerousLog.builder()
                        .memberId(memberId)
                        .logCreatedAt(parseTime(currentTime))
                        .level(LogLevel.WARN.name())
                        .className(className)
                        .methodName(methodName)
                        .executionTime(executionTime)
                        .errorClassName(errorClassName)
                        .build()
        );
    }

    private Long extractMemberId(Object[] args) {
        Member member = extractLoginMember(args);

        if(member != null){
            return member.getId();
        }
        return 0L;
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private LocalDateTime parseTime(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getSimpleClassName(String fullName) {
        return fullName.substring(fullName.lastIndexOf(".") + 1);
    }

    private Member extractLoginMember(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Member) {
                return (Member) arg;
            }
        }
        return null;
    }
}