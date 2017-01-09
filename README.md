# Moodle JUnit Excercise Corrector (MoJEC) - Backend

## Usage
### Conventions
- The JUnit test classes cannot be organized in packages. Tasks and helper classes can use packages.

## Development Hints
### Build
This is a maven project. It can be build with `mvn package`.

There is also ready to go docker image available. It can be found [on Docker Hub](https://hub.docker.com/r/hftstuttgart/mojec-backend/)

### Local application configuration
We're using the **application.properties** file to configure our application.

To configure your local configuration create a file called _application-local.properties_ in _/src/main/resources/_ and override the properties.
Afterwards configure the application to use the local profile using the run configuration or adding _spring.profiles.active=local_ to the global _application.properties_ file.

### Integration tests
MoJEC-Backend has some rudimentary API tests using [Spring Boot Testing](https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4). This tests assure that there won't be any regressions in the API when changing the backend code.

To be able to run the integration tests the system where the tests are executed needs to be a *nix System because a `/tmp/` folder must exist. Also the needed libraries [JUnit](http://central.maven.org/maven2/junit/junit/4.12/junit-4.12.jar) and [Hamcrest](http://central.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar) need to be downloaded into `/opt/mojec/junit/`.
This is the reason why the tests are disabled by default. They can be enabled by setting `-DskipTests=false`

## Interfaces
##### POST /v1/unittest
Used for uploading / creating of assignments. The body needs to contain two fields as form data:<br/>
*assignmentId*: The ID of the created assignment. This is created by moodle.<br/>
*unitTestFile*: The zip file containing the unit tests for this assignment.

##### DELETE /v1/unittest?assignmentId=<111>
Delete the created assignment. The assignment ID of the unit tests which need to be deleted is passed as a query parameter

##### POST /v1/task
The upload of the Java files to be tested. The body needs to contain two form fields:
*taskFile*: The zip file containing the java files
*assignmentId*: The id of the assignment. Provided by moodle

