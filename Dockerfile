# Base stage with common dependencies
FROM sbtscala/scala-sbt:eclipse-temurin-25_36_1.11.7_3.7.3 AS builder
WORKDIR /app
COPY build.sbt .
COPY project/build.properties ./project/build.properties
COPY project/plugins.sbt ./project/plugins.sbt
COPY EmailAuthenticationService ./EmailAuthenticationService
COPY Client ./Client
RUN sbt compile

# Email Service stage
FROM builder AS email-service-builder
RUN ["sbt", "EmailAuthenticationService/stage"]

FROM eclipse-temurin:21-jre-jammy AS email-service
WORKDIR /app
# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=email-service-builder /app/EmailAuthenticationService/target/universal/stage ./
EXPOSE 8080
CMD ["bin/emailauthenticationservice"]

# Client stage
FROM builder AS client-builder
RUN ["sbt", "Client/stage"]

FROM eclipse-temurin:21-jre-jammy AS client
WORKDIR /app
COPY --from=client-builder /app/Client/target/universal/stage ./
CMD ["bin/client"]