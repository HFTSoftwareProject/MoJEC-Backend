FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD target/mojec-backend-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir /home/cp
ADD http://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar /home/cp
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
