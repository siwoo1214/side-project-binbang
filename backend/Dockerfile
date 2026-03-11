# =====================
# 1단계: 빌드 스테이지
# =====================
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

#Gradle 의존성 캐싱 (소스 변경 없으면 이 레이어는 재사용됨)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 후 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# =====================
# 2단계: 실행 스테이지
# =====================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR만 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]