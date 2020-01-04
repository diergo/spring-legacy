Spring Legacy
=============

When migrating a legacy project to dependency injection based on Spring you often need dependencies to be injected
found in legacy code which has no Spring support. This library provides class path scanning to create bean definitions
for typical singleton and factory patterns found in legacy code. You may also access Spring beans from legacy code.


Usage
-----

### Using Spring beans from Legacy code

To use a singleton bean from a Spring context, you should obtain it using
[`LegacySpringAccess.getSpringBean()`](src/main/java/diergo/spring/legacy/LegacySpringAccess.java).
Have a look into the [example](src/test/java/example/legacy/LegacyCodeUsingSpring.java) and how it is used in the
[integration test](src/test/java/example/IntegrationTest.java). The usage has to be prepared using a Spring
configuration including the `LegacySpringAccess` which can be easily
[imported from your own configuration](src/test/java/example/spring/SpringConfig.java).


Dependency
----------

To integrate the library in your project, use the following dependency:

**gradle:**

```gradle
  compile 'diergo:spring-legacy:1.0.0-SNAPSHOT'
```

**maven:**
```xml
  <dependency>
    <groupId>diergo</groupId>
    <artifactId>spring-legacy</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </dependency>
```

The library has no external dependencies except [Spring Framework 5](https://spring.io/projects/spring-framework) (spring-context 5.1.0.RELEASE).


License
-------

This library is published under [Apache License Version 2.0](LICENSE).