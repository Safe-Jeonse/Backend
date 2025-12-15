# 1. Build Stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Gradle 캐시 활용을 위해 의존성 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

COPY gradlew .

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew
# 의존성 다운로드 (소스코드 없이)
RUN ./gradlew dependencies --no-daemon

# 소스코드 복사 및 빌드
COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

# 2. Run Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경변수 설정 (필요시 오버라이딩 가능)
ENV TZ=Asia/Seoul

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

