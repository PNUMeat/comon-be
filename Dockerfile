# Spring Boot 빌드 — Gradle wrapper, 산출물 build/libs/<app>.jar (bootJar).
#
# bootJar 가 test·문서화 태스크(asciidoctor·copyRestDocs = spring-restdocs)에 의존하는
# 프로젝트가 흔하다. test 는 DB/Redis 가 필요해 빌드 환경에서 실패하고, asciidoctor 는
# 테스트가 만든 스니펫이 없으면 실패한다 → 빌드 시 제외한다.
# 단, 이 태스크들은 프로젝트마다 없을 수 있어(없으면 `task not found`로 첫 명령 실패)
# 있으면 제외하고 / 없으면 test 만 제외해 재시도한다.
#
# Java 17 기준 (현재 검증 대상 SUT opus-backend = toolchain 17). 다른 Java 버전 앱은
# toolchain 자동 다운로드(foojay) 또는 베이스 이미지 변경이 필요 — 추후 일반화 과제.

# ── Stage 1: build ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && \
    ( ./gradlew bootJar -x test -x asciidoctor -x copyRestDocs --no-daemon \
      || ./gradlew bootJar -x test --no-daemon )

# ── Stage 2: runtime ───────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app
# bootJar 산출물만 선택 (bootJar 단독 실행이라 plain jar 미생성 → 단일 jar)
COPY --from=builder /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
