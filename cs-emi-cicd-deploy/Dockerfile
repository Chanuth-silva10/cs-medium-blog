# Stage 1: build the JAR. The build does not depend on what is installed on your computer.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /project
# The dependencies first: this layer is rebuilt only when pom.xml changes.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# Stage 2: run it on a small JRE image, as a user that is not root.
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 1001 -S devcraft && adduser -S -u 1001 -G devcraft devcraft
WORKDIR /app
COPY --from=build --chown=devcraft:devcraft /project/target/emi.jar /app/emi.jar
USER devcraft
EXPOSE 7550
HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://127.0.0.1:7550/actuator/health | grep -q '"status":"UP"' || exit 1
# "exec" makes Java the main process, so docker stop reaches it and it can shut down gracefully.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/emi.jar"]

