# MoJEC-Backend

## Usage
### Conventions
- The JUnit test classes cannot be organized in packages. Tasks and helper classes can use packages.


## Development Hints
### Local application configuration
We're using the **application.properties** file to configure our application.

To configure your local configuration create a file called _application-local.properties_ in _/src/main/resources/_ and override the properties.
Afterwards configure the application to use the local profile using the run configuration or adding _spring.profiles.active=local_ to the global _application.properties_ file.

## Testing
### Integration tests
MoJEC-Backend has some rudimentary API tests using [Spring Boot Testing](https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4). This tests assure that there won't be any regressions in the API when changing the backend code.

To be able to run the integration tests the system where the tests are executed needs to be a *nix System because a `/tmp/` folder must exist. Also the needed libraries [JUnit](http://central.maven.org/maven2/junit/junit/4.12/junit-4.12.jar) and [Hamcrest](http://central.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar) need to be downloaded into `/opt/mojec/junit/`
