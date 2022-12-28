Contributing to Spring Legacy
=============================

Build
-----

The project is build using [Gradle](https://gradle.org/):
```
$ ./gradlew clean check
```

For the _check_ task beside tests also a
and static code analysis (using [SpotBugs](https://spotbugs.github.io)) is done.


Issues
------

Any bugs or feature requests should be reported as [issues](https://github.com/diergo/spring-legacy/issues).


Changes
--------

The branch `main` is the integration branch for the upcoming release.
Past releases are tagged with `v` followed by the version.

Every branch is built continuously using [GitHub actions](.github/workflows/gradle.yml).
The build is done using Open JDK 8, 11 and 17 depending on Spring 5 and 6.