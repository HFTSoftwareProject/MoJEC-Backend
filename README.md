# MoJEC-Backend

## Usage
### Conventions
1. The JUnit test classes cannot be organized in packages. Tasks and helper classes can use packages.
2. JUnit test classes need to be named ...Test.java all other classes cannot end with "Test" or "test"


## Development Hints
### Local application configuration
We're using the **application.properties** file to configure our application.

To configure your local configuration create a file called _application-local.properties_ in _/src/main/resources/_ and override the properties.
Afterwards configure the application to use the local profile using the run configuration or adding _spring.profiles.active=local_ to the global _application.properties_ file.
