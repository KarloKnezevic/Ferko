ARG BUILDPLATFORM
ARG TARGETPLATFORM

FROM --platform=$BUILDPLATFORM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY backend/pom.xml backend/pom.xml
COPY backend/ferko-domain/pom.xml backend/ferko-domain/pom.xml
COPY backend/ferko-application/pom.xml backend/ferko-application/pom.xml
COPY backend/ferko-infrastructure/pom.xml backend/ferko-infrastructure/pom.xml
COPY backend/ferko-security/pom.xml backend/ferko-security/pom.xml
COPY backend/ferko-web-api/pom.xml backend/ferko-web-api/pom.xml
COPY backend/ferko-architecture-tests/pom.xml backend/ferko-architecture-tests/pom.xml
COPY build-tools build-tools

RUN ./mvnw -B -ntp -pl backend/ferko-web-api -am dependency:go-offline

COPY backend backend

RUN ./mvnw -B -ntp -pl backend/ferko-web-api -am -DskipTests package

FROM --platform=$TARGETPLATFORM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --gid 10001 ferko \
    && useradd --uid 10001 --gid ferko --home-dir /app --shell /usr/sbin/nologin ferko

COPY --from=build /workspace/backend/ferko-web-api/target/ferko-web-api-*-exec.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication"

EXPOSE 8080

USER ferko:ferko

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
