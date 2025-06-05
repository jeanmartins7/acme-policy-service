
FROM eclipse-temurin:17-jdk-debian

RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends git curl wget unzip less \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

ENV MAVEN_VERSION 3.9.6
ENV MAVEN_HOME /usr/local/apache-maven
ENV PATH "${MAVEN_HOME}/bin:${PATH}"
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -o /tmp/apache-maven.tar.gz \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/local/ \
    && mv /usr/local/apache-maven-${MAV