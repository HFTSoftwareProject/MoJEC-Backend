FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD target/mojec-backend-1.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'

# Prepare environment.
# Create needed folders
RUN mkdir /home/mojec && \
    mkdir /home/mojec/libs

# Download needed libs for compilation
ADD http://central.maven.org/maven2/junit/junit/4.12/junit-4.12.jar /home/mojec/libs/junit.jar
ADD http://central.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar /home/mojec/libs/hamcrest.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=server","-jar","/app.jar"]
