FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /build

COPY .mvn .mvn
COPY pom.xml mvnw ./

RUN chmod +x mvnw \
    && ./mvnw -q -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN useradd -r -u 10001 appuser
USER 10001:10001

COPY --from=build /build/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]