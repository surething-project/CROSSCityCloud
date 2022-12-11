# Fetch basic image
FROM maven:3.8-jdk-11

# Application placed into /opt/app
RUN mkdir -p /opt/app
WORKDIR /opt/app

# Copy CROSS Dataset
COPY cross-field-experiments /opt/app/cross-field-experiments

# Resolve maven dependencies
COPY pom.xml /opt/app/
COPY local-maven-repo /opt/app/local-maven-repo
RUN mvn install

# Source code
COPY src /opt/app/src
RUN mvn package
