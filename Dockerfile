### STAGE 1: Build ###
FROM maven:3.6.3-openjdk-15-slim AS build
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
RUN mvn -f pom.xml clean package -Dmaven.test.skip=true

### STAGE 2: Run ###
FROM openjdk:15-alpine

RUN apk --no-cache add curl

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]