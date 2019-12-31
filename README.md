Spring Legacy
=============

When migrating a legacy project to dependency injection based on Spring you often need dependencies to be injected
found in legacy code which has no Spring support. This library provides class path scanning to create bean definitions
for typical singleton and factory patterns found in legacy code. You may also access Spring beans from legacy code.
