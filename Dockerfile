# chaoslab 빌드 파이프라인용 — 소스에서 직접 bootJar 빌드 (Kaniko가 클러스터 안에서 이 파일로 이미지 생성)
# ── Stage 1: build ──
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew bootJar -x test --no-daemon

# ── Stage 2: runtime ──
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
