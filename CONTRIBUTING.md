Contributing to DeCS
====================

Build
-----

The project is build using [Gradle](https://gradle.org/):
```
$ ./gradlew clean check
```

For the _check_ task beside tests also a
and static code analysis (using [spotbugs](https://spotbugs.github.io)) is done.


Branches ![CI status](https://travis-ci.org/aburmeis/spring-legacy.svg)
--------------------------------------------------------------

The branch `master` is the integration branch for the upcoming release.
Past releases are tagged with `v` followed by the version.

Every branch is built continuously by
[Travis CI](https://travis-ci.org/aburmeis/spring-legacy/) by executing the task `check`.
Any contribution can be done on a feature branch to be merged to the integration branch.
To do so, use a pull request.
