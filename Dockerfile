#
# Build stage
#
FROM maven:3.8.6-eclipse-temurin-17 AS build

# serum-data
COPY / /home/app/src
RUN mvn -f /home/app/src/pom.xml clean package -Dgpg.skip