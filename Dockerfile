FROM clojure:lein-2.8.1-alpine as builder
LABEL maintainer="Gavin Lam <me@gavin.hk>"

WORKDIR /usr/src/app

# Fetch dependencies unless project metadata changed
COPY project.clj .
RUN lein deps

# Compile uberjar
COPY . .
RUN lein uberjar

# =============================================================================
FROM openjdk:8u151-jre-alpine3.7
LABEL maintainer="Gavin Lam <me@gavin.hk>"

EXPOSE 8080

COPY --from=builder \
    /usr/src/app/target/tabula-api-1.1.0-SNAPSHOT-standalone.jar \
    /usr/src/app/tabula-api-standalone.jar

CMD ["java", "-jar", "/usr/src/app/tabula-api-standalone.jar"]
