################ Dev ################
# Build stage will be used:
# - as target for development (see devspace.yaml)
FROM maven:3-eclipse-temurin-24-alpine as dev

# Create project directory (workdir)
WORKDIR /app


################ Build & Dev ################
# Build stage will be used:
# - for building the application for production
# - as target for development (see devspace.yaml)
FROM maven:3-eclipse-temurin-24-alpine as build

# Create project directory (workdir)
WORKDIR /app

# Install maven dependency packages
COPY pom.xml .
RUN mvn -T 1C install && rm -rf target

# Add source code files to WORKDIR
ADD . .

# Build application
RUN ./build.sh

# Container start command for development
# Allows DevSpace to restart the dev container
# It is also possible to override this in devspace.yaml via images.*.cmd
CMD ["./build.sh", "run"]


################ Production ################
# Creates a minimal image for production using distroless base image
# More info here: https://github.com/GoogleContainerTools/distroless
FROM gcr.io/distroless/java17-debian12:latest as production

# Create project directory (workdir)
WORKDIR /app

# Copy application binary from build/dev stage to the production container
COPY --from=build /app/target/discordbot.jar /app

# Container start command for production
CMD ["discordbot.jar"]
