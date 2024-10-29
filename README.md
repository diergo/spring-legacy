Spring Legacy
=============

When migrating a legacy project to dependency injection based on Spring you often need dependencies to be injected
found in legacy code which has no Spring support. [This library](https://diergo.github.io/spring-legacy/javadoc/)
provides class path scanning to create bean definitions for typical singleton and factory patterns found in legacy code.
You may also access Spring beans from legacy code.


Usage
-----

### Using Spring beans from legacy code

To use a singleton bean from a Spring context, you should obtain it using
[`LegacySpringAccess.getSpringBean()`](src/main/java/diergo/spring/legacy/LegacySpringAccess.java).
Have a look into the [example](src/test/java/example/legacy/LegacyCodeUsingSpring.java) and how it is used in the
[integration test](src/test/java/example/IntegrationTest.java). The usage has to be prepared using a Spring
configuration including the `LegacySpringAccess` which can be easily
[imported from your own configuration](src/test/java/example/spring/SpringConfig.java).

### Using legacy singletons with Spring

Instead of directly access legacy singletons from new code, you should use DI as provided by Spring. To support this,
[register all singletons](src/main/java/diergo/spring/legacy/LegacyBeanRegistryPostProcessorBuilder.java) from legacy
code as Spring beans. The post processor is configured using a
[static bean method in your config](src/test/java/example/spring/SpringConfig.java).
Have a look into the [example](src/test/java/example/spring/SpringBeanInjectedLegacy.java) and how it is used in the
[integration test](src/test/java/example/IntegrationTest.java).


Dependency [![Release](https://jitpack.io/v/de.diergo/spring-legacy.svg)](https://jitpack.io/#de.diergo/spring-legacy)
----------

To integrate the library in your project, add the artifact `spring-legacy` of group `de.diergo` to your Java dependency
management. At [JitPack](https://jitpack.io/#de.diergo/spring-legacy) you can find examples for Gradle and Maven.

The library has no external dependencies except [Spring Framework](https://spring.io/projects/spring-framework)
(spring-context starting with 5.3.0). For the release notes, have a look at the [change log](CHANGELOG.md).


License
-------

This library is published under [Apache License Version 2.0](LICENSE).
