# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /src
COPY gradlew settings.gradle.kts gradle.properties detekt.yml ./
COPY gradle gradle
COPY buildSrc buildSrc
COPY domain domain
COPY use-case use-case
COPY infrastructure infrastructure
COPY rest-api rest-api
COPY flyway flyway
COPY app app
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :app:installDist --no-daemon --console=plain

FROM eclipse-temurin:21-jre-jammy
ENV TZ=Europe/Moscow
WORKDIR /opt/quotelab
COPY --from=build /src/app/build/install/quotelab ./
EXPOSE 8080 8085
ENTRYPOINT ["/opt/quotelab/bin/quotelab"]
