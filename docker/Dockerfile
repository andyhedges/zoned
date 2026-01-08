##
## Build stage
##
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

# Speed + repeatability knobs
ARG MAVEN_OPTS="-Dstyle.color=never"
ENV MAVEN_OPTS=${MAVEN_OPTS}

COPY pom.xml ./

COPY core/pom.xml core/pom.xml
COPY netty/pom.xml netty/pom.xml
COPY app/pom.xml app/pom.xml
COPY coverage/pom.xml coverage/pom.xml

COPY .mvn .mvn
COPY mvnw mvnw

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -pl !coverage -B -ntp -DskipTests -am -U \
      dependency:go-offline

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -DskipTests \
      clean package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN useradd -r -u 10001 -g root zoned
USER 10001

COPY --from=build /workspace/app/target/zoned-server.jar /app/zoned-server.jar

COPY defaults/zoned.yaml /etc/zoned/zoned.yaml

EXPOSE 53/udp
EXPOSE 53/tcp

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/zoned-server.jar"]
